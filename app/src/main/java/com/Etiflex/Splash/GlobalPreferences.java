package com.Etiflex.Splash;

import androidx.appcompat.app.AppCompatActivity;

import com.Etiflex.Splash.ROC.ModelInventory;
import com.SCAF.CAFv2.Historial.ControladorHistorial;
import com.SCAF.CAFv2.Inventario.Main;
import com.SCAF.SQLiteTools.SQLiteHelper;

import java.util.ArrayList;

public class GlobalPreferences {

    public static boolean DEVELOP_MODE = false;
    public static Main.ModelInventario current_inventory_item;
    public static com.SCAF.CAFv2.Administracion.Incidences.Main.model_incidencia current_incidencia_model;
    public static com.SCAF.CAFv2.Administracion.Usuarios.Main.model_usuarios current_user_item;

    public static String DEVICE;
    public static String URL = GlobalPreferences.URL+"/HellmannCAF/webservices/";

    public static String TMP_IP = "192.168.0.1";
    public static String FILTRO_ITEM = "0";
    public static String FILTRO_CAJA = "0";
    public static String FILTRO_PALLET = "0";

    public static String SERVER_PRINTER_IP;

    public static String ID_USUARIO;
    public static String NOMBRE_USUARIO = "Indefinido";
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

    //DATABASE VARIABLES

    public static SQLiteHelper db_manager;

    private class model_incidencia{
        private String IdIncidencia;
        private String IdActivo;
        private String NombreActivo;
        private String NombreAlta;
        private String Motivo;
        private String Comentario;
        private String FechaAlta;
        private String NombreBaja;
        private String FechaBaja;
        private String EPC;
        private String ActivoFijo;
        private String DenominacionDelActivoFijo;
        private int StatusIncidencia;

        public String getIdIncidencia() {
            return IdIncidencia;
        }

        public void setIdIncidencia(String idIncidencia) {
            IdIncidencia = idIncidencia;
        }

        public String getIdActivo() {
            return IdActivo;
        }

        public void setIdActivo(String idActivo) {
            IdActivo = idActivo;
        }

        public String getNombreActivo() {
            return NombreActivo;
        }

        public void setNombreActivo(String nombreActivo) {
            NombreActivo = nombreActivo;
        }

        public String getNombreAlta() {
            return NombreAlta;
        }

        public void setNombreAlta(String nombreAlta) {
            NombreAlta = nombreAlta;
        }

        public String getMotivo() {
            return Motivo;
        }

        public void setMotivo(String motivo) {
            Motivo = motivo;
        }

        public String getComentario() {
            return Comentario;
        }

        public void setComentario(String comentario) {
            Comentario = comentario;
        }

        public String getFechaAlta() {
            return FechaAlta;
        }

        public String getNombreBaja() {
            return NombreBaja;
        }

        public void setNombreBaja(String nombreBaja) {
            NombreBaja = nombreBaja;
        }

        public String getFechaBaja() {
            return FechaBaja;
        }

        public void setFechaBaja(String fechaBaja) {
            FechaBaja = fechaBaja;
        }

        public void setFechaAlta(String fechaAlta) {
            FechaAlta = fechaAlta;
        }

        public String getEPC() {
            return EPC;
        }

        public void setEPC(String EPC) {
            this.EPC = EPC;
        }

        public String getActivoFijo() {
            return ActivoFijo;
        }

        public void setActivoFijo(String activoFijo) {
            ActivoFijo = activoFijo;
        }

        public String getDenominacionDelActivoFijo() {
            return DenominacionDelActivoFijo;
        }

        public void setDenominacionDelActivoFijo(String denominacionDelActivoFijo) {
            DenominacionDelActivoFijo = denominacionDelActivoFijo;
        }

        public int getStatusIncidencia() {
            return StatusIncidencia;
        }

        public void setStatusIncidencia(int statusIncidencia) {
            StatusIncidencia = statusIncidencia;
        }
    }

    public static ArrayList<String> tag_list_global = new ArrayList<>();
    public static String DESDE = "";

}
