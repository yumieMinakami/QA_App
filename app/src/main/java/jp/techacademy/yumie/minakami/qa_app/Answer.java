package jp.techacademy.yumie.minakami.qa_app;

import java.io.Serializable;

/**
 * Created by user on 2016/12/02.
 */

public class Answer implements Serializable {

    private String mBody;       // Answer content which got from Firebase
    private String mName;       // Answerer name which got from Firebase
    private String mUid;        // Answerer UID which got from Firebase
    private String mAnswerUid; // Answer's UID which got from Firebase

    public Answer(String body, String name, String uid, String answerUid){
        mBody = body;
        mName = name;
        mUid = uid;
        mAnswerUid = answerUid;
    }

    public String getBody(){
        return mBody;
    }

    public String getName(){
        return mName;
    }

    public String getUid(){
        return mUid;
    }

    public String getAnswerUid(){
        return mAnswerUid;
    }
}
