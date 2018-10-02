package com.example.chatme.chatme;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UserSessionUtil {

    public static SharedPreferences prefs;

    public static void setSession(Context appContext, String key, String value)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key , value);
        editor.commit();
    }

    public static String getSession(Context appContext, String key)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        return  prefs.getString(key, "");
    }

    public static void clearSession(Context appContext)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }
}
