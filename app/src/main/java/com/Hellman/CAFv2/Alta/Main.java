package com.Hellman.CAFv2.Alta;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Addons.ProgressBarAnimation;
import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.uhf.uhf.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
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

import static com.Etiflex.Splash.GlobalPreferences.CODE_BAR_READER;
import static com.Etiflex.Splash.GlobalPreferences.URL;

public class Main extends AppCompatActivity {

    private RelativeLayout CameraHolder;
    private ConstraintLayout PanelBuscador;
    private EditText et_buscador_activo, et_persona_asignada, et_descripcion, et_cantidad;
    private RadioGroup rg_tipo_etiqueta;
    private RadioButton rb_etiqueta_papel, rb_etiqueta_metal;
    private TextView btn_encontrar_activo_numero, btn_encontrar_activo_nombre, spinner_departamento, spinner_oficina, contador_letras_descripcion;
    private ImageButton btn_camara_por_numero;
    private RecyclerView rv_buscador;
    private ArrayList<SearchModel> main_list_buscador;
    private boolean FIND_BY_CODE = false, SearchingIsAboutToStart = false;
    private ImageView img_1, img_2, img_preview;
    private ConstraintLayout holder_image_buttons;
    private final int PICK_IMAGE_FROM_GALLERY = 1;
    private final int PICK_IMAGE_FROM_CAMERA = 2;
    private final int IMAGE_1 = 3, IMAGE_2 = 4;
    private int CURRENT_IMAGE = 0;
    private File file_img_1, file_img_2;
    private TextView btn_continuar_alta, btn_photos_from_camera, btn_photos_from_gallery, btn_tipo_activo, txt_indicador_fotos, txt_descripcion_preview, txt_numero_preview, txt_limpiar_activo;
    private int ContadorNewCAF = 0, TotalNewCAF = 0;
    private boolean ProductLoaded = false;
    private ProgressDialog progressDialog;
    private ConstraintLayout Card_Fotos, Card_Descripcion, Panel_preview;
    private LinearLayout Card_Tipo;

    private String search_key, IdActivo, NumeroActivo;
    final Handler handler = new Handler();
    final Runnable runnable = () -> {
        searchItem(search_key);
        SearchingIsAboutToStart = false;
    };
    
    //Ubicaciones
    private PopupMenu menu_area, menu_oficinas, menu_tipos;
    private ArrayList<ModelUbicaciones> main_list_areas, main_list_oficinas, main_list_tipos;
    private String IdArea = "0", IdOficina = "0", IdTipo, tipo_etiqueta = "Papel", TipoActivo = "0", DescripcionActivo = "";
    interface guardar_caf_activo_nuevo{
        @Multipart
        @POST("/insert_nuevo_activo.php")
        void setData(
                @Part("data") String data,
                @Part("file_1")TypedFile file_1,
                @Part("file_2")TypedFile file_2,
                Callback<Response> callback
        );
    }

    interface guardar_caf_activo_existente{
        @FormUrlEncoded
        @POST("/insert_activo_existente.php")
        void setData(
                @Field("data") String data,
                Callback<Response> callback
        );
    }

    private ArrayList<String> main_list_epcs, main_list_final_areas, main_list_final_oficinas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_alta);
        initViews();
        getTipos();
        getAreas();
        rv_buscador.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        btn_encontrar_activo_numero.setOnClickListener(v-> PanelBuscador.setVisibility(View.VISIBLE));
        btn_camara_por_numero.setOnClickListener(v-> {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.etiflex.sdl", "com.zebra.sdl.SDLguiActivity"));
            startActivityForResult(intent, CODE_BAR_READER);
        });
        et_descripcion.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){ }

            @Override
            public void afterTextChanged(Editable s) {
                contador_letras_descripcion.setText(s.toString().length() + "/150");
            }
        });
        et_buscador_activo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(SearchingIsAboutToStart){
                    handler.removeCallbacks(runnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                SearchingIsAboutToStart = true;
                search_key = s.toString();
                handler.postDelayed(runnable, 1000);
            }
        });
        btn_tipo_activo.setOnClickListener(v-> menu_tipos.show());
        spinner_departamento.setOnClickListener(v-> menu_area.show());
        spinner_oficina.setOnClickListener(v-> menu_oficinas.show());
        btn_photos_from_camera.setOnClickListener(v->{
            CURRENT_IMAGE = IMAGE_1;
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, PICK_IMAGE_FROM_CAMERA);
        });
        btn_photos_from_gallery.setOnClickListener(v->{
            CURRENT_IMAGE = IMAGE_1;
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, PICK_IMAGE_FROM_GALLERY);
        });
        btn_continuar_alta.setOnClickListener(v-> {
            progressDialog.setMessage("Subiendo información...");
            progressDialog.show();
            main_list_epcs = new ArrayList<>();
            main_list_final_areas = new ArrayList<>();
            main_list_final_oficinas = new ArrayList<>();
            TotalNewCAF = Integer.parseInt(et_cantidad.getText().toString());
            switch (rg_tipo_etiqueta.getCheckedRadioButtonId()){
                case R.id.rb_etiqueta_papel:
                    tipo_etiqueta = "Papel";
                    break;
                case R.id.rb_etiqueta_metal:
                    tipo_etiqueta = "Metal";
                    break;
            }
            String prepared_data = null;
            if(ProductLoaded){
                prepared_data = "{" +
                        "\"NumeroActivo\":\""+NumeroActivo+"\"," +
                        "\"IdActivo\":\""+IdActivo+"\"," +
                        "\"IdCedis\":\""+ GlobalPreferences.ID_CEDIS +"\"," +
                        "\"IdArea\":\""+IdArea+"\"," +
                        "\"IdOficina\":\""+IdOficina+"\"," +
                        "\"PersonaAsignada\":\""+et_persona_asignada.getText().toString()+"\"," +
                        "\"TipoEtiqueta\":\""+tipo_etiqueta+"\"}";
            }else{
                DescripcionActivo = et_descripcion.getText().toString();
                prepared_data = "{" +
                        "\"Descripcion\":\""+et_descripcion.getText().toString()+"\"," +
                        "\"IdCedis\":\""+ GlobalPreferences.ID_CEDIS +"\"," +
                        "\"IdArea\":\""+IdArea+"\"," +
                        "\"IdOficina\":\""+IdOficina+"\"," +
                        "\"Tipo\":\""+IdTipo+"\"," +
                        "\"PersonaAsignada\":\""+et_persona_asignada.getText().toString()+"\"," +
                        "\"TipoEtiqueta\":\""+tipo_etiqueta+"\"}";
            }
            uploadCAF(prepared_data);
        });
        txt_limpiar_activo.setOnClickListener(v->clearItem());
    }

    private void getTipos() {
        main_list_tipos = new ArrayList<>();
        menu_tipos = new PopupMenu(this, btn_tipo_activo);
        Volley.newRequestQueue(this).add(new JsonObjectRequest(Request.Method.GET, "https://rfidmx.com/HellmanCAF/webservices/AltaActivo/getTipos.php", null, response -> {
            JSONArray json = response.optJSONArray("Data");

            try {
                for (int i = 0; i < json.length(); i++) {
                    ModelUbicaciones model = new ModelUbicaciones();
                    JSONObject jsonObject = null;
                    jsonObject = json.getJSONObject(i);
                    model.setId(jsonObject.optString("Id"));
                    model.setNombre(jsonObject.optString("Nombre"));

                    menu_tipos.getMenu().add(model.getNombre());
                    main_list_tipos.add(model);

                }

                menu_tipos.setOnMenuItemClickListener(item -> {
                    btn_tipo_activo.setText(item.getTitle());
                    for(int i = 0; i<main_list_tipos.size(); i++){
                        if(main_list_tipos.get(i).getNombre().equals(item.getTitle())){
                            IdTipo = main_list_tipos.get(i).getId();
                        }
                    }
                    return false;
                });

            } catch (JSONException | NullPointerException e) {
                Log.e("Validacion", "JSON | Null Exception" + e);
            }

        }, error -> {
            Log.e("Validacion", "Volley error" + error);
        }));
    }

    private void initViews() {
        Card_Fotos = findViewById(R.id.card_fotos);
        Card_Descripcion = findViewById(R.id.card_descripcion);
        Card_Tipo = findViewById(R.id.card_tipo);
        txt_limpiar_activo = findViewById(R.id.txt_limpiar_activo);
        Panel_preview = findViewById(R.id.Panel_preview);
        txt_numero_preview = findViewById(R.id.txt_numero_activo_preview);
        txt_descripcion_preview = findViewById(R.id.txt_descripcion_preview);
        img_preview = findViewById(R.id.card_photo_preview);
        progressDialog = new ProgressDialog(this);
        CameraHolder = findViewById(R.id.camera_holder);
        PanelBuscador = findViewById(R.id.Panel_buscador);
        et_buscador_activo = findViewById(R.id.et_buscador_activo);
        rv_buscador = findViewById(R.id.rv_buscador);
        btn_encontrar_activo_numero = findViewById(R.id.txt_encontrar_activo_numero);
        btn_camara_por_numero = findViewById(R.id.btn_camara_buscador_numero);
        holder_image_buttons = findViewById(R.id.holder_image_buttons);
        txt_indicador_fotos = findViewById(R.id.txt_indicador_fotos);
        img_1 = findViewById(R.id.img_1);
        img_2 = findViewById(R.id.img_2);
        et_persona_asignada = findViewById(R.id.et_persona_asignada);
        rg_tipo_etiqueta = findViewById(R.id.rg_tipo_etiqueta);
        rb_etiqueta_papel = findViewById(R.id.rb_etiqueta_papel);
        rb_etiqueta_metal = findViewById(R.id.rb_etiqueta_metal);
        et_descripcion = findViewById(R.id.et_descripcion);
        contador_letras_descripcion = findViewById(R.id.contador_letras_descripcion);
        spinner_departamento = findViewById(R.id.spinner_Departamento);
        spinner_oficina = findViewById(R.id.spinner_Oficina);
        et_cantidad = findViewById(R.id.et_cantidad);
        btn_photos_from_camera = findViewById(R.id.btn_add_photos_camera);
        btn_photos_from_gallery = findViewById(R.id.btn_add_photos_from_gallery);
        btn_tipo_activo = findViewById(R.id.btn_tipo_activo);
        btn_continuar_alta = findViewById(R.id.btn_continuar_alta);
    }

    private void getAreas() {
        main_list_areas = new ArrayList<>();
        menu_area = new PopupMenu(this, spinner_departamento);
        Volley.newRequestQueue(this).add(new JsonObjectRequest(Request.Method.GET, "https://rfidmx.com/HellmanCAF/webservices/Loaders/getAreas.php?IdCedis="+GlobalPreferences.ID_CEDIS, null, response -> {
            JSONArray json = response.optJSONArray("Data");

            try {
                for (int i = 0; i < json.length(); i++) {
                    ModelUbicaciones model = new ModelUbicaciones();
                    JSONObject jsonObject = null;
                    jsonObject = json.getJSONObject(i);
                    model.setId(jsonObject.optString("Id"));
                    model.setNombre(jsonObject.optString("Nombre"));

                    menu_area.getMenu().add(model.getNombre());
                    main_list_areas.add(model);

                }

                menu_area.setOnMenuItemClickListener(item -> {
                    spinner_departamento.setText(item.getTitle());
                    spinner_oficina.setText("Oficina");
                    for(int i = 0; i<main_list_areas.size(); i++){
                        if(main_list_areas.get(i).getNombre().equals(item.getTitle())){
                            IdOficina = "0";
                            IdArea = main_list_areas.get(i).getId();
                            getOficinas();
                        }
                    }
                    return false;
                });

            } catch (JSONException | NullPointerException e) {
                Log.e("Validacion", "JSON | Null Exception" + e);
            }

        }, error -> {
            Log.e("Validacion", "Volley error" + error);
        }));
    }

    private void getOficinas() {
        main_list_oficinas = new ArrayList<>();
        menu_oficinas = new PopupMenu(this, spinner_oficina);
        Volley.newRequestQueue(this).add(new JsonObjectRequest(Request.Method.GET, "https://rfidmx.com/HellmanCAF/webservices/Loaders/getOficinas.php?IdArea="+IdArea, null, response -> {
            JSONArray json = response.optJSONArray("Data");

            try {
                for (int i = 0; i < json.length(); i++) {
                    ModelUbicaciones model = new ModelUbicaciones();
                    JSONObject jsonObject = null;
                    jsonObject = json.getJSONObject(i);
                    model.setId(jsonObject.optString("Id"));
                    model.setNombre(jsonObject.optString("Nombre"));

                    menu_oficinas.getMenu().add(model.getNombre());
                    main_list_oficinas.add(model);

                }

                menu_oficinas.setOnMenuItemClickListener(item -> {
                    spinner_oficina.setText(item.getTitle());
                    for(int i = 0; i<main_list_oficinas.size(); i++){
                        if(main_list_oficinas.get(i).getNombre().equals(item.getTitle())){
                            IdOficina = main_list_oficinas.get(i).getId();
                        }
                    }
                    return false;
                });

            } catch (JSONException | NullPointerException e) {
                Log.e("Validacion", "JSON | Null Exception" + e);
            }

        }, error -> {
            Log.e("Validacion", "Volley error" + error);
        }));
    }

    private void uploadCAF(String data){
        Log.e("main_alta", "Comenzando subida");
        if(ProductLoaded){
            if(!IdArea.equals("0") && !IdOficina.equals("0") && et_persona_asignada.getText().length() > 0 && et_cantidad.getText().length() > 0){
                new RestAdapter.Builder().setEndpoint("https://rfidmx.com/HellmanCAF/webservices/AltaActivo/").build().create(guardar_caf_activo_existente.class).setData(data, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        try {
                            String resp = new BufferedReader(new InputStreamReader(response.getBody().in())).readLine();
                            JSONObject json = new JSONObject(resp);
                            main_list_epcs.add(json.getString("EPC"));
                            main_list_final_areas.add(json.getString("NombreArea"));
                            main_list_final_oficinas.add(json.getString("NombreOficina"));
                            ContadorNewCAF = ContadorNewCAF + 1;
                            if(ContadorNewCAF < TotalNewCAF){
                                uploadCAF(resp);
                            }else{
                                finishUpload();
                            }
                        }catch (IOException e){
                            Log.e("MainAlta", "Excepción de lecturas");
                        }catch (JSONException e){
                            Log.e("MainAlta", "Excepción de código EPC");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e("Main", "Error de conexión");
                    }
                });
            }else{
                Toast.makeText(this, "Por favor, introduzca información válida", Toast.LENGTH_SHORT).show();
            }
        }else{
            if(file_img_1 != null && file_img_2 != null && !IdArea.equals("0") && !IdOficina.equals("0") && et_persona_asignada.getText().length() > 0 && et_cantidad.getText().length() > 0 && et_descripcion.getText().length() > 0){
                new RestAdapter.Builder().setEndpoint("https://rfidmx.com/HellmanCAF/webservices/AltaActivo").build().create(guardar_caf_activo_nuevo.class).setData(data, new TypedFile("multipart/form-data", file_img_1), new TypedFile("multipart/form-data", file_img_2), new Callback<Response>() {
                        @Override
                        public void success(Response response, Response response2) {
                            try {
                                String respuesta = new BufferedReader(new InputStreamReader(response.getBody().in())).readLine();
                                JSONObject json = new JSONObject(respuesta);
                                main_list_epcs.add(json.getString("EPC"));
                                ContadorNewCAF = ContadorNewCAF + 1;
                                if(ContadorNewCAF < TotalNewCAF){
                                    ProductLoaded = true;
                                    uploadCAF(respuesta);
                                }else{
                                    finishUpload();
                                }
                            }catch (IOException e){
                                Log.e("MainAlta", "Excepción de respuesta de servidor");
                            }catch (JSONException e){
                                Log.e("MainAlta", "Excepción de código epc");
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.e("MainAlta", "Error->"+error.getMessage());
                        }
                });
            }else{
                Toast.makeText(this, "Por favor, introduzca información válida", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void finishUpload(){
        if(rg_tipo_etiqueta.getCheckedRadioButtonId() == R.id.rb_etiqueta_papel){
            new printTags().execute(main_list_epcs);
        }else{
            progressDialog.dismiss();
            Toast.makeText(Main.this, "Proceso finalizado con éxito", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (reqCode){
                case PICK_IMAGE_FROM_CAMERA:
                    switch (CURRENT_IMAGE){
                            case IMAGE_1:
                                Bitmap photo = (Bitmap) data.getExtras().get("data");
                                file_img_1 = BitmapToFile(photo, "temporary_file_1");
                                Glide.with(this).load(photo).override(360).into(img_1);
                                CURRENT_IMAGE = IMAGE_2;
                                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(cameraIntent, PICK_IMAGE_FROM_CAMERA);
                                break;
                            case IMAGE_2:
                                Bitmap photo_2 = (Bitmap) data.getExtras().get("data");
                                file_img_2 = BitmapToFile(photo_2, "temporary_file_2");
                                Glide.with(this).load(photo_2).override(360).into(img_2);
                                holder_image_buttons.setVisibility(View.GONE);
                                CURRENT_IMAGE = 0;
                                break;
                        }
                    break;
                case PICK_IMAGE_FROM_GALLERY:
                    try {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        switch (CURRENT_IMAGE){
                            case IMAGE_1:
                                file_img_1 = BitmapToFile(selectedImage, "temporary_file_1");
                                Glide.with(this).load(selectedImage).override(360).into(img_1);
                                CURRENT_IMAGE = IMAGE_2;
                                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(i, PICK_IMAGE_FROM_GALLERY);
                                break;
                            case IMAGE_2:
                                file_img_2 = BitmapToFile(selectedImage, "temporary_file_2");
                                Glide.with(this).load(selectedImage).override(360).into(img_2);
                                holder_image_buttons.setVisibility(View.GONE);
                                CURRENT_IMAGE = 0;
                                break;
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Algo salió mal", Toast.LENGTH_LONG).show();
                    }
                    break;
                case CODE_BAR_READER:
                    et_buscador_activo.setText(data.getDataString());
                    break;
            }
        }else {
            Toast.makeText(this, "No se seleccionó ninguna imagen",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if(PanelBuscador.getVisibility() == View.VISIBLE){
            PanelBuscador.setVisibility(View.GONE);
        }else{
            super.onBackPressed();
        }
    }

    private void searchItem(String key) {
        main_list_buscador = new ArrayList<>();
        Volley.newRequestQueue(this).add(new JsonObjectRequest(Request.Method.GET, "https://rfidmx.com/HellmanCAF/webservices/AltaActivo/getSearch.php?key=" + key.substring(0, 4), null, response -> {
            JSONArray json= response.optJSONArray("Data");
            try {

                if(!FIND_BY_CODE){
                    for(int i=0; i<json.length();i++){
                        SearchModel model = new SearchModel();
                        JSONObject jsonObject = json.getJSONObject(i);
                        model.setId(jsonObject.optString("Id"));
                        model.setNumero(jsonObject.optString("Numero"));
                        model.setNombre(jsonObject.optString("Nombre"));
                        model.setTipo(jsonObject.optString("Tipo"));
                        model.setDescripcion(jsonObject.optString("Descripcion"));

                        main_list_buscador.add(model);

                    }

                    if(main_list_buscador.size() > 0){
                        rv_buscador.setAdapter(new search_adapter(main_list_buscador));
                    }
                }else{
                    SearchModel model = new SearchModel();
                    JSONObject jsonObject = json.getJSONObject(0);
                    model.setId(jsonObject.optString("Id"));
                    model.setNumero(jsonObject.optString("Numero"));
                    model.setNombre(jsonObject.optString("Nombre"));
                    model.setDescripcion(jsonObject.optString("Descripcion"));
                    setUpResult(model);
                }

            } catch (JSONException | NullPointerException e) {
                Toast.makeText(this, "Error de código, contacte a un desarrollador", Toast.LENGTH_SHORT).show();
            }

        },error -> {
            Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
        }));
    }

    private class SearchModel{
        private String Id;
        private String Numero;
        private String Nombre;
        private String Tipo;
        private String Descripcion;

        public String getId() {
            return Id;
        }

        public void setId(String id) {
            Id = id;
        }

        public String getNumero() {
            return Numero;
        }

        public void setNumero(String numero) {
            Numero = numero;
        }

        public String getNombre() {
            return Nombre;
        }

        public void setNombre(String nombre) {
            Nombre = nombre;
        }

        public String getTipo() {
            return Tipo;
        }

        public void setTipo(String tipo) {
            Tipo = tipo;
        }

        public String getDescripcion() {
            return Descripcion;
        }

        public void setDescripcion(String descripcion) {
            Descripcion = descripcion;
        }
    }

    public class ModelUbicaciones{
        private String Id;
        private String Nombre;

        public String getId() {
            return Id;
        }

        public void setId(String id) {
            Id = id;
        }

        public String getNombre() {
            return Nombre;
        }

        public void setNombre(String nombre) {
            Nombre = nombre;
        }
    }

    public class search_adapter extends RecyclerView.Adapter<search_adapter.ViewHolder> implements View.OnClickListener{

        public search_adapter(ArrayList<SearchModel> lista) {
            this.child_list = lista;
        }

        ArrayList<SearchModel> child_list;
        Context child_context;
        private View.OnClickListener listener;


        @Override
        public search_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_buscador,parent,false);
            view.setOnClickListener(this);
            child_context = parent.getContext();
            return new search_adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(search_adapter.ViewHolder holder, int position) {

            holder.item.setOnClickListener(v->{
                setUpResult(child_list.get(position));
            });

            Glide.with(child_context).load("https://rfidmx.com/HellmanCAF/assets/Activo/"+child_list.get(position).getNumero()).override(160).into(holder.img_activo);

            holder.nombre.setText(child_list.get(position).getNombre());
            holder.descripcion.setText(child_list.get(position).getDescripcion());
            holder.numero.setText("Número de activo: " + child_list.get(position).getNumero());

        }

        @Override
        public int getItemCount() {
            return child_list.size();
        }

        @Override
        public void onClick(View view) {

        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ConstraintLayout item;
            ImageView img_activo;
            TextView numero, nombre, descripcion;
            public ViewHolder(View itemView) {
                super(itemView);
                item = itemView.findViewById(R.id.item);
                img_activo = itemView.findViewById(R.id.img_activo);
                numero = itemView.findViewById(R.id.numero_activo);
                nombre = itemView.findViewById(R.id.nombre_activo);
                descripcion = itemView.findViewById(R.id.descripcion_activo);
            }
        }
    }

    private void setUpResult(SearchModel result){
        ProductLoaded = true;
        IdActivo = result.getId();
        NumeroActivo = result.getNumero();
        TipoActivo = result.getTipo();
        DescripcionActivo = result.getDescripcion();
        Glide.with(this).load("https://rfidmx.com/HellmanCAF/assets/Activo/" + result.getNumero()).override(360).into(img_preview);
        txt_descripcion_preview.setText(DescripcionActivo);
        txt_numero_preview.setText("Número de activo: "+NumeroActivo);
        Card_Tipo.setVisibility(View.GONE);
        Card_Descripcion.setVisibility(View.GONE);
        Card_Fotos.setVisibility(View.GONE);
        txt_indicador_fotos.setVisibility(View.GONE);
        btn_encontrar_activo_numero.setVisibility(View.GONE);
        txt_limpiar_activo.setVisibility(View.VISIBLE);
        Panel_preview.setVisibility(View.VISIBLE);
        PanelBuscador.setVisibility(View.GONE);
    }

    private void clearItem(){
        ProductLoaded = false;
        IdActivo = "0";
        NumeroActivo = "0";
        TipoActivo = "0";
        DescripcionActivo = "";
        Glide.with(this).load(R.drawable.empty_photo).override(360).into(img_preview);
        txt_descripcion_preview.setText("");
        txt_numero_preview.setText("");
        Card_Tipo.setVisibility(View.VISIBLE);
        Card_Descripcion.setVisibility(View.VISIBLE);
        Card_Fotos.setVisibility(View.VISIBLE);
        txt_indicador_fotos.setVisibility(View.VISIBLE);
        txt_limpiar_activo.setVisibility(View.GONE);
        btn_encontrar_activo_numero.setVisibility(View.VISIBLE);
        Panel_preview.setVisibility(View.GONE);
    }

    private File BitmapToFile(Bitmap bitmap, String name) {
        File filesDir = this.getApplicationContext().getFilesDir();
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

    private class printTags extends AsyncTask<ArrayList<String>, Void, Boolean> {

        @Override
        protected Boolean doInBackground(ArrayList<String>... arrayLists) {
            try {
                Socket clientSocket = new Socket(GlobalPreferences.SERVER_PRINTER_IP, 9100);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                String desc_1 = "",desc_2 = "" ,desc_3 = "" ,desc_4 = "";
                if(DescripcionActivo.length() <= 50){
                    desc_1 = DescripcionActivo;
                    desc_2 = "";
                    desc_3 = "";
                    desc_4 = "";
                }else if(DescripcionActivo.length() >= 50 && DescripcionActivo.length() <= 100){
                    desc_1 = DescripcionActivo.substring(0,50);
                    desc_2 = DescripcionActivo.substring(50,DescripcionActivo.length());
                    desc_3 = "";
                    desc_4 = "";
                }else if(DescripcionActivo.length() >= 100){
                    desc_1 = DescripcionActivo.substring(0,50);
                    desc_2 = DescripcionActivo.substring(50,100);
                    desc_3 = DescripcionActivo.substring(100,DescripcionActivo.length());
                    desc_4 = "";
                }
                for(int position = 0; position < arrayLists[0].size(); position++){
                    String zpl_2 = "^XA\n" +
                            "^RS,,,3,N,,,2\n" +
                            "^RR10\n" +
                            "^XZ\n" +
                            "<xpml><page quantity='0' pitch='24.0 mm'></xpml>^XA\n" +
                            "^SZ2^JMA\n" +
                            "^MCY^PMN\n" +
                            "^PW573\n" +
                            "~JSN\n" +
                            "^JZY\n" +
                            "^LH0,0^LRN\n" +
                            "^XZ\n" +
                            "<xpml></page></xpml><xpml><page quantity='1' pitch='24.0 mm'></xpml>~DGR:SSGFX000.GRF,790,10,:Z64:eJxNU7Fq21AUPc9qrUISmdChDlZlOnUrwhnymjiuP8DQfoAhhgwdI5NCJLCM2qmTmg8o7o+Y+JVCvQSU0UOoVDx4KcjFBRtMk94rpRANjwPSOefdc48AQCbIH91fBjk6SC9WOeqOzW4Giot0uFCMSjG02GF0GgKhZDSjN0aPgBjxkdKhmfxiTIq6bDKNjq2EFZ6SYfkrW23OgZfvz/lr8jsRbCqGwHe4TCaFS/xh1IBwxQ9GeyjY4hOjx0HREQOW0YOHCdYsbTSFQpcHetDZUDi94psnpQBHLKglBpov6oyCKjqWx3bn+3hTSVlmVYMpxizt1lDXIkarHs50nwWvWzj+kGXVauPsm8d2bReN3ymjngPTy7iOi/oi49oTPIl9RbaNj9BCqVCeGhPoA+LuzK0VimvyPVH7DudMWcGmAIgrEtQmAHH1AMSFIenmIC4sb0kxWDRNJduRJO722HQyX1SiutLhtIEq7fJQtVvMnULOLy3mzoSvriXvd6jdYGVn+6XEaN5Hi0jWgHkVr2L/5wTiqoTjUE4DysrA24G3pEGCjaC/TkecqVDm9tjinHXV340o1E2ld94d+XSpUlCwO6Gk5e4Gwn028Ci158Cvwpqz6tOkZpeL8Rl4PeSseOeHy7iX96A8De28G1uzmHLcIbo+4m7sqfu9Ehe8vyxquhz0XtZJErWyTpa+/O9pcX17k9XjXp9xENXnd73/e6vufoH8X/gHTBHUuQ==:F2F6\n" +
                            "^XA\n" +
                            "^FO36,233\n" +
                            "^BY3^BCN,22,N,N^FD>;"+arrayLists[0].get(position)+"^FS\n" +
                            "^FO4,84\n" +
                            "^BQN,2,6^FDLA,"+arrayLists[0].get(position)+"^FS\n" +
                            "^FT118,276\n" +
                            "^CI0\n" +
                            "^A0N,21,28^FD"+arrayLists[0].get(position)+"^FS\n" +
                            "^FT146,43\n" +
                            "^A0N,21,27^FD"+btn_tipo_activo.getText().toString()+"^FS\n" +
                            "^FT146,99\n" +
                            "^A0N,21,27^FD"+spinner_oficina.getText().toString()+"^FS\n" +
                            "^FT146,71\n" +
                            "^A0N,21,27^FD"+spinner_departamento.getText().toString()+"^FS\n" +
                            "^FT417,65\n" +
                            "^A0N,54,72^FD"+arrayLists[0].get(position).substring(0, 4)+"^FS\n" +
                            "^FO432,71\n" +
                            "^BY2^BCN,18,N,N^FD>;"+arrayLists[0].get(position).substring(0, 4)+"^FS\n" +
                            "^FT146,139\n" +
                            "^A0N,29,20^FD"+desc_1+"\n" +
                            "^FT146,169\n" +
                            "^A0N,29,20^F"+desc_2+"^FS\n" +
                            "^FT146,199\n" +
                            "^A0N,29,20^FD"+desc_3+"^FS\n" +
                            "^FO30,10\n" +
                            "^XGR:SSGFX000.GRF,1,1^FS\n" +
                            "^PQ1,0,1,Y\n" +
                            "^RFW,H,2,12,1^FD"+arrayLists[0].get(position)+"^FS\n"+
                            "^XZ\n" +
                            "<xpml></page></xpml>^XA\n" +
                            "^IDR:SSGFX000.GRF^XZ\n" +
                            "<xpml><end/></xpml>";
                    outToServer.writeBytes(zpl_2);
                    Thread.sleep(800);
                }
                clientSocket.close();
                return true;
            } catch (IOException e) {
                Log.e("main_alta", "IO Error -> "+e.getMessage());
                return false;
            } catch (InterruptedException e){
                Log.e("main_alta", "Interruped Error -> "+e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            progressDialog.dismiss();
            if(bool){
                Toast.makeText(Main.this, "Proceso finalizado con éxito", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(Main.this, "Algo salió mal, intente nuevamente", Toast.LENGTH_SHORT).show();
            }
        }
    }
}