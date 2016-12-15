package jp.techacademy.yumie.minakami.qa_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText        mEmailEditText;
    EditText        mPasswordEditText;
    EditText        mNameEditText;
    ProgressDialog  mProgress;          // Progress Dialog during login/creating account

    FirebaseAuth                    mAuth;                  // The entry point of the Firebase Authentication SDK.
    OnCompleteListener<AuthResult>  mCreateAccountListener; // Listener called when "AuthResult(authentication related operation)" completes for account creation
    OnCompleteListener<AuthResult>  mLoginListener;     // Listener called when "AuthResult(authentication related operation)" completes for login
    DatabaseReference               mDataBaseReference; // Read/Write to DB; The entry point for accessing a Firebase Database

    boolean mIsCreateAccount = false;   // Set flag in account creation, and save name on Firebase after login process

    // Firebase関連の初期化、UI準備
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);    // Creating a window for placable UI

        mDataBaseReference = FirebaseDatabase.getInstance().getReference(); // Gets a DatabaseReference for the provided path

        mAuth = FirebaseAuth.getInstance();     // Gets the instance of FirebaseDatabase

        // Create a Listener for account creation process
        mCreateAccountListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){    // isSuccessful Method can confirm whether login was successful or not
                    // if succeeded, login
                    String email = mEmailEditText.getText().toString();
                    String password = mPasswordEditText.getText().toString();
                    login(email, password);
                } else {
                    // if failed, show error message
                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, "アカウント作成に失敗しました", Snackbar.LENGTH_LONG);

                    mProgress.dismiss();    // Hide Progress Dialog
                }
            }
        };

        // Create a Listener for login process
        mLoginListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    // if succeeded
                    FirebaseUser user = mAuth.getCurrentUser(); // Get current signed-in user obj
                    DatabaseReference userRef = mDataBaseReference.child(Const.UsersPATH).child(user.getUid()); // Get a reference to location relative to this one

                    if(mIsCreateAccount){   // Login by pushing Account creation
                        // Save displayed name to Firebase in creating account
                        String name = mNameEditText.getText().toString();

                        Map<String, String> data = new HashMap<String, String>();   // Create array for user's name
                        data.put("name", name);
                        userRef.setValue(data); // Set the data at userRef to data.

                        // Save displayed name to Preference
                        saveName(name);

                    } else {    // Login by registered user
                        // Get displayed name from Firebase and save to Preference
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {   // Get data only one time
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {   // DataSnapshot : this instance has data from a Firebase DB loc. When reading data from DB, it's received the data as a DataSnapshot
                                Map data = (Map) snapshot.getValue();
                                saveName((String) data.get("name"));
                            }

                            @Override
                            public void onCancelled(DatabaseError firebaseError) {
                            }
                        });
                    }

                    mProgress.dismiss();    // Hide Progress Dialog

                    finish();   // Close this Activity
                } else {
                    // if failed, show errors
                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show();

                    mProgress.dismiss();    // Hide Progress Dialog
                }
            }
        };

        // UI preparation
        // Change title of Title bar
        setTitle("ログイン");

        // Copy UI status on Member vars.
        mEmailEditText = (EditText) findViewById(R.id.emailText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordText);
        mNameEditText = (EditText) findViewById(R.id.nameText);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("処理中...");

        // Set Account Create Button and its Click Listener
        Button createButton = (Button) findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                String name = mNameEditText.getText().toString();

                if(email.length() != 0 && password.length() >= 6 && name.length() != 0){
                    mIsCreateAccount = true;    // Set flag to be able to save displayed name in login

                    createAccount(email, password);
                } else {
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();   // Show error message
                }
            }
        });

        // Set Login Button and its Click Listener
        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                if(email.length() != 0 && password.length() >= 6){
                    mIsCreateAccount = false;   // Flag off

                    login(email, password);
                } else {
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    // 処理中のダイアログ表示、Firebaseにアカウント作成を指示
    private void createAccount(String email, String password){
        mProgress.show();   // Show Progress Dialog

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener);    // Create account
            // createUserWithEmailAndPassword() : Create account
            //          arguments : email address, password
            // addOnCompleteListener() : Set listener
    }

    // 処理中のダイアログを表示、Firebaseにログイン指示
    private void login(String email, String password){
        mProgress.show();   // Show Progress Dialog

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener);    // Create account
            // signInWithEmailAndPassword() : Login process
            //        arguments : email address, password
            // addOnCompleteListener() : Set listener
    }

    // Preference (local) に表示名保存
    private void saveName(String name){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Const.NameKEY, name);
        editor.commit();        // Update Save process
    }
}
