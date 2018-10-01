package com.example.chatme.chatme;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AccountActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PIC_CROP = 2;
    private Uri picUri;
    private Button action_save_image;
    private ImageView imgprofile;
    private SharedPreferences prefs;
    private FirebaseUser user;
    private Bitmap bitmapImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("photos");

        user = FirebaseAuth.getInstance().getCurrentUser();

        // Camera Permission
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.CAMERA  },
                    MY_PERMISSIONS_REQUEST_CAMERA );
        }

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    101 );
        }

        imgprofile = (ImageView) findViewById(R.id.imgprofile);
        Uri imguri = user.getPhotoUrl();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userPhoto = prefs.getString("userPhoto", "");
        if(userPhoto != "")
        {
            byte[] decodedString = Base64.decode(userPhoto, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imgprofile.setImageBitmap(decodedByte);
        }

        action_save_image = (Button) findViewById(R.id.action_save_image);
        action_save_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(action_save_image.getText().equals("save changes"))
                {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream .toByteArray();
                    String bitmapImageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

                    Toast.makeText(getApplicationContext(),"Image successfully saved.",Toast.LENGTH_LONG).show();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("userPhoto", bitmapImageBase64);
                    editor.commit();

                    String id = user.getUid();
                    UserPhoto photo = new UserPhoto(bitmapImageBase64);
                    myRef.child(id).setValue(photo);

                    action_save_image.setTextColor(Color.BLACK);
                    action_save_image.setBackgroundColor(Color.parseColor("#cccccc"));
                    action_save_image.setText("change photo");
                }
                else {
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    File photo = new File(Environment.getExternalStorageDirectory(), currentDateFormat() + ".jpg");
                    picUri = Uri.fromFile(photo);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                performCrop();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (requestCode == PIC_CROP && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            bitmapImage = extras.getParcelable("data");
            imgprofile.setImageBitmap(bitmapImage);
            action_save_image.setText("save changes");
            action_save_image.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            action_save_image.setTextColor(Color.WHITE);
        }
    }

    private String currentDateFormat(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
        String  currentTimeStamp = dateFormat.format(new Date());
        return currentTimeStamp;
    }

    public void performCrop()
    {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 512);
            cropIntent.putExtra("outputY", 512);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        catch(Exception e){
            e.printStackTrace();
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
