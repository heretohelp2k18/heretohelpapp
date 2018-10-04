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
    LinearLayout chatbotContainer;
    LinearLayout answerContainer;
    ScrollView cbsView;
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

        FetchDataTask fetchTask = new FetchDataTask();
        fetchTask.execute((Void) null);
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
        String follow = UserSessionUtil.getSession(appContext, tag+"_follow");
        if((follow != null) && (!follow.equals("")))
        {
            BotRouter(follow);
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
        bgdrawable.setColor(Color.parseColor("#3F51B5"));
        chatbotContainer .addView(botMsg);
        convoScrollDown();
    }

    public void setBotAnswers(String tag, int value, final String action)
    {
        final String message = CommonUtil.stripHtml(UserSessionUtil.getSession(appContext, tag));

        answerContainer = (LinearLayout) findViewById(R.id.answerContainer);
        Button answer1 = new Button(appContext);
        answer1.setText(message);
        answer1.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        answer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                chatbotContainer .addView(botMsg);
                answerContainer.removeAllViewsInLayout();
                BotRouter(action);
            }
        });
        answerContainer.addView(answer1);
        convoScrollDown();
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
