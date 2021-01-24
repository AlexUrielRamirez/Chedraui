package com.Hellman.CAFv2.Administracion.Activo;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.bumptech.glide.Glide;
import com.uhf.uhf.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

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

import static android.app.Activity.RESULT_OK;
import static com.Etiflex.Splash.GlobalPreferences.CODE_BAR_READER;
import static com.Etiflex.Splash.GlobalPreferences.PICK_IMAGE_FROM_CAMERA;
import static com.Etiflex.Splash.GlobalPreferences.PICK_IMAGE_FROM_GALLERY;

public class admin_activo extends Fragment {

    public admin_activo() {}

    public static admin_activo newInstance() {
        admin_activo fragment = new admin_activo();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public static EditText et_epc;
    private ImageButton btn_bar_code;
    private TextView txt_error;
    private Button btn_buscar;
    private ProgressDialog progressDialog;
    private Boolean EXIST_FILE_1 = false, EXIST_FILE_2 = false;

    private final String FROM_CAMERA = "Tomar foto desde la cámara", FROM_GALLERY = "Cargar foto desde la galería", PHOTO_1 = "PHOTO_1", PHOTO_2 = "PHOTO_2";
    private String CURRENT_PHOTO = null;
    public static ConstraintLayout Panel_detalle;
    private String NumeroActivo;

    private interface api_network_get_data{
        @FormUrlEncoded
        @POST("/getItem.php")
        void setData(
                @Field("EPC") String EPC,
                @Field("Cedis") String Cedis,
                Callback<Response> callback
        );
    }

    private interface api_network_set_data{
        @FormUrlEncoded
        @POST("/setItem.php")
        void setData(
                @Field("data") String json,
                Callback<Response> callback
        );
    }

    private interface api_network_upload_photos{
        @Multipart
        @POST("/UploadPhoto.php")
        void setData(
                @Part("file")TypedFile file,
                @Part("path")String path,
                Callback<Response> callback
        );
    }

    private ImageView img_activo, img_activo_ref;
    private EditText et_descripcion, et_persona_asignada;
    private TextView txt_numero_activo, txt_tipo_etiqueta, txt_ubicacion, txt_tipo_activo, txt_epc, txt_fecha, btn_foto_1, btn_foto_2;
    private JSONObject json;
    private Button btn_guardar;
    private  String getDate(String date){

        String[] parts = date.split(" ");
        String Fecha = parts[0];
        String Hora = parts[1];

        String dia = Fecha.substring(8, 10);
        String mes = Fecha.substring(5, 7);
        switch (mes){
            case "01":
                mes = "Enero";
                break;
            case "02":
                mes = "Febrero";
                break;
            case "03":
                mes = "Marzo";
                break;
            case "04":
                mes = "Abril";
                break;
            case "05":
                mes = "Mayo";
                break;
            case "06":
                mes = "Junio";
                break;
            case "07":
                mes = "Julio";
                break;
            case "08":
                mes = "Agosto";
                break;
            case "09":
                mes = "Septiembre";
                break;
            case "10":
                mes = "Octubre";
                break;
            case "11":
                mes = "Noviembre";
                break;
            case "12":
                mes = "Diciembre";
                break;
        }
        String anio = Fecha.substring(0, 4);

        return dia + " del " + mes + " de " + anio + " a las " + Hora + " horas";
    }
    private PopupMenu menu;
    private File file_1, file_2;
    private Bitmap photo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_activo, container, false);
        initViews(view);
        progressDialog.setCancelable(false);
        btn_foto_1.setOnClickListener(v->{
            CURRENT_PHOTO = PHOTO_1;
            setUpMenu(btn_foto_1);
        });
        btn_foto_2.setOnClickListener(v->{
            CURRENT_PHOTO = PHOTO_2;
            setUpMenu(btn_foto_2);
        });
        btn_buscar.setOnClickListener(v-> {
            txt_error.setVisibility(View.GONE);
            if(et_epc.getText().length() > 23){
                progressDialog.setMessage("Buscando activo...");
                progressDialog.show();
                new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmanCAF/webservices/AdministracionActivo").build().create(api_network_get_data.class).setData(et_epc.getText().toString(), GlobalPreferences.ID_CEDIS, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        try {
                            json = new JSONObject(new BufferedReader(new InputStreamReader(response.getBody().in())).readLine());
                            Log.e("admin_activo", "response -> " + json);
                            Glide.with(getContext()).load(GlobalPreferences.URL+"/HellmanCAF/assets/Activo/"+json.getString("NumeroActivo")).into(img_activo);
                            Glide.with(getContext()).load(GlobalPreferences.URL+"/HellmanCAF/assets/Activo/"+json.getString("NumeroActivo")+"_ref").into(img_activo_ref);
                            et_descripcion.setText(json.getString("Descripcion"));
                            txt_numero_activo.setText("Número de activo: " + json.getString("NumeroActivo"));
                            et_persona_asignada.setText(json.getString("PersonaAsignada"));
                            txt_tipo_etiqueta.setText("Tipo de etiqueta: " + json.getString("TipoEtiqueta"));
                            txt_ubicacion.setText("Ubicacion: " + json.getString("NombreArea") + " > " + json.getString("NombreOficina"));
                            txt_tipo_activo.setText("Tipo de activo: " + json.getString("Tipo"));
                            txt_epc.setText("EPC: " + json.getString("EPC"));
                            txt_fecha.setText("Fecha de adquisición: " + getDate(json.getString("FechaCreacion")));
                            NumeroActivo = json.getString("NumeroActivo");
                            Panel_detalle.setVisibility(View.VISIBLE);
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            Log.e("admin_activo", "JSONException -> " + e.getMessage());
                            progressDialog.dismiss();
                            txt_error.setText("No se encontró el EPC especificado");
                            txt_error.setVisibility(View.VISIBLE);
                        } catch (IOException e) {
                            Log.e("admin_activo", "IOException -> " + e.getMessage());
                            progressDialog.dismiss();
                            txt_error.setText("Algo salió mal, intente nuevamente");
                            txt_error.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        txt_error.setText("No hay conexión al servidor");
                        txt_error.setVisibility(View.VISIBLE);
                    }
                });
            }else{
                txt_error.setText("EPC inválido");
                txt_error.setVisibility(View.VISIBLE);
            }
        });
        btn_bar_code.setOnClickListener(v->{
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.etiflex.sdl", "com.zebra.sdl.SDLguiActivity"));
            getActivity().startActivityForResult(intent, CODE_BAR_READER);
        });
        btn_guardar.setOnClickListener(v->{
            if(et_descripcion.getText().length() > 0 && et_persona_asignada.getText().length() > 0 && json != null){
                progressDialog.setMessage("Actualizando índices...");
                progressDialog.show();
                try {
                    json.put("PersonaAsignada", et_persona_asignada.getText().toString());
                    json.put("Descripcion", et_descripcion.getText().toString());
                }catch (JSONException e){

                }
                new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmanCAF/webservices/AdministracionActivo").build().create(api_network_set_data.class).setData(json.toString(), new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        try {
                            GlobalPreferences.mHistorial.GuardarHistorico(GlobalPreferences.ID_CEDIS, GlobalPreferences.ID_USUARIO, GlobalPreferences.HISTORIAL_TIPO_MODIFICACION_ACTIVO, json.getString("IdCAF"));
                            String resp = new BufferedReader(new InputStreamReader(response.getBody().in())).readLine();
                            if(resp.equals("succes")){
                                if(EXIST_FILE_1){
                                    boolean should_do_again = false;
                                    if(EXIST_FILE_2){
                                        should_do_again = true;
                                    }
                                    uploadPhoto(file_1, NumeroActivo, should_do_again);
                                }else if(EXIST_FILE_2){
                                    uploadPhoto(file_2, NumeroActivo + "_ref", false);
                                }else{
                                    finishChanges();
                                }
                            }else{
                                finishChanges();
                            }
                        }catch (JSONException | IOException e){
                            Toast.makeText(getContext(), "Algo salió mal, intente nuevamente", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(getContext(), "No hay conexión con el servidor", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }else{
                Toast.makeText(getContext(), "Por favor, ingrese información válida", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private void setUpMenu(TextView txt_view){
        menu = new PopupMenu(getContext(), txt_view);
        menu.getMenu().add(FROM_CAMERA);
        menu.getMenu().add(FROM_GALLERY);
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()){
                case FROM_CAMERA:
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, PICK_IMAGE_FROM_CAMERA);
                    break;
                case FROM_GALLERY:
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, PICK_IMAGE_FROM_GALLERY);
                    break;
            }
            return false;
        });
        menu.show();
    }

    private void initViews(View view) {
        et_epc = view.findViewById(R.id.et_epc);
        btn_bar_code = view.findViewById(R.id.btn_code_bar);
        txt_error = view.findViewById(R.id.txt_error);
        btn_buscar = view.findViewById(R.id.btn_buscar);
        progressDialog = new ProgressDialog(getContext());

        Panel_detalle = view.findViewById(R.id.Panel_detalle);
        btn_foto_1 = view.findViewById(R.id.btn_cambiar_foto_1);
        btn_foto_2 = view.findViewById(R.id.btn_cambiar_foto_2);
        img_activo = view.findViewById(R.id.img_activo);
        img_activo_ref = view.findViewById(R.id.img_activo_ref);
        et_descripcion = view.findViewById(R.id.et_descripcion);
        et_persona_asignada = view.findViewById(R.id.et_persona_asignada);
        txt_numero_activo = view.findViewById(R.id.txt_numero_activo);
        txt_tipo_etiqueta = view.findViewById(R.id.tipo_etiqueta);
        txt_ubicacion = view.findViewById(R.id.txt_ubicacion_activo);
        txt_tipo_activo = view.findViewById(R.id.txt_tipo_activo);
        txt_epc = view.findViewById(R.id.txt_epc);
        txt_fecha = view.findViewById(R.id.txt_fecha);
        btn_guardar = view.findViewById(R.id.btn_guardar);
    }

    private void uploadPhoto(File file, String path, boolean SHOULD_DO_AGAIN){
        new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmanCAF/webservices/AdministracionActivo").build().create(api_network_upload_photos.class).setData(new TypedFile("multipart/form-data", file), path, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                if(SHOULD_DO_AGAIN){
                    uploadPhoto(file_2, NumeroActivo+"_ref", false);
                }else{
                    finishChanges();
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void finishChanges(){
        progressDialog.dismiss();
        Toast.makeText(getContext(), "Cambios guardados", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case PICK_IMAGE_FROM_CAMERA:
                    photo = (Bitmap) data.getExtras().get("data");
                    switch (CURRENT_PHOTO){
                        case PHOTO_1:
                            file_1 = BitmapToFile(photo, "temporary_file_1");
                            EXIST_FILE_1 = true;
                            Glide.with(this).load(photo).override(360).into(img_activo);
                            break;
                        case PHOTO_2:
                            file_2 = BitmapToFile(photo, "temporary_file_2");
                            EXIST_FILE_2 = true;
                            Glide.with(this).load(photo).override(360).into(img_activo_ref);
                            break;
                    }
                    break;
                case PICK_IMAGE_FROM_GALLERY:
                    try {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        switch (CURRENT_PHOTO){
                            case PHOTO_1:
                                file_1 = BitmapToFile(selectedImage, "temporary_file_1");
                                EXIST_FILE_1 = true;
                                Glide.with(this).load(selectedImage).override(360).into(img_activo);
                                break;
                            case PHOTO_2:
                                file_2 = BitmapToFile(selectedImage, "temporary_file_2");
                                EXIST_FILE_2 = true;
                                Glide.with(this).load(selectedImage).override(360).into(img_activo_ref);
                                break;
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Algo salió mal", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }

    private File BitmapToFile(Bitmap bitmap, String name) {
        File filesDir = getActivity().getApplicationContext().getFilesDir();
        File file = new File(filesDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
            return file;
        } catch (Exception e) {
            Log.e("Main", "Error writing bitmap", e);
            return null;
        }
    }
}