package jp.techacademy.yumie.minakami.qa_app;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by user on 2016/12/02.
 */

public class Question implements Serializable {

    private String              mTitle;             // A title which got from Firebase
    private String              mBody;              // Content which got from Firebase
    private String              mName;              // A questioner which got from Firebase
    private String              mUid;               // Questioner's UID which got from Firebase
    private String              mQuestionUid;      // Question's UID which got from Firebase
    private int                 mGenre;            // A genre of question
    private byte[]              mBitmapArray;     // A Bitmap which got from Firebase arrays by Byte-type
    private ArrayList<Answer>   mAnswerArrayList; // ArrayList of Answer which is a model class of question which got from Firebase

    public String getTitle(){
        return   mTitle;
    }

    public String getBody(){
        return mBody;
    }

    public String getName(){
        return mName;
    }

    public String getmUid(){
        return mUid;
    }

    public String getQuestionUid(){
        return mQuestionUid;
    }

    public int getGenre(){
        return mGenre;
    }

    public byte[] getImageBytes(){
        return mBitmapArray;
    }

    public ArrayList<Answer> getAnswers(){
        return mAnswerArrayList;
    }

    public Question(String title, String body, String name, String uid, String questionUid, int genre, byte[] bytes, ArrayList<Answer> answers){
        mTitle = title;
        mBody = body;
        mName = name;
        mUid = uid;
        mQuestionUid = questionUid;
        mGenre = genre;
        mBitmapArray = bytes.clone();
        mAnswerArrayList = answers;

    }

}
