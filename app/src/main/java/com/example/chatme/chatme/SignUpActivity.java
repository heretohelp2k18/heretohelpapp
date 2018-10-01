package com.example.chatme.chatme;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
//import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText s_email_address, s_password, s_confirm_password;
    private Button s_signup;
    private ProgressDialog pDialog;
    private Context appContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        this.appContext = this;

        this.mAuth = FirebaseAuth.getInstance();

        // Spinner Setup
        Spinner spinner = (Spinner) findViewById(R.id.s_gender);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        s_email_address = (EditText) findViewById(R.id.s_email_address);
        s_password = (EditText) findViewById(R.id.s_password);
        s_confirm_password = (EditText) findViewById(R.id.s_confirm_password);
        s_signup = (Button) findViewById(R.id.s_signup);

        s_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s_email_address_val = s_email_address.getText().toString();
                String s_password_val = s_password.getText().toString();
                String s_confirm_password_val = s_confirm_password.getText().toString();

                Boolean error = false;
                View focusView = null;
                if(TextUtils.isEmpty(s_email_address_val) && (s_email_address_val.length() < 6))
                {
                    s_email_address.setError(getString(R.string.error_field_required));
                    focusView = s_email_address;
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

                if(error) {
                    focusView.requestFocus();
                }
                else
                {
                    showProgress(true);
                    InsertUser(s_email_address_val,s_password_val);
                }
            }
        });
    }

    private void InsertUser(String s_email_address_val, String s_password_val)
    {
        mAuth.createUserWithEmailAndPassword(s_email_address_val, s_password_val)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent i = new Intent(appContext, MainActivity.class);
                            i.putExtra("username",user.getEmail());
                            startActivity(i);
                            showProgress(false);
                        } else {
                            showProgress(false);
                            Toast.makeText(appContext, "Failed to connect to server.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showProgress(final boolean show) {
        if(show)
        {
            pDialog = new ProgressDialog(appContext);  //<<-- Couldnt Recognise
            pDialog.setMessage("Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        else
        {
            pDialog.dismiss();
        }
    }
}
