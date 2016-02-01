package com.pits.smbbrowse.utils;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppGlobals extends Application {

    private static SharedPreferences sPreferences;
    private static final String FIRST_RUN_KEY = "first_run";
    private static final String HOST_NAME_KEY = "host_address";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";

    @Override
    public void onCreate() {
        super.onCreate();
        sPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public static boolean isRunningForTheFirstTime() {
        return sPreferences.getBoolean(FIRST_RUN_KEY, true);
    }

    public static void setIsRunningForTheFirstTime(boolean firstTime) {
        sPreferences.edit().putBoolean(FIRST_RUN_KEY, firstTime).apply();
    }

    public static String getSambaHostAddress() {
        return sPreferences.getString(HOST_NAME_KEY, null);
    }

    public static void setSambaHostAddress(String address) {
        sPreferences.edit().putString(HOST_NAME_KEY, address).apply();
    }

    public static String getUsername() {
        return sPreferences.getString(USERNAME_KEY, null);
    }

    public static void setUsername(String username) {
        sPreferences.edit().putString(USERNAME_KEY, username).apply();
    }

    public static String getPassword() {
        return sPreferences.getString(PASSWORD_KEY, null);
    }

    public static void setPassword(String password) {
        sPreferences.edit().putString(PASSWORD_KEY, password).apply();
    }

}
