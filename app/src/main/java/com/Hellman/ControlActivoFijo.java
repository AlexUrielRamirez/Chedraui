package com.Hellman;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.bumptech.glide.Glide;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.uhf.uhf.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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

import static com.uhf.uhf.UHFApplication.getContext;

public class ControlActivoFijo extends AppCompatActivity {


    ConstraintLayout PanelAlta, PanelBaja;

    //Alta
    public static ImageView image;
    public static EditText EPCAlta, NombreAlta, DescripcionAlta, CategoriaAlta, AlmacenAlta;
    public static SignaturePad signaturePad_alta;
    interface api_network_upload_data{
        @FormUrlEncoded
        @POST("/upload_register.php")
        void setData(
                @Field("epc") String epc,
                @Field("nombre") String nombre,
                @Field("descripcion") String descripcion,
                @Field("almacen") String almacen,
                @Field("categoria") String categoria,
                Callback<Response> callback
        );
    }

    interface  api_network_upload_images{
        @Multipart
        @POST("/upload_images.php")
        void setData(
                @Part("file_1") TypedFile file_1,
                @Part("file_2") TypedFile file_2,
                @Part("id") String Id,
                Callback<Response> callback
        );
    }

    //Baja

    EditText et_epc_baja;
    interface api_network_get_item{
        @FormUrlEncoded
        @POST("/get_single_item.php")
        void setData(
                @Field("EPC") String EPC,
                Callback<Response> callback
        );
    }
    interface api_network_delete_item{
        @FormUrlEncoded
        @POST("/delete_item.php")
        void setData(
                @Field("id") String id,
                Callback<Response> callback
        );
    }

    //PhotoManager
    public static Uri imageUri;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_control_activo_fijo);
        GlobalPreferences.CAF_STATE = GlobalPreferences.CAF_STATE_IDLE;
        initViews();
        findViewById(R.id.btn_alta).setOnClickListener(v->{
            GlobalPreferences.CAF_STATE = GlobalPreferences.CAF_STATE_UP;
            PanelAlta.setVisibility(View.VISIBLE);
        });
        findViewById(R.id.btn_baja).setOnClickListener(v->{
            GlobalPreferences.CAF_STATE = GlobalPreferences.CAF_STATE_DOWN;
            PanelBaja.setVisibility(View.VISIBLE);
        });
    }

    private void initViews() {
        PanelAlta = findViewById(R.id.PanelAlta);
        image = findViewById(R.id.image);
        EPCAlta = findViewById(R.id.et_epc);
        NombreAlta = findViewById(R.id.et_nombre);
        DescripcionAlta = findViewById(R.id.et_descripcion);
        AlmacenAlta = findViewById(R.id.et_almacen);
        CategoriaAlta = findViewById(R.id.et_categoria);
        signaturePad_alta = findViewById(R.id.signature_pad_alta);
        findViewById(R.id.btn_tomar_foto).setOnClickListener(v -> {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
            imageUri = Uri.fromFile(photo);
            startActivityForResult(intent, GlobalPreferences.TAKE_PICTURE);

        });
        findViewById(R.id.btn_terminar_alta).setOnClickListener(v -> {
            ProgressDialog pd_insidencia = new ProgressDialog(this);
            pd_insidencia.setMessage("Por favor espere...");
            pd_insidencia.setCancelable(false);
            pd_insidencia.show();
            new RestAdapter.Builder().setEndpoint("https://rfidmx.com/demo/android").build().create(api_network_upload_data.class).setData(EPCAlta.getText().toString(), NombreAlta.getText().toString(), DescripcionAlta.getText().toString(), AlmacenAlta.getText().toString(), CategoriaAlta.getText().toString(), new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    try {
                        UploadImages(new BufferedReader(new InputStreamReader(response.getBody().in())).readLine(), pd_insidencia);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("Hellmann", "Error:" + error.getMessage());
                    Toast.makeText(ControlActivoFijo.this, "Error:"+error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        findViewById(R.id.btn_limpiar_firma).setOnClickListener(v->{
            signaturePad_alta.clear();
        });

        PanelBaja = findViewById(R.id.PanelBaja);
        et_epc_baja = findViewById(R.id.et_epc_baja);
        findViewById(R.id.btn_buscar_articulo).setOnClickListener(v->{
            BottomSheetDialog bsd = new BottomSheetDialog(ControlActivoFijo.this);
            bsd.setContentView(R.layout.bsd_baja_articulo);
            bsd.show();

            new RestAdapter.Builder().setEndpoint("https://rfidmx.com/demo/android").build().create(api_network_get_item.class).setData(et_epc_baja.getText().toString(), new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    try {

                        String data = new BufferedReader(new InputStreamReader(response.getBody().in())).readLine();

                        if(!data.equals("404")){
                            JSONObject json = new JSONObject(data);
                            RelativeLayout PanelLoading = bsd.findViewById(R.id.Panel_loading);
                            ImageView img = bsd.findViewById(R.id.img);
                            TextView Nombre = bsd.findViewById(R.id.nombre);
                            TextView Descripcion = bsd.findViewById(R.id.descripcion);
                            TextView Almacen = bsd.findViewById(R.id.almacen);
                            TextView Categoria = bsd.findViewById(R.id.Categoria);

                            Glide.with(ControlActivoFijo.this).load("https://rfidmx.com/demo/assets/Products/"+json.getString("field_3")+".jpg").override(120).into(img);

                            Nombre.setText("Nombre: "+json.getString("field_1"));
                            Descripcion.setText("Descripción: "+json.getString("field_2"));
                            Almacen.setText("Almacen: " + json.getString("field_4"));
                            Categoria.setText("Categoría: " + json.getString("field_5"));

                            bsd.findViewById(R.id.btn_eliminar).setOnClickListener(v->{
                                ProgressDialog pd = new ProgressDialog(ControlActivoFijo.this);
                                pd.setMessage("Procesando, por favor espere...");
                                pd.show();
                                try {
                                    new RestAdapter.Builder().setEndpoint("https://rfidmx.com/demo/android").build().create(api_network_delete_item.class).setData(json.getString("id"), new Callback<Response>() {
                                        @Override
                                        public void success(Response response, Response response2) {
                                            Toast.makeText(ControlActivoFijo.this, "Proceso finalizado con exito", Toast.LENGTH_LONG).show();
                                            pd.dismiss();
                                            bsd.dismiss();
                                        }

                                        @Override
                                        public void failure(RetrofitError error) {
                                            Toast.makeText(ControlActivoFijo.this, "Error, revise su conexión", Toast.LENGTH_LONG).show();
                                            Log.e("Hellmann", error.getMessage());
                                            pd.dismiss();
                                        }
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                            PanelLoading.setVisibility(View.GONE);
                        }else{
                            Toast.makeText(ControlActivoFijo.this, "No se encontró ningún articulo", Toast.LENGTH_SHORT).show();
                            bsd.dismiss();
                        }

                    }catch (IOException | JSONException | NullPointerException e){
                        Toast.makeText(ControlActivoFijo.this, "Algo salió mal, intente nuevamente o contacte a un desarrollador", Toast.LENGTH_LONG).show();
                        Log.e("Hellmann", e.getMessage());
                        bsd.dismiss();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(ControlActivoFijo.this, "Error, revise su conexión", Toast.LENGTH_SHORT).show();
                    bsd.dismiss();
                }
            });

        });

    }

    private void UploadImages(String IdInventario, ProgressDialog pd) {

        //Generar imagen articulo
        image.invalidate();
        BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        File imagen_articulo = getFileFromBitmap(bitmap, "tmp_art");
        //Generar imagen firma
        File imagen_firma = getFileFromBitmap(signaturePad_alta.getSignatureBitmap(), "tmp_sign");

        new RestAdapter.Builder().setEndpoint("https://rfidmx.com/demo/android").build().create(api_network_upload_images.class).setData(new TypedFile("multipart/form-data", imagen_articulo),new TypedFile("multipart/form-data", imagen_firma), IdInventario, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                pd.dismiss();
                Toast.makeText(ControlActivoFijo.this, "Articulo registrado correctamente", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Hellmann", "CAF->images error->"+error.getMessage());
                pd.dismiss();
                Toast.makeText(ControlActivoFijo.this, "Error, revise su conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public File getFileFromBitmap(Bitmap bmp, String child){
        File f = null;
        try{
            f = new File(ControlActivoFijo.this.getCacheDir(), child);
            f.createNewFile();
            Bitmap bitmap = bmp;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        }catch (IOException e){

        }finally {
            return f;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GlobalPreferences.TAKE_PICTURE && resultCode == RESULT_OK){
            Uri selectedImage = imageUri;
            getContentResolver().notifyChange(selectedImage, null);
            ContentResolver cr = getContentResolver();
            Bitmap bitmap;
            try {
                bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, selectedImage);
                Glide.with(this).load(bitmap).override(360).into(ControlActivoFijo.image);
            } catch (Exception e) {
                Toast.makeText(this, "Error al cargar", Toast.LENGTH_SHORT).show();
                Log.e("Camera", e.toString());
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(PanelAlta.getVisibility() == View.VISIBLE)
            PanelAlta.setVisibility(View.GONE);
        else if(PanelBaja.getVisibility() == View.VISIBLE)
            PanelBaja.setVisibility(View.GONE);
        else
            super.onBackPressed();
    }
}