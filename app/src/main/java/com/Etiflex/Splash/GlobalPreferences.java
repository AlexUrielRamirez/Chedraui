package com.Etiflex.Splash;

import androidx.appcompat.app.AppCompatActivity;

import com.Etiflex.Splash.ROC.ModelInventory;
import com.Hellman.CAFv2.Historial.ControladorHistorial;

import java.util.ArrayList;

public class GlobalPreferences {

    public static boolean DEVELOP_MODE = false;

    public static String DEVICE;
    public static String URL = GlobalPreferences.URL+"/HellmanCAF/webservices/";
    public static String SERVER_PRINTER_IP;

    public static String ID_USUARIO;
    public static String NOMBRE_USUARIO;
    public static String CODIGO_USUARIO;
    public static String ID_CEDIS;
    public static String NOMBRE_CEDIS;
    public static int NIVEL_USUARIO;

    public static ControladorHistorial mHistorial;
    public static final String HISTORIAL_TIPO_REIMPRESION = "1";
    public static final String HISTORIAL_TIPO_INVENTARIOS = "2";
    public static final String HISTORIAL_TIPO_ALTA_INCIDENCIA = "3";
    public static final String HISTORIAL_TIPO_BAJA_INCIDENCIA = "4";
    public static final String HISTORIAL_TIPO_TRASPASOS = "5";
    public static final String HISTORIAL_TIPO_ALTA_ACTIVO = "6";
    public static final String HISTORIAL_TIPO_MODIFICACION_ACTIVO = "7";

    public static final int PICK_IMAGE_FROM_CAMERA = 1;
    public static final int PICK_IMAGE_FROM_GALLERY = 2;

    public static final int CODE_BAR_READER = 350;

    public static ArrayList<String> tag_list;
    public static ArrayList<ModelInventory> main_list;

    public static AppCompatActivity activity;

    public static final int INTENT_RESULT_ADD_FILE = 1;
    public static final int INTENT_RESULT_ADD_FILE_EXCEL = 2;

    public static String CURRENT_TAG = "";

    public static int PAGE_STATE = 0;
    public static final int PAGE_STATE_IDLE = 0;
    public static final int PAGE_STATE_SETTING_UBICATION = 1;
    public static final int PAGE_STATE_INVENTORY = 2;
    public static final int PAGE_STATE_SEARCHING = 3;
    public static final int PAGE_STATE_DETAILS = 4;
    public static final int PAGE_STATE_PROCESING = 5;
    public static final int PAGE_STATE_INCIDENCE_FOUND = 6;

    public static int ADMIN_PAGE_STATE = 0;
    public static final int ADMIN_PAGE_STATE_IDLE = 0;
    public static final int ADMIN_PAGE_STATE_UBICATIONS = 1;
    public static final int ADMIN_PAGE_STATE_INCIDENCES = 2;
    public static final int ADMIN_PAGE_STATE_TRANSFER = 3;
    public static final int ADMIN_PAGE_STATE_ITEM = 4;
    public static final int ADMIN_PAGE_STATE_PRINT = 5;

    public static int CAF_STATE = 0;
    public static final int CAF_STATE_IDLE = 1;
    public static final int CAF_STATE_UP = 2;
    public static final int CAF_STATE_DOWN = 3;

    public static final int TAKE_PICTURE = 22;

    //CAFv2

    public static final String FRAGMENT_ALTA = "CAF_FragmentAlta";
    public static final String FRAGMENT_AJUSTES = "CAF_Ajustes";
    public static final String FRAGMENT_INVENTARIO = "CAF_Inventario";

}
