package com.pits.smbbrowse.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.WorkerThread;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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

    public static void createFileLog(NtlmPasswordAuthentication auth, String filename,
                                     String location) {

        try {
            String newFile = location + changeFileExtension(filename);
            SmbFile logFile = new SmbFile(newFile, auth);
            logFile.createNewFile();
        } catch (MalformedURLException | SmbException e) {
            e.printStackTrace();
        }
    }

    public static String getDeletedLogLocation() {
        return AppGlobals.getSambaHostAddress() + Constants.DIRECTORY_DELETE_LOG + "/";
    }

    public static String getRenamedLogLocation() {
        return AppGlobals.getSambaHostAddress() + Constants.DIRECTORY_RENAME_LOG + "/";
    }

    public static String changeFileExtension(String filename) {
        String nameWithoutExtension;
        if (filename.contains(".")) {
            // there is an extension
            nameWithoutExtension = filename.substring(0, filename.lastIndexOf('.'));
        } else {
            nameWithoutExtension = filename;
        }
        return nameWithoutExtension + ".txt";
    }

    @WorkerThread
    public static void runRemoteCommand(String command) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(
                Constants.REMOTE_USER,
                Constants.REMOTE_HOST,
                Constants.REMOTE_PORT
        );
        session.setPassword(Constants.REMOTE_PASS);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);
        session.connect();

        ChannelExec channelSsh = (ChannelExec) session.openChannel("exec");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        channelSsh.setOutputStream(byteArrayOutputStream);

        // Execute command
        System.out.println(command);
        String debugCommand = String.format("%s > %s 2>&1", command, "/volume1/stdout.txt");

        channelSsh.setCommand(debugCommand);
        channelSsh.connect();
        channelSsh.disconnect();

        System.out.println(byteArrayOutputStream.toString());
    }
}
