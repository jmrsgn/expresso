package com.example.expresso.utils;

import com.example.expresso.BuildConfig;

public class Constants {
    // OAUTH KEY
    // web key
    public static final String CLIENT_OAUTH_KEY = BuildConfig.CLIENT_OAUTH_KEY;

    // FILE EXTENSIONS
    public static final String MAIN_SCRIPT_FILE_NAME = "script";
    public static final String MAIN_CERTIFICATE_FILE_NAME = "certificate";
    public static final String TXT_EXTENSION = ".txt";
    public static final String PDF_EXTENSION = ".pdf";

    // USER INFO
    public static String ID = "";
    public static String EMAIL = "";
    public static String GIVEN_NAME = "";
    public static String FAMILY_NAME = "";
    public static String IMAGE_URL = "";
    public static boolean IS_EMAIL_IN_DOMAIN = false;

    // SHARED PREFERENCES
    public static final String SP_USER_SAVED_MODULES_ID = "User saved modules id";
    public static final String SP_MODULES_INFO = "Modules info";
    public static final String SP_TOPICS_INFO = "Topics info";
    public static final String SP_QUIZ_CHOICES_INFO = "Quiz choices info";
    public static final String SP_TOPIC_QUIZ_CHOICES_INFO = "Topic quiz choices info";
    public static final String SP_USER_AVAILABLE_TOPICS_ID = "User available topics id";
    public static final String SP_MODULES_PATH_INDEXES = "Modules path indexes";
    public static final String SP_TOPICS_PATH_INDEXES = "Topics path indexes";

    // EXERCISE TESTING
    public static int TESTING_SCORE = 0;

    // DEVS INFO
    public static final String SYSTEM_EMAIL = BuildConfig.SYSTEM_EMAIL;
    public static final String SYSTEM_PASSWORD = BuildConfig.SYSTEM_PASSWORD;

    // YOUTUBE PLAYER API
    public static final String YOUTUBE_PLAYER_API_KEY = BuildConfig.YOUTUBE_PLAYER_API_KEY;
}
