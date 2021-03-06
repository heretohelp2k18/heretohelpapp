package com.example.chatme.chatme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbRequest;
import android.inputmethodservice.KeyboardView;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Context appContext;
    private Button sign_in_as_guest;
    private Button signup;
    private Button psychoSignUpButton;
    // Firebase
    // private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.appContext = this;

        // Firebase Auth
        // this.mAuth = FirebaseAuth.getInstance();

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        sign_in_as_guest = (Button) findViewById(R.id.sign_in_as_guest);
        sign_in_as_guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GuestLoginTask guestTask = new GuestLoginTask();
                guestTask.execute((Void) null);
            }
        });
                signup = (Button) findViewById(R.id.email_sign_up_button);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(appContext,SignUpActivity.class);
                i.putExtra("action","sign-in");
                startActivity(i);
            }
        });

        psychoSignUpButton = (Button) findViewById(R.id.psycho_sign_up_button);
        psychoSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(appContext,SignUpActivity.class);
                i.putExtra("action","sign-psycho");
                startActivity(i);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if(currentUser != null)
//        {
//            Intent i = new Intent(this, MainActivity.class);
//            i.putExtra("username",currentUser.getEmail());
//            startActivity(i);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        if(UserSessionUtil.isValidSession(appContext))
        {
            if(UserSessionUtil.getSession(appContext, "isguest").equals("1")){
                UserSessionUtil.clearSession(appContext);
            }
            else {
                Intent i = new Intent(appContext, ChatBotActivity.class);
                startActivity(i);
            }
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;



        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a invalid password.
        else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);

//            mAuth.signInWithEmailAndPassword(email, password)
//                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            if (task.isSuccessful()) {
//                                // Sign in success, update UI with the signed-in user's information
////                                Log.d(TAG, "signInWithEmail:success");
//                                FirebaseUser user = mAuth.getCurrentUser();
//                                Intent i = new Intent(appContext, MainActivity.class);
//                                i.putExtra("username",user.getEmail());
//                                startActivity(i);
//                                showProgress(false);
//                            } else {
//                                // If sign in fails, display a message to the user.
////                                Log.w(TAG, "signInWithEmail:failure", task.getException());
//                                showProgress(false);
//                                Toast.makeText(appContext, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//
//                            // ...
//                        }
//                    });
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        String responseMessage = getResources().getString(R.string.connection_failed);
        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
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
            try
            {
                List<NameValuePair> http_params = new LinkedList<NameValuePair>();
                http_params.add(new BasicNameValuePair("username", mEmail));
                http_params.add(new BasicNameValuePair("password", mPassword));

                String paramString = URLEncodedUtils.format(http_params, "utf-8");
                Log.e("paramString",paramString);
                String server_url = getResources().getString(R.string.serverUrl) + "/signin?"+paramString;

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
                            UserSessionUtil.setSession(appContext, key, jsonReader.nextString());
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
            mAuthTask = null;
            CommonUtil.showProgress(appContext, false);

            if (success) {
                String userType = UserSessionUtil.getSession(appContext, "usertype");
                Intent i = new Intent(appContext, ChatBotActivity.class);
                startActivity(i);
            }
            else {
                CommonUtil.showAlert(appContext, responseMessage);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            CommonUtil.showProgress(appContext, false);
        }
    }

    public class GuestLoginTask extends AsyncTask<Void, Void, Boolean> {

        String responseMessage = getResources().getString(R.string.connection_failed);

        GuestLoginTask() {
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
            try
            {
                String server_url = getResources().getString(R.string.serverUrl) + "/GuestLogin?";

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
                            UserSessionUtil.setSession(appContext, key, jsonReader.nextString());
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

            if (success) {
                // Auto Logout
                TimerTask myTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if(UserSessionUtil.isValidSession(appContext)) {
                                        if(UserSessionUtil.getSession(appContext, "isguest").equals("1"))
                                        {
                                            FirebaseDatabase fireDB = FirebaseDatabase.getInstance();
                                            DatabaseReference fireRef = fireDB.getReference("guest");
                                            fireRef.child(UserSessionUtil.getSession(appContext, "userid")).setValue("0");
                                            UserSessionUtil.clearSession(appContext);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e("Error:::" , e.toString());
                                }
                            }
                        });

                    }
                };
                Timer timer = new Timer();
                timer.schedule(myTimerTask, Integer.parseInt(getResources().getString(R.string.guestTimeOut)));

                CommonUtil.showAlertWithCallback(appContext, UserSessionUtil.getSession(appContext, "message_to_guest"), new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        Intent i = new Intent(LoginActivity.this, ChatBotActivity.class);
                        startActivity(i);
                        return null;
                    }
                });
            }
            else {
                CommonUtil.showAlert(appContext, responseMessage);
            }
        }

        @Override
        protected void onCancelled() {
            CommonUtil.showProgress(appContext, false);
        }
    }
}

