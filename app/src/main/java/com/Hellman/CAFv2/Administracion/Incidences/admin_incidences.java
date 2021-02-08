package com.Hellman.CAFv2.Administracion.Incidences;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.Hellman.CAFv2.Administracion.Impresion.admin_impresion;
import com.Hellman.CAFv2.Administracion.Ubicaciones.admin_ubicaciones;
import com.Hellman.CAFv2.BuscadorEPC.Buscador;
import com.Hellman.CAFv2.Incidencias.ControlIncidencias;
import com.Hellman.CAFv2.Inventario.Main;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

import static android.app.Activity.RESULT_OK;

public class admin_incidences extends Fragment {

    public admin_incidences() {}

    public static admin_incidences newInstance() {
        admin_incidences fragment = new admin_incidences();
        return fragment;
    }

    private ConstraintLayout Panel_loading;
    private RecyclerView rv_incidencias;
    private ArrayList<ModelIncidencias> main_list_incidencias;

    private final int CODE_BAR_FOR_METAL = 250;
    public ModelIncidencias CURRENT_MODEL = null;
    public BottomSheetDialog bsd;

    interface api_network_clean_incidencia {
        @Multipart
        @POST("/cleanIncidencia.php")
        void setData(
                @Part("file") TypedFile file,
                @Part("IdCAF") String IdCAF,
                @Part("IdIncidencia") String IdIncidencia,
                @Part("Resolutor") String Resolutor,
                Callback<Response> callback
        );
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_incidences, container, false);
        initViews(view);
        return view;
    }

    public void initViews(View view) {
        Panel_loading = view.findViewById(R.id.Panel_loading);
        rv_incidencias = view.findViewById(R.id.rv_incidencias);
        rv_incidencias.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        getData();
    }

    private void getData(){
        main_list_incidencias = new ArrayList<>();
        Panel_loading.setVisibility(View.VISIBLE);
        Volley.newRequestQueue(getContext()).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmanCAF/webservices/AdministracionIncidencias/getAllIncidences.php", null, response -> {
            JSONArray json = response.optJSONArray("Data");

            try {
                for (int i = 0; i < json.length(); i++) {
                    ModelIncidencias model = new ModelIncidencias();
                    JSONObject jsonObject = null;
                    jsonObject = json.getJSONObject(i);

                    model.setIdIncidencia(jsonObject.optString("IdIncidencia"));
                    model.setIdCAF(jsonObject.optString("IdCAF"));
                    model.setEPC(jsonObject.optString("EPC"));
                    model.setNombreCreador(jsonObject.optString("NombreCreador"));
                    model.setFechaCreacion(jsonObject.optString("FechaCreacion"));
                    model.setNombreResolutor(jsonObject.optString("NombreResolutor"));
                    model.setFechaResolucion(jsonObject.optString("FechaResolucion"));
                    model.setNumeroActivo(jsonObject.optString("NumeroActivo"));
                    model.setNombreActivo(jsonObject.optString("NombreActivo"));
                    model.setDescripcionActivo(jsonObject.optString("DescripcionActivo"));
                    model.setIdUbicacion(jsonObject.optString("IdUbicacion"));
                    model.setIdArea(jsonObject.optString("IdArea"));
                    model.setNombreArea(jsonObject.optString("NombreArea"));
                    model.setIdOficina(jsonObject.optString("IdOficina"));
                    model.setNombreOficina(jsonObject.optString("NombreOficina"));
                    model.setStatus(jsonObject.optInt("Status"));

                    main_list_incidencias.add(model);
                }
                rv_incidencias.setAdapter(new rv_adapter(main_list_incidencias));
                Panel_loading.setVisibility(View.GONE);
            } catch (JSONException | NullPointerException e) {
                Panel_loading.setVisibility(View.GONE);
                Log.e("Validacion", "JSON | Null Exception" + e);
            }

        }, error -> {
            Panel_loading.setVisibility(View.GONE);
            Log.e("Validacion", "Error, no hay conexión con el servidor");
        }));
    }

    public class rv_adapter extends RecyclerView.Adapter<rv_adapter.ViewHolder> implements View.OnClickListener{
        Context context;
        private ArrayList<ModelIncidencias> child_list;
        public rv_adapter(ArrayList<ModelIncidencias> list){
            child_list = list;
        }
        @Override
        public rv_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_admin_incidencias,parent,false);
            view.setOnClickListener(this);
            context = parent.getContext();
            return new rv_adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(rv_adapter.ViewHolder holder, int position) {
            holder.item.setOnClickListener(v->{
                bsd = new BottomSheetDialog(getContext());
                bsd.setContentView(R.layout.bsd_admin_incidencias);
                if(child_list.get(position).getStatus() == 1){
                    bsd.findViewById(R.id.btn_resolver_incidencia).setOnClickListener(v1->{
                        CURRENT_MODEL = child_list.get(position);
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("com.etiflex.sdl", "com.zebra.sdl.SDLguiActivity"));
                        startActivityForResult(intent, CODE_BAR_FOR_METAL);
                    });
                }else{
                    bsd.findViewById(R.id.btn_resolver_incidencia).setVisibility(View.GONE);
                }
                bsd.findViewById(R.id.btn_buscar_activo).setOnClickListener(v1->{
                    GlobalPreferences.CURRENT_TAG = main_list_incidencias.get(position).getEPC();
                    bsd.dismiss();
                    startActivity(new Intent(getContext(), Buscador.class));
                });
                bsd.findViewById(R.id.btn_volver).setOnClickListener(v1->{
                    bsd.dismiss();
                });
                bsd.show();
            });
            Glide.with(context).load(GlobalPreferences.URL+"/HellmanCAF/assets/Activo/" + child_list.get(position).getNumeroActivo()).override(140).into(holder.img_activo);
            holder.txtNombreActivo.setText(child_list.get(position).getNombreActivo());
            holder.txtDescripcionActivo.setText(child_list.get(position).getDescripcionActivo());
            holder.txtNumeroActivo.setText("Número de activo: " + child_list.get(position).getNumeroActivo());
            holder.txtUbicacionActivo.setText("Ubicación: " + child_list.get(position).getNombreArea() + " > " + child_list.get(position).getNombreOficina());
            String txt = null;
            switch (child_list.get(position).getStatus()){
                case 0:
                    holder.txtStatus.setText("ENCONTRADO");
                    holder.txtStatus.setBackgroundColor(context.getColor(R.color.green_2));
                    txt = "<font color=#c12622>Creado por " + child_list.get(position).getNombreCreador() + getDate(child_list.get(position).getFechaCreacion()) + "</font> <font color=#000000><br>°<br>°<br>°<br></font>" +" <font color=#1157A2>Encontrado por " + child_list.get(position).getNombreResolutor() + getDate(child_list.get(position).getFechaResolucion()) + "</font>";
                    break;
                case 1:
                    holder.txtStatus.setText("NO ENCONTRADO");
                    holder.txtStatus.setBackgroundColor(context.getColor(R.color.menu_orange));
                    txt = "<font color=#c12622>Creado por " + child_list.get(position).getNombreCreador() + getDate(child_list.get(position).getFechaCreacion()) + "</font>";
                    break;
            }
            holder.txtCreacion.setText(Html.fromHtml(txt));
        }

        String getDate(String date){
            try {
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

                return " el " + dia + " del " + mes + " de " + anio + " a las " + Hora + " horas";
            }catch (ArrayIndexOutOfBoundsException  e){
                return ", fecha no especificada";
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
            ConstraintLayout item;
            ImageView img_activo;
            TextView txtNombreActivo, txtDescripcionActivo, txtNumeroActivo, txtUbicacionActivo, txtStatus, txtCreacion;
            public ViewHolder(View itemView) {
                super(itemView);
                item = itemView.findViewById(R.id.item);
                img_activo = itemView.findViewById(R.id.img_activo);
                txtNombreActivo = itemView.findViewById(R.id.txt_nombre_activo);
                txtDescripcionActivo = itemView.findViewById(R.id.txt_descripcion_activo);
                txtNumeroActivo = itemView.findViewById(R.id.txt_numero_activo);
                txtUbicacionActivo = itemView.findViewById(R.id.txt_ubicacion_activo);
                txtCreacion = itemView.findViewById(R.id.txt_creacion);
                txtStatus = itemView.findViewById(R.id.txt_status);
            }
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if(data != null){
                String EPC = data.getDataString().substring(0,24);
                if(EPC.equals(CURRENT_MODEL.getEPC())){
                    ProgressDialog progressDialog = new ProgressDialog(getContext());
                    final Dialog dialog = new Dialog(getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog.setContentView(R.layout.alert_insidence);
                    TextView NombreCreador = dialog.findViewById(R.id.et_persona_alta);
                    NombreCreador.setText(GlobalPreferences.NOMBRE_USUARIO);
                    dialog.findViewById(R.id.btn_volver).setOnClickListener(v2->{
                        dialog.dismiss();
                    });
                    final SignaturePad signaturePad = dialog.findViewById(R.id.signature_pad);
                    dialog.findViewById(R.id.btn_limpiar_firma).setOnClickListener(v3->{
                        signaturePad.clear();
                    });
                    dialog.findViewById(R.id.btn_continuar).setOnClickListener(v2->{
                        if(!signaturePad.isEmpty()){
                            progressDialog.setMessage("Actualizando índices...");
                            progressDialog.show();
                            try{
                                File f = new File(getContext().getCacheDir(), "tmp_bitmap");
                                f.createNewFile();
                                Bitmap bitmap = signaturePad.getSignatureBitmap();
                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                                byte[] bitmapdata = bos.toByteArray();
                                FileOutputStream fos = new FileOutputStream(f);
                                fos.write(bitmapdata);
                                fos.flush();
                                fos.close();

                                new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmanCAF/webservices/Incidencias").build().create(api_network_clean_incidencia.class).setData(new TypedFile("multipart/form-data", f), CURRENT_MODEL.getIdCAF(), CURRENT_MODEL.getIdIncidencia(), GlobalPreferences.NOMBRE_USUARIO, new Callback<Response>() {
                                    @Override
                                    public void success(Response response, Response response2) {
                                        try{
                                            if(new BufferedReader(new InputStreamReader(response.getBody().in())).readLine().equals("succes")){
                                                GlobalPreferences.mHistorial.GuardarHistorico(GlobalPreferences.ID_CEDIS, GlobalPreferences.ID_USUARIO, GlobalPreferences.HISTORIAL_TIPO_BAJA_INCIDENCIA, CURRENT_MODEL.getIdCAF());
                                                getData();
                                                dialog.dismiss();
                                                progressDialog.dismiss();
                                                bsd.dismiss();
                                                Toast.makeText(getContext(), "Incidencia resuelta con éxito", Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(getContext(), "Algo salió mal, intente nuevamente", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }

                                        }catch (IOException e){
                                            Toast.makeText(getContext(), "Algo salió mal, intente nuevamente", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        progressDialog.dismiss();
                                        Log.e("ControlIncidencias", "Error->" + error.getMessage());
                                        Toast.makeText(getContext(), "No hay conexión con el servidor", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }catch (IOException e){
                                Toast.makeText(getContext(), "Por favor, revise los permisos de almacenamiento", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(getContext(), "Por favor, ingrese una firma válida", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.show();
                }else{
                    Toast.makeText(getContext(), "Error, el EPC no coincide con el activo actual", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getContext(), "No se identificó el EPC", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ModelIncidencias{
        private String IdIncidencia;
        private String IdCAF;
        private String EPC;
        private String NombreCreador;
        private String FechaCreacion;
        private String NombreResolutor;
        private String FechaResolucion;
        private String NumeroActivo;
        private String NombreActivo;
        private String DescripcionActivo;
        private String IdUbicacion;
        private String IdArea;
        private String IdOficina;
        private String NombreArea;
        private String NombreOficina;
        private int Status;

        public String getIdIncidencia() {
            return IdIncidencia;
        }

        public void setIdIncidencia(String idIncidencia) {
            IdIncidencia = idIncidencia;
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

        public String getNombreCreador() {
            return NombreCreador;
        }

        public void setNombreCreador(String nombreCreador) {
            NombreCreador = nombreCreador;
        }

        public String getFechaCreacion() {
            return FechaCreacion;
        }

        public void setFechaCreacion(String fechaCreacion) {
            FechaCreacion = fechaCreacion;
        }

        public String getNombreResolutor() {
            return NombreResolutor;
        }

        public void setNombreResolutor(String nombreResolutor) {
            NombreResolutor = nombreResolutor;
        }

        public String getFechaResolucion() {
            return FechaResolucion;
        }

        public void setFechaResolucion(String fechaResolucion) {
            FechaResolucion = fechaResolucion;
        }

        public String getNumeroActivo() {
            return NumeroActivo;
        }

        public void setNumeroActivo(String numeroActivo) {
            NumeroActivo = numeroActivo;
        }

        public String getNombreActivo() {
            return NombreActivo;
        }

        public void setNombreActivo(String nombreActivo) {
            NombreActivo = nombreActivo;
        }

        public String getDescripcionActivo() {
            return DescripcionActivo;
        }

        public void setDescripcionActivo(String descripcionActivo) {
            DescripcionActivo = descripcionActivo;
        }

        public String getIdUbicacion() {
            return IdUbicacion;
        }

        public void setIdUbicacion(String idUbicacion) {
            IdUbicacion = idUbicacion;
        }

        public String getIdArea() {
            return IdArea;
        }

        public void setIdArea(String idArea) {
            IdArea = idArea;
        }

        public String getIdOficina() {
            return IdOficina;
        }

        public void setIdOficina(String idOficina) {
            IdOficina = idOficina;
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

        public int getStatus() {
            return Status;
        }

        public void setStatus(int status) {
            Status = status;
        }
    }
    
}