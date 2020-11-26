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
import android.widget.ImageView;

import com.Addons.ProgressBarAnimation;
import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.Hellman.CAFv2.Administracion.Activo.admin_activo;
import com.Hellman.CAFv2.Administracion.Incidences.admin_incidences;
import com.Hellman.CAFv2.Administracion.Traspasos.admin_traspasos;
import com.Hellman.CAFv2.Administracion.Ubicaciones.admin_ubicaciones;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.uhf.uhf.R;

import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE;
import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE_IDLE;
import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE_INCIDENCES;
import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE_ITEM;
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
    private ConstraintLayout menu_ubicaciones, menu_incidencias, menu_activo, menu_traspasos;
    private ConstraintLayout fragment_holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        methods = new Methods();
        methods.CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_main_administracion);
        initViews();
        setUpViews();
    }

    private void initViews() {
        btn_on_back_pressed = findViewById(R.id.btn_onbackpressed);
        menu_ubicaciones = findViewById(R.id.menu_ubicaciones);
        menu_incidencias = findViewById(R.id.menu_incidencias);
        menu_activo = findViewById(R.id.menu_activo);
        menu_traspasos = findViewById(R.id.menu_traspaso);
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