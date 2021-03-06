package com.SCAF.CAFv2.Administracion.Impresion;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Etiflex.Splash.GlobalPreferences;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.uhf.uhf.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

import static android.app.Activity.RESULT_OK;

public class admin_impresion extends Fragment {

    public admin_impresion() {}

    public static admin_impresion newInstance() {
        admin_impresion fragment = new admin_impresion();
        return fragment;
    }

    public static ConstraintLayout Panel_etiquetas;
    private ConstraintLayout Panel_loading;

    //Ubicación
    private TextView Spinner_Departamento, Spinner_Oficina, txt_cedis_actual, btn_continuar;
    private PopupMenu menu_area, menu_oficinas;
    private ArrayList<ModelUbicaciones> main_list_areas, main_list_oficinas;
    private String IdArea = "0", IdOficina = "0";
    private ProgressBar pb_loading_ofices;

    //Panel loading
    private TextView txt_progress;

    //General Data
    private RecyclerView rv_tags_papel, rv_tags_metal;
    private ArrayList<ModelTags> main_list, main_list_metal;
    private Button btn_print;

    //Printing
    private boolean HasPrintableTags = false;
    private interface api_network_update{
        @FormUrlEncoded
        @POST("/updateIndices.php")
        void setData(
                @Field("IDS") String IDS,
                Callback<Response> response
        );
    }

    //MetalTagsTools
    private final int CODE_BAR_FOR_METAL = 250;
    public String CurrentMetalNumber, CurrentIdCAF;
    public int CURRENT_METAL_TAG_POSITION = 0;
    interface api_network_update_tag{
        @FormUrlEncoded
        @POST("/update_metal_tag.php")
        void setData(
                @Field("IdCAF") String IdCAF,
                @Field("EPC") String EPC,
                Callback<Response> callback
        );
    }

    //Tabs
    private TabLayout tabs;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_impresion, container, false);
        initViews(view);
        getAreas();
        return view;
    }

    private void initViews(View view) {
        tabs = view.findViewById(R.id.tabs_impresion);
        Panel_etiquetas = view.findViewById(R.id.Panel_etiquetas);
        Panel_loading = view.findViewById(R.id.Panel_loading);
        Spinner_Departamento = view.findViewById(R.id.spinner_Departamento);
        Spinner_Oficina = view.findViewById(R.id.spinner_Oficina);
        txt_cedis_actual = view.findViewById(R.id.txt_cedis_actual);
        pb_loading_ofices = view.findViewById(R.id.pb_loading_ofices);
        btn_continuar = view.findViewById(R.id.btn_continuar);
        txt_progress = view.findViewById(R.id.txt_progress);
        rv_tags_papel = view.findViewById(R.id.rv_tags_papel);
        rv_tags_metal = view.findViewById(R.id.rv_tags_metal);
        btn_print = view.findViewById(R.id.btn_imprimir);
        tabs.addTab(tabs.newTab().setText("PAPEL"));
        tabs.addTab(tabs.newTab().setText("METAL"));
        tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        rv_tags_papel.setVisibility(View.VISIBLE);
                        btn_print.setVisibility(View.VISIBLE);
                        rv_tags_metal.setVisibility(View.GONE);
                        break;
                    case 1:
                        rv_tags_papel.setVisibility(View.GONE);
                        btn_print.setVisibility(View.GONE);
                        rv_tags_metal.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        txt_cedis_actual.setText("Cedis actual: " + GlobalPreferences.NOMBRE_CEDIS);
        Spinner_Departamento.setOnClickListener(v->{
            menu_area.show();
        });
        Spinner_Oficina.setOnClickListener(v->{
            if(IdArea.equals("0"))
                Toast.makeText(getContext(), "Primero seleccione un departamento", Toast.LENGTH_SHORT).show();
            else
                menu_oficinas.show();
        });
        btn_continuar.setOnClickListener(v->{
            if(!IdOficina.equals("0")){
                Panel_loading.setVisibility(View.VISIBLE);
                getData();
            }else{
                Toast.makeText(getContext(), "Seleccione una ubicación válida", Toast.LENGTH_SHORT).show();
            }
        });
        rv_tags_papel.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        rv_tags_metal.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        btn_print.setOnClickListener(v->{
            txt_progress.setText("Analizando registros...");
            Panel_loading.setVisibility(View.VISIBLE);
            new printTags().execute(main_list);
        });
    }

    private void getAreas() {
        main_list_areas = new ArrayList<>();
        menu_area = new PopupMenu(getContext(), Spinner_Departamento);
        Volley.newRequestQueue(getContext()).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmannCAF/webservices/Loaders/getAreas.php?IdCedis="+GlobalPreferences.ID_CEDIS, null, response -> {
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
        menu_oficinas = new PopupMenu(getContext(), Spinner_Oficina);
        pb_loading_ofices.setVisibility(View.VISIBLE);
        Volley.newRequestQueue(getContext()).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmannCAF/webservices/Loaders/getOficinas.php?IdArea="+IdArea, null, response -> {
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

    private void getData(){
        txt_progress.setText("Consiguiendo recursos de lectura...");
        Volley.newRequestQueue(getContext()).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmannCAF/webservices/AdministracionImpresion/getData.php?IdArea="+IdArea+"&IdOficina="+IdOficina, null, response -> {
                JSONArray json = response.optJSONArray("Data");
                main_list = new ArrayList<>();
                main_list_metal = new ArrayList<>();
                try {
                    for (int i = 0; i < json.length(); i++) {
                        ModelTags model = new ModelTags();
                        JSONObject jsonObject = null;
                        jsonObject = json.getJSONObject(i);
                        model.setIdCaf(jsonObject.optString("Id"));
                        model.setNumeroActivo(jsonObject.optString("NumeroActivo"));
                        model.setDescripcion(jsonObject.optString("Descripcion"));
                        model.setEPC(jsonObject.optString("EPC"));
                        model.setNombreArea(jsonObject.optString("NombreArea"));
                        model.setNombreOficina(jsonObject.optString("NombreOficina"));
                        model.setTipoActivo(jsonObject.optString("Tipo"));
                        model.setTipoEtiqueta(jsonObject.optString("TipoEtiqueta"));
                        model.setStatus(jsonObject.optString("Status"));
                        model.setPrint(false);
                        switch (model.getTipoEtiqueta()){
                            case "Papel":
                                main_list.add(model);
                                break;
                            case "Metal":
                                main_list_metal.add(model);
                                break;
                        }
                    }

                    rv_tags_papel.setAdapter(new rv_adapter(main_list));
                    rv_tags_metal.setAdapter(new rv_adapter(main_list_metal));
                    Panel_loading.setVisibility(View.GONE);
                    Panel_etiquetas.setVisibility(View.VISIBLE);
                } catch (JSONException | NullPointerException e) {
                    Log.e("Validacion", "JSON | Null Exception" + e.getMessage());
                    Panel_loading.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "No se encontraron resultados válidos", Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                Panel_loading.setVisibility(View.GONE);
                Log.e("Validacion", "Retrofit error: " + error.getMessage());
                Toast.makeText(getContext(), "No se encontraron resultados válidos", Toast.LENGTH_SHORT).show();
        }));
    }

    private class rv_adapter extends RecyclerView.Adapter<rv_adapter.ViewHolder> implements View.OnClickListener{
        Context context;
        private ArrayList<ModelTags> child_list;
        public rv_adapter(ArrayList<ModelTags> list){
            this.child_list = list;
        }
        @Override
        public rv_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_tag,parent,false);
            view.setOnClickListener(this);
            context = parent.getContext();
            return new rv_adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(rv_adapter.ViewHolder holder, int position) {
            Glide.with(context).load(GlobalPreferences.URL+"/HellmannCAF/assets/Activo/" + child_list.get(position).getNumeroActivo()).placeholder(R.drawable.empty_photo).override(240).into(holder.img);
            holder.Descripcion.setText(child_list.get(position).getDescripcion());
            holder.NumeroActivo.setText("EPC: "+child_list.get(position).getEPC());
            if(!child_list.get(position).getStatus().equals("5")){
                holder.status_indicator.setImageDrawable(getContext().getDrawable(R.drawable.done_circle_green));
            }
            holder.item_holder.setOnClickListener(v->{
                switch (child_list.get(position).getTipoEtiqueta()){
                    case "Papel":
                        if(child_list.get(position).isPrint()){
                            holder.indicator_selected.setVisibility(View.GONE);
                            child_list.get(position).setPrint(false);
                        }else{
                            holder.indicator_selected.setVisibility(View.VISIBLE);
                            child_list.get(position).setPrint(true);
                        }
                        break;
                    case "Metal":
                        CurrentIdCAF = child_list.get(position).getIdCaf();
                        CurrentMetalNumber = child_list.get(position).getNumeroActivo();
                        CURRENT_METAL_TAG_POSITION = position;
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("com.etiflex.sdl", "com.zebra.sdl.SDLguiActivity"));
                        startActivityForResult(intent, CODE_BAR_FOR_METAL);
                        break;
                }
            });
        }

        @Override
        public int getItemCount() {
            return child_list.size();
        }

        @Override
        public void onClick(View view) {}

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView img, status_indicator;
            TextView Descripcion, NumeroActivo;
            ConstraintLayout item_holder;
            CardView indicator_selected;
            public ViewHolder(View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.img);
                Descripcion = itemView.findViewById(R.id.descripcion);
                NumeroActivo = itemView.findViewById(R.id.Categoria);
                status_indicator = itemView.findViewById(R.id.status_indicator);
                item_holder = itemView.findViewById(R.id.item_holder);
                indicator_selected = itemView.findViewById(R.id.selected);
            }
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if(data != null){
                ProgressDialog progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage("Actualizando índices,  por favor espere...");
                progressDialog.setCancelable(false);
                BottomSheetDialog bsd = new BottomSheetDialog(getContext());
                bsd.setCancelable(false);
                bsd.setContentView(R.layout.bsd_enlazar_etiqueta);
                TextView txt_accion = bsd.findViewById(R.id.txt_accion);
                String newEPC = data.getDataString().substring(0,24);
                String msg = "Estás a punto de enlazar el EPC:\n"+newEPC+"\nAl activo: "+CurrentMetalNumber+"\n¿Deseas continuar?";
                bsd.findViewById(R.id.btn_continuar).setOnClickListener(v -> {
                    progressDialog.show();
                    new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmannCAF/webservices/AdministracionImpresion/").build().create(api_network_update_tag.class).setData(CurrentIdCAF, newEPC, new Callback<Response>() {
                        @Override
                        public void success(Response response, Response response2) {
                            main_list_metal.get(CURRENT_METAL_TAG_POSITION).setEPC(newEPC);
                            rv_tags_metal.getAdapter().notifyItemChanged(CURRENT_METAL_TAG_POSITION);
                            CURRENT_METAL_TAG_POSITION = 0;
                            CurrentMetalNumber = "";
                            CurrentIdCAF = "";
                            progressDialog.dismiss();
                            bsd.dismiss();
                            Toast.makeText(getContext(), "Cambios realizados correctamente", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(getContext(), "Algo salió  mal, revise su  conexión", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                });
                bsd.findViewById(R.id.btn_cancelar).setOnClickListener(v->{
                    CurrentMetalNumber = "";
                    CurrentIdCAF = "";
                    bsd.dismiss();
                });
                txt_accion.setText(msg);
                bsd.show();
            }else{
                Toast.makeText(getContext(), "No se identificó el EPC a enlazar", Toast.LENGTH_SHORT).show();
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

    private class ModelTags{
        private String IdCaf;
        private String Descripcion;
        private String NumeroActivo;
        private String EPC;
        private String NombreArea;
        private String NombreOficina;
        private String TipoActivo;
        private String TipoEtiqueta;
        private String Status;
        private boolean Print;

        public String getIdCaf() {
            return IdCaf;
        }

        public void setIdCaf(String idCaf) {
            IdCaf = idCaf;
        }

        public String getDescripcion() {
            return Descripcion;
        }

        public void setDescripcion(String descripcion) {
            Descripcion = descripcion;
        }

        public String getNumeroActivo() {
            return NumeroActivo;
        }

        public void setNumeroActivo(String numeroActivo) {
            NumeroActivo = numeroActivo;
        }

        public String getEPC() {
            return EPC;
        }

        public void setEPC(String EPC) {
            this.EPC = EPC;
        }

        public String getNombreArea() {
            return NombreArea;
        }

        public void setNombreArea(String nombreArea) {
            NombreArea = nombreArea;
        }

        public String getNombreOficina() {
            return NombreOficina;
        }

        public void setNombreOficina(String nombreOficina) {
            NombreOficina = nombreOficina;
        }

        public String getTipoActivo() {
            return TipoActivo;
        }

        public void setTipoActivo(String tipoActivo) {
            TipoActivo = tipoActivo;
        }

        public String getTipoEtiqueta() {
            return TipoEtiqueta;
        }

        public void setTipoEtiqueta(String tipoEtiqueta) {
            TipoEtiqueta = tipoEtiqueta;
        }

        public String getStatus() {
            return Status;
        }

        public void setStatus(String status) {
            Status = status;
        }

        public boolean isPrint() {
            return Print;
        }

        public void setPrint(boolean print) {
            Print = print;
        }
    }

    private class printTags extends AsyncTask<ArrayList<ModelTags>, Void, Boolean> {

        private String IdCAFS = "";

        @Override
        protected Boolean doInBackground(ArrayList<ModelTags>... arrayLists) {
            try {
                Socket clientSocket = new Socket(GlobalPreferences.SERVER_PRINTER_IP, 9100);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                String desc_1 = " ",desc_2 = " " ,desc_3 = " " ,desc_4 = " ";
                for (int position = 0; position < arrayLists[0].size(); position++){
                    if(!arrayLists[0].get(position).getStatus().equals("5")){
                        GlobalPreferences.mHistorial.GuardarHistorico(GlobalPreferences.ID_CEDIS, GlobalPreferences.ID_USUARIO, GlobalPreferences.HISTORIAL_TIPO_REIMPRESION, arrayLists[0].get(position).getIdCaf());
                    }
                    if(arrayLists[0].get(position).isPrint()){
                        /*Cortar cadena*/
                        String Descripcion = arrayLists[0].get(position).getDescripcion();
                        int lenght = Descripcion.length();
                        if(lenght <= 50){
                            desc_1 = Descripcion;
                        }else if(lenght > 50 && lenght <=100){
                            desc_1 = Descripcion.substring(0, 50);
                            desc_2 = Descripcion.substring(51);
                        }else if(lenght > 100 && lenght <=150){
                            desc_1 = Descripcion.substring(0, 50);
                            desc_2 = Descripcion.substring(51, 100);
                            desc_3 = Descripcion.substring(101);
                        }
                        String zpl = "^XA\n" +
                                "^RS,,,3,N,,,2\n" +
                                "^RR10\n" +
                                "^XZ\n" +
                                "<xpml><page quantity='0' pitch='24.0 mm'></xpml>^XA\n" +
                                "^SZ2^JMA\n" +
                                "^MCY^PMN\n" +
                                "^PW388\n" +
                                "~JSN\n" +
                                "^JZY\n" +
                                "^LH0,0^LRN\n" +
                                "^XZ\n" +
                                "<xpml></page></xpml><xpml><page quantity='1' pitch='24.0 mm'></xpml>~DGR:SSGFX000.GRF,371,7,:Z64:eJwdkL1KA0EURs9m4y7iuFEsTLEkFhYpIzYjCWGfwEeQdFpOsHCRBEYU7FKKna/gE8iIoBZifIJkJYUWFmIKA1H0bm5xD8P9m+8D74E8GoNryd7o90fg98tjC6HjyEHVEmQQS8FAM4EU71MKLXiVtgjv9gVKhHYKCzbM2zxX4itHhZGg3WRX5uualsybmmrIRpPEq7ImSSpLj3BgwvMUdN2/1wJTushfWTTL0MoVO47Ds6wwcUxV2xulvovrPGvfakMl7VLOaLxNUY4tUVR0bA+sqRmqvZOh1kQ7vtMpe8cl0low+9CYTdW5W8EpNRlYsnj9qif/LPuRprCxrIZdgiS0s/dcUX590fpNub6G9z2aax/34Qn2Ld6luMTcJWWIRWEwPb2Z+/mX+0mksxyBKOIfImphyA==:3890\n" +
                                "^XA\n" +
                                "^FO27,157\n" +
                                "^BY2^BCN,15,N,N^FD>;"+arrayLists[0].get(position).getEPC()+"^FS\n" +
                                "^FO3,54\n" +
                                "^BQN,2,4^FDLA,"+arrayLists[0].get(position).getEPC()+"^FS\n" +
                                "^FT86,186\n" +
                                "^CI0\n" +
                                "^A0N,14,19^FD"+arrayLists[0].get(position).getEPC()+"^FS\n" +
                                "^FT99,28\n" +
                                "^A0N,14,18^FD"+arrayLists[0].get(position).getTipoActivo()+"^FS\n" +
                                "^FT99,66\n" +
                                "^A0N,14,18^FD"+arrayLists[0].get(position).getNombreOficina()+"^FS\n" +
                                "^FT99,47\n" +
                                "^A0N,14,18^FD"+arrayLists[0].get(position).getNombreArea()+"^FS\n" +
                                "^FT283,44\n" +
                                "^A0N,37,49^FD"+arrayLists[0].get(position).getEPC().substring(0,4)+"^FS\n" +
                                "^FO292,48\n" +
                                "^BY1^BCN,12,N,N^FD>;"+arrayLists[0].get(position).getEPC().substring(0,4)+"^FS\n" +
                                "^FT99,84\n" +
                                "^A0N,20,14^FD"+desc_1+"^FS\n" +
                                "^FT99,104\n" +
                                "^A0N,20,14^FD"+desc_2+"^FS\n" +
                                "^FT99,124\n" +
                                "^A0N,20,14^FD"+desc_3+"^FS\n" +
                                "^FT99,145\n" +
                                "^A0N,20,14^FD"+desc_4+"^FS\n" +
                                "^FO20,7\n" +
                                "^XGR:SSGFX000.GRF,1,1^FS\n" +
                                "^PQ1,0,1,Y\n" +
                                "^RFW,H,2,12,1^FD"+arrayLists[0].get(position).getEPC()+"^FS\n"+
                                "^XZ\n" +
                                "<xpml></page></xpml>^XA\n" +
                                "^IDR:SSGFX000.GRF^XZ\n" +
                                "<xpml><end/></xpml>";
                        outToServer.writeBytes(zpl);
                        IdCAFS = arrayLists[0].get(position).getIdCaf() + ",";
                        Thread.sleep(800);
                    }
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
            if(bool){
                txt_progress.setText("Actualizando índices");
                new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmannCAF/webservices/AdministracionImpresion/").build().create(api_network_update.class).setData(IdCAFS.substring(0, IdCAFS.length() - 1), new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Panel_loading.setVisibility(View.GONE);
                        getData();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Panel_loading.setVisibility(View.GONE);
                    }
                });
            }else{
                Panel_loading.setVisibility(View.GONE);
            }
        }
    }
}