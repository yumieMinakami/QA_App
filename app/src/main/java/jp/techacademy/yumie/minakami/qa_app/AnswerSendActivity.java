package jp.techacademy.yumie.minakami.qa_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AnswerSendActivity extends AppCompatActivity implements View.OnClickListener, DatabaseReference.CompletionListener{

    private EditText         mAnswerEditText;   // var as holding EditText info
    private Question         mQuestion;         // var as holding Question from Intent
    private ProgressDialog   mProgress;         // var as holding ProgressDialog info

    // Hold Instance of received Question, UI preparation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_send);

        // Hold received Question obj
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        // UI preparation
        mAnswerEditText = (EditText) findViewById(R.id.answerEditText);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("投稿中...");

        Button sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);
    }

    // Get written completion to Firebase
    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference){
        mProgress.dismiss();

        if(databaseError == null){
            finish();   // close Activity
        } else {
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show();
        }
    }

    // Call when pushing post Button; close keyboard and write Answer to Firebase
    @Override
    public void onClick(View v){
        // Close if keyboard shows
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference answerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);

        Map<String, String> data = new HashMap<String, String>();

        // UID
        data.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

        // displayed name; get name from Preference
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sp.getString(Const.NameKEY, "");
        data.put("name", name);

        // displayed name; get answer
        String answer = mAnswerEditText.getText().toString();

        if(answer.length() == 0){
            // Just show errors when an answer didn't input
            Snackbar.make(v, "回答を入力してください", Snackbar.LENGTH_LONG).show();;
            return;
        }
        data.put("body", answer);

        mProgress.show();
        answerRef.push().setValue(data, this);
    }
}
