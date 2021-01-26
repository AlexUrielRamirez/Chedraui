package com.Hellman.CAFv2.Administracion.Ubicaciones;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Etiflex.Splash.GlobalPreferences;
import com.Hellman.CAFv2.Inventario.Main;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.uhf.uhf.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public class admin_ubicaciones extends Fragment {

    public admin_ubicaciones() {}

    public static admin_ubicaciones newInstance() {
        return new admin_ubicaciones();
    }

    public ImageView btn_add_ubicacion;

    public ArrayList<ModelUbicaciones> main_list_areas, main_list_oficinas;
    public RecyclerView rv_areas, rv_oficinas;
    public static RelativeLayout Holder_rv_oficinas;

    public static TextView txt_indicador_area, txt_indicador;

    public ConstraintLayout Panel_loading_oficinas;

    String ChildIdArea = null, ChildTXTJson = null;
    ProgressDialog progressDialog;

    private interface api_network_create_ubicacion{
        @FormUrlEncoded
        @POST("/createUbication.php")
        void setData(
                @Field("data") String data,
                Callback<Response> callback
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_ubicaciones, container, false);
        initViews(view);
        getAreas();
        return view;
    }

    private void initViews(View view) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        btn_add_ubicacion = view.findViewById(R.id.btn_add_ubicacion);
        txt_indicador = view.findViewById(R.id.textView19);
        rv_areas = view.findViewById(R.id.rv_areas);
        rv_oficinas = view.findViewById(R.id.rv_oficinas);
        Holder_rv_oficinas = view.findViewById(R.id.Holder_rv_oficinas);
        txt_indicador_area = view.findViewById(R.id.txt_indicador_area);
        Panel_loading_oficinas = view.findViewById(R.id.Panel_loading_oficinas);
        setUpViews();
    }

    private void setUpViews() {
        progressDialog.setMessage("Por favor, espere...");
        btn_add_ubicacion.setOnClickListener(v->{
            PopupMenu menu_area = new PopupMenu(getContext(), btn_add_ubicacion);
            menu_area.getMenu().add("Añadir Departamento");
            menu_area.getMenu().add("Añadir Oficina");
            menu_area.setOnMenuItemClickListener(item -> {
                switch (item.getTitle().toString()){
                    case "Añadir Departamento":
                        setUpAddingBSD(1);
                        break;
                    case "Añadir Oficina":
                        setUpAddingBSD(2);
                        break;
                }
                return false;
            });
            menu_area.show();
        });
        rv_areas.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        rv_oficinas.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
    }

    private void setUpAddingBSD(int UbicationType){
        BottomSheetDialog bsd = new BottomSheetDialog(getContext());
        bsd.setContentView(R.layout.add_ubicacion);

        PopupMenu areas_menu;
        TextView txtHeader = bsd.findViewById(R.id.textView22);
        LinearLayout HolderSelectorArea = bsd.findViewById(R.id.holder_selector_area);
        TextView SpinnerDepartamento = bsd.findViewById(R.id.spinner_Departamento);
        EditText etNombreUbicacion = bsd.findViewById(R.id.et_nombre_ubicacion);
        Button btnGuardar = bsd.findViewById(R.id.btn_guardar);

        switch (UbicationType){
            case 1:
                HolderSelectorArea.setVisibility(View.GONE);
                txtHeader.setText("Añadir Departamento");
                break;
            case 2:
                txtHeader.setText("Añadir Oficina");
                areas_menu = new PopupMenu(getContext(), SpinnerDepartamento);
                for(int position = 0; position < main_list_areas.size(); position++){
                    areas_menu.getMenu().add(main_list_areas.get(position).getNombre());
                }
                areas_menu.setOnMenuItemClickListener(item -> {
                    SpinnerDepartamento.setText(item.getTitle());
                    for(int i = 0; i<main_list_areas.size(); i++){
                        if(main_list_areas.get(i).getNombre().equals(item.getTitle())){
                            ChildIdArea = main_list_areas.get(i).getId();
                        }
                    }
                    return false;
                });
                SpinnerDepartamento.setOnClickListener(v->{
                    areas_menu.show();
                });
                break;
        }

        btnGuardar.setOnClickListener(v->{
            if(etNombreUbicacion.getText().length() > 0){
                if(UbicationType == 2){
                    if(ChildIdArea != null){
                        uploadUbication(UbicationType, etNombreUbicacion.getText().toString(), bsd);
                    }else{
                        Toast.makeText(getContext(), "Por favor, ingrese los datos solicitados", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    uploadUbication(UbicationType, etNombreUbicacion.getText().toString(), bsd);
                }
            }else{
                Toast.makeText(getContext(), "Por favor, ingrese los datos solicitados", Toast.LENGTH_SHORT).show();
            }
        });

        bsd.show();
    }

    private void uploadUbication(int UbicationType, String Name, BottomSheetDialog dialog){
        progressDialog.show();
        switch (UbicationType){
            case 1:
                ChildTXTJson = "{" +
                        "\"UbicationType\":\"1\"," +
                        "\"Name\":\""+Name+"\"" +
                        "}";
                break;
            case 2:
                ChildTXTJson = "{" +
                        "\"UbicationType\":\"2\"," +
                        "\"IdArea\":\""+ChildIdArea+"\"," +
                        "\"Name\":\""+Name+"\"" +
                        "}";
                break;
        }
        new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmanCAF/webservices/AdministracionUbicaciones/").build().create(api_network_create_ubicacion.class).setData(ChildTXTJson, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                try{
                    if(new BufferedReader(new InputStreamReader(response.getBody().in())).readLine().equals("succes")){
                        switch (UbicationType){
                            case 1:
                                ChildIdArea = null;
                                ChildTXTJson = null;
                                Toast.makeText(getContext(), "Area ingresada corrctamente", Toast.LENGTH_SHORT).show();
                                getAreas();
                                break;
                            case 2:
                                Toast.makeText(getContext(), "Oficina ingresada corrctamente", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        dialog.dismiss();
                        progressDialog.dismiss();
                    }else{
                        Toast.makeText(getContext(), "Error de base de datos, contacte a un desarrollador", Toast.LENGTH_SHORT).show();
                    }
                }catch (IOException e){
                    Toast.makeText(getContext(), "Error de lectura, contacte a un desarrollador", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getContext(), "Error, revise su conexión a internet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAreas() {
        Panel_loading_oficinas.setVisibility(View.VISIBLE);
        main_list_areas = new ArrayList<>();
        Volley.newRequestQueue(getContext()).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmanCAF/webservices/Loaders/getAreas.php?IdCedis="+ GlobalPreferences.ID_CEDIS, null, response -> {
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
                rv_adapter areas_adapter = new rv_adapter(main_list_areas, 1);
                rv_areas.setAdapter(areas_adapter);
                Panel_loading_oficinas.setVisibility(View.GONE);
            } catch (JSONException | NullPointerException e) {
                Log.e("Validacion", "JSON | Null Exception" + e);
            }

        }, error -> {
            Panel_loading_oficinas.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error, no hay conexión con el servidor", Toast.LENGTH_SHORT).show();
            Log.e("Validacion", "Volley error" + error);
        }));
    }

    private void getOficinas(String IdArea) {
        main_list_oficinas = new ArrayList<>();
        Volley.newRequestQueue(getContext()).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmanCAF/webservices/Loaders/getOficinas.php?IdArea="+IdArea, null, response -> {
            JSONArray json = response.optJSONArray("Data");

            try {
                for (int i = 0; i < json.length(); i++) {
                    ModelUbicaciones model = new ModelUbicaciones();
                    JSONObject jsonObject = null;
                    jsonObject = json.getJSONObject(i);
                    model.setId(jsonObject.optString("Id"));
                    model.setNombre(jsonObject.optString("Nombre"));

                    main_list_oficinas.add(model);

                }
                rv_adapter oficinas_adapter = new rv_adapter(main_list_oficinas, 2);
                rv_oficinas.setAdapter(oficinas_adapter);
                new Handler().postDelayed(() -> {
                    Panel_loading_oficinas.setVisibility(View.GONE);
                }, 1500);

            } catch (JSONException | NullPointerException e) {
                Log.e("Validacion", "JSON | Null Exception" + e);
            }

        }, error -> {
            Log.e("Validacion", "Volley error" + error);
        }));
    }

    public class rv_adapter extends RecyclerView.Adapter<rv_adapter.ViewHolder> implements View.OnClickListener{
        private View.OnClickListener listener;
        Context context;
        private ArrayList<ModelUbicaciones> child_list;
        private int AdapterType;
        public rv_adapter(ArrayList<ModelUbicaciones> list, int AdapterType){
            child_list = list;
            this.AdapterType = AdapterType;
        }
        @Override
        public rv_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incidences,parent,false);
            view.setOnClickListener(this);
            context = parent.getContext();
            return new rv_adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(rv_adapter.ViewHolder holder, int position) {
            holder.txt_nombre.setText(child_list.get(position).getNombre());
            switch (AdapterType){
                case 1:
                    holder.letter.setBackgroundColor(getContext().getColor(R.color.green_2));
                    holder.letter.setText("D");
                    holder.item.setOnClickListener(v->{
                        txt_indicador_area.setText(child_list.get(position).getNombre() + " >");
                        txt_indicador_area.setVisibility(View.VISIBLE);
                        Holder_rv_oficinas.setVisibility(View.VISIBLE);
                        Panel_loading_oficinas.setVisibility(View.VISIBLE);
                        getOficinas(child_list.get(position).getId());
                    });
                    break;
                case 2:
                    holder.letter.setBackgroundColor(getContext().getColor(R.color.menu_purple));
                    holder.letter.setText("O");
                    holder.item.setOnClickListener(v->{
                        Toast.makeText(context, "Oficina activa", Toast.LENGTH_SHORT).show();
                    });
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return child_list.size();
        }

        @Override
        public void onClick(View view) {

        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            CardView item;
            TextView letter, txt_nombre;
            public ViewHolder(View itemView) {
                super(itemView);
                item = itemView.findViewById(R.id.item);
                txt_nombre = itemView.findViewById(R.id.txt_nombre);
                letter = itemView.findViewById(R.id.textView21);
            }
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

}