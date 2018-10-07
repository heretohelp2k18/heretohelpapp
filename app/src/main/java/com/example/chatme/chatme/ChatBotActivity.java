package com.example.chatme.chatme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

public class ChatBotActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Context appContext;
    FirebaseDatabase fireDB;
    DatabaseReference fireRef;
    LinearLayout chatbotContainer;
    LinearLayout answerContainer;
    ScrollView cbsView;
    Double questionCounter = 0.0;
    Double answerCounter = 0.0;
    Boolean isQuestion = false;
    ValueEventListener chatRoomListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);
        appContext = this;
        cbsView = (ScrollView) (findViewById(R.id.cbsview));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nView = navigationView.getHeaderView(0);
        TextView txtfirstname = (TextView) nView.findViewById(R.id.navFirstName);
        TextView txtemail = (TextView) nView.findViewById(R.id.navUserEmai);
        String fname = UserSessionUtil.getSession(appContext, "userfirstname");
        String email = UserSessionUtil.getSession(appContext, "useremail");
        txtfirstname.setText(fname);
        txtemail.setText(email);

        if(UserSessionUtil.getSession(appContext, "usertype").equals("User")) {
            FetchDataTask fetchTask = new FetchDataTask();
            fetchTask.execute((Void) null);
        } else {
            fireDB = FirebaseDatabase.getInstance();
            fireRef = fireDB.getReference("online");
            DatabaseReference userRef = fireRef.child(UserSessionUtil.getSession(appContext, "userid"));
            Online ol = new Online(true, true);
            userRef.setValue(ol);
            chatNotifListener();
            userRef.onDisconnect().removeValue();
            Toast.makeText(appContext, "We're finding you a member to talk to.", Toast.LENGTH_LONG).show();
        }
    }

    public void convoScrollDown() {
        cbsView.post(new Runnable() {
            @Override
            public void run() {
                cbsView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    public void BotRouter(String tag)
    {
        // Message and Follow-up
        setBotMessage(UserSessionUtil.getSession(appContext, tag));
        if(tag.charAt(0) == 'Q')
        {
            if(tag.equals("Q1"))
            {
                questionCounter = 0.0;
                answerCounter = 0.0;
            }
            isQuestion = true;
        }
        else
        {
            isQuestion = false;
        }

        String follow = UserSessionUtil.getSession(appContext, tag+"_follow");
        if((follow != null) && (!follow.equals("")))
        {
            BotRouter(follow);
        }
        else if(tag.equals("CONNECT"))
        {
            initializeChat();
        }
        else {
            //Answers
            try {
                String sequence = UserSessionUtil.getSession(appContext, tag + "_sequence");
                JSONObject sequenceObject = new JSONObject(sequence);
                JSONArray dataArray = sequenceObject.getJSONArray("data");
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject finalObject = dataArray.getJSONObject(i);
                    String answer = finalObject.getString("tag");
                    int value = Integer.parseInt(finalObject.getString("value"));
                    String action = finalObject.getString("action");
                    setBotAnswers(answer, value, action);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setBotMessage(String message)
    {
        chatbotContainer = (LinearLayout) findViewById(R.id.cbcomments);
        TextView botMsg = new TextView(appContext);
        botMsg.setText(Html.fromHtml(message));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.LEFT;
        lp.setMargins(10, 10, 10, 10);
        botMsg.setLayoutParams(lp);
        botMsg.setBackgroundResource(R.drawable.bgroundedleft);
        botMsg.setTextColor(getResources().getColor(R.color.white));

        GradientDrawable bgdrawable = (GradientDrawable) botMsg.getBackground();
        bgdrawable.setColor(Color.parseColor("#3498db"));
        chatbotContainer .addView(botMsg);
        convoScrollDown();
    }

    public void setBotAnswers(String tag, final int value, final String action)
    {
        final String message = CommonUtil.stripHtml(UserSessionUtil.getSession(appContext, tag));
        answerContainer = (LinearLayout) findViewById(R.id.answerContainer);
        Button answer = new Button(appContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.RIGHT;
        lp.setMargins(10, 10, 10, 10);
        answer.setLayoutParams(lp);
        answer.setBackgroundResource(R.drawable.bgrounded);
        answer.setTextColor(getResources().getColor(R.color.white));
        GradientDrawable bgdrawable = (GradientDrawable) answer.getBackground();
        bgdrawable.setColor(Color.parseColor("#3498db"));
        answer.setText(message);
        answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isQuestion)
                {
                    questionCounter ++;
                    answerCounter += value;
                }

                TextView botMsg = new TextView(appContext);
                botMsg.setText(message);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.RIGHT;
                lp.setMargins(10, 10, 10, 10);
                botMsg.setLayoutParams(lp);
                botMsg.setBackgroundResource(R.drawable.bgroundedright);
                GradientDrawable bgdrawable = (GradientDrawable) botMsg.getBackground();
                bgdrawable.setColor(Color.parseColor("#DDDDDD"));
                botMsg.setGravity(Gravity.RIGHT);
                chatbotContainer.addView(botMsg);
                answerContainer.removeAllViewsInLayout();
                if(action.equals("EVAL")){
                    EvaluateQuestions();
                }
                else {
                    BotRouter(action);
                }
            }
        });

        answerContainer.addView(answer);
        convoScrollDown();
    }

    public void EvaluateQuestions()
    {
        Double result = (answerCounter / questionCounter) * 100;
        if(result > 60)
        {
            BotRouter("EVALRESULT1");
        }
        else
        {
            BotRouter("EVALRESULT0");
        }
    }

    public void initializeChat()
    {
        CommonUtil.showProgressCustom(appContext,"We're finding you a psychologist..");
        fireDB = FirebaseDatabase.getInstance();

        UserSessionUtil.setSession(appContext,"requesting", "yes");
        UserSessionUtil.setSession(appContext, "chatroom", "");

        // Setting up Chat Room
        final DatabaseReference fireChatRoom = fireDB.getReference("chatroom");
        final String chatRoomId = fireChatRoom.push().getKey();
        UserSessionUtil.setSession(appContext, "chatroom", chatRoomId);
        ChatRoom chatRoom = new ChatRoom(UserSessionUtil.getSession(appContext, "userid"), "0");
        fireChatRoom.child(chatRoomId).setValue(chatRoom);
        chatRoomListener = fireChatRoom.child(chatRoomId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                if(!chatRoom.getPsychoid().equals("0"))
                {
                    fireChatRoom.child(chatRoomId).removeEventListener(chatRoomListener);
                    CommonUtil.dismissProgressDialog();
                    Intent i = new Intent(appContext, MainActivity.class);
                    startActivity(i);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // End of Chat Room

        // Broadcasting notification
        final DatabaseReference fireChatNotif = fireDB.getReference("chatnotif");
        fireRef = fireDB.getReference("online");
        fireRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Online ol = snapshot.getValue(Online.class);
                    if(ol.getAvailable() && (UserSessionUtil.getSession(appContext, "requesting").equals("yes"))) {
                        String psyID = snapshot.getKey();
                        UserDataNotif userData = new UserDataNotif(UserSessionUtil.getSession(appContext, "userfirstname"),
                                UserSessionUtil.getSession(appContext, "userid"),
                                UserSessionUtil.getSession(appContext, "usergender"),
                                chatRoomId);
                        String index = fireChatNotif.child(psyID).push().getKey();
                        fireChatNotif.child(psyID).child(index).setValue(userData);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void chatNotifListener()
    {
        final String userid = UserSessionUtil.getSession(appContext, "userid");
        final DatabaseReference fireChatNotif = fireDB.getReference("chatnotif").child(userid);
        fireChatNotif.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserDataNotif userdn = snapshot.getValue(UserDataNotif.class);
                    if(!userdn.getRead()) {
                        Intent intentNotif = new Intent(appContext, MainActivity.class);
                        intentNotif.putExtra("chatroomid", userdn.getChatroom());
                        String content = "Name: " + userdn.getFirstname() + ", Gender: " + userdn.getGender();
                        CommonUtil.showNotification(appContext, "New chat Request", content, intentNotif, R.drawable.chat);
                        userdn.setRead(true);
                        String key = snapshot.getKey();
                        fireChatNotif.child(key).setValue(userdn);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(UserSessionUtil.getSession(appContext, "usertype").equals("Psychologist"))
        {
            fireDB = FirebaseDatabase.getInstance();
            fireRef = fireDB.getReference("online");
            DatabaseReference userRef = fireRef.child(UserSessionUtil.getSession(appContext, "userid"));
            Online ol = new Online(true, true);
            userRef.setValue(ol);
            chatNotifListener();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat_bot, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class FetchDataTask extends AsyncTask<Void, Void, Boolean> {

        String responseMessage = getResources().getString(R.string.connection_failed);
        String errorcode = "";

        FetchDataTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CommonUtil.showProgress(appContext, true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            Boolean success = false;
            BufferedReader reader;
            try
            {
                List<NameValuePair> http_params = new LinkedList<NameValuePair>();
                http_params.add(new BasicNameValuePair("username", UserSessionUtil.getSession(appContext, "username")));
                http_params.add(new BasicNameValuePair("token", UserSessionUtil.getSession(appContext, "token")));

                String paramString = URLEncodedUtils.format(http_params, "utf-8");
                Log.e("paramString",paramString);
                String server_url = getResources().getString(R.string.serverUrl) + "/chatbotdata?"+paramString;

                URL server = new URL(server_url);
                // Create connection
                HttpURLConnection myConnection = (HttpURLConnection) server.openConnection();
                myConnection.connect();
                InputStream stream = myConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while((line = reader.readLine()) != null){
                    buffer.append(line);
                }
                String finalJSON = buffer.toString();
                JSONObject parentObject = new JSONObject(finalJSON);

                // For Preferences
                JSONArray prefArray = parentObject.getJSONArray("preferences");
                for(int i = 0; i < prefArray .length(); i++){
                    JSONObject finalObject = prefArray .getJSONObject(i);
                    String tag = finalObject.getString("tag");
                    String content = finalObject.getString("content");
                    UserSessionUtil.setSession(appContext, tag, content);
                }

                // For ChatBot
                JSONArray chatBotArray = parentObject.getJSONArray("chatbot");
                for(int i = 0; i < chatBotArray .length(); i++){
                    JSONObject finalObject = chatBotArray .getJSONObject(i);
                    String tag = finalObject.getString("tag");
                    String sequence = finalObject.getString("sequence");
                    String follow = finalObject.getString("follow");
                    UserSessionUtil.setSession(appContext, tag + "_sequence", sequence);
                    UserSessionUtil.setSession(appContext, tag + "_follow", follow);
                }

                success = parentObject.getBoolean("success");
                responseMessage = parentObject.getString("message");
                errorcode = parentObject.getString("errorcode");

                myConnection.disconnect();
            }
            catch(Exception e)
            {
                Log.e("Error",e.toString());
            }
            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            CommonUtil.showProgress(appContext, false);

            if (success) {
                BotRouter("G1");
            }
            else {
                if(errorcode.equals("143"))
                {
                    CommonUtil.showAlertWithCallback(appContext, responseMessage, new Callable<Void>() {
                        public Void call() {
                            UserSessionUtil.clearSession(appContext);
                            Intent i = new Intent(appContext, LoginActivity.class);
                            startActivity(i);
                            return null;
                        }
                    }) ;
                }
                else
                {
                    CommonUtil.showAlert(appContext, responseMessage);
                }
            }
        }

        @Override
        protected void onCancelled() {
            CommonUtil.showProgress(appContext, false);
        }
    }
}
