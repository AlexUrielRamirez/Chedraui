package com.Hellman.CAFv2.Administracion;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.Addons.ProgressBarAnimation;
import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.Hellman.CAFv2.Administracion.Activo.admin_activo;
import com.Hellman.CAFv2.Administracion.Impresion.admin_impresion;
import com.Hellman.CAFv2.Administracion.Incidences.admin_incidences;
import com.Hellman.CAFv2.Administracion.Traspasos.admin_traspasos;
import com.Hellman.CAFv2.Administracion.Ubicaciones.admin_ubicaciones;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.uhf.uhf.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE;
import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE_IDLE;
import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE_INCIDENCES;
import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE_ITEM;
import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE_PRINT;
import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE_TRANSFER;
import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE_UBICATIONS;
import static com.Etiflex.Splash.GlobalPreferences.CODE_BAR_READER;
import static com.Etiflex.Splash.GlobalPreferences.PAGE_STATE;
import static com.Hellman.CAFv2.Administracion.Traspasos.admin_traspasos.HolderCamera;
import static com.Hellman.CAFv2.Administracion.Traspasos.admin_traspasos.Panel_crear;
import static com.Hellman.CAFv2.Administracion.Traspasos.admin_traspasos.scanner;

public class Main extends AppCompatActivity {

    Methods methods;

    private ImageView btn_on_back_pressed;
    private ConstraintLayout menu_ubicaciones, menu_incidencias, menu_activo, menu_traspasos, menu_impresion;
    private ConstraintLayout fragment_holder;

    private interface check_admin_pass{
        @FormUrlEncoded
        @POST("/check_admin_acces.php")
        void setData(
                @Field("pass") String Password,
                Callback<Response> callback
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        methods = new Methods();
        methods.CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_main_administracion);
        initViews();
        setUpViews();

        checkUserLevel();

    }

    private void checkUserLevel() {
        if(GlobalPreferences.NIVEL_USUARIO != 1){
            BottomSheetDialog bsd = new BottomSheetDialog(this);
            bsd.setContentView(R.layout.bsd_admin_password);
            bsd.setCancelable(false);
            EditText et_contrasena = bsd.findViewById(R.id.et_contrasena);
            bsd.findViewById(R.id.btn_continuar).setOnClickListener(v->{
                if(et_contrasena.getText().length() != 0){
                    new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmannCAF/webservices/Administracion").build().create(check_admin_pass.class).setData(et_contrasena.getText().toString(), new Callback<Response>() {
                        @Override
                        public void success(Response response, Response response2) {
                            try {
                                if(new BufferedReader(new InputStreamReader(response.getBody().in())).readLine().equals("ok")){
                                    Toast.makeText(Main.this, "¡Gracias!", Toast.LENGTH_SHORT).show();
                                    bsd.dismiss();
                                }else{
                                    et_contrasena.setText("");
                                    Toast.makeText(Main.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                                }
                            }catch (IOException e){
                                Toast.makeText(Main.this, "Algo salió mal, intente nuevamente por favor", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(Main.this, "Error, revise su conexión", Toast.LENGTH_SHORT).show();
                            Log.e("Administracion", error.getMessage());
                        }
                    });
                }else{
                    Toast.makeText(this, "Por favor, ingrese una contraseña válida", Toast.LENGTH_SHORT).show();
                }
            });
            bsd.findViewById(R.id.btn_volver).setOnClickListener(v->{
                this.finish();
            });
            bsd.show();
        }
    }

    private void initViews() {
        btn_on_back_pressed = findViewById(R.id.btn_onbackpressed);
        menu_ubicaciones = findViewById(R.id.menu_ubicaciones);
        menu_incidencias = findViewById(R.id.menu_incidencias);
        menu_activo = findViewById(R.id.menu_activo);
        menu_traspasos = findViewById(R.id.menu_traspaso);
        menu_impresion = findViewById(R.id.menu_impresion);
        fragment_holder = findViewById(R.id.fragment_holder);
    }

    private void setUpViews() {
        btn_on_back_pressed.setOnClickListener(v-> mOnBackPressed());
        menu_ubicaciones.setOnClickListener(v->{
            ADMIN_PAGE_STATE = ADMIN_PAGE_STATE_UBICATIONS;
            getSupportFragmentManager().beginTransaction().add(fragment_holder.getId(), admin_ubicaciones.newInstance(), "Ubicaciones").commit();
            fragment_holder.setVisibility(View.VISIBLE);
        });
        menu_incidencias.setOnClickListener(v->{
            ADMIN_PAGE_STATE = ADMIN_PAGE_STATE_INCIDENCES;
            getSupportFragmentManager().beginTransaction().add(fragment_holder.getId(), admin_incidences.newInstance(), "Incidencias").commit();
            fragment_holder.setVisibility(View.VISIBLE);
        });
        menu_activo.setOnClickListener(v->{
            ADMIN_PAGE_STATE = ADMIN_PAGE_STATE_ITEM;
            getSupportFragmentManager().beginTransaction().add(fragment_holder.getId(), admin_activo.newInstance(), "Activo").commit();
            fragment_holder.setVisibility(View.VISIBLE);
        });
        menu_traspasos.setOnClickListener(v->{
            ADMIN_PAGE_STATE = ADMIN_PAGE_STATE_TRANSFER;
            getSupportFragmentManager().beginTransaction().add(fragment_holder.getId(), admin_traspasos.newInstance(), "Traspasos").commit();
            fragment_holder.setVisibility(View.VISIBLE);
        });
        menu_impresion.setOnClickListener(v->{
            ADMIN_PAGE_STATE = ADMIN_PAGE_STATE_PRINT;
            getSupportFragmentManager().beginTransaction().add(fragment_holder.getId(), admin_impresion.newInstance(), "Impresion").commit();
            fragment_holder.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onBackPressed() {
        mOnBackPressed();
    }

    public void mOnBackPressed(){
        switch (ADMIN_PAGE_STATE){
            case ADMIN_PAGE_STATE_IDLE:
                finish();
                break;
            case ADMIN_PAGE_STATE_UBICATIONS:
                if(admin_ubicaciones.Holder_rv_oficinas.getVisibility() == View.VISIBLE){
                    admin_ubicaciones.Holder_rv_oficinas.setVisibility(View.GONE);
                    admin_ubicaciones.txt_indicador_area.setText("");
                    admin_ubicaciones.txt_indicador_area.setVisibility(View.GONE);
                }else{
                    ADMIN_PAGE_STATE = ADMIN_PAGE_STATE_IDLE;
                    fragment_holder.setVisibility(View.GONE);
                }
                break;
            case ADMIN_PAGE_STATE_INCIDENCES:
                ADMIN_PAGE_STATE = ADMIN_PAGE_STATE_IDLE;
                fragment_holder.setVisibility(View.GONE);
                break;
            case ADMIN_PAGE_STATE_TRANSFER:
                if(HolderCamera.getVisibility() == View.VISIBLE){
                    scanner.stopCamera();
                    HolderCamera.removeAllViews();
                    HolderCamera.setVisibility(View.GONE);
                }else if(Panel_crear.getVisibility() == View.VISIBLE){
                    Panel_crear.setVisibility(View.GONE);
                }else{
                    ADMIN_PAGE_STATE = ADMIN_PAGE_STATE_IDLE;
                    fragment_holder.setVisibility(View.GONE);
                }
                break;
            case ADMIN_PAGE_STATE_ITEM:
                if(admin_activo.Panel_detalle.getVisibility() == View.VISIBLE){
                    ADMIN_PAGE_STATE = ADMIN_PAGE_STATE_ITEM;
                    admin_activo.Panel_detalle.setVisibility(View.GONE);
                }else{
                    ADMIN_PAGE_STATE = ADMIN_PAGE_STATE_IDLE;
                    fragment_holder.setVisibility(View.GONE);
                }
                break;
            case ADMIN_PAGE_STATE_PRINT:
                if(admin_impresion.Panel_etiquetas.getVisibility() == View.VISIBLE){
                    ADMIN_PAGE_STATE = ADMIN_PAGE_STATE_PRINT;
                    admin_impresion.Panel_etiquetas.setVisibility(View.GONE);
                }else{
                    ADMIN_PAGE_STATE = ADMIN_PAGE_STATE_IDLE;
                    fragment_holder.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case CODE_BAR_READER:
                    if(ADMIN_PAGE_STATE == ADMIN_PAGE_STATE_ITEM){
                        admin_activo.et_epc.setText(data.getDataString());
                    }
                    break;
            }
        }
    }
}