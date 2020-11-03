package com.Hellman;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.Etiflex.Splash.ConnectorManager;
import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.Hellman.CAFv2.CAF_Ajustes;
import com.Hellman.CAFv2.CAF_AltaActivo;
import com.Hellman.CAFv2.CAF_Inventario;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.uhf.uhf.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

import static com.Etiflex.Splash.GlobalPreferences.FRAGMENT_AJUSTES;
import static com.Etiflex.Splash.GlobalPreferences.FRAGMENT_ALTA;
import static com.Etiflex.Splash.GlobalPreferences.FRAGMENT_INVENTARIO;

public class Hellman extends AppCompatActivity {

    private DrawerLayout MotionLayout;
    RXObserver rx = null;
    private ArrayList<String> tag_list;
    private TextView btn_addFile;

    private interface api_network_send_data{
        @FormUrlEncoded
        @POST("/upload_data.php")
        void setData(
                @Field("Type") String type,
                @Field("json") String json,
                Callback<Response> callback
        );
    }

    private interface ANUploadXLSX{
        @Multipart
        @POST("/upload_data.php")
        void setData(
                @Part("Type") String Type,
                @Part("file") TypedFile file,
                Callback<Response> callback
        );
    }

    interface api_network_restore{
        @FormUrlEncoded
        @POST("/reset.php")
        void setData(
                @Field("dummy") String dummy,
                Callback<Response> callback
        );
    }

    //CAFv2
    private RelativeLayout MainFragmentHolder;
    private Button btn_inventario, btn_alta, btn_ajustes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.setProperty("javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_hellman);
        initViews();
        tag_list = new ArrayList<>();
        rx = new RXObserver(){
            @Override
            protected void onInventoryTag(RXInventoryTag tag) {
                super.onInventoryTag(tag);
                String EPC = tag.strEPC.replaceAll(" ", "");
                if(!tag_list.contains(EPC)){
                    tag_list.add(EPC);
                    Log.e("EPCS", EPC);
                }
                try {
                    if(getSupportFragmentManager().findFragmentByTag(FRAGMENT_INVENTARIO).isVisible()){
                        switch (GlobalPreferences.PAGE_STATE){
                            case GlobalPreferences.PAGE_STATE_INVENTORY:
                                if(CAF_Inventario.tag_list.contains(EPC)){
                                    new Thread(() -> {
                                        for(int position = 0; position < CAF_Inventario.tag_list.size(); position++){
                                            final int final_position = position;
                                            if(CAF_Inventario.main_list.get(position).getEPC().equals(EPC) && CAF_Inventario.main_list.get(position).getStatus() == 0){
                                                CAF_Inventario.main_list.get(position).setStatus(1);
                                                Hellman.this.runOnUiThread(() -> {
                                                    CAF_Inventario.adapter.notifyItemChanged(final_position, CAF_Inventario.main_list.get(final_position));
                                                    CAF_Inventario.conter = CAF_Inventario.conter + 1;
                                                    CAF_Inventario.mProgress.setProgress(CAF_Inventario.conter);
                                                    CAF_Inventario.txt_contador.setText(CAF_Inventario.conter+"/"+ CAF_Inventario.main_list.size());
                                                });
                                                break;
                                            }
                                        }
                                    }).run();
                                }
                                break;
                            case GlobalPreferences.PAGE_STATE_SEARCHING:
                                Log.e("Hellman", "SEARCHING");
                                new Thread(() -> {
                                    if(EPC.equals(GlobalPreferences.CURRENT_TAG)){
                                        CAF_Inventario.pb_potencia.setProgress(Math.round(Float.parseFloat(tag.strRSSI)));
                                        new Methods().PlayBeep_Short();
                                    }
                                }).run();
                                break;
                        }
                    }
                }catch (NullPointerException e){
                    Log.e("Hellmann", "Lecturas"+e.getMessage());
                }
            }
        };

        btn_inventario.setOnClickListener(v->{
            getSupportFragmentManager().beginTransaction().replace(MainFragmentHolder.getId(), new CAF_Inventario(), FRAGMENT_INVENTARIO).commit();
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.bottom_up_in);
            MainFragmentHolder.setAnimation(animation);
            animation.start();
            MainFragmentHolder.setVisibility(View.VISIBLE);
        });

        btn_alta.setOnClickListener(v->{
            getSupportFragmentManager().beginTransaction().replace(MainFragmentHolder.getId(), CAF_AltaActivo.newInstance("", ""), FRAGMENT_ALTA).commit();
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.bottom_up_in);
            MainFragmentHolder.setAnimation(animation);
            animation.start();
            MainFragmentHolder.setVisibility(View.VISIBLE);
        });

        btn_ajustes.setOnClickListener(v->{
            getSupportFragmentManager().beginTransaction().replace(MainFragmentHolder.getId(), CAF_Ajustes.newInstance("", ""), FRAGMENT_AJUSTES).commit();
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.bottom_up_in);
            MainFragmentHolder.setAnimation(animation);
            animation.start();
            MainFragmentHolder.setVisibility(View.VISIBLE);
        });

    }

    private void initViews() {

        findViewById(R.id.textView2).setOnClickListener(v->{
            Fragment Lecturas = getSupportFragmentManager().findFragmentByTag("Lecturas");
            if(Lecturas != null){
                CAF_Inventario.conter = 0;
                getSupportFragmentManager().beginTransaction().remove(Lecturas).commit();
                MotionLayout.closeDrawer(GravityCompat.START);
            }
        });

        findViewById(R.id.textView4).setOnClickListener(v->{
            getSupportFragmentManager().beginTransaction().add(R.id.content, new CAF_Inventario(), "Lecturas").commit();
            MotionLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.textView5).setOnClickListener(v->{
            MotionLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, ControlActivoFijo.class));
        });

        findViewById(R.id.btn_restaurar_lecturas).setOnClickListener(v->{
            ProgressDialog pd_restore = new ProgressDialog(this);
            pd_restore.setMessage("Procesando, por favor espere...");
            pd_restore.show();
            new RestAdapter.Builder().setEndpoint("https://rfidmx.com/demo/Hellman/webservices/masterquerys").build().create(api_network_restore.class).setData("dummy-data", new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    try {
                        if(new BufferedReader(new InputStreamReader(response.getBody().in())).readLine().equals("succes")){
                            Toast.makeText(Hellman.this, "Proceso completado", Toast.LENGTH_SHORT).show();
                            pd_restore.dismiss();
                        }else{
                            Toast.makeText(Hellman.this, "Error en servidor, intente nuevamente", Toast.LENGTH_SHORT).show();
                            pd_restore.dismiss();
                        }
                    }catch (IOException | NullPointerException e){
                        Toast.makeText(Hellman.this, "algo salió mal, revise la información pertinente", Toast.LENGTH_SHORT).show();
                        Log.e("Hellmann", e.getMessage());
                        pd_restore.dismiss();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(Hellman.this, "Error revise su conexión", Toast.LENGTH_SHORT).show();
                    Log.e("Hellmann", error.getMessage());
                    pd_restore.dismiss();
                }
            });
        });

        MotionLayout = findViewById(R.id.motionLayout);
        MotionLayout.setScrimColor(getColor(android.R.color.transparent));
        MotionLayout.setDrawerElevation(0);

        //Inicio
        btn_addFile = findViewById(R.id.btn_add_file);
        btn_addFile.setOnClickListener(v->{
            String[] mimeTypes = {"text/plain"};

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.setType(mimeTypes[0]);
                if (mimeTypes.length > 0) {
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                }
            } else {
                String mimeTypesStr = "";
                for (String mimeType : mimeTypes) {
                    mimeTypesStr += mimeType + "|";
                }
                intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
            }
            startActivityForResult(Intent.createChooser(intent,"Seleccionando txt"), GlobalPreferences.INTENT_RESULT_ADD_FILE);
        });
        findViewById(R.id.btn_add_file_xls).setOnClickListener(v->{
            String[] mimeTypes = {"application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.setType(mimeTypes[0]);
                if (mimeTypes.length > 0) {
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                }
            } else {
                String mimeTypesStr = "";
                for (String mimeType : mimeTypes) {
                    mimeTypesStr += mimeType + "|";
                }
                intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
            }
            startActivityForResult(Intent.createChooser(intent,"Seleccionando archivo excel"), GlobalPreferences.INTENT_RESULT_ADD_FILE_EXCEL);
        });

        //CAFv2

        MainFragmentHolder = findViewById(R.id.MainFragmentHolder);

        btn_alta = findViewById(R.id.btn_alta);
        btn_ajustes = findViewById(R.id.btn_ajustes);
        btn_inventario = findViewById(R.id.btn_inventario);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)  {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GlobalPreferences.INTENT_RESULT_ADD_FILE && resultCode == RESULT_OK && data != null){
            BufferedReader br = null;
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Subiendo contenido, por favor espere...");
            pd.setCancelable(false);
            pd.show();
            try {

                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/" + getFileName(data.getData()));

                br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    try {
                        String string = line;

                        String[] parts = string.split("\\|");
                        String part1 = parts[0];
                        String part2 = parts[1];
                        String part3 = parts[2];
                        String part4 = parts[3];
                        String part5 = parts[4];
                        String part6 = parts[5];
                        JSONObject item = new JSONObject();
                        try {
                            item.put("EPC", part1);
                            item.put("field_1", part2);
                            item.put("field_2", part3);
                            item.put("field_3", part4);
                            item.put("field_4", part5);
                            item.put("field_5", part6);

                            new RestAdapter.Builder().setEndpoint("https://rfidmx.com/demo/android").build().create(api_network_send_data.class).setData("1", item.toString(), new Callback<Response>() {
                                @Override
                                public void success(Response response, Response response2) {
                                    try {
                                        Log.e("Hellman","Response:" + new BufferedReader(new InputStreamReader(response.getBody().in())).readLine());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    Toast.makeText(Hellman.this, "Error", Toast.LENGTH_LONG).show();
                                    Log.e("Hellman", error.getMessage());
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }catch (IndexOutOfBoundsException e){

                    }
                }

                new Handler().postDelayed(() -> {
                    pd.dismiss();
                    Toast.makeText(Hellman.this, "Datos sincronizados con servidor finalizado", Toast.LENGTH_SHORT).show();
                }, 1500);

            } catch (IOException e) {
                Log.e("Hellman", e.getMessage());

            } finally{
                /*try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        }
        Log.e("Excel","dentro activity result");
        if(requestCode == GlobalPreferences.INTENT_RESULT_ADD_FILE_EXCEL && resultCode == RESULT_OK && data != null) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/" + getFileName(data.getData()));
            Log.e("Excel","dentro excel");
            new RestAdapter.Builder().setEndpoint("https://rfidmx.com/demo/android/").build().create(ANUploadXLSX.class).setData("2", new TypedFile("multipart/form-data", file), new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    Log.e("Excel","succes");
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("Excel","error->"+error);
                }
            });

        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == 134){
            new ConnectorManager().Connect(rx);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        try {
            Fragment Lecturas = getSupportFragmentManager().findFragmentByTag("Lecturas");
            assert Lecturas != null;
            if(Lecturas.isVisible()){
                switch (GlobalPreferences.PAGE_STATE){
                    case GlobalPreferences.PAGE_STATE_PROCESING:
                        Toast.makeText(this, "Por favor, espere...", Toast.LENGTH_SHORT).show();
                        break;
                    case GlobalPreferences.PAGE_STATE_DETAILS:
                        GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_INVENTORY;
                        CAF_Inventario.PanelDetalle.setVisibility(View.GONE);
                        break;
                    case GlobalPreferences.PAGE_STATE_SEARCHING:
                        GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_DETAILS;
                        CAF_Inventario.Buscador.setVisibility(View.GONE);
                        break;
                    default:
                        CAF_Inventario.conter = 0;
                        getSupportFragmentManager().beginTransaction().remove(Lecturas).commit();
                }
            }
        }catch (NullPointerException e){

        }
            //super.onBackPressed();
    }

}