package com.pits.smbbrowse.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.pits.smbbrowse.R;

import java.net.MalformedURLException;
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

    public static void createFileLog(NtlmPasswordAuthentication auth, String filename,
                                     String location) {

        try {
            String newFile = location + changeFileExtension(filename);
            SmbFile logFile = new SmbFile(newFile, auth);
            System.out.println(newFile);
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

    private static String changeFileExtension(String filename) {
        String nameWithoutExtension;
        if (filename.contains(".")) {
            // there is an extension
            nameWithoutExtension = filename.substring(0, filename.lastIndexOf('.'));
        } else {
            nameWithoutExtension = filename;
        }
        return nameWithoutExtension + ".txt";
    }

    public void fadeOutView(View view) {
        Animation fadeOut = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);
        if (fadeOut != null) {
            fadeOut.setAnimationListener(new ViewAnimationListener(view) {
                @Override
                protected void onAnimationStart(View view, Animation animation) {

                }

                @Override
                protected void onAnimationEnd(View view, Animation animation) {
                    view.setVisibility(View.GONE);
                }
            });
            view.startAnimation(fadeOut);
        }
    }

    public void fadeInView(View view) {
        Animation fadeIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);
        if (fadeIn != null) {
            fadeIn.setAnimationListener(new ViewAnimationListener(view) {
                @Override
                protected void onAnimationStart(View view, Animation animation) {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onAnimationEnd(View view, Animation animation) {

                }
            });
            view.startAnimation(fadeIn);
        }
    }

    public void slideInView(View view) {
        Animation slideIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_in);
        if (slideIn != null) {
            slideIn.setAnimationListener(new ViewAnimationListener(view) {
                @Override
                protected void onAnimationStart(View view, Animation animation) {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onAnimationEnd(View view, Animation animation) {

                }
            });
            view.startAnimation(slideIn);
        }
    }

    public void slideOutView(View view) {
        Animation slideOut = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_out);
        if (slideOut != null) {
            slideOut.setAnimationListener(new ViewAnimationListener(view) {
                @Override
                protected void onAnimationStart(View view, Animation animation) {

                }

                @Override
                protected void onAnimationEnd(View view, Animation animation) {
                    view.setVisibility(View.GONE);
                }
            });
            view.startAnimation(slideOut);
        }
    }

    private abstract class ViewAnimationListener implements Animation.AnimationListener {

        private final View view;

        protected ViewAnimationListener(View view) {
            this.view = view;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            onAnimationStart(this.view, animation);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            onAnimationEnd(this.view, animation);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        protected abstract void onAnimationStart(View view, Animation animation);
        protected abstract void onAnimationEnd(View view, Animation animation);
    }
}
