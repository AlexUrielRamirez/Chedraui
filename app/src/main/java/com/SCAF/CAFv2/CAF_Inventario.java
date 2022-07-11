package com.SCAF.CAFv2;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Etiflex.Splash.GlobalPreferences;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.github.gcacace.signaturepad.views.SignaturePad;
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

public class CAF_Inventario extends Fragment {

    private View view = null;
    public static ProgressBar mProgress;
    private ModelInventario model;
    public static ArrayList<ModelInventario> main_list;
    public static ArrayList<String> tag_list;
    public static TextView txt_contador;
    public static RecyclerView rv_content;
    public static rv_adapter adapter;
    public static int conter = 0;

    //Ubicación
    private TextView Spinner_Departamento, Spinner_Oficina;
    private PopupMenu menu_area, menu_oficinas;
    private ArrayList<ModelUbicaciones> main_list_areas, main_list_oficinas;
    private String IdArea = "0", IdOficina = "0";

    //Detalle
    public static ConstraintLayout PanelDetalle;
    public static ImageView ImgDetalle;
    public static TextView NombreDetalle, DescripcionDetalle, AlmacenDetalle, CategoriaDetalle;
    public static Button btn_buscar, btn_insidencia;

    //Buscador
    public static ConstraintLayout Buscador;
    public static ProgressBar pb_potencia;
    public static TextView CurrentEPC;

    interface api_network {
        @FormUrlEncoded
        @POST("/update_data.php")
        void setData(
                @Field("id_list") String IdList,
                Callback<Response> callback
        );
    }

    interface api_network_insidencia {
        @Multipart
        @POST("/generate_insidence.php")
        void setData(
                @Part("file") TypedFile file,
                @Part("id") String Id,
                Callback<Response> callback
        );
    }

    interface api_network_check_insidence{
        @FormUrlEncoded
        @POST("/checkInsidencia.php")
        void setData(
                @Field("IdInventario") String IdInventario,
                Callback<Response> callback
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_lecturas, container, false);
        initViews(view);
        getAreas();
        return view;
    }

    private void getAreas() {
        main_list_areas = new ArrayList<>();
        menu_area = new PopupMenu(getContext(), Spinner_Departamento);
        Volley.newRequestQueue(getContext()).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmannCAF/webservices/Loaders/getAreas.php?IdCedis=1", null, response -> {
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

            } catch (JSONException | NullPointerException e) {
                Log.e("Validacion", "JSON | Null Exception" + e);
            }

        }, error -> {
            Log.e("Validacion", "Volley error" + error);
        }));
    }

    private void initViews(View view) {
        Spinner_Departamento = view.findViewById(R.id.spinner_Departamento);
        Spinner_Oficina = view.findViewById(R.id.spinner_Oficina);
        Spinner_Departamento.setOnClickListener(v->{
            menu_area.show();
        });
        Spinner_Oficina.setOnClickListener(v->{
            if(IdArea.equals("0"))
                Toast.makeText(getContext(), "Primero seleccione un departamento", Toast.LENGTH_SHORT).show();
            else
                menu_oficinas.show();
        });
        view.findViewById(R.id.btn_continuar).setOnClickListener(v->{
            view.findViewById(R.id.Panel_ubicacion).setVisibility(View.GONE);
            DescargarInformacion(getContext());
        });
        mProgress = view.findViewById(R.id.ProgressCount);
        txt_contador = view.findViewById(R.id.txt_contador);
        rv_content = view.findViewById(R.id.rv_content);
        rv_content.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        rv_content.getItemAnimator().setChangeDuration(0);
        PanelDetalle = view.findViewById(R.id.PanelDetalle);
        ImgDetalle = view.findViewById(R.id.img_detalle);
        NombreDetalle = view.findViewById(R.id.nombre_detalle);
        DescripcionDetalle = view.findViewById(R.id.descripcion_detalle);
        AlmacenDetalle = view.findViewById(R.id.almacen_detalle);
        CategoriaDetalle = view.findViewById(R.id.categoria_detalle);
        btn_buscar = view.findViewById(R.id.btn_buscar);
        btn_insidencia = view.findViewById(R.id.btn_insidencia);
        Buscador = view.findViewById(R.id.layout_buscador);
        pb_potencia = view.findViewById(R.id.Potencia);
        CurrentEPC = view.findViewById(R.id.txt_epc);
    }

    private void DescargarInformacion(Context ctx) {

        Volley.newRequestQueue(ctx).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmannCAF/webservices/Inventario/getData.php?IdArea="+IdArea+"&IdOficina="+IdOficina, null, response -> {
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

                    main_list.add(model);
                    tag_list.add(model.getEPC().replaceAll(" ", ""));

                }
                mProgress.setMax(main_list.size());
                mProgress.setProgress(0);
                txt_contador.setText("0/" + main_list.size());
                adapter = new rv_adapter();
                rv_content.setAdapter(adapter);
                GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_INVENTORY;
                view.findViewById(R.id.btn_terminar).setOnClickListener(v -> {
                    if (conter >= main_list.size()) {
                        GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_PROCESING;
                        ProgressDialog pd = new ProgressDialog(getContext());
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
                        new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/demo/android").build().create(api_network.class).setData(id_list, new Callback<Response>() {
                            @Override
                            public void success(Response response, Response response2) {
                                try {
                                    if (new BufferedReader(new InputStreamReader(response.getBody().in())).readLine().equals("succes")) {
                                        GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_IDLE;
                                        CAF_Inventario.Buscador.setVisibility(View.GONE);
                                        CAF_Inventario.PanelDetalle.setVisibility(View.GONE);
                                        getActivity().onBackPressed();
                                        Toast.makeText(getContext(), "¡Proceso finalizado con exito!", Toast.LENGTH_SHORT).show();
                                        pd.dismiss();
                                    } else {
                                        Toast.makeText(getContext(), "Algo salió mal, intente nuevamente o contacte a un desarrollador", Toast.LENGTH_SHORT).show();
                                        pd.dismiss();
                                    }
                                } catch (IOException | NullPointerException e) {

                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {

                            }
                        });
                    } else {
                        final Dialog dialog = new Dialog(getContext());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        dialog.setContentView(R.layout.error_validating);
                        dialog.findViewById(R.id.btn_volver).setOnClickListener(v2 -> {
                            dialog.dismiss();
                        });
                        dialog.show();
                    }
                });
            } catch (JSONException | NullPointerException e) {
                Log.e("Validacion", "JSON | Null Exception" + e);
            }

        }, error -> {
            Log.e("Validacion", "Volley error" + error);
        }));
    }

    public static class rv_adapter extends RecyclerView.Adapter<rv_adapter.ViewHolder> implements View.OnClickListener{
        private View.OnClickListener listener;
        Context context;
        @Override
        public rv_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag,parent,false);
            view.setOnClickListener(this);
            context = parent.getContext();
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(rv_adapter.ViewHolder holder, int position) {
            Glide.with(context).load(GlobalPreferences.URL+"/HellmannCAF/assets/Activo/" + main_list.get(position).getNumero()).placeholder(R.drawable.empty_photo).override(240).into(holder.img);
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
                    Buscador.setVisibility(View.VISIBLE);
                });
                btn_insidencia.findViewById(R.id.btn_insidencia).setOnClickListener(v1->{
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog.setContentView(R.layout.alert_insidence);
                    dialog.findViewById(R.id.btn_volver).setOnClickListener(v2->{
                        dialog.dismiss();
                    });
                    SignaturePad signaturePad = dialog.findViewById(R.id.signature_pad);
                    dialog.findViewById(R.id.btn_limpiar_firma).setOnClickListener(v2->{
                        signaturePad.clear();
                    });
                    dialog.findViewById(R.id.btn_continuar).setOnClickListener(v2->{
                        ProgressDialog pd_insidencia = new ProgressDialog(context);
                        pd_insidencia.setMessage("Por favor espere...");
                        pd_insidencia.show();

                        //Create signature
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
                        }catch (IOException e){

                        }



                        new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/demo/android").build().create(api_network_insidencia.class).setData(new TypedFile("multipart/form-data", f), main_list.get(position).getId(), new Callback<Response>() {
                            @Override
                            public void success(Response response, Response response2) {
                                try{
                                    if(new BufferedReader(new InputStreamReader(response.getBody().in())).readLine().equals("succes")){
                                        main_list.get(position).setStatus(2);
                                        adapter.notifyItemChanged(position, CAF_Inventario.main_list.get(position));
                                        if(conter < main_list.size()){
                                            conter = CAF_Inventario.conter + 1;
                                            mProgress.setProgress(CAF_Inventario.conter);
                                            txt_contador.setText(CAF_Inventario.conter+"/"+ CAF_Inventario.main_list.size());
                                        }

                                        Toast.makeText(context, "Incidencia generada correctamente", Toast.LENGTH_SHORT).show();
                                        pd_insidencia.dismiss();
                                        dialog.dismiss();
                                    }else{
                                        Toast.makeText(context, "Ocurrió un error, contacte a un desarrollador", Toast.LENGTH_SHORT).show();
                                        pd_insidencia.dismiss();
                                        dialog.dismiss();
                                    }
                                }catch (IOException | NullPointerException e){
                                    Toast.makeText(context, "Ocurrió un error, contacte a un desarrollador", Toast.LENGTH_SHORT).show();
                                    pd_insidencia.dismiss();
                                    dialog.dismiss();
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Toast.makeText(context, "Error, revise su conexión", Toast.LENGTH_SHORT).show();
                                Log.e("Hellmann", error.getMessage());
                                pd_insidencia.dismiss();
                                dialog.dismiss();
                            }
                        });
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
                holder.status.setTextColor(context.getColor(R.color.green_2));
            }else if(main_list.get(position).getStatus() == 2){
                holder.status.setText("INSIDENCIA");
                holder.status.setTextColor(context.getColor(R.color.menu_orange));
            }
        }

        private void setUpDetailData(ModelInventario model) {
            Glide.with(context).load(GlobalPreferences.URL+"/HellmannCAF/assets/Activo/" + model.getNumero()).placeholder(R.drawable.empty_photo).override(360).into(ImgDetalle);
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
        public void onClick(View view) {

        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ConstraintLayout item_holder;
            ImageView img;
            TextView nombre, descripcion, almacen, categoria, status;
            public ViewHolder(View itemView) {
                super(itemView);
                item_holder = itemView.findViewById(R.id.item_holder);
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