package com.pits.smbbrowse.utils;

public class Constants {

    public static final String DIRECTORY_ROOT = "/home/om26er/volume1/";

    public static final String DIRECTORY_DELETE_LOG = "directory_static";
    public static final String DIRECTORY_RENAME_LOG = "directory_static";
    public static final String DIRECTORY_MOVED = "directory_moved";

    public static final String LOCATION_DELETE_LOG = String.format(
            "%s%s/", DIRECTORY_ROOT, DIRECTORY_DELETE_LOG
    );
    public static final String LOCATION_RENAME_LOG = String.format(
            "%s%s/", DIRECTORY_ROOT, DIRECTORY_RENAME_LOG
    );
    public static final String LOCATION_MOVED = String.format(
            "%s%s/", DIRECTORY_ROOT, DIRECTORY_MOVED
    );

    public static final String REMOTE_USER = "om26er";
    public static final String REMOTE_PASS = "changed";
    public static final String REMOTE_HOST = "192.168.10.101";
    public static final int REMOTE_PORT = 22;

    public static final String DIALOG_TEXT_RENAME = "Rename";
    public static final String HOST_PREFIX = "smb://";

    public static final String IP_ADDRESS_PATTERN =
                    "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
}
