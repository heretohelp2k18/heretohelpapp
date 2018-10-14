package com.example.chatme.chatme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Html;
import android.util.JsonReader;
import android.util.Log;
import android.util.TypedValue;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import java.util.Timer;
import java.util.TimerTask;
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
        chatbotContainer = (LinearLayout) findViewById(R.id.cbcomments);
        answerContainer = (LinearLayout) findViewById(R.id.answerContainer);
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

    }

    public void showWaitingLoader()
    {
        chatbotContainer.removeAllViews();
        answerContainer.removeAllViews();
        ProgressBar progressBar = new ProgressBar(appContext);
        LinearLayout.LayoutParams progressBarLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBarLp.gravity = Gravity.CENTER;
        progressBarLp.setMargins(0, 100,0, 40);
        progressBar.setLayoutParams(progressBarLp);
        chatbotContainer.addView(progressBar);
        TextView findingText = new TextView(appContext);
        findingText.setText("We're finding you a member to talk to.");
        findingText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        chatbotContainer.addView(findingText);
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
            initializeChat(true);
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
        TextView botMsg = new TextView(appContext);
        botMsg.setText(Html.fromHtml(message));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.LEFT;
        lp.setMargins(10, 10, 10, 10);
        botMsg.setLayoutParams(lp);
        botMsg.setBackgroundResource(R.drawable.bgroundedleft);

        GradientDrawable bgdrawable = (GradientDrawable) botMsg.getBackground();
        bgdrawable.setColor(Color.parseColor("#DDDDDD"));
        chatbotContainer .addView(botMsg);
        convoScrollDown();
    }

    public void setBotAnswers(String tag, final int value, final String action)
    {
        final String message = CommonUtil.stripHtml(UserSessionUtil.getSession(appContext, tag));
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
                botMsg.setTextColor(getResources().getColor(R.color.white));
                botMsg.setBackgroundResource(R.drawable.bgroundedright);
                GradientDrawable bgdrawable = (GradientDrawable) botMsg.getBackground();
                bgdrawable.setColor(Color.parseColor("#3498db"));
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

    public void initializeChat(Boolean newChatRoom)
    {
        //CommonUtil.showProgressCustom(appContext,"We're finding you a psychologist..");
        chatbotContainer.removeAllViews();
        answerContainer.removeAllViews();
        ProgressBar progressBar = new ProgressBar(appContext);
        LinearLayout.LayoutParams progressBarLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBarLp.gravity = Gravity.CENTER;
        progressBarLp.setMargins(0, CommonUtil.dpToPx(appContext, 100),0, 40);
        progressBar.setLayoutParams(progressBarLp);
        chatbotContainer.addView(progressBar);
        TextView findingText = new TextView(appContext);
        findingText.setText("We're finding you a psychologist...");
        findingText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        chatbotContainer.addView(findingText);

        fireDB = FirebaseDatabase.getInstance();

        UserSessionUtil.setSession(appContext,"requesting", "yes");

        // Setting up Chat Room
        String RoomId;
        final DatabaseReference fireChatRoom = fireDB.getReference("chatroom");
        if(newChatRoom) {
            RoomId = fireChatRoom.push().getKey();
            UserSessionUtil.setSession(appContext, "chatroom", RoomId);
        } else {
            RoomId = UserSessionUtil.getSession(appContext, "chatroom");
        }

        final String chatRoomId = RoomId;

        ChatRoom chatRoom = new ChatRoom(UserSessionUtil.getSession(appContext, "userid"),
                                            UserSessionUtil.getSession(appContext, "userfirstname"),
                                            "0","");
        fireChatRoom.child(chatRoomId).setValue(chatRoom);
        chatRoomListener = fireChatRoom.child(chatRoomId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                if(!chatRoom.getPsychoid().equals("0"))
                {
                    fireChatRoom.child(chatRoomId).removeEventListener(chatRoomListener);
                    CommonUtil.dismissProgressDialog();
                    UserSessionUtil.setSession(appContext,"requesting", "no");
                    Intent i = new Intent(appContext, MainActivity.class);
                    startActivity(i);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Chat Room Request Expiry
        TimerTask myTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(UserSessionUtil.getSession(ChatBotActivity.this, "requesting").equals("yes"))
                        {
                            fireChatRoom.child(chatRoomId).removeEventListener(chatRoomListener);
                            ChatBotActivity.this.chatbotContainer.removeAllViews();
                            ChatBotActivity.this.RetryPrompt();
                        }
                    }
                });

            }
        };
        Timer timer = new Timer();
        timer.schedule(myTimerTask, Integer.parseInt(getResources().getString(R.string.psychoRequestTimeOut)));
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
                        UserDataNotif userData = new UserDataNotif(UserSessionUtil.getSession(appContext, "userfirstname") + " " + UserSessionUtil.getSession(appContext, "userlastname"),
                                UserSessionUtil.getSession(appContext, "userid"),
                                UserSessionUtil.getSession(appContext, "usergender"),
                                chatRoomId);
                        fireRef.child(psyID).child("available").setValue(false);
                        fireChatNotif.child(psyID).setValue(userData);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void PassNotificationToOtherPsych(final UserDataNotif userData)
    {
        // Broadcasting notification
        final DatabaseReference fireChatNotif = fireDB.getReference("chatnotif");
        fireRef = fireDB.getReference("online");
        fireRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Online ol = snapshot.getValue(Online.class);
                    if(ol.getAvailable() && (!snapshot.getKey().equals(UserSessionUtil.getSession(appContext, "userid")))) {
                        String psyID = snapshot.getKey();
                        fireRef.child(psyID).child("available").setValue(false);
                        fireChatNotif.child(psyID).setValue(userData);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void RetryPrompt()
    {
        CommonUtil.showAlertMessageWithAction(appContext, "Psychologists seems busy at the moment. Would you like to retry?", new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ChatBotActivity.this.initializeChat(false);
                return null;
            }
        }, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ChatBotActivity.this.BotRouter("G1");
                return null;
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
                final UserDataNotif userdn = dataSnapshot.getValue(UserDataNotif.class);
                if(userdn != null) {
                    chatbotContainer.removeAllViews();
                    // Image Avatar
                    ImageView imgAvatar = new ImageView(appContext);
                    Bitmap bitmap;
                    if (userdn.getGender().equals("Male")) {
                        bitmap = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.boy);
                    } else {
                        bitmap = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.girl);
                    }

                    RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                    roundDrawable.setCircular(true);
                    imgAvatar.setImageDrawable(roundDrawable);
                    LinearLayout.LayoutParams lpAvatar = new LinearLayout.LayoutParams(
                            CommonUtil.dpToPx(appContext, 150),
                            CommonUtil.dpToPx(appContext, 150));
                    lpAvatar.gravity = Gravity.CENTER;
                    lpAvatar.setMargins(10, CommonUtil.dpToPx(appContext,100), 10, 20);
                    imgAvatar.setLayoutParams(lpAvatar);

                    chatbotContainer.addView(imgAvatar);

                    // Name text
                    TextView userNameText = new TextView(appContext);
                    userNameText.setText(userdn.getName());
                    userNameText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    userNameText.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
                    LinearLayout.LayoutParams lpName = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    lpName.setMargins(10, CommonUtil.dpToPx(appContext,10), 10, CommonUtil.dpToPx(appContext,50));
                    userNameText.setLayoutParams(lpName);
                    chatbotContainer.addView(userNameText);

                    // ACCEPT BUTTON
                    Button acceptBtn = new Button(appContext);
                    acceptBtn.setText("ACCEPT");
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.gravity = Gravity.CENTER;
                    lp.setMargins(10, 10, 10, 10);
                    acceptBtn.setLayoutParams(lp);
                    acceptBtn.setBackgroundResource(R.drawable.bgrounded);
                    acceptBtn.setTextColor(getResources().getColor(R.color.white));
                    GradientDrawable bgdrawable = (GradientDrawable) acceptBtn.getBackground();
                    bgdrawable.setColor(getResources().getColor(R.color.colorPrimaryDark));
                    acceptBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fireChatNotif.removeValue();
                            InsertChatRoomTask insertChatRoomTask = new InsertChatRoomTask(userdn.getChatroom(), userdn.getId());
                            insertChatRoomTask.execute((Void) null);
                        }
                    });
                    chatbotContainer.addView(acceptBtn);

                    // DENY BUTTON
                    Button denyBtn = new Button(appContext);
                    denyBtn.setText("DENY");
                    LinearLayout.LayoutParams lpd = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    lpd.gravity = Gravity.CENTER;
                    lpd.setMargins(10, 10, 10, 10);
                    denyBtn.setLayoutParams(lpd);
                    denyBtn.setBackgroundResource(R.drawable.bgrounded);
                    denyBtn.setTextColor(getResources().getColor(R.color.white));
                    GradientDrawable bgddrawable = (GradientDrawable) denyBtn.getBackground();
                    bgddrawable.setColor(getResources().getColor(R.color.colorRed));
                    denyBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fireChatNotif.removeValue();
                            showWaitingLoader();
                            DatabaseReference fireOnline = fireDB.getReference("online").child(UserSessionUtil.getSession(appContext,"userid"));
                            fireOnline.child("available").setValue(true);
                            PassNotificationToOtherPsych(userdn);
                        }
                    });
                    chatbotContainer.addView(denyBtn);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!UserSessionUtil.isValidSession(appContext))
        {
            Intent i = new Intent(appContext, LoginActivity.class);
            startActivity(i);
        }
        else
        {
            if(UserSessionUtil.getSession(appContext, "usertype").equals("Psychologist"))
            {
                fireDB = FirebaseDatabase.getInstance();
                fireRef = fireDB.getReference("online");
                DatabaseReference userRef = fireRef.child(UserSessionUtil.getSession(appContext, "userid"));
                Online ol = new Online(true, true);
                userRef.setValue(ol);
                chatNotifListener();
                userRef.onDisconnect().removeValue();
                showWaitingLoader();
            }
            else if((UserSessionUtil.getSession(appContext, "usertype").equals("User")) && (!UserSessionUtil.getSession(appContext, "initialBotLoaded").equals("yes"))) {
                FetchDataTask fetchTask = new FetchDataTask();
                fetchTask.execute((Void) null);
            }
            else if(UserSessionUtil.getSession(appContext, "initialBotLoaded").equals("yes"))
            {
                chatbotContainer.removeAllViews();
                answerContainer.removeAllViews();
                BotRouter("G1");
            }
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
//        getMenuInflater().inflate(R.menu.chat_bot, menu);
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

        if (id == R.id.nav_history) {

        } else if (id == R.id.nav_account) {
            Intent i = new Intent(appContext,SignUpActivity.class);
            i.putExtra("action","sign-update");
            startActivity(i);
        } else if (id == R.id.nav_logout) {
            CommonUtil.showAlertMessageWithAction(appContext, "Are you sure you want to log out?", new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    UserSessionUtil.clearSession(ChatBotActivity.this);
                    Intent i = new Intent(ChatBotActivity.this, LoginActivity.class);
                    startActivity(i);
                    return null;
                }
            }, null);
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
                UserSessionUtil.setSession(appContext, "initialBotLoaded", "yes");
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

    public class InsertChatRoomTask extends AsyncTask<Void, Void, Boolean> {

        String responseMessage = getResources().getString(R.string.connection_failed);
        String errorcode = "";
        String chatroom = "";
        String userid = "";

        InsertChatRoomTask(String chatRoom, String userId) {
            chatroom = chatRoom;
            userid = userId;
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
                http_params.add(new BasicNameValuePair("psychoid", UserSessionUtil.getSession(appContext, "userid")));
                http_params.add(new BasicNameValuePair("userid", userid));
                http_params.add(new BasicNameValuePair("chatroom", chatroom));

                String paramString = URLEncodedUtils.format(http_params, "utf-8");
                Log.e("paramString",paramString);
                String server_url = getResources().getString(R.string.serverUrl) + "/AddChatroom?"+paramString;

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
                Intent intentNotif = new Intent(appContext, MainActivity.class);
                intentNotif.putExtra("chatroomid", chatroom);
                startActivity(intentNotif);
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
