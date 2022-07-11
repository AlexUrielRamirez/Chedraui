package com.PICKING;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.PICKING.Create.Instructions;
import com.PICKING.Create.Picking;
import com.PICKING.Create.Picking_items;
import com.uhf.uhf.R;

public class Main extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picking);

        new Methods().CambiarColorStatusBar(this, R.color.main);
        prefs = this.getSharedPreferences("rfidmx.samsung.demo", Context.MODE_PRIVATE);
        findViewById(R.id.btn_agregar_items).setOnClickListener(v -> startActivity(new Intent(this, Picking_items.class)));

        findViewById(R.id.btn_crear_pallete_).setOnClickListener(v -> startActivity(new Intent(this, Picking.class)));

        findViewById(R.id.btn_consultar_orden_).setOnClickListener(v ->startActivity(new Intent(this, com.PICKING.Consult.Main.class)));

        findViewById(R.id.btn_radar_sonoro_).setOnClickListener(v -> {
            Intent i = new Intent(this, com.PICKING.Consult.Main.class);
            i.putExtra("Search", true);
            startActivity(i);
        });

        findViewById(R.id.btn_configuracion_).setOnClickListener(v ->startActivity(new Intent(this, config_ip.class)));

        findViewById(R.id.btn_cerrar).setOnClickListener(v->this.onBackPressed());

    }

    @Override
    protected void onResume() {
        super.onResume();
        GlobalPreferences.TMP_IP = prefs.getString("IP", "192.168.0.0");
        GlobalPreferences.FILTRO_ITEM = prefs.getString("filtro_item", "0");
        GlobalPreferences.FILTRO_CAJA = prefs.getString("filtro_caja", "0");
        GlobalPreferences.FILTRO_PALLET = prefs.getString("filtro_pallete", "0");
    }
}