package com.pits.smbbrowse.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class Helpers {

    public static NtlmPasswordAuthentication getAuthenticationCredentials() {
        String username = AppGlobals.getUsername();
        String password = AppGlobals.getPassword();
        return new NtlmPasswordAuthentication("", username, password);
    }

    public static List<SmbFile> filterFilesLargerThan(SmbFile[] contentArray, int sizeConstraint)
            throws SmbException {

        List<SmbFile> filesList = new ArrayList<>();
        for (SmbFile file: contentArray) {
            if (file.isFile()) {
                if (isLargerThan(file, sizeConstraint)) {
                    filesList.add(file);
                }
            } else {
                filesList.add(file);
            }
        }
        return filesList;
    }

    public static boolean isLargerThan(SmbFile fileToCheck, int sizeConstraint) {
        double sizeInMbs = 0;
        try {
            sizeInMbs = (double) fileToCheck.length() / 100000;
        } catch (SmbException e) {
            e.printStackTrace();
        }
        return sizeInMbs > sizeConstraint;
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isValidIp(String ip) {
        Pattern pattern = Pattern.compile(Constants.IP_ADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    public static String getIpFromSambaUrl(String url) {
        String stripped = url.substring(Constants.HOST_PREFIX.length());
        int forwardSlashCount = getCharacterRepetitionCount("/");
        if (forwardSlashCount == 0) {
            return stripped;
        } else {
            return stripped.split("/")[0];
        }
    }

    public static int getCharacterRepetitionCount(String text) {
        int count = 0;
        for (char c: text.toCharArray()) {
            if (c == '/') {
                count++;
            }
        }
        return count;
    }
}
