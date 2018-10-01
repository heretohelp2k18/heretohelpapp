package com.example.chatme.chatme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.concurrent.Callable;

public class CommonUtil {

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
}
