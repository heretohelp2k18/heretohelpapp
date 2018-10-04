package com.example.chatme.chatme;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.widget.ProgressBar;

import java.util.concurrent.Callable;

public class CommonUtil {
    public static User currentUser = new User();
    public static ProgressDialog pDialog;

    public static void showAlert(Context appContext, String message)
    {
        new AlertDialog.Builder(appContext)
                .setMessage(message)
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

//    CommonUtil.showAlertMessageWithAction(this,"Please Confirm",
//            new Callable<Void>() {
//        public Void call() {
//            Toast.makeText(appContext, "Oopsss.",Toast.LENGTH_SHORT).show();
//            return null;
//        }
//    },
//            null
//            );
}
