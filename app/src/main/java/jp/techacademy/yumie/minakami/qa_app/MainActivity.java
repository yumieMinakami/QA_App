package jp.techacademy.yumie.minakami.qa_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private int     mGenre = 0;
    private boolean mFavFlag;

    // Member vars for access to Firebase
    private DatabaseReference   mDatabaseReference;
    private DatabaseReference   mGenreRef;
    private ListView            mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {     // It calls when an item - Question - added
            HashMap  map         = (HashMap) dataSnapshot.getValue();
            String   title       = (String) map.get("title");
            String   body        = (String) map.get("body");
            String   name        = (String) map.get("name");
            String   uid         = (String) map.get("uid");
            String   imageString = (String) map.get("image");
            Bitmap   image       = null;
            byte[]   bytes;

            if(imageString != null){
                BitmapFactory.Options options = new BitmapFactory.Options();
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap) map.get("ansers");
            if(answerMap != null){
                for(Object key : answerMap.keySet()){
                    HashMap temp = (HashMap) answerMap.get((String) key);
                    String answereBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    Answer answer = new Answer(answereBody, answerName, answerUid, (String) key);
                    answerArrayList.add(answer);
                }
            }

            Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
            mQuestionArrayList.add(question);   // Add ArrayList
            mAdapter.notifyDataSetChanged();
        }

        // It calls when an Answer posted against Question
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {   // It calls when an item changed
            HashMap map = (HashMap) dataSnapshot.getValue();

            // Search changed Question
            for(Question question : mQuestionArrayList){
                if(dataSnapshot.getKey().equals(question.getQuestionUid())){
                    // It is possible for only Answer to change in this app
                    question.getAnswers().clear();

                    HashMap answerMap = (HashMap) map.get("answers");

                    if(answerMap != null){
                        for(Object key : answerMap.keySet()){
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };      // for adding/changing q & a

    private ChildEventListener mFavEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//            HashMap hm = (HashMap) dataSnapshot.getValue();
//            String Quid = (String) hm.get("key");
//            Log.d("favref", String.format("onChildAdded ::: getkey() = %s, key = %s, value = %s", dataSnapshot.getKey(), hm.get("key"), hm.get("value")));
            Log.d("favref", String.format("onChildAdded ::: getkey() = %s getvalue() = %s", dataSnapshot.getKey(), dataSnapshot.getValue()));
            String sFav = (String) dataSnapshot.getKey();
//            Query q = mDatabaseReference.child(Const.ContentsPATH).child("genre").orderByKey().equalTo(sFav);
            Query q = mDatabaseReference.child(Const.ContentsPATH).child(dataSnapshot.getValue().toString()).orderByKey().equalTo(sFav);
            Log.d("favref", String.format("onChildAdded ::: q = %s", q));
            q.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    HashMap  map         = (HashMap) dataSnapshot.getValue();
                    String   title       = (String) map.get("title");
                    String   body        = (String) map.get("body");
                    String   name        = (String) map.get("name");
                    String   uid         = (String) map.get("uid");
                    String   imageString = (String) map.get("image");
                    Bitmap   image       = null;
                    byte[]   bytes;

                    if(imageString != null){
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        bytes = Base64.decode(imageString, Base64.DEFAULT);
                    } else {
                        bytes = new byte[0];
                    }
                    ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                    HashMap answerMap = (HashMap) map.get("ansers");
                    if(answerMap != null){
                        for(Object key : answerMap.keySet()){
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answereBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answereBody, answerName, answerUid, (String) key);
                            answerArrayList.add(answer);
                        }
                    }

                    Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
                    mQuestionArrayList.add(question);   // Add ArrayList
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.d("favref", "query onchildchanged");
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//            HashMap hm = (HashMap) dataSnapshot.getValue();
//            Log.d("favref", String.format("onChildchanged ::: getkey() = %s, keyset = %s", dataSnapshot.getKey(), hm.keySet()));
            Log.d("favref", String.format("onChildchanged ::: getkey() = %s", dataSnapshot.getKey()));

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            HashMap hm = (HashMap) dataSnapshot.getValue();
            Log.d("favref", String.format("onChildRemoved ::: keyset = %s", hm.keySet()));
            Question qstn = dataSnapshot.getValue(Question.class);
            String title = qstn.getTitle();
            String body = qstn.getBody();
            String name = qstn.getName();
            String uid = (String) qstn.getUid();
            byte[] bytes = qstn.getImageBytes();

            ArrayList<Answer> answerArrayList = qstn.getAnswers();

            Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
            mQuestionArrayList.remove(question);   // remove ArrayList
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };   // for showing favorites

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(mDatabaseReference == null && savedInstanceState == null){
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                if(mGenre == 0){
                    Snackbar.make(view, "ジャンルを選択してください", Snackbar.LENGTH_LONG).show();
                    return;
                }

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();    // Get user's login status

                if(user == null){   // if not login, move to login screen
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                    intent.putExtra("genre", mGenre);
                    startActivity(intent);
                }
            }
        });

        // Set Navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name){
            public void onDrawerOpened(View drawerView){
                invalidateOptionsMenu();
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();     // Synchronize  with the linked DrawerLayout

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);   // Get ID of NavigationView
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(MenuItem item){ // If chosen any items on NavigationView
                int id = item.getItemId();

                mFavFlag = false;

                // Set title of screen depending on chosen item
                if(id == R.id.nav_hobby){
                    mToolbar.setTitle("趣味");
                    mGenre = 1;
                } else if (id == R.id.nav_life){
                    mToolbar.setTitle("生活");
                    mGenre  =2;
                } else if (id == R.id.nav_health){
                    mToolbar.setTitle("健康");
                    mGenre = 3;
                } else if (id == R.id.nav_computer){
                    mToolbar.setTitle("コンピューター");
                    mGenre = 4;
                } else if (id == R.id.nav_favorite){
                    mToolbar.setTitle("お気に入り");
                    mFavFlag = true;

                    FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
                    if(usr == null) {
                        mToolbar.setTitle(R.string.app_name);
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                // Set Adapter again after clear the Question list, and reset Adapter to ListView
                mQuestionArrayList.clear();
                mAdapter.setQuestionArrayList(mQuestionArrayList);
                mListView.setAdapter(mAdapter);

                // Set Listener to selected genre
                if(mGenreRef != null){
                    mGenreRef.removeEventListener(mEventListener);
                }
                if(mFavFlag == true){
                    FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
                    if(usr != null){
                        mGenreRef = mDatabaseReference.child(Const.UsersPATH).child(usr.getUid()).child(Const.FavPATH);
                        mGenreRef.addChildEventListener(mFavEventListener);
                        fab.setVisibility(INVISIBLE);
                    }
                } else {
                    mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
                    mGenreRef.addChildEventListener(mEventListener);
                    fab.setVisibility(VISIBLE);
                }

                return true;
            }
        });

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // ListView preparation
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();

        // Listen a tap on question list on question list screen, and move to detail question screen
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                // Send Question instance and Start detail Question screen
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
