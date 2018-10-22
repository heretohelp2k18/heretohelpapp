package com.example.chatme.chatme;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.support.v4.app.NotificationCompat;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.text.Html;
import android.os.Vibrator;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Callable;

import static android.content.Context.ACTIVITY_SERVICE;

public class CommonUtil {
    public static User currentUser = new User();
    public static ProgressDialog pDialog;
    public static int notifCounter = 0;
    public static void showAlert(Context appContext, String message)
    {
        new AlertDialog.Builder(appContext)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    public static void showAlertWithCallback(Context appContext, String message, final Callable<Void> method)
    {
        new AlertDialog.Builder(appContext)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    try {
                        method.call();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    }
                })
                .show();
    }

    public static void showAlertMessageWithAction(Context appContext, String message, final Callable<Void> positiveMethod, final Callable<Void> negativeMethod) {
        new AlertDialog.Builder(appContext)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            positiveMethod.call();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(negativeMethod == null) {
                            dialog.cancel();
                        }
                        else
                        {
                            try {
                                negativeMethod.call();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).show();
    }

    public static void  showProgress(Context appContext, final boolean show) {
        if(show)
        {
            pDialog = new ProgressDialog(appContext);
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

    public static void  showProgressCustom(Context appContext, final String message) {
        pDialog = new ProgressDialog(appContext);
        pDialog.setMessage(message);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    public static void dismissProgressDialog()
    {
        pDialog.dismiss();
    }

    public static String stripHtml(String html) {
        String refinedString = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            refinedString = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            refinedString = Html.fromHtml(html).toString();
        }
        refinedString = refinedString.replaceAll("\\n"," ").trim();
        return refinedString;
    }

    public static void showNotification(Context appContext, String title, String content, Intent notifIntent, int icon)
    {
        notifCounter++;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(appContext);
        mBuilder.setSmallIcon(icon);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(content);

        PendingIntent contentIntent = PendingIntent.getActivity(appContext, notifCounter, notifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notifCounter, mBuilder.build());
    }

    public static int dpToPx(Context appContext, int dp) {
        float density = appContext.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }

    public static void notifSound(Context appContext)
    {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone notifSound = RingtoneManager.getRingtone(appContext, notification);
        notifSound.play();
    }

    public static void vibrateDevice(Context appContext)
    {
        Vibrator v = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            //deprecated in API 26
            v.vibrate(500);
        }
    }
}
