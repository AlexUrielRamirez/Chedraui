package com.SCAF.CAFv2.Administracion.Traspasos;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.uhf.uhf.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

import static android.app.Activity.RESULT_OK;
import static com.Etiflex.Splash.GlobalPreferences.CODE_BAR_READER;

public class admin_traspasos extends Fragment {

    public admin_traspasos() {}

    public static admin_traspasos newInstance() {
        admin_traspasos fragment = new admin_traspasos();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private RecyclerView rv_traspasos;
    private ArrayList<ModelTraspasos> main_list_traspasos;

    //Busqueda
    public static ConstraintLayout Panel_crear, Panel_carga_buscando;
    public static RelativeLayout HolderCamera;
    public static ZXingScannerView scanner;
    public EditText et_buscador_activo;
    public RecyclerView rv_busqueda;
    public boolean SearchingIsAboutToStart = false;
    private String search_key;
    final Handler handler = new Handler();
    boolean should_search = true;
    final Runnable runnable = () -> {
        searchItem(search_key, "1");
        SearchingIsAboutToStart = false;
    };
    public ImageView more;
    public ArrayList<ModelSearchResult> main_list_search;
    private PopupMenu menu_cedis, menu_area, menu_oficinas;
    private ArrayList<ModelUbicaciones> main_list_cedis, main_list_areas, main_list_oficinas;
    private String IdCedis, IdArea = "0", IdOficina = "0";
    private ProgressDialog pd_downloading;
    private TextView txt_error;

    private interface api_network_get_ubications{
        @FormUrlEncoded
        @POST("/getUbicaciones.php")
        void setData(
                @Field("Ubicaciones") String Ubicaciones,
                Callback<Response> callback
        );
    }
    private interface api_network_get_search_result{
        @FormUrlEncoded
        @POST("/getSearchResult.php")
        void setData(
                @Field("key") String Key,
                @Field("Type") String Type,
                Callback<Response> callback
        );
    }
    private interface api_network_insert_traspaso{
        @FormUrlEncoded
        @POST("/InsertTraspaso.php")
        void setData(
                @Field("data") String Data,
                Callback<Response> callback
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_traspasos, container, false);
        initViews(view);
        getData();
        getCedis();
        return view;
    }

    private void initViews(View view) {
        pd_downloading = new ProgressDialog(getContext());
        pd_downloading.setMessage("Obteniendo información");
        rv_traspasos = view.findViewById(R.id.rv_traspasos);
        rv_traspasos.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        rv_busqueda = view.findViewById(R.id.rv_busqueda);
        rv_busqueda.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        Panel_crear = view.findViewById(R.id.Panel_crear);
        Panel_carga_buscando = view.findViewById(R.id.PanelCargaBuscando);
        HolderCamera = view.findViewById(R.id.HolderCamera);
        view.findViewById(R.id.btn_add_traspaso).setOnClickListener(v->{
            Panel_crear.setVisibility(View.VISIBLE);
        });
        et_buscador_activo = view.findViewById(R.id.et_buscador_activo);
        txt_error = view.findViewById(R.id.txt_error);
        et_buscador_activo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(should_search){
                    if(SearchingIsAboutToStart){
                        handler.removeCallbacks(runnable);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                SearchingIsAboutToStart = true;
                search_key = s.toString();
                handler.postDelayed(runnable, 1000);
            }
        });
        more = view.findViewById(R.id.btn_menu_buscar);
        more.setOnClickListener(v->{
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.etiflex.sdl", "com.zebra.sdl.SDLguiActivity"));
            startActivityForResult(intent, CODE_BAR_READER);
        });
    }

    private void searchItem(String key, String Type){
        Panel_carga_buscando.setVisibility(View.VISIBLE);
        new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmannCAF/webservices/AdministracionTraspasos").build().create(api_network_get_search_result.class).setData(key, Type, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                try {
                    processResult(new BufferedReader(new InputStreamReader(response.getBody().in())).readLine(), Type);
                }catch (IOException e){
                    Log.e("admin_traspasos", "Error de respuesta->"+e.getMessage());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("admin_traspasos", "Error de conexión:"+error.getMessage());
            }
        });
    }

    private void processResult(String data, String Type){
        main_list_search = new ArrayList<>();
        switch (Type){
            case "1":
                try {
                    JSONObject data_json = new JSONObject(data);
                    JSONArray json_ubications = data_json.optJSONArray("Data");
                    for (int i = 0; i < json_ubications.length(); i++) {
                        JSONObject jsonObject = json_ubications.getJSONObject(i);
                        ModelSearchResult model = new ModelSearchResult();
                        model.setIdCAF(jsonObject.optString("IdCAF"));
                        model.setNumero(jsonObject.optString("Numero"));
                        model.setNombre(jsonObject.optString("Nombre"));
                        model.setDescripcion(jsonObject.optString("Descripcion"));
                        model.setEPC(jsonObject.optString("EPC"));
                        model.setUbicacion(jsonObject.optString("Ubicacion"));
                        model.setIdArea(jsonObject.optString("IdArea"));
                        model.setArea(jsonObject.optString("Area"));
                        model.setIdOficina(jsonObject.optString("IdOficina"));
                        model.setOficina(jsonObject.optString("Oficina"));

                        main_list_search.add(model);
                    }
                }catch (JSONException e){
                    Log.e("admin_traspasos", "Error type 1: " + e.getMessage());
                }
                break;
            case "2":
                try {
                    JSONObject data_json = new JSONObject(data);
                    ModelSearchResult model = new ModelSearchResult();
                    model.setIdCAF(data_json.optString("IdCAF"));
                    model.setNumero(data_json.optString("Numero"));
                    model.setNombre(data_json.optString("Nombre"));
                    model.setDescripcion(data_json.optString("Descripcion"));
                    model.setEPC(data_json.optString("EPC"));
                    model.setUbicacion(data_json.optString("Ubicacion"));
                    model.setIdArea(data_json.optString("IdArea"));
                    model.setArea(data_json.optString("Area"));
                    model.setIdOficina(data_json.optString("IdOficina"));
                    model.setOficina(data_json.optString("Oficina"));

                    main_list_search.add(model);
                }catch (JSONException e){
                    Log.e("admin_traspasos", "Error type 2: " + e.getMessage());
                }
                break;
        }
        rv_busqueda.setAdapter(new rv_search_adapter(main_list_search));
        new Handler().postDelayed(() -> Panel_carga_buscando.setVisibility(View.GONE), 1000);
    }

    private void getData(){
        pd_downloading.show();
        txt_error.setVisibility(View.GONE);
        main_list_traspasos = new ArrayList<>();
        Volley.newRequestQueue(getContext()).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmannCAF/webservices/AdministracionTraspasos/getTraspasos.php", null, response -> {
            JSONArray json = response.optJSONArray("Data");
            try {
                for (int i = 0; i < json.length(); i++) {
                    ModelTraspasos model = new ModelTraspasos();
                    JSONObject jsonObject = null;
                    jsonObject = json.getJSONObject(i);

                    model.setIdTraspaso(jsonObject.optString("IdIncidencia"));
                    model.setIdCAF(jsonObject.optString("IdCAF"));
                    model.setEPC(jsonObject.optString("EPC"));
                    model.setUbicaciones(jsonObject.optString("Ubicaciones"));
                    model.setNumero(jsonObject.optString("NumeroActivo"));
                    model.setNombre(jsonObject.optString("NombreActivo"));
                    model.setDescripcion(jsonObject.optString("DescripcionActivo"));

                    main_list_traspasos.add(model);
                    pd_downloading.dismiss();

                }
                rv_traspasos.setAdapter(new rv_adapter(main_list_traspasos));
            } catch (JSONException | NullPointerException e) {
                pd_downloading.dismiss();
                Toast.makeText(getContext(), "No hay información para mostrar", Toast.LENGTH_SHORT).show();
                txt_error.setVisibility(View.VISIBLE);
                Log.e("Validacion", "JSON | Null Exception" + e);
            }

        }, error -> {
            pd_downloading.dismiss();
            txt_error.setVisibility(View.VISIBLE);
            Log.e("Validacion", "Volley error" + error);
        }));
    }

    public class rv_adapter extends RecyclerView.Adapter<rv_adapter.ViewHolder> implements View.OnClickListener{
        Context context;
        private ArrayList<ModelTraspasos> child_list;
        public rv_adapter(ArrayList<ModelTraspasos> list){
            child_list = list;
        }
        @Override
        public rv_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_admin_traspasos,parent,false);
            view.setOnClickListener(this);
            context = parent.getContext();
            return new rv_adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(rv_adapter.ViewHolder holder, int position) {
            Glide.with(context).load(GlobalPreferences.URL+"/HellmannCAF/assets/Activo/" + child_list.get(position).getNumero()).placeholder(R.drawable.empty_photo).override(140).into(holder.img_activo);
            holder.txtNombreActivo.setText(child_list.get(position).getNombre());
            holder.txtDescripcionActivo.setText(child_list.get(position).getDescripcion());
            holder.txtNumeroActivo.setText("Número de activo: " + child_list.get(position).getNumero());
            new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmannCAF/webservices/AdministracionTraspasos").build().create(api_network_get_ubications.class).setData(child_list.get(position).getUbicaciones(), new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    try {
                        JSONObject data_json = new JSONObject(new BufferedReader(new InputStreamReader(response.getBody().in())).readLine());
                        JSONArray json_ubications = data_json.optJSONArray("Data");
                        String ubicaciones = null;
                        for (int i = 0; i < json_ubications.length(); i++) {
                            JSONObject jsonObject = json_ubications.getJSONObject(i);
                            if(i == 0){
                                ubicaciones = "·  " + jsonObject.optString("NombreArea") + " > " + jsonObject.optString("NombreOficina");
                            }else if(i >= 1){
                                ubicaciones = ubicaciones + "\n·  \n·  \n·  " + jsonObject.optString("NombreArea") + " > " + jsonObject.optString("NombreOficina");
                            }
                        }
                        holder.txtUbicaciones.setText(ubicaciones);

                    }catch (JSONException e){

                    }catch (IOException e){

                    }
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return child_list.size();
        }

        @Override
        public void onClick(View view) {

        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView img_activo;
            TextView txtNombreActivo, txtDescripcionActivo, txtNumeroActivo, txtUbicaciones;
            public ViewHolder(View itemView) {
                super(itemView);
                img_activo = itemView.findViewById(R.id.img_activo);
                txtNombreActivo = itemView.findViewById(R.id.txt_nombre_activo);
                txtDescripcionActivo = itemView.findViewById(R.id.txt_descripcion_activo);
                txtNumeroActivo = itemView.findViewById(R.id.txt_numero_activo);
                txtUbicaciones = itemView.findViewById(R.id.txt_ubicaciones);
            }
        }
    }

    public class rv_search_adapter extends RecyclerView.Adapter<rv_search_adapter.ViewHolder> implements View.OnClickListener{
        Context context;
        private ArrayList<ModelSearchResult> child_list;
        public rv_search_adapter(ArrayList<ModelSearchResult> list){
            child_list = list;
        }
        @Override
        public rv_search_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_admin_traspasos,parent,false);
            view.setOnClickListener(this);
            context = parent.getContext();
            return new rv_search_adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(rv_search_adapter.ViewHolder holder, int position) {
            holder.item.setOnClickListener(v->{
                BottomSheetDialog bsd = new BottomSheetDialog(context);
                bsd.setContentView(R.layout.bsd_traspaso);
                TextView spinnerCedis = bsd.findViewById(R.id.spinner_Cedis);
                TextView spinnerArea = bsd.findViewById(R.id.spinner_Departamento);
                TextView spinnerOficina = bsd.findViewById(R.id.spinner_Oficina);

                ProgressBar pb_loading_departamentos = bsd.findViewById(R.id.pb_loading_departamento);
                ProgressBar pb_loading_ofices = bsd.findViewById(R.id.pb_loading_ofices);

                menu_cedis = new PopupMenu(getContext(), spinnerCedis);
                for(int i = 0; i < main_list_cedis.size(); i++){
                    menu_cedis.getMenu().add(main_list_cedis.get(i).getNombre());
                }
                menu_cedis.setOnMenuItemClickListener(item -> {
                    spinnerCedis.setText(item.getTitle());
                    spinnerArea.setText("Departamento");
                    spinnerOficina.setText("Oficina");
                    for(int i = 0; i<main_list_cedis.size(); i++){
                        if(main_list_cedis.get(i).getNombre().equals(item.getTitle())){
                            IdArea = "0";
                            IdOficina = "0";
                            IdCedis = main_list_cedis.get(i).getId();
                            main_list_areas = new ArrayList<>();
                            menu_area = new PopupMenu(context, spinnerArea);
                            Volley.newRequestQueue(context).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmannCAF/webservices/Loaders/getAreas.php?IdCedis="+IdCedis, null, response -> {
                                JSONArray json = response.optJSONArray("Data");
                                pb_loading_departamentos.setVisibility(View.VISIBLE);
                                try {
                                    for (int i_2 = 0; i_2 < json.length(); i_2++) {
                                        ModelUbicaciones model = new ModelUbicaciones();
                                        JSONObject jsonObject = json.getJSONObject(i_2);
                                        model.setId(jsonObject.optString("Id"));
                                        model.setNombre(jsonObject.optString("Nombre"));

                                        menu_area.getMenu().add(model.getNombre());
                                        main_list_areas.add(model);
                                        pb_loading_departamentos.setVisibility(View.GONE);

                                    }
                                    menu_area.setOnMenuItemClickListener(item2 -> {
                                        spinnerArea.setText(item2.getTitle());
                                        spinnerOficina.setText("Oficina");
                                        for(int i3 = 0; i3<main_list_areas.size(); i3++){
                                            if(main_list_areas.get(i3).getNombre().equals(item2.getTitle())){
                                                IdOficina = "0";
                                                IdArea = main_list_areas.get(i3).getId();
                                                main_list_oficinas = new ArrayList<>();
                                                menu_oficinas = new PopupMenu(context, spinnerOficina);
                                                Volley.newRequestQueue(context).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmannCAF/webservices/Loaders/getOficinas.php?IdArea="+IdArea, null, response2 -> {
                                                    JSONArray json2 = response2.optJSONArray("Data");
                                                    pb_loading_ofices.setVisibility(View.VISIBLE);
                                                    try {
                                                        for (int i_2 = 0; i_2 < json2.length(); i_2++) {
                                                            ModelUbicaciones model = new ModelUbicaciones();
                                                            JSONObject jsonObject = json2.getJSONObject(i_2);
                                                            model.setId(jsonObject.optString("Id"));
                                                            model.setNombre(jsonObject.optString("Nombre"));

                                                            menu_oficinas.getMenu().add(model.getNombre());
                                                            main_list_oficinas.add(model);
                                                        }

                                                        menu_oficinas.setOnMenuItemClickListener(item3 -> {
                                                            spinnerOficina.setText(item3.getTitle());
                                                            for(int i4 = 0; i4<main_list_oficinas.size(); i4++){
                                                                if(main_list_oficinas.get(i4).getNombre().equals(item3.getTitle())){
                                                                    IdOficina = main_list_oficinas.get(i4).getId();
                                                                }
                                                            }
                                                            return false;
                                                        });
                                                        pb_loading_ofices.setVisibility(View.GONE);
                                                    } catch (JSONException | NullPointerException e) {
                                                        pb_loading_ofices.setVisibility(View.GONE);
                                                        Toast.makeText(context, "No hay conexión con el servidor", Toast.LENGTH_SHORT).show();
                                                        Log.e("Validacion", "JSON | Null Exception" + e);
                                                    }

                                                }, error -> {
                                                    pb_loading_ofices.setVisibility(View.GONE);
                                                    Toast.makeText(context, "Error, no hay conexión con el servidor", Toast.LENGTH_SHORT).show();
                                                    Log.e("Validacion", "Volley error" + error);
                                                }));
                                                break;
                                            }
                                        }
                                        return false;
                                    });
                                    pb_loading_departamentos.setVisibility(View.GONE);
                                } catch (JSONException | NullPointerException e) {
                                    pb_loading_departamentos.setVisibility(View.GONE);
                                    Toast.makeText(context, "No hay conexión al servidor", Toast.LENGTH_SHORT).show();
                                    Log.e("Validacion", "JSON | Null Exception" + e);
                                }

                            }, error -> {
                                pb_loading_departamentos.setVisibility(View.GONE);
                                Toast.makeText(context, "Error, No hay conexión al servidor", Toast.LENGTH_SHORT).show();
                                Log.e("Validacion", "Volley error" + error);
                            }));
                            break;
                        }
                    }
                    return false;
                });
                spinnerCedis.setOnClickListener(v1 -> menu_cedis.show());
                spinnerArea.setOnClickListener(v1->{
                    try{
                        menu_area.show();
                    }catch (NullPointerException e){}
                });
                spinnerOficina.setOnClickListener(v1->{
                    try {
                        menu_oficinas.show();
                    }catch (NullPointerException e){}
                });
                ProgressDialog pd = new ProgressDialog(context);
                pd.setMessage("Actualizando registros...");
                bsd.findViewById(R.id.btn_aplicar_cambios).setOnClickListener(v1->{
                    bsd.findViewById(R.id.txt_error).setVisibility(View.GONE);
                    if(!IdArea.equals("0") || !IdOficina.equals("0")){
                        if(!IdOficina.equals(child_list.get(position).getIdOficina())){
                            pd.show();
                            String data = "{\"Cedis\":\""+IdCedis+"\", \"Area\":\""+IdArea+"\", \"Oficina\":\""+IdOficina+"\", \"IdCAF\":\""+child_list.get(position).getIdCAF()+"\"}";
                            new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmannCAF/webservices/AdministracionTraspasos").build().create(api_network_insert_traspaso.class).setData(data, new Callback<Response>() {
                                @Override
                                public void success(Response response, Response response2) {
                                    GlobalPreferences.mHistorial.GuardarHistorico(GlobalPreferences.ID_CEDIS, GlobalPreferences.ID_USUARIO, GlobalPreferences.HISTORIAL_TIPO_TRASPASOS, child_list.get(position).getIdCAF());
                                    pd.dismiss();
                                    bsd.dismiss();
                                    Panel_crear.setVisibility(View.GONE);
                                    should_search = false;
                                    et_buscador_activo.setText("");
                                    should_search = true;
                                    getData();
                                    Toast.makeText(context, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    pd.dismiss();
                                    Toast.makeText(context, "Error, no hay conexión con el servidor", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else{
                            bsd.findViewById(R.id.txt_error).setVisibility(View.VISIBLE);
                        }
                    }else{
                        Toast.makeText(context, "Seleccione una ubicación válida", Toast.LENGTH_SHORT).show();
                    }
                });
                bsd.show();
            });
            Glide.with(context).load(GlobalPreferences.URL+"/HellmannCAF/assets/Activo/" + child_list.get(position).getNumero()).placeholder(R.drawable.empty_photo).override(140).into(holder.img_activo);
            holder.txtNombreActivo.setText(child_list.get(position).getNombre());
            holder.txtDescripcionActivo.setText(child_list.get(position).getDescripcion());
            holder.txtNumeroActivo.setText("Número de activo: " + child_list.get(position).getNumero());
            holder.txtUbicaciones.setText(child_list.get(position).getArea() + " > " + child_list.get(position).getOficina());
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
            TextView txtNombreActivo, txtDescripcionActivo, txtNumeroActivo, txtUbicaciones;
            public ViewHolder(View itemView) {
                super(itemView);
                item = itemView.findViewById(R.id.item);
                img_activo = itemView.findViewById(R.id.img_activo);
                txtNombreActivo = itemView.findViewById(R.id.txt_nombre_activo);
                txtDescripcionActivo = itemView.findViewById(R.id.txt_descripcion_activo);
                txtNumeroActivo = itemView.findViewById(R.id.txt_numero_activo);
                txtUbicaciones = itemView.findViewById(R.id.txt_ubicaciones);
            }
        }
    }

    private void getCedis() {
        main_list_cedis = new ArrayList<>();
        Volley.newRequestQueue(getContext()).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmannCAF/webservices/Loaders/getCedis.php", null, response -> {
            JSONArray json = response.optJSONArray("Data");

            try {
                for (int i = 0; i < json.length(); i++) {
                    ModelUbicaciones model = new ModelUbicaciones();
                    JSONObject jsonObject = null;
                    jsonObject = json.getJSONObject(i);
                    model.setId(jsonObject.optString("Id"));
                    model.setNombre(jsonObject.optString("Nombre"));
                    main_list_cedis.add(model);
                }

            } catch (JSONException | NullPointerException e) {
                Log.e("Validacion", "JSON | Null Exception" + e);
            }

        }, error -> {
            Log.e("Validacion", "Volley error" + error);
        }));
    }

    private void getAreas() {
        main_list_areas = new ArrayList<>();
        Volley.newRequestQueue(getContext()).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmannCAF/webservices/Loaders/getAreas.php?IdCedis="+GlobalPreferences.ID_CEDIS, null, response -> {
            JSONArray json = response.optJSONArray("Data");

            try {
                for (int i = 0; i < json.length(); i++) {
                    ModelUbicaciones model = new ModelUbicaciones();
                    JSONObject jsonObject = null;
                    jsonObject = json.getJSONObject(i);
                    model.setId(jsonObject.optString("Id"));
                    model.setNombre(jsonObject.optString("Nombre"));

                    
                    main_list_areas.add(model);

                }

            } catch (JSONException | NullPointerException e) {
                Log.e("Validacion", "JSON | Null Exception" + e);
            }

        }, error -> {
            Log.e("Validacion", "Volley error" + error);
        }));
    }
    
    private class ModelTraspasos{
        private String IdTraspaso;
        private String IdCAF;
        private String EPC;
        private String Ubicaciones;
        private String Numero;
        private String Nombre;
        private String Descripcion;

        public String getIdTraspaso() {
            return IdTraspaso;
        }

        public void setIdTraspaso(String idTraspaso) {
            IdTraspaso = idTraspaso;
        }

        public String getIdCAF() {
            return IdCAF;
        }

        public void setIdCAF(String idCAF) {
            IdCAF = idCAF;
        }

        public String getEPC() {
            return EPC;
        }

        public void setEPC(String EPC) {
            this.EPC = EPC;
        }

        public String getUbicaciones() {
            return Ubicaciones;
        }

        public void setUbicaciones(String ubicaciones) {
            Ubicaciones = ubicaciones;
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

    private class ModelSearchResult{
        private String IdCAF;
        private String Numero;
        private String Nombre;
        private String Descripcion;
        private String EPC;
        private String Ubicacion;
        private String IdArea;
        private String Area;
        private String IdOficina;
        private String Oficina;

        public String getIdCAF() {
            return IdCAF;
        }

        public void setIdCAF(String idCAF) {
            IdCAF = idCAF;
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

        public String getEPC() {
            return EPC;
        }

        public void setEPC(String EPC) {
            this.EPC = EPC;
        }

        public String getUbicacion() {
            return Ubicacion;
        }

        public void setUbicacion(String ubicacion) {
            Ubicacion = ubicacion;
        }

        public String getIdArea() {
            return IdArea;
        }

        public void setIdArea(String idArea) {
            IdArea = idArea;
        }

        public String getArea() {
            return Area;
        }

        public void setArea(String area) {
            Area = area;
        }

        public String getIdOficina() {
            return IdOficina;
        }

        public void setIdOficina(String idOficina) {
            IdOficina = idOficina;
        }

        public String getOficina() {
            return Oficina;
        }

        public void setOficina(String oficina) {
            Oficina = oficina;
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case CODE_BAR_READER:
                    search_key = data.getDataString();
                    et_buscador_activo.setText(data.getDataString());
                    break;
            }
        }
    }
}