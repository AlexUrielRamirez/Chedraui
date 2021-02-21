package com.Hellman.CAFv2.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.Etiflex.Splash.Splash;
import com.Hellman.CAFv2.Historial.ControladorHistorial;
import com.Hellman.Hellman;
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

public class Main extends AppCompatActivity {

    private EditText et_usuario, et_contrasena;
    private Button btn_continuar;
    private TextView txt_error;
    private ProgressDialog progressDialog;

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

        findViewById(R.id.btn_configuracion).setOnClickListener(v -> startActivity(new Intent(this, com.Hellman.CAFv2.Ajustes.Main.class)));

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
                new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmanCAF/webservices/Login").build().create(api_network.class).setData(et_usuario.getText().toString(), et_contrasena.getText().toString(), new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        try{
                            String res = new BufferedReader(new InputStreamReader(response.getBody().in())).readLine();
                            if (!res.equals("INVALID_CREDENTIALS")){
                                try {
                                    JSONObject json = new JSONObject(res);
                                    GlobalPreferences.ID_USUARIO = json.getString("Id");
                                    GlobalPreferences.NOMBRE_USUARIO = json.getString("Nombre");
                                    GlobalPreferences.CODIGO_USUARIO = json.getString("Codigo");
                                    GlobalPreferences.ID_CEDIS = json.getString("IdCedis");
                                    switch (GlobalPreferences.ID_CEDIS){
                                        case "1":
                                            GlobalPreferences.NOMBRE_CEDIS = "TEPO";
                                            break;
                                        case "2":
                                            GlobalPreferences.NOMBRE_CEDIS = "CPA";
                                            break;
                                        case "3":
                                            GlobalPreferences.NOMBRE_CEDIS = "MTY";
                                            break;
                                    }
                                    GlobalPreferences.NIVEL_USUARIO = Integer.parseInt(json.getString("Status"));
                                    GlobalPreferences.mHistorial = new ControladorHistorial();

                                    startActivity(new Intent(Main.this, Hellman.class));
                                    progressDialog.dismiss();
                                    Main.this.finish();
                                }catch (JSONException e){
                                    progressDialog.dismiss();
                                    Toast.makeText(Main.this, "Algo salió mal, intente nuevamente", Toast.LENGTH_SHORT).show();
                                    Log.e("Login", e.getMessage());
                                }
                            }else{
                                txt_error.setVisibility(View.VISIBLE);
                                progressDialog.dismiss();
                            }
                        }catch (IOException e){
                            Toast.makeText(Main.this, "Algo salió mal, intente nuevamente", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(Main.this, "Error, revise su conexión", Toast.LENGTH_SHORT).show();
                        Log.e("Login","Error no connection->"+error.getMessage());
                        progressDialog.dismiss();
                    }
                });
            }else{
                Toast.makeText(this, "Por favor, introduzca la información correspondiente", Toast.LENGTH_SHORT).show();
            }
        });

    }
}