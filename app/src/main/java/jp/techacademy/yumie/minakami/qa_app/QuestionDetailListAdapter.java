package jp.techacademy.yumie.minakami.qa_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by user on 2016/12/02.
 */

public class QuestionDetailListAdapter extends BaseAdapter {

    // Consts for flag of layout
    private final static int TYPE_QUESTION = 0; // for question layout
    private final static int TYPE_ANSWER = 1;   // for answer layout

    private LayoutInflater mLayoutInflater = null;
    private Question mQuestion;

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
            if(bytes.length != 0){
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
                imageView.setImageBitmap(image);
            }
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
