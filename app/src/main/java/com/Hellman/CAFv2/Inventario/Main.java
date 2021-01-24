package com.Hellman.CAFv2.Inventario;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Addons.ProgressBarAnimation;
import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.Hellman.CAFv2.Incidencias.ControlIncidencias;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.uhf.uhf.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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

import static com.Etiflex.Splash.GlobalPreferences.DEVELOP_MODE;
import static com.Etiflex.Splash.GlobalPreferences.PAGE_STATE_INVENTORY;
import static com.Etiflex.Splash.GlobalPreferences.PAGE_STATE_SETTING_UBICATION;

public class Main extends AppCompatActivity {

    public static int ParentHolderId = R.id.MainHolder;
    RXObserver rx = null;
    ToneGenerator mToneGenerator;

    public static ProgressBar mProgress;
    private ModelInventario model;
    public static ArrayList<ModelInventario> main_list;
    public static ArrayList<String> tag_list;
    public static TextView txt_contador, txt_cedis_actual;
    public static RecyclerView rv_content;
    public static rv_adapter adapter;
    public static int conter = 0;
    public static ConstraintLayout PanelUbicacion, PanelDescarga;
    public String IdIncidencia = null, IdCAF_for_incidence = null;
    public boolean IS_FULL = true;

    //Antenna
    String PORT = "dev/ttyS4";
    RFIDReaderHelper mReader;

    //Ubicación
    private TextView Spinner_Departamento, Spinner_Oficina, txt_download_progress;
    private PopupMenu menu_area, menu_oficinas;
    private ArrayList<ModelUbicaciones> main_list_areas, main_list_oficinas;
    private String IdArea = "0", IdOficina = "0";
    private ProgressBar pb_loading_ofices;

    //Insidencias
    public static ArrayList<ModelInsidencias> main_list_insidencias;
    public static ArrayList<String> tag_list_insidencias;
    public static boolean AreIncidences = false;

    //Detalle
    public static ConstraintLayout PanelDetalle;
    public static ImageView ImgDetalle;
    public static TextView NombreDetalle, DescripcionDetalle, AlmacenDetalle, CategoriaDetalle;
    public static Button btn_buscar, btn_insidencia;

    //Buscador
    public static ConstraintLayout Buscador;
    public static ProgressBar pb_potencia;
    public static TextView CurrentEPC;
    private SeekBar sb_potencia;
    private TextView txt_indicador_potencia;

    //Fragments
    public static RelativeLayout FragmentHolder;

    //Alerta
    public static LinearLayout AlertLayout;

    interface api_network {
        @FormUrlEncoded
        @POST("/upload_alta.php")
        void setData(
                @Field("id_list") String IdList,
                Callback<Response> callback
        );
    }

    interface api_network_alta_insidencia {
        @Multipart
        @POST("/setAltaIncidencia.php")
        void setData(
                @Part("IdCAF") String IdCAf,
                @Part("CreatorName") String CreatorName,
                @Part("file") TypedFile file,
                Callback<Response> callback
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_main_inventario);
        AreIncidences = false;
        mToneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        initViews();
        getAreas();
        this.setTitle(getString(R.string.Inventario));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == 134){
            mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void connectToAntenna(){
        ModuleConnector connector = new ReaderConnector();
        if (connector.connectCom(PORT, 115200)) {
            ModuleManager.newInstance().setUHFStatus(true);
            try {
                mReader = RFIDReaderHelper.getDefaultHelper();
                mReader.registerObserver(rx);
            } catch (Exception e) {
                Log.e("Main", "error connecting to antenna->"+e.getMessage());
                e.printStackTrace();
            }
        }else{
            Log.e("Main", "error connecting to antenna");
        }
    }

    private void initViews() {
        FragmentHolder = findViewById(R.id.fragment_holder);
        AlertLayout = findViewById(R.id.alerta_incidencia);
        AlertLayout.setOnClickListener(v->{
            GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_INCIDENCE_FOUND;
            getFragmentManager().beginTransaction().add(FragmentHolder.getId(), ControlIncidencias.newInstance(IdCAF_for_incidence, IdIncidencia), "ControlIncidencias").commit();
            FragmentHolder.setVisibility(View.VISIBLE);
        });
        PanelUbicacion = findViewById(R.id.Panel_ubicacion);
        pb_loading_ofices = findViewById(R.id.pb_loading_ofices);
        PanelDescarga = findViewById(R.id.Panel_descarga);
        txt_download_progress = findViewById(R.id.txt_progress);
        Spinner_Departamento = findViewById(R.id.spinner_Departamento);
        txt_cedis_actual = findViewById(R.id.txt_cedis_actual);
        txt_cedis_actual.setText("Cedis actual: " + GlobalPreferences.NOMBRE_CEDIS);
        Spinner_Oficina = findViewById(R.id.spinner_Oficina);
        Spinner_Departamento.setOnClickListener(v->{
            menu_area.show();
        });
        Spinner_Oficina.setOnClickListener(v->{
            if(IdArea.equals("0"))
                Toast.makeText(this, "Primero seleccione un departamento", Toast.LENGTH_SHORT).show();
            else
                menu_oficinas.show();
        });
        findViewById(R.id.btn_continuar).setOnClickListener(v->{
            if(!IdArea.equals("0") && !IdOficina.equals("0")){
                PanelDescarga.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> DescargarInformacion(this), 1500);
            }else{
                Toast.makeText(this, "Por favor, elija una ubicación válida", Toast.LENGTH_SHORT).show();
            }
        });
        mProgress = findViewById(R.id.ProgressCount);
        txt_contador = findViewById(R.id.txt_contador);
        rv_content = findViewById(R.id.rv_content);
        rv_content.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rv_content.getItemAnimator().setChangeDuration(0);
        PanelDetalle = findViewById(R.id.PanelDetalle);
        ImgDetalle = findViewById(R.id.img_detalle);
        NombreDetalle = findViewById(R.id.nombre_detalle);
        DescripcionDetalle = findViewById(R.id.descripcion_detalle);
        AlmacenDetalle = findViewById(R.id.almacen_detalle);
        CategoriaDetalle = findViewById(R.id.categoria_detalle);
        btn_buscar = findViewById(R.id.btn_buscar);
        btn_insidencia = findViewById(R.id.btn_insidencia);
        Buscador = findViewById(R.id.layout_buscador);
        txt_indicador_potencia = findViewById(R.id.txt_indicador_potencia);
        sb_potencia = findViewById(R.id.sb_potencia);
        pb_potencia = findViewById(R.id.Potencia);
        sb_potencia.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txt_indicador_potencia.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                byte btOutputPower = (byte)Integer.parseInt(String.valueOf(seekBar.getProgress()));
                mReader.setOutputPower((byte)0xff, btOutputPower);
            }
        });
        CurrentEPC = findViewById(R.id.txt_epc);
        findViewById(R.id.btn_onbackpressed).setOnClickListener(v-> mOnBackPressed());
    }

    private void getAreas() {
        main_list_areas = new ArrayList<>();
        menu_area = new PopupMenu(Main.this, Spinner_Departamento);
        Volley.newRequestQueue(Main.this).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmanCAF/webservices/Loaders/getAreas.php?IdCedis="+GlobalPreferences.ID_CEDIS, null, response -> {
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
                    Spinner_Departamento.setText(item.getTitle());
                    Spinner_Oficina.setText("Oficina");
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
        menu_oficinas = new PopupMenu(Main.this, Spinner_Oficina);
        pb_loading_ofices.setVisibility(View.VISIBLE);
        Volley.newRequestQueue(Main.this).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmanCAF/webservices/Loaders/getOficinas.php?IdArea="+IdArea, null, response -> {
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
                    Spinner_Oficina.setText(item.getTitle());
                    for(int i = 0; i<main_list_oficinas.size(); i++){
                        if(main_list_oficinas.get(i).getNombre().equals(item.getTitle())){
                            IdOficina = main_list_oficinas.get(i).getId();
                        }
                    }
                    return false;
                });

                pb_loading_ofices.setVisibility(View.GONE);

            } catch (JSONException | NullPointerException e) {
                Log.e("Validacion", "JSON | Null Exception" + e);
            }

        }, error -> {
            Log.e("Validacion", "Volley error" + error);
        }));
    }

    @SuppressLint("SetTextI18n")
    private void DescargarInformacion(Context ctx) {

        conter = 0;
        txt_download_progress.setText("Consiguiendo recursos de lectura...");
        Volley.newRequestQueue(ctx).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmanCAF/webservices/Inventario/getData.php?IdArea="+IdArea+"&IdOficina="+IdOficina, null, response -> {
            JSONArray json = response.optJSONArray("Data");
            main_list = new ArrayList<>();
            tag_list = new ArrayList<>();

            try {
                for (int i = 0; i < json.length(); i++) {
                    model = new ModelInventario();
                    JSONObject jsonObject = null;
                    jsonObject = json.getJSONObject(i);
                    model.setId(jsonObject.optString("Id"));
                    model.setEPC(jsonObject.optString("EPC"));
                    model.setNumero(jsonObject.optString("Numero"));
                    model.setNombre(jsonObject.optString("Nombre"));
                    model.setDescripcion(jsonObject.optString("Descripcion"));
                    model.setStatus(Integer.parseInt(jsonObject.optString("Status")));

                    if(model.getStatus() != 1){
                        IS_FULL = false;
                    }

                    if (model.getStatus() == 1) {
                        conter = conter + 1;
                    }

                    main_list.add(model);
                    tag_list.add(model.getEPC().replaceAll(" ", ""));

                }

                mProgress.setMax(main_list.size());
                mProgress.setProgress(conter);
                txt_contador.setText(conter + "/" + main_list.size());
                adapter = new rv_adapter();
                rv_content.setAdapter(adapter);
                GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_INVENTORY;
                findViewById(R.id.btn_terminar).setOnClickListener(v -> {
                    if (conter >= main_list.size()) {
                        GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_PROCESING;
                        ProgressDialog pd = new ProgressDialog(Main.this);
                        pd.setMessage("Procesando, por favor espere...");
                        pd.setCancelable(false);
                        pd.show();
                        StringBuilder ids = new StringBuilder();
                        for (int i = 0; i < main_list.size(); i++) {
                            if (main_list.get(i).getStatus() == 1) {
                                ids.append(main_list.get(i).getId() + ",");
                            }
                        }
                        String id_list = ids.toString();
                        id_list = id_list.substring(0, id_list.length() - 1);
                        new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmanCAF/webservices/Inventario").build().create(api_network.class).setData(id_list, new Callback<Response>() {
                            @Override
                            public void success(Response response, Response response2) {
                                try {
                                    if (new BufferedReader(new InputStreamReader(response.getBody().in())).readLine().equals("succes")) {
                                        GlobalPreferences.PAGE_STATE = PAGE_STATE_SETTING_UBICATION;
                                        Buscador.setVisibility(View.GONE);
                                        PanelDetalle.setVisibility(View.GONE);
                                        Spinner_Departamento.setText("Departamento");
                                        Spinner_Oficina.setText("Oficina");
                                        PanelUbicacion.setVisibility(View.VISIBLE);
                                        Toast.makeText(Main.this, "¡Proceso finalizado con exito!", Toast.LENGTH_SHORT).show();
                                        pd.dismiss();
                                    } else {
                                        Log.e("Main","\"Error de php\"");
                                        Toast.makeText(Main.this, "Algo salió mal, intente nuevamente o contacte a un desarrollador", Toast.LENGTH_SHORT).show();
                                        pd.dismiss();
                                    }
                                } catch (IOException | NullPointerException e) {
                                    Log.e("Main","\"Error al leer respuesta\"");
                                    Toast.makeText(Main.this, "Algo salió mal, intente nuevamente o contacte a un desarrollador", Toast.LENGTH_SHORT).show();
                                    pd.dismiss();
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Toast.makeText(Main.this, "No hay conexión con el servidor", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }
                        });
                    } else {
                        final Dialog dialog = new Dialog(Main.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        dialog.setContentView(R.layout.error_validating);
                        dialog.findViewById(R.id.btn_volver).setOnClickListener(v2 -> {
                            dialog.dismiss();
                        });
                        dialog.show();
                    }
                });
                new Handler().postDelayed(() -> DownloadInsidences(), 1500);
            } catch (JSONException | NullPointerException e) {
                Log.e("Validacion", "JSON | Null Exception" + e);
            }

        }, error -> {
            Log.e("Validacion", "Volley error ln 430" + error);
        }));
    }

    private void DownloadInsidences() {
        txt_download_progress.setText("Consiguiendo registros de insidencias...");
        Volley.newRequestQueue(Main.this).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmanCAF/webservices/Inventario/getIncidencias.php", null, response -> {
            JSONArray json = response.optJSONArray("Data");
            main_list_insidencias = new ArrayList<>();
            tag_list_insidencias = new ArrayList<>();

            try {
                for (int i = 0; i < json.length(); i++) {
                    ModelInsidencias model = new ModelInsidencias();
                    JSONObject jsonObject = null;
                    jsonObject = json.getJSONObject(i);
                    model.setId(jsonObject.optString("Id"));
                    model.setIdCAF(jsonObject.optString("IdCAF"));
                    model.setFecha(jsonObject.optString("FechaAlta"));
                    model.setEPC(jsonObject.optString("EPC"));
                    model.setNumero(jsonObject.optString("Numero"));
                    model.setNombre(jsonObject.optString("Nombre"));
                    model.setDescripcion(jsonObject.optString("Descripcion"));

                    main_list_insidencias.add(model);
                    tag_list_insidencias.add(model.getEPC().replaceAll(" ", ""));

                }
                Log.e("Validacion", "Lista llena - "+main_list_insidencias.size());
                AreIncidences = true;
                setUpData();
            } catch (JSONException | NullPointerException e) {
                Log.e("Validacion", "JSON | Null Exception" + e);
                setUpData();
            }

        }, error -> {
            Log.e("Validacion", "Volley error ln 467" + error);
            setUpData();
        }));
    }

    private void setUpReader() {
        rx = new RXObserver(){
            @Override
            protected void onInventoryTag(RXInventoryTag tag) {
                super.onInventoryTag(tag);
                String EPC = tag.strEPC.replaceAll(" ", "");
                try {
                    switch (GlobalPreferences.PAGE_STATE){
                            case GlobalPreferences.PAGE_STATE_INVENTORY:
                                if(AreIncidences){
                                    if(tag_list_insidencias.contains(EPC)){
                                        new Thread(()->{
                                            if(AlertLayout.getVisibility() != View.VISIBLE){
                                                for(int position = 0; position < tag_list_insidencias.size(); position++){
                                                    if(main_list_insidencias.get(position).getEPC().equals(EPC)){
                                                        IdIncidencia = main_list_insidencias.get(position).getId();
                                                        IdCAF_for_incidence = main_list_insidencias.get(position).getIdCAF();
                                                    }
                                                }
                                                Animation animation = AnimationUtils.loadAnimation(Main.this, R.anim.right_to_left_in);
                                                AlertLayout.setAnimation(animation);
                                                Main.this.runOnUiThread(() -> {
                                                    AlertLayout.setVisibility(View.VISIBLE);
                                                    animation.start();
                                                });
                                            }
                                        }).run();
                                    }
                                }
                                if(tag_list.contains(EPC)){
                                    new Thread(() -> {
                                        for(int position = 0; position < tag_list.size(); position++){
                                            final int final_position = position;
                                            if(main_list.get(position).getEPC().equals(EPC) && main_list.get(position).getStatus() == 0){
                                                main_list.get(position).setStatus(1);
                                                Main.this.runOnUiThread(() -> {
                                                    //adapter.notifyItemChanged(final_position, main_list.get(final_position));
                                                    adapter.notifyDataSetChanged();
                                                    conter = conter + 1;
                                                    mProgress.setProgress(conter);
                                                    txt_contador.setText(conter+"/"+ main_list.size());
                                                });
                                                break;
                                            }
                                        }
                                    }).run();
                                }
                                break;
                            case GlobalPreferences.PAGE_STATE_SEARCHING:
                                new Thread(() -> {
                                    if(EPC.equals(GlobalPreferences.CURRENT_TAG)){
                                        mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                                        pb_potencia.startAnimation(new ProgressBarAnimation(pb_potencia, Math.round(Float.parseFloat(tag.strRSSI)), 0));
                                    }
                                }).run();
                                break;
                        }

                }catch (NullPointerException e){
                    Log.e("Hellmann", "Lecturas"+e.getMessage());
                }
            }
        };
        connectToAntenna();
    }

    private void setUpData(){
        if(!DEVELOP_MODE){
            setUpReader();
        }
        new Handler().postDelayed(() -> {
            txt_download_progress.setText("Cargando...");
            PanelUbicacion.setVisibility(View.GONE);
            PanelDescarga.setVisibility(View.GONE);
            if(IS_FULL || conter == main_list.size()){
                txt_contador.setText( main_list.size() + "/" + main_list.size());
                mProgress.setProgress(main_list.size());
                final Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.setContentView(R.layout.dialog_full_inventory);
                dialog.findViewById(R.id.btn_volver).setOnClickListener(v->{
                    dialog.dismiss();
                    Main.this.onBackPressed();
                });
                dialog.findViewById(R.id.btn_continuar).setOnClickListener(v->{
                    startActivity(new Intent(Main.this, com.Hellman.CAFv2.Alta.Main.class));
                    dialog.dismiss();
                });
                dialog.show();
            }
        }, 1500);
    }

    public static class rv_adapter extends RecyclerView.Adapter<rv_adapter.ViewHolder> implements View.OnClickListener{
        private View.OnClickListener listener;
        Context context;
        @Override
        public rv_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag,parent,false);
            view.setOnClickListener(this);
            context = parent.getContext();
            return new rv_adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(rv_adapter.ViewHolder holder, int position) {
            Glide.with(context).load(GlobalPreferences.URL+"/HellmanCAF/assets/Activo/" + main_list.get(position).getNumero()).placeholder(R.drawable.empty_photo).override(240).into(holder.img);
            holder.item_holder.setOnClickListener(v->{

                GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_DETAILS;
                GlobalPreferences.CURRENT_TAG = main_list.get(position).getEPC();
                CurrentEPC.setText(GlobalPreferences.CURRENT_TAG);
                setUpDetailData(main_list.get(position));
                PanelDetalle.setVisibility(View.VISIBLE);
                if(main_list.get(position).getStatus() == 2){
                    btn_insidencia.setEnabled(false);
                    btn_insidencia.setBackgroundColor(context.getColor(R.color.gray_light));
                }else{
                    btn_insidencia.setEnabled(true);
                    btn_insidencia.setBackgroundColor(context.getColor(R.color.menu_orange));
                }
                btn_buscar.setOnClickListener(v_1->{
                    GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_SEARCHING;
                    GlobalPreferences.CURRENT_TAG = main_list.get(position).getEPC();
                    CurrentEPC.setText(GlobalPreferences.CURRENT_TAG);
                    //setAntennaPower("30");
                    Buscador.setVisibility(View.VISIBLE);
                });
                btn_insidencia.setOnClickListener(v1->{
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog.setContentView(R.layout.alert_insidence);
                    TextView NombreCreador = dialog.findViewById(R.id.et_persona_alta);
                    NombreCreador.setText(GlobalPreferences.NOMBRE_USUARIO);
                    dialog.findViewById(R.id.btn_volver).setOnClickListener(v2->{
                        dialog.dismiss();
                    });
                    SignaturePad signaturePad = dialog.findViewById(R.id.signature_pad);
                    dialog.findViewById(R.id.btn_limpiar_firma).setOnClickListener(v2->{
                        signaturePad.clear();
                    });
                    dialog.findViewById(R.id.btn_continuar).setOnClickListener(v2->{
                        if(!signaturePad.isEmpty()){
                            ProgressDialog pd_insidencia = new ProgressDialog(context);
                            pd_insidencia.setMessage("Por favor espere...");
                            pd_insidencia.show();
                            File f = null;
                            try{
                                f = new File(context.getCacheDir(), "tmp_bitmap");
                                f.createNewFile();
                                Bitmap bitmap = signaturePad.getSignatureBitmap();
                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                                byte[] bitmapdata = bos.toByteArray();
                                FileOutputStream fos = new FileOutputStream(f);
                                fos.write(bitmapdata);
                                fos.flush();
                                fos.close();
                                new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmanCAF/webservices/Incidencias").build().create(api_network_alta_insidencia.class).setData(main_list.get(position).getId(), GlobalPreferences.NOMBRE_USUARIO,new TypedFile("multipart/form-data", f), new Callback<Response>() {
                                    @Override
                                    public void success(Response response, Response response2) {
                                        try{
                                            if(new BufferedReader(new InputStreamReader(response.getBody().in())).readLine().equals("succes")){
                                                /*main_list.get(position).setStatus(2);
                                                adapter.notifyItemChanged(position, main_list.get(position));
                                                if(conter < main_list.size()){
                                                    conter = conter + 1;
                                                    mProgress.setProgress(conter);
                                                    txt_contador.setText(conter+"/"+ main_list.size());
                                                }
                                                Toast.makeText(context, "Incidencia generada correctamente", Toast.LENGTH_SHORT).show();*/
                                                GlobalPreferences.mHistorial.GuardarHistorico(GlobalPreferences.ID_CEDIS, GlobalPreferences.ID_USUARIO, GlobalPreferences.HISTORIAL_TIPO_ALTA_INCIDENCIA, main_list.get(position).getId());
                                                main_list.remove(position);
                                                adapter.notifyDataSetChanged();
                                                mProgress.setMax(main_list.size());
                                                if(conter < main_list.size()){
                                                    conter = conter + 1;
                                                }else if(conter > main_list.size()){
                                                    conter = conter - 1;
                                                }
                                                mProgress.setProgress(conter);
                                                txt_contador.setText(conter+"/"+ main_list.size());
                                                PanelDetalle.setVisibility(View.GONE);
                                                Toast.makeText(context, "Incidencia generada correctamente", Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(context, "Ocurrió un error, contacte a un desarrollador", Toast.LENGTH_SHORT).show();
                                                Log.e("Main", "Server Response Error");
                                            }
                                            pd_insidencia.dismiss();
                                            dialog.dismiss();
                                        }catch (IOException | NullPointerException e){
                                            Toast.makeText(context, "Ocurrió un error, contacte a un desarrollador", Toast.LENGTH_SHORT).show();
                                            Log.e("Main", "Server Response Error II");
                                            pd_insidencia.dismiss();
                                            dialog.dismiss();
                                        }
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        Toast.makeText(context, "Error, revise su conexión", Toast.LENGTH_SHORT).show();
                                        Log.e("Main", "Error de conexión:"+error.getMessage());
                                        pd_insidencia.dismiss();
                                        dialog.dismiss();
                                    }
                                });
                            }catch (IOException e){
                                pd_insidencia.dismiss();
                                Toast.makeText(context, "Por favor, revise los permisos de la aplicación", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(context, "Por favor, ingrese los datos solicitados", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.show();
                });
            });
            holder.nombre.setText(main_list.get(position).getNombre());
            holder.descripcion.setText(main_list.get(position).getDescripcion());
            holder.almacen.setText(main_list.get(position).getDescripcion());
            holder.categoria.setText("Número: "+main_list.get(position).getNumero());

            if(main_list.get(position).getStatus() == 1){
                holder.status.setText("ENCONTRADO");
                holder.tag_status.setCardBackgroundColor(context.getColor(R.color.green_2));
            }else {
                holder.status.setText("NO ENCONTRADO");
                holder.tag_status.setCardBackgroundColor(context.getColor(R.color.menu_orange));
            }
        }

        private void setUpDetailData(ModelInventario model) {
            Glide.with(context).load(GlobalPreferences.URL+"/HellmanCAF/assets/Activo/" + model.getNumero()).override(360).into(ImgDetalle);
            NombreDetalle.setText("Nombre: "+model.getNombre());
            DescripcionDetalle.setText("Descripción: "+model.getDescripcion());
            AlmacenDetalle.setText("Isal: "+model.getDescripcion());
            CategoriaDetalle.setText("Número de activo: "+model.getNumero());
        }


        @Override
        public int getItemCount() {
            return main_list.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public void onClick(View view) {

        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ConstraintLayout item_holder;
            CardView tag_status;
            ImageView img;
            TextView nombre, descripcion, almacen, categoria, status;
            public ViewHolder(View itemView) {
                super(itemView);
                item_holder = itemView.findViewById(R.id.item_holder);
                tag_status = itemView.findViewById(R.id.tag_status);
                img = itemView.findViewById(R.id.img);
                nombre = itemView.findViewById(R.id.nombre);
                descripcion = itemView.findViewById(R.id.descripcion);
                almacen = itemView.findViewById(R.id.almacen);
                categoria = itemView.findViewById(R.id.Categoria);
                status = itemView.findViewById(R.id.status);
            }
        }
    }

    public class ModelInventario{
        private String Id;
        private String EPC;
        private String Numero;
        private String Nombre;
        private String Descripcion;
        private int Status;

        public String getId() {
            return Id;
        }

        public void setId(String id) {
            Id = id;
        }

        public String getEPC() {
            return EPC;
        }

        public void setEPC(String EPC) {
            this.EPC = EPC;
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

        public String getDescripcion() {
            return Descripcion;
        }

        public void setDescripcion(String descripcion) {
            Descripcion = descripcion;
        }

        public int getStatus() {
            return Status;
        }

        public void setStatus(int status) {
            Status = status;
        }
    }

    public class ModelInsidencias{
        private String Id;
        private String IdCAF;
        private String Fecha;
        private String EPC;
        private String Numero;
        private String Nombre;
        private String Descripcion;

        public String getId() {
            return Id;
        }

        public void setId(String id) {
            Id = id;
        }

        public String getIdCAF() {
            return IdCAF;
        }

        public void setIdCAF(String idCAF) {
            IdCAF = idCAF;
        }

        public String getFecha() {
            return Fecha;
        }

        public void setFecha(String fecha) {
            Fecha = fecha;
        }

        public String getEPC() {
            return EPC;
        }

        public void setEPC(String EPC) {
            this.EPC = EPC;
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

    @Override
    public void onBackPressed() {
        mOnBackPressed();
    }

    private void mOnBackPressed(){
        switch (GlobalPreferences.PAGE_STATE){
            case GlobalPreferences.PAGE_STATE_PROCESING:
                Toast.makeText(this, "Por favor, espere...", Toast.LENGTH_SHORT).show();
                break;
            case PAGE_STATE_SETTING_UBICATION:
                this.finish();
                break;
            case PAGE_STATE_INVENTORY:
                GlobalPreferences.PAGE_STATE = PAGE_STATE_SETTING_UBICATION;
                PanelUbicacion.setVisibility(View.VISIBLE);
                break;
            case GlobalPreferences.PAGE_STATE_DETAILS:
                GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_INVENTORY;
                PanelDetalle.setVisibility(View.GONE);
                break;
            case GlobalPreferences.PAGE_STATE_SEARCHING:
                GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_DETAILS;
                Buscador.setVisibility(View.GONE);
                break;
            case GlobalPreferences.PAGE_STATE_INCIDENCE_FOUND:
                GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_INVENTORY;
                getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag("ControlIncidencias")).commit();
                FragmentHolder.setVisibility(View.GONE);
                AlertLayout.setVisibility(View.GONE);
                break;
            default:
                conter = 0;
        }
    }

}