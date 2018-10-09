package com.example.chatme.chatme;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
//import android.support.annotation.NonNull;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

public class SignUpActivity extends AppCompatActivity {
    private String signupUserType = "User";
    private String psychIDFileName = "";
    private EditText s_first_name, s_middle_name, s_last_name, s_age, s_email_address, s_password, s_confirm_password;
    private Spinner s_gender;
    private Button s_signup;
    private LinearLayout photoContainer;
    private Button addPhoto;
    private Context appContext;
    File nativeCaptureTempPhoto;
    String imagePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        this.appContext = this;

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        photoContainer = (LinearLayout) findViewById(R.id.sign_up_picture_container);
        addPhoto = (Button) findViewById(R.id.sign_up_add_photo);
        s_signup = (Button) findViewById(R.id.s_signup);

        Intent intentExtra = getIntent();
        String intentAction = intentExtra.getStringExtra("action");

        switch (intentAction)
        {
            case "sign-in":
                break;
            case "sign-psycho":
                signupUserType = "Psychologist";
                photoContainer.setVisibility(View.VISIBLE);
                addPhoto.setVisibility(View.VISIBLE);
                addPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        capturePhoto();
                    }
                });
                break;
            case "sign-update":
                try {
                    getActionBar().setTitle("My Account");
                } catch (Exception e)
                {
                    getSupportActionBar().setTitle("My Account");
                    e.printStackTrace();
                }
                s_signup.setText("Update Account");
                break;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Spinner Setup
        s_gender = (Spinner) findViewById(R.id.s_gender);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        s_gender.setAdapter(adapter);

        s_email_address = (EditText) findViewById(R.id.s_email_address);
        s_password = (EditText) findViewById(R.id.s_password);
        s_confirm_password = (EditText) findViewById(R.id.s_confirm_password);
        s_first_name = (EditText) findViewById(R.id.s_first_name);
        s_middle_name = (EditText) findViewById(R.id.s_middle_name);
        s_last_name = (EditText) findViewById(R.id.s_last_name);
        s_age = (EditText) findViewById(R.id.s_age);

        // Signup Action
        s_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s_email_address_val = s_email_address.getText().toString();
                String s_password_val = s_password.getText().toString();
                String s_confirm_password_val = s_confirm_password.getText().toString();
                String s_first_name_val = s_first_name.getText().toString();
                String s_middle_name_val = s_middle_name.getText().toString();
                String s_last_name_val = s_last_name.getText().toString();
                String s_age_val = s_age.getText().toString();

                Boolean error = false;
                View focusView = null;
                if(TextUtils.isEmpty(s_first_name_val))
                {
                    s_first_name.setError(getString(R.string.error_field_required));
                    focusView = s_first_name;
                    error = true;
                }
                else if(TextUtils.isEmpty(s_last_name_val))
                {
                    s_last_name.setError(getString(R.string.error_field_required));
                    focusView = s_last_name;
                    error = true;
                }
                else if(TextUtils.isEmpty(s_age_val))
                {
                    s_age.setError(getString(R.string.error_field_required));
                    focusView = s_age;
                    error = true;
                }
                else if(TextUtils.isEmpty(s_email_address_val) && (s_email_address_val.length() < 6))
                {
                    s_email_address.setError(getString(R.string.error_field_required));
                    focusView = s_email_address;
                    error = true;
                }
                else if(!s_email_address_val.contains("@"))
                {
                    s_email_address.setError("Invalid email address");
                    focusView = s_email_address;
                    error = true;
                }
                else if(TextUtils.isEmpty(s_password_val))
                {
                    s_password.setError(getString(R.string.error_field_required));
                    focusView = s_password;
                    error = true;
                }
                else if(TextUtils.isEmpty(s_confirm_password_val))
                {
                    s_confirm_password.setError(getString(R.string.error_field_required));
                    focusView = s_confirm_password;
                    error = true;
                }
                else if(!s_password_val.equals(s_confirm_password_val) || (s_password_val.length()<6))
                {
                    s_confirm_password.setError("Passwords do not match, must be atleast 6 characters.");
                    focusView = s_confirm_password;
                    error = true;
                }
                else if((signupUserType.equals("Psychologist")) && (imagePath == null))
                {
                    CommonUtil.showAlert(appContext, "Take a picture of your license.");
                    focusView = s_confirm_password;
                    error = true;
                }

                if(error) {
                    focusView.requestFocus();
                }
                else
                {
                    User signupUser = new User();
                    signupUser.setFirstname(s_first_name_val);
                    signupUser.setMiddlename(s_middle_name_val);
                    signupUser.setLastname(s_last_name_val);
                    signupUser.setGender(s_gender.getSelectedItem().toString());
                    signupUser.setAge(Integer.parseInt(s_age_val));
                    signupUser.setEmail(s_email_address_val);
                    signupUser.setUsername(s_email_address_val);
                    signupUser.setPassword(s_password_val);
                    signupUser.setUsertype(signupUserType);

                    UserSignUpTask signUpTask = new UserSignUpTask(signupUser);
                    signUpTask.execute((Void) null);
                }
            }
        });
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

    public void capturePhoto()
    {
        try {
            if (ActivityCompat.checkSelfPermission(SignUpActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SignUpActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3004);
            } else {
                launchNativeCamera(3003);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void launchNativeCamera(int REQUEST_CODE) {
        Calendar call = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        psychIDFileName = call.getTimeInMillis()+"_application.png";
        nativeCaptureTempPhoto = new File(Environment.getExternalStorageDirectory(), psychIDFileName);
        imagePath = nativeCaptureTempPhoto.getAbsolutePath();
        Uri picUri = Uri.fromFile(nativeCaptureTempPhoto);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
        startActivityForResult(intent, 3003);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Callback for Native Camera
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == 3003)
            {
                try {
                    File imgFile = new  File(imagePath);
                    if(imgFile.exists()){
                        photoContainer.removeAllViews();

                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        ImageView myImage = new ImageView(this);
                        myImage.setImageBitmap(myBitmap);
                        myImage.setAdjustViewBounds(true);

                        photoContainer.addView(myImage);

                        new UploadFileAsync().execute("");
                    }

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        switch (requestCode) {
            case 3004:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchNativeCamera(3003);
                } else {
                    //do something like displaying a message that he didn`t allow the app to access gallery and you wont be able to let him select from gallery
                }
                break;
        }
    }

    public class UserSignUpTask extends AsyncTask<Void, Void, Boolean> {

        User signupUser;
        String responseMessage = getResources().getString(R.string.connection_failed);

        UserSignUpTask(User userData) {
            signupUser = userData;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CommonUtil.showProgress(appContext, true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean success = false;
            try
            {
                List<NameValuePair> http_params = new LinkedList<NameValuePair>();
                http_params.add(new BasicNameValuePair("firstname", signupUser.getFirstname()));
                http_params.add(new BasicNameValuePair("middlename", signupUser.getMiddlename()));
                http_params.add(new BasicNameValuePair("lastname", signupUser.getLastname()));
                http_params.add(new BasicNameValuePair("gender", signupUser.getGender()));
                http_params.add(new BasicNameValuePair("age", signupUser.getAge() + ""));
                http_params.add(new BasicNameValuePair("email", signupUser.getEmail()));
                http_params.add(new BasicNameValuePair("username", signupUser.getUsername()));
                http_params.add(new BasicNameValuePair("password", signupUser.getPassword()));
                http_params.add(new BasicNameValuePair("usertype", signupUser.getUsertype()));
                http_params.add(new BasicNameValuePair("idimage", psychIDFileName));

                String paramString = URLEncodedUtils.format(http_params, "utf-8");
                Log.e("paramString",paramString);
                String server_url = getResources().getString(R.string.serverUrl) + "/register?"+paramString;

                URL server = new URL(server_url);
                // Create connection
                HttpURLConnection myConnection = (HttpURLConnection) server.openConnection();
                myConnection.setRequestMethod("POST");
                if (myConnection.getResponseCode() == 200) {
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader =
                            new InputStreamReader(responseBody, "UTF-8");
                    JsonReader jsonReader = new JsonReader(responseBodyReader);
                    jsonReader.beginObject(); // Start processing the JSON object
                    while (jsonReader.hasNext()) { // Loop through all keys
                        String key = jsonReader.nextName(); // Fetch the next key
                        if (key.equals("success")) {
                            success = jsonReader.nextBoolean();
                        }
                        else if(key.equals("message"))
                        {
                            responseMessage = jsonReader.nextString();
                        }
                        else
                        {
                            jsonReader.skipValue(); // Skip values of other keys
                        }
                    }
                    jsonReader.close();
                    myConnection.disconnect();
                } else {
                    // Error handling code goes here
                }
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
            if (success)
            {
                CommonUtil.showAlertWithCallback(appContext, responseMessage, new Callable<Void>() {
                    public Void call() {
                        Intent i = new Intent(appContext, MainActivity.class);
                        startActivity(i);
                        return null;
                    }
                }) ;
            }
            else{
                CommonUtil.showAlert(appContext, responseMessage);
            }
        }

        @Override
        protected void onCancelled() {
            CommonUtil.showProgress(appContext, false);
        }
    }

    private class UploadFileAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {

                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(imagePath);

                if (sourceFile.isFile()) {

                    try {
                        String upLoadServerUri = getResources().getString(R.string.serverUrl) + "/uploadimage/?";

                        // open a URL connection to the Servlet
                        FileInputStream fileInputStream = new FileInputStream(
                                sourceFile);
                        URL url = new URL(upLoadServerUri);

                        // Open a HTTP connection to the URL
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE",
                                "multipart/form-data");
                        conn.setRequestProperty("Content-Type",
                                "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("image", imagePath);

                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\""
                                + imagePath + "\"" + lineEnd);

                        dos.writeBytes(lineEnd);

                        // create a buffer of maximum size
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {

                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math
                                    .min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0,
                                    bufferSize);

                        }

                        // send multipart form data necesssary after file
                        // data...
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens
                                + lineEnd);

                        // Responses from the server (code and message)
                        int serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn
                                .getResponseMessage();

                        if (serverResponseCode == 200) {

                            // messageText.setText(msg);
                            //Toast.makeText(ctx, "File Upload Complete.",
                            //      Toast.LENGTH_SHORT).show();

                            // recursiveDelete(mDirectory1);

                        }

                        // close the streams //
                        fileInputStream.close();
                        dos.flush();
                        dos.close();

                    } catch (Exception e) {

                        // dialog.dismiss();
                        e.printStackTrace();

                    }
                    // dialog.dismiss();

                } // End else block


            } catch (Exception ex) {
                // dialog.dismiss();

                ex.printStackTrace();
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            CommonUtil.dismissProgressDialog();
        }

        @Override
        protected void onPreExecute() {
            CommonUtil.showProgressCustom(appContext, "Processing image...");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
