package com.example.chatme.chatme;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private DatabaseReference fireGuest;
    private ValueEventListener guestSessionListener;
    private ValueEventListener chatListener;
    private DatabaseReference myPhoto;
    private List<Messages> messages = new ArrayList<>();
    private ListView lvMessages;
    private EditText txtUserComment;
    private ImageView btnSend;
    private CommentList commentAdapter;
    private String UserID = "";
    private String UserType = "";
    private String UserFullName = "";
    LinearLayout llhistory;
    LinearLayout commentBox;
    Context appContext;

    private SharedPreferences prefs;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appContext = this;
        UserSessionUtil.setSession(appContext, "ACTIVITY", "4");
        //llhistory = (LinearLayout) findViewById(R.id.llhistory);
        commentBox = (LinearLayout) findViewById(R.id.llcomponents);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        database = FirebaseDatabase.getInstance();

        if(UserSessionUtil.getSession(appContext, "isguest").equals("1"))
        {
            fireGuest = database.getReference("guest");
            final String userId = UserSessionUtil.getSession(appContext, "userid");
            fireGuest.child(userId).setValue("1");

            guestSessionListener = fireGuest.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String key = dataSnapshot.getKey();
                    String status = dataSnapshot.getValue().toString();
                    if(status.equals("0") && (key.equals(userId)))
                    {
                        UserSessionUtil.clearSession(appContext);
                        CommonUtil.showAlertWithCallback(appContext, appContext.getResources().getString(R.string.guestTimeOutMessage), new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                Intent i = new Intent(appContext, LoginActivity.class);
                                startActivity(i);
                                return null;
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        // Getting intent extras
        Intent intent = getIntent();
        if((intent.getStringExtra("chatroomid") != null) && (UserSessionUtil.getSession(appContext, "usertype").equals("Psychologist")))
        {
            UserSessionUtil.setSession(appContext, "chatroom", intent.getStringExtra("chatroomid"));
        }

        if(intent.getStringExtra("chatmate") != null)
        {
            UserSessionUtil.setSession(appContext, "chatmate", intent.getStringExtra("chatmate"));
        }

        if((intent.getStringExtra("viewonly") != null) && (intent.getStringExtra("viewonly").equals("yes")))
        {
            UserSessionUtil.setSession(appContext, "chatroom", intent.getStringExtra("chatroomid"));
//            commentBox.setVisibility(View.GONE);
        }

        UserID = UserSessionUtil.getSession(appContext, "userid");
        UserType = UserSessionUtil.getSession(appContext, "usertype");
        UserFullName = UserSessionUtil.getSession(appContext, "userfirstname") + " " + UserSessionUtil.getSession(appContext, "userlastname");

        if(UserType.equals("Psychologist")) {
            final DatabaseReference fireChatRoom = myRef = database.getReference("chatroom").child(UserSessionUtil.getSession(appContext, "chatroom"));
            fireChatRoom.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                    if(chatRoom.getExpired()) {
                        CommonUtil.showAlertWithCallback(appContext, "Chat request expired.", new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                UserSessionUtil.setSession(appContext, "chatroom", "");
                                Intent intent = new Intent(appContext, ChatBotActivity.class);
                                startActivity(intent);
                                return null;
                            }
                        });
                    } else if ((chatRoom.getPsychoid().equals("0")) || (UserID.equals(chatRoom.getPsychoid()))) {
                        if (chatRoom.getPsychoid().equals("0")) {
                            fireChatRoom.child("psychoid").setValue(UserID);
                            fireChatRoom.child("psychoname").setValue(UserFullName);
                            DatabaseReference fireChatRoomMessages = database.getReference("chatroom").child(UserSessionUtil.getSession(appContext, "chatroom")).child("messages");
                            String id = fireChatRoomMessages.push().getKey();
                            String autoresponse = UserSessionUtil.getSession(appContext, "userautoresponse");
                            if(autoresponse.trim().equals(""))
                            {
                                autoresponse = getResources().getString(R.string.default_auto_response);
                            }
                            Messages msg = new Messages(UserID, autoresponse, UserType);
                            fireChatRoomMessages.child(id).setValue(msg);
                        }
                    } else {
                        CommonUtil.showAlertWithCallback(appContext, "This request has been taken by other psychologist.", new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                UserSessionUtil.setSession(appContext, "chatroom", "");
                                Intent intent = new Intent(appContext, ChatBotActivity.class);
                                startActivity(intent);
                                return null;
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        //Load Chat History
        try {
            ListView lv = (ListView) findViewById(R.id.lvHistoryList);
            String chatmate = UserSessionUtil.getSession(appContext, "chatmate");
            if(!chatmate.trim().equals("")) {
                ArrayList arrayList = new ArrayList();
                final ArrayList arrayListIndex = new ArrayList();

                String chatroom_json_data = UserSessionUtil.getSession(appContext, "chatroom_json_data");
                JSONObject parentObject = new JSONObject(chatroom_json_data);
                JSONArray crArray = parentObject.getJSONArray("chatroom_data_raw");
                for (int i = 0; i < crArray.length(); i++) {
                    JSONObject finalObject = crArray.getJSONObject(i);
                    if(chatmate.equals(finalObject.getString("chatmate"))) {
                        arrayList.add(finalObject.getString("chatdate"));
                        arrayListIndex.add(finalObject.getString("chatroom"));
                    }
                }

                ArrayAdapter adapter = new ArrayAdapter<String>(appContext, android.R.layout.simple_list_item_1, arrayList);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String chatroom = arrayListIndex.get(position).toString();
                        UserSessionUtil.setSession(appContext, "chatroom", chatroom);
                        loadMessages();
                        bindingChatMessages();
                    }
                });

                lv.setVisibility(View.VISIBLE);
                UserSessionUtil.setSession(appContext, "chatmate", "");
            }
            else
            {
                lv.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e("JSON Error:", e.toString());
        }
        //End of Load chat History

        loadMessages();
    }

    public void loadMessages()
    {
        myRef = database.getReference("chatroom").child(UserSessionUtil.getSession(appContext, "chatroom")).child("messages");
        lvMessages = (ListView) findViewById(R.id.lvMessages);
        lvMessages.setDivider(null);
        txtUserComment = (EditText) findViewById(R.id.txtUserComment);
        btnSend = (ImageView) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = txtUserComment.getText().toString();
                if(!comment.trim().equals("")) {
                    String id = myRef.push().getKey();
                    Messages msg = new Messages(UserID, comment, UserType);
                    myRef.child(id).setValue(msg);
                    txtUserComment.setText("");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.topbuttons, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        switch (item.getItemId()) {
//            case R.id.action_logout:
//                FirebaseAuth.getInstance().signOut();
//                Intent i = new Intent(appContext, LoginActivity.class);
//                startActivity(i);
//                return true;
//            case R.id.action_account:
//                Intent ac = new Intent(appContext, AccountActivity.class);
//                startActivity(ac);
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                myRef.removeEventListener(chatListener);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //attaching value event listener
        bindingChatMessages();

    }

    public void bindingChatMessages()
    {
        if(chatListener != null)
        {
            myRef.removeEventListener(chatListener);
            lvMessages.setAdapter(null);
        }
        chatListener = myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //clearing the previous messages list
                messages.clear();
                //String lastUserFetched = "";
                //iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //getting message
                    Messages message = postSnapshot.getValue(Messages.class);
                    //adding message to the list
                    //lastUserFetched = message.getName();
                    messages.add(message);
                }

                //creating adapter
                commentAdapter = new CommentList(MainActivity.this, messages);
                //attaching adapter to the listview
                lvMessages.setAdapter(commentAdapter);
                lvMessages.post(new Runnable() {
                    @Override
                    public void run() {
                        // Select the last row so it will scroll into view...
                        lvMessages.setSelection(commentAdapter.getCount() - 1);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        myRef.removeEventListener(chatListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(UserSessionUtil.getSession(appContext, "isguest").equals("1")) {
            fireGuest = database.getReference("guest");
            final String userId = UserSessionUtil.getSession(appContext, "userid");
            fireGuest.child(userId).removeEventListener(guestSessionListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!UserSessionUtil.isValidSession(appContext))
        {
            Intent i = new Intent(appContext, LoginActivity.class);
            startActivity(i);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //FirebaseAuth.getInstance().signOut();

    }
}
