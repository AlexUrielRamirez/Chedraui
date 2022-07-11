package com.Etiflex.Splash;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.PICKING.Main;
import com.SCAF.SQLiteTools.SQLiteHelper;
import com.bumptech.glide.Glide;
import com.uhf.uhf.R;

import static com.Etiflex.Splash.GlobalPreferences.db_manager;

public class Splash extends AppCompatActivity {

    @SuppressLint("NewApi")
    public void CambiarColorStatusBar(Activity activity, int color){
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(activity, color));
    }

    private ImageView img_logo;
    private ConstraintLayout PanelStartUp;
    private TextView SpinnerCedis;
    private String IdCedis, NombreCedis;
    private Button btn_continuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        CambiarColorStatusBar(this, R.color.volaris_main);

        /*img_logo = findViewById(R.id.logo);
        PanelStartUp = findViewById(R.id.Panel_startup);
        SpinnerCedis = findViewById(R.id.spinner_Cedis);
        btn_continuar = findViewById(R.id.btn_continuar);

        SharedPreferences settings = getSharedPreferences("Global", Context.MODE_PRIVATE);

        btn_continuar.setOnClickListener(v->{
            if(IdCedis != null){
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("first_log", "false");
                editor.putString("IdCedis", IdCedis);
                editor.putString("NombreCedis", NombreCedis);
                editor.apply();
                GlobalPreferences.ID_CEDIS = IdCedis;
                GlobalPreferences.NOMBRE_CEDIS = NombreCedis;
                startActivity(new Intent(Splash.this, Main.class));
                this.finish();
            }else{
                Toast.makeText(this, "Por favor, seleccione un centro de distribución", Toast.LENGTH_SHORT).show();
            }
        });

        db_manager = new SQLiteHelper(this);
        SQLiteDatabase db = db_manager.getWritableDatabase();
        //db.execSQL("INSERT INTO tb_users (NombreUsuario, MailUsuario, Password, Type, Status) VALUES ('Uriel Alejandro Ramírez Moreno','a', 'a', 1, 1)");

        new Handler().postDelayed(() -> {
            GlobalPreferences.ID_CEDIS = settings.getString("IdCedis", "1");
            GlobalPreferences.NOMBRE_CEDIS = settings.getString("NombreCedis", "No especificado");
            GlobalPreferences.SERVER_PRINTER_IP = settings.getString("SERVER_PRINTER_IP", "0.0.0.0");
            GlobalPreferences.URL = settings.getString("SERVER_IP", "0.0.0.0");
            startActivity(new Intent(Splash.this, Main.class));
            Splash.this.finish();
        }, 1500);*/
        new Handler().postDelayed(() -> {
            startActivity(new Intent(this, Main.class));
            this.finish();
        }, 3000);
    }
}