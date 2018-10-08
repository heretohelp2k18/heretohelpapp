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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private DatabaseReference myPhoto;
    private List<Messages> messages = new ArrayList<>();
    private ListView lvMessages;
    private EditText txtUserComment;
    private ImageView btnSend;
    private CommentList commentAdapter;
    private String UserID = "";
    private String UserType = "";
    Context appContext;

    private SharedPreferences prefs;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appContext = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        database = FirebaseDatabase.getInstance();
        // Getting intent extras
        Intent intent = getIntent();
        if((intent.getStringExtra("chatroomid") != null) && (UserSessionUtil.getSession(appContext, "usertype").equals("Psychologist")))
        {
            UserSessionUtil.setSession(appContext, "chatroom", intent.getStringExtra("chatroomid"));
        }

        UserID = UserSessionUtil.getSession(appContext, "userid");
        UserType = UserSessionUtil.getSession(appContext, "usertype");

        if(UserType.equals("Psychologist")) {
            final DatabaseReference fireChatRoom = myRef = database.getReference("chatroom").child(UserSessionUtil.getSession(appContext, "chatroom"));
            fireChatRoom.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                    if ((chatRoom.getPsychoid().equals("0")) || (UserID.equals(chatRoom.getPsychoid()))) {
                        if (chatRoom.getPsychoid().equals("0")) {
                            fireChatRoom.child("psychoid").setValue(UserID);
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //attaching value event listener
        myRef.addValueEventListener(new ValueEventListener() {
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

//        myPhoto.orderByChild("id").equalTo(currentUser.getUid()).limitToFirst(1).addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                UserPhoto user = dataSnapshot.getValue(UserPhoto.class);
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth.getInstance().signOut();
    }
}
