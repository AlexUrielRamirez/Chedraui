package com.Etiflex.Splash;

import androidx.appcompat.app.AppCompatActivity;

import com.Etiflex.Splash.ROC.ModelInventory;

import java.util.ArrayList;

public class GlobalPreferences {
    public static String DEVICE;
    public static String URL = "https://rfidmx.com/HellmanCAF/webservices/";

    public static ArrayList<String> tag_list;
    public static ArrayList<ModelInventory> main_list;

    public static AppCompatActivity activity;

    public static final int INTENT_RESULT_ADD_FILE = 1;
    public static final int INTENT_RESULT_ADD_FILE_EXCEL = 2;

    public static String CURRENT_TAG = "";

    public static int PAGE_STATE = 0;
    public static final int PAGE_STATE_IDLE = 0;
    public static final int PAGE_STATE_INVENTORY = 1;
    public static final int PAGE_STATE_SEARCHING = 2;
    public static final int PAGE_STATE_DETAILS = 3;
    public static final int PAGE_STATE_PROCESING = 4;

    public static int CAF_STATE = 0;
    public static final int CAF_STATE_IDLE = 1;
    public static final int CAF_STATE_UP = 2;
    public static final int CAF_STATE_DOWN = 3;

    public static final int TAKE_PICTURE = 22;

    //CAFv2

    public static final String FRAGMENT_ALTA = "CAF_FragmentAlta";
    public static final String FRAGMENT_AJUSTES = "CAF_Ajustes";

}
