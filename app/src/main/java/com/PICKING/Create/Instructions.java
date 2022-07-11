package com.PICKING.Create;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.Etiflex.Splash.Methods;
import com.uhf.uhf.R;

public class Instructions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        new Methods().CambiarColorStatusBar(this, R.color.main_yellow_dhl);

        findViewById(R.id.btn_volver).setOnClickListener(v->{
            this.onBackPressed();
        });
        findViewById(R.id.btn_crear_orden).setOnClickListener(v -> {startActivity(new Intent(this, Picking.class));this.finish();});

    }
}