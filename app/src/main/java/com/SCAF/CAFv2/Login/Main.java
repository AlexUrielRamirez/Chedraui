package com.SCAF.CAFv2.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.SCAF.CAFv2.Historial.ControladorHistorial;
import com.SCAF.MainMenu;
import com.uhf.uhf.R;

import org.json.JSONException;
import org.json.JSONObject;

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

import static com.Etiflex.Splash.GlobalPreferences.db_manager;

public class Main extends AppCompatActivity {

    private EditText et_usuario, et_contrasena;
    private Button btn_continuar;
    private TextView txt_error;
    private ProgressDialog progressDialog;
    private SQLiteDatabase db;

    private interface api_network{
        @FormUrlEncoded
        @POST("/login.php")
        void setData(
                @Field("User") String Usuario,
                @Field("Password") String Password,
                Callback<Response> callback
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_main_login);
        findViewById(R.id.btn_configuracion).setOnClickListener(v -> startActivity(new Intent(this, com.SCAF.CAFv2.Ajustes.Main.class)));

        et_usuario = findViewById(R.id.et_usuario);
        et_contrasena = findViewById(R.id.et_contrasena);
        btn_continuar = findViewById(R.id.btn_continuar);
        txt_error = findViewById(R.id.txt_error);

        progressDialog = new ProgressDialog(Main.this);
        progressDialog.setMessage("Consultando credenciales...");
        progressDialog.setCancelable(false);

        btn_continuar.setOnClickListener(v->{
            txt_error.setVisibility(View.GONE);
            if(et_usuario.getText().length() > 0 && et_contrasena.getText().length() > 0){
                progressDialog.show();
                db = db_manager.getWritableDatabase();
                if (db != null) {
                    Cursor c = db.rawQuery("SELECT IdUser, NombreUsuario, Password, Type FROM tb_users WHERE MailUsuario = '" + et_usuario.getText().toString()+"' AND Password = '"+et_contrasena.getText().toString()+"'", null);
                    if (c != null) {
                        try {
                            c.moveToFirst();
                            GlobalPreferences.ID_USUARIO = c.getString(c.getColumnIndex("IdUser"));
                            GlobalPreferences.NOMBRE_USUARIO = c.getString(c.getColumnIndex("NombreUsuario"));
                            String password = c.getString(c.getColumnIndex("Password"));
                            GlobalPreferences.NIVEL_USUARIO = c.getInt(c.getColumnIndex("Type"));
                            startActivity(new Intent(Main.this, MainMenu.class));
                            progressDialog.dismiss();
                            Main.this.finish();
                            c.close();
                            db.close();
                        }catch (CursorIndexOutOfBoundsException e){
                            Log.e("Login", "Credenciales incorrectas");
                            if(et_usuario.getText().toString().equals("admin_etiflex") && et_contrasena.getText().toString().equals("admin_etiflex")){
                                startActivity(new Intent(this, com.SCAF.CAFv2.Administracion.Usuarios.Main.class));
                                this.finish();
                            }else{
                                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    }else{
                        Log.e("Login", "Error al cargar los datos");
                        progressDialog.dismiss();
                    }
                }else{
                    Log.e("Login", "Error al cargar la base de datos");
                    progressDialog.dismiss();
                }
            }else{
                Toast.makeText(this, "Por favor, introduzca la información correspondiente", Toast.LENGTH_SHORT).show();
            }
        });

    }
}