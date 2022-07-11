package com.PICKING;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.uhf.uhf.R;

public class config_ip extends AppCompatActivity {

    private EditText et_ip, et_filtro_item, et_filtro_caja, et_filtro_pallet;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_ip);
        new Methods().CambiarColorStatusBar(this, R.color.main);

        prefs = this.getSharedPreferences("rfidmx.samsung.demo", Context.MODE_PRIVATE);

        et_ip = findViewById(R.id.et_direccion_ip);
        et_filtro_item = findViewById(R.id.et_filtro_item);
        et_filtro_caja = findViewById(R.id.et_filtro_caja);
        et_filtro_pallet = findViewById(R.id.et_filtro_pallet);

        et_ip.setText(GlobalPreferences.TMP_IP);
        et_filtro_item.setText(GlobalPreferences.FILTRO_ITEM);
        et_filtro_caja.setText(GlobalPreferences.FILTRO_CAJA);
        et_filtro_pallet.setText(GlobalPreferences.FILTRO_PALLET);

        findViewById(R.id.btn_volver).setOnClickListener(v->{
            this.onBackPressed();
        });

        findViewById(R.id.btn_guardar).setOnClickListener(v->{
            this.onBackPressed();
        });

    }

    @Override
    public void onBackPressed() {

        prefs.edit().putString("IP", et_ip.getText().toString()).apply();
        prefs.edit().putString("filtro_item", et_filtro_item.getText().toString()).apply();
        prefs.edit().putString("filtro_caja", et_filtro_caja.getText().toString()).apply();
        prefs.edit().putString("filtro_pallete", et_filtro_pallet.getText().toString()).apply();
        Toast.makeText(this, "Ajustes guardados", Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }
}