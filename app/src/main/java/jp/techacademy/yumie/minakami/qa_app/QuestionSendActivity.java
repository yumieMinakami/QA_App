package jp.techacademy.yumie.minakami.qa_app;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class QuestionSendActivity extends AppCompatActivity implements View.OnClickListener, DatabaseReference.CompletionListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int CHOOSER_REQUEST_CODE = 100;

    private ProgressDialog  mProgress;
    private EditText        mTitleText;
    private EditText        mBodyText;
    private ImageView       mImageView;
    private Button          mSendButton;

    private int             mGenre;         // Hold genre info.
    private Uri             mPictureUri;    // URI which saves captured image by camera


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_send);

        // Get received Genre No.
        Bundle extras = getIntent().getExtras();
        mGenre = extras.getInt("genre");

        // UI preparation
        setTitle("質問作成");

        mTitleText = (EditText) findViewById(R.id.titleText);
        mBodyText = (EditText) findViewById(R.id.bodyText);

        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(this);

        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setOnClickListener(this);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("投稿中...");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == CHOOSER_REQUEST_CODE){
            if(resultCode != RESULT_OK){
                if(mPictureUri != null){
                    getContentResolver().delete(mPictureUri, null, null);
                    mPictureUri = null;
                }
                return;
            }

            // Get picture
            Uri uri = (data == null || data.getData() == null) ? mPictureUri : data.getData();

            // Get Bitmap from URI
            Bitmap image;
            try {
                ContentResolver contentResolver = getContentResolver();
                InputStream inputStream = contentResolver.openInputStream(uri);
                image = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (Exception e) {
                return;
            }

            // Resize long size of gotten Bitmap as 500 px
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            float scale = Math.min((float) 500 / imageWidth, (float) 500 / imageHeight);

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            Bitmap resizedImage = Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true);

            // Set Bitmap to ImageView
            mImageView.setImageBitmap(resizedImage);

            mPictureUri = null;
        }
    }

    @Override
    public void onClick(View v){
        if(v == mImageView){    // when tapped ImageView
            // Confirm permission's status
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ // if Android 6.0 or later
                if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){   // confirm write right to external storage
                    showChooser();  // permitted
                } else {
                    // Show permitted dialog due to unpermitted
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);

                    return;
                }
            } else {    // if Android 5 or former
                showChooser();
            }
        } else if(v == mSendButton){    // when tapped send button
            // Close if Keyboard shows
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference genreRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));

            Map<String, String> data = new HashMap<String, String>();

            // UID
            data.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

            // Get title and content
            String title = mTitleText.getText().toString();
            String body = mBodyText.getText().toString();

            if(title.length() == 0){
                // Show errors when questions don't input
                Snackbar.make(v, "タイトルを入力してください", Snackbar.LENGTH_LONG).show();
                return;
            }

            if(body.length() == 0){
                // Show errors when questions don't input
                Snackbar.make(v, "タイトルを入力してください", Snackbar.LENGTH_LONG).show();
                return;
            }

            // Get name from Preference
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String name = sp.getString(Const.NameKEY, "");

            data.put("title", title);
            data.put("body", body);
            data.put("name", name);

            // Get Attached image
            BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();

            // if attached image is set, get image and encode BASE64
            if(drawable != null){
                Bitmap bitmap = drawable.getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                String bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                data.put("image", bitmapString);
            }

            genreRef.push().setValue(data, this);
            mProgress.show();
        }
    }

    // Get result which chosen by user on permission dialog
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case PERMISSIONS_REQUEST_CODE: {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    showChooser();  // if user permits
                }
                return;
            }
        }
    }

    private void showChooser(){
        // Intent chosen from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

        // Intent captured by camera
        String filename = System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        mPictureUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri);

        // Give intent chosen at gallery and call createChooser method
        Intent chooserIntent = Intent.createChooser(galleryIntent, "画像を取得");

        // Add intent camera capturing to EXTRA_INITIAL_INTENTS
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE);
    }

    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference){
        mProgress.dismiss();

        if(databaseError == null){
            finish();
        } else {
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show();
        }
    }
}
