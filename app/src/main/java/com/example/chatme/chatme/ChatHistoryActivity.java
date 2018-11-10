package com.example.chatme.chatme;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

public class ChatHistoryActivity extends AppCompatActivity {

    Context appContext;
    private List<ChatHistory> chatHistoryItem = new ArrayList<>();
    ChatHistoryList chatHistoryAdapter;
    ListView lvChatRoomHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContext = this;

        setContentView(R.layout.activity_chat_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        lvChatRoomHistory = (ListView) findViewById(R.id.lvhistory);

        FetchChatRoomTask fetchTask = new FetchChatRoomTask();
        fetchTask.execute((Void) null);
    }

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

    public class FetchChatRoomTask extends AsyncTask<Void, Void, Boolean> {

        String responseMessage = getResources().getString(R.string.connection_failed);
        String errorcode = "";

        FetchChatRoomTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CommonUtil.showProgress(appContext, true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean success = false;
            BufferedReader reader;
            try
            {
                List<NameValuePair> http_params = new LinkedList<NameValuePair>();
                http_params.add(new BasicNameValuePair("username", UserSessionUtil.getSession(appContext, "username")));
                http_params.add(new BasicNameValuePair("token", UserSessionUtil.getSession(appContext, "token")));
                http_params.add(new BasicNameValuePair("userid", UserSessionUtil.getSession(appContext, "userid")));
                http_params.add(new BasicNameValuePair("usertype", UserSessionUtil.getSession(appContext, "usertype")));

                String paramString = URLEncodedUtils.format(http_params, "utf-8");
                Log.e("paramString",paramString);
                String server_url = getResources().getString(R.string.serverUrl) + "/GetChatroom?"+paramString;

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

                chatHistoryItem.clear();
                UserSessionUtil.setSession(appContext, "chatroom_json_data", finalJSON);
                // ChatRoom Array
                JSONArray crArray = parentObject.getJSONArray("chatroom_data_distinct_for_client");
                for(int i = 0; i < crArray .length(); i++){
                    JSONObject finalObject = crArray .getJSONObject(i);
                    ChatHistory newItem = new ChatHistory();
                    newItem.setChatroom(finalObject.getString("chatroom"));
                    newItem.setChatmate(finalObject.getString("chatmate"));
                    newItem.setChatdate(finalObject.getString("chatdate"));
                    newItem.setGender(finalObject.getString("gender"));
                    chatHistoryItem.add(newItem);
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
                chatHistoryAdapter = new ChatHistoryList(ChatHistoryActivity.this, chatHistoryItem);
                //attaching adapter to the listview
                lvChatRoomHistory.setAdapter(chatHistoryAdapter);
                lvChatRoomHistory.post(new Runnable() {
                    @Override
                    public void run() {
                        // Select the last row so it will scroll into view...
                        lvChatRoomHistory.setSelection(chatHistoryAdapter.getCount() - 1);
                    }
                });
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
