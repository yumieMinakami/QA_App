package jp.techacademy.yumie.minakami.qa_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created by user on 2016/12/02.
 */

//public class QuestionDetailListAdapter extends BaseAdapter {
public class QuestionDetailListAdapter extends BaseAdapter {

    // Consts for flag of layout
    private final static int TYPE_QUESTION = 0; // for question layout
    private final static int TYPE_ANSWER = 1;   // for answer layout

    private LayoutInflater mLayoutInflater = null;
    private Question mQuestion;
    private DatabaseReference mRef;
    private DatabaseReference mFavRef;
    private FirebaseUser mUser;
    private String mUserId;

    public QuestionDetailListAdapter(Context context, Question question){
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mQuestion = question;
    }

    @Override
    public int getCount(){
        return 1 + mQuestion.getAnswers().size();
    }

    // return position type; Question or answer
    @Override
    public int getItemViewType(int position){
        if(position == 0){
            return TYPE_QUESTION;
        } else {
            return TYPE_ANSWER;
        }
    }

    @Override
    public int getViewTypeCount(){
        return 2;
    }

    @Override
    public Object getItem(int position){
        return mQuestion;
    }

    @Override
    public long getItemId(int position){
        return 0;
    }

    // get view whether question or answer
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(getItemViewType(position) == TYPE_QUESTION){
            if(convertView == null){
                convertView = mLayoutInflater.inflate(R.layout.list_question_detail, parent, false);
            }

            String body = mQuestion.getBody();
            String name = mQuestion.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);

            byte[] bytes = mQuestion.getImageBytes();
            if(bytes.length != 0) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
                imageView.setImageBitmap(image);
            }

            final ImageButton favImgBtnOff = (ImageButton) convertView.findViewById(R.id.favIbtnOff);
            final ImageButton favImgBunOn = (ImageButton) convertView.findViewById(R.id.favIbtnOn);

            mUser = FirebaseAuth.getInstance().getCurrentUser();
            mRef = FirebaseDatabase.getInstance().getReference();
            mUserId = mUser.getUid();
            mFavRef = mRef.child(Const.UsersPATH).child(mUserId.toString()).child(Const.FavPATH);
//            mFavRef = mRef.child(Const.UsersPATH).child(mUserId.toString()).child(Const.FavPATH).child(mQuestion.getQuestionUid());
            Query qry = mFavRef.orderByKey().equalTo(mQuestion.getQuestionUid());
//            Query qry = mFavRef.equalTo(mQuestion.getQuestionUid());
            qry.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null) {
                        favImgBtnOff.setVisibility(INVISIBLE);
                        favImgBunOn.setVisibility(VISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

//            if(qry != null){
//                favImgBtnOff.setVisibility(View.INVISIBLE);
//                favImgBunOn.setVisibility(View.VISIBLE);
//            }

            favImgBtnOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("favrev", "FavButton ON");
                    favImgBtnOff.setVisibility(INVISIBLE);
                    favImgBunOn.setVisibility(VISIBLE);

//                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                    String userId = mUser.getUid();
//                    DatabaseReference favref = FirebaseDatabase.getInstance().getReference()
//                    DatabaseReference favref = mRef
//                            .child(Const.UsersPATH)
//                            .child(mUserId.toString())
//                            .child(Const.FavPATH);
                    HashMap<String, Object> dataFav = new HashMap<String, Object>();
                    dataFav.put(mQuestion.getQuestionUid(), mQuestion.getGenre());
//                    dataFav.put("quid", mQuestion.getQuestionUid());
//                    favref.push().updateChildren(dataFav); // "push" with unique id
//                    favref.updateChildren(dataFav);
                    mFavRef.updateChildren(dataFav);
                }
            });

            favImgBunOn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Log.d("favref", "FavButton : OFF");
                    favImgBunOn.setVisibility(INVISIBLE);
                    favImgBtnOff.setVisibility(VISIBLE);

//                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                    String userId = mUser.getUid();
//                    DatabaseReference favref = FirebaseDatabase.getInstance().getReference()
//                    DatabaseReference favref = mRef
//                            .child(Const.UsersPATH)
//                            .child(userId.toString())
//                            .child(Const.FavPATH);
                    HashMap<String, Object> dataFav = new HashMap<String, Object>();
                    dataFav.put(mQuestion.getQuestionUid(), null);
//                    dataFav.put("quid", null);
//                    favref.updateChildren(dataFav);
                    mFavRef.updateChildren(dataFav);
//                    favref.push().updateChildren(dataFav); // "push" with unique id
//                    favref.removeValue();     // gone all items under pointed lobation
                }
            });
        } else {
            if(convertView == null){
                convertView = mLayoutInflater.inflate(R.layout.list_answer, parent, false);
            }

            Answer answer = mQuestion.getAnswers().get(position - 1);
            String body = answer.getBody();
            String name = answer.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);
        }

        return convertView;
    }
}
