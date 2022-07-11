package com.SCAF.CAFv2.Historial;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;

public class Main extends AppCompatActivity {

    private RecyclerView rv_historial;
    private ArrayList<Model> main_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_main_historial);
        findViewById(R.id.btn_onbackpressed).setOnClickListener(v -> Main.this.finish());
        rv_historial = findViewById(R.id.rv_historial);
        rv_historial.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        Volley.newRequestQueue(this).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL + "/HellmannCAF/webservices/Historial/getHistorial.php", null, response -> {
            JSONArray json = response.optJSONArray("Data");
            main_list = new ArrayList<>();
            try {
                for (int i = 0; i < json.length(); i++) {
                    Model model = new Model();
                    JSONObject jsonObject = null;
                    jsonObject = json.getJSONObject(i);
                    model.setIdHistorial(jsonObject.optString("Id"));
                    model.setIdCAF(jsonObject.optString("IdCAF"));
                    model.setNumeroActivo(jsonObject.optString("NumeroActivo"));
                    model.setEPC(jsonObject.optString("EPC"));
                    model.setDescripcionActivo(jsonObject.optString("DescripcionActivo"));
                    model.setIdUsuario(jsonObject.optString("IdUsuario"));
                    model.setNombreUsuario(jsonObject.optString("NombreUsuario"));
                    model.setFecha(jsonObject.optString("Fecha"));
                    model.setNombreArea(jsonObject.optString("NombreArea"));
                    model.setNombreOficina(jsonObject.optString("NombreOficina"));
                    model.setTipo(Integer.parseInt(jsonObject.optString("Tipo")));
                    main_list.add(model);
                }
                rv_historial.setAdapter(new rv_adapter());
            } catch (JSONException | NullPointerException e) {
                Log.e("Historial", "JSON | Null Exception" + e);
            }
        }, error -> {
            Log.e("Historial", "Volley error: " + error);
        }));
    }

    private class rv_adapter extends RecyclerView.Adapter<rv_adapter.ViewHolder>{
        @Override
        public rv_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial,parent,false);
            return new rv_adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(rv_adapter.ViewHolder holder, int position) {
            String TypeAction = null;
            switch (String.valueOf(main_list.get(position).getTipo())){
                case GlobalPreferences.HISTORIAL_TIPO_REIMPRESION:
                    TypeAction = " reimprimió el activo " + main_list.get(position).getNumeroActivo() + " con EPC: " + main_list.get(position).getEPC() + " con ubicación en " + main_list.get(position).getNombreArea() + ", " + main_list.get(position).getNombreOficina();
                    break;
                case GlobalPreferences.HISTORIAL_TIPO_INVENTARIOS:
                    TypeAction = " inventarió el activo " + main_list.get(position).getNumeroActivo() + " con EPC: " + main_list.get(position).getEPC() + " con ubicación en " + main_list.get(position).getNombreArea() + ", " + main_list.get(position).getNombreOficina();;
                    break;
                case GlobalPreferences.HISTORIAL_TIPO_ALTA_INCIDENCIA:
                    TypeAction = " levantó una incidencia del activo " + main_list.get(position).getNumeroActivo() + " con EPC: " + main_list.get(position).getEPC() + " con ubicación en " + main_list.get(position).getNombreArea() + ", " + main_list.get(position).getNombreOficina();;
                    break;
                case GlobalPreferences.HISTORIAL_TIPO_BAJA_INCIDENCIA:
                    TypeAction = " resolvió una incidencia del activo " + main_list.get(position).getNumeroActivo() + " con EPC: " + main_list.get(position).getEPC() + " con ubicación en " + main_list.get(position).getNombreArea() + ", " + main_list.get(position).getNombreOficina();;
                    break;
                case GlobalPreferences.HISTORIAL_TIPO_TRASPASOS:
                    TypeAction = " traspasó el activo " + main_list.get(position).getNumeroActivo() + " con EPC: " + main_list.get(position).getEPC() + " con ubicación en " + main_list.get(position).getNombreArea() + ", " + main_list.get(position).getNombreOficina();;
                    break;
                case GlobalPreferences.HISTORIAL_TIPO_ALTA_ACTIVO:
                    TypeAction = " dió de alta el activo " + main_list.get(position).getNumeroActivo() + " con EPC: " + main_list.get(position).getEPC() + " con ubicación en " + main_list.get(position).getNombreArea() + ", " + main_list.get(position).getNombreOficina();;
                    break;
                case GlobalPreferences.HISTORIAL_TIPO_MODIFICACION_ACTIVO:
                    TypeAction = " modificó el activo " + main_list.get(position).getNumeroActivo() + " con EPC: " + main_list.get(position).getEPC() + " con ubicación en " + main_list.get(position).getNombreArea() + ", " + main_list.get(position).getNombreOficina();;
                    break;
            }
            String info = "El usuario " + main_list.get(position).getNombreUsuario() ;
            holder.txt_info.setText(info + TypeAction);
            Glide.with(Main.this).load("http://" + GlobalPreferences.TMP_IP + "/HellmannCAF/assets/Activo/0623").override(240).placeholder(R.drawable.empty_photo).into(holder.img_activo);
            holder.txt_descripcion.setText(main_list.get(position).getDescripcionActivo());
        }

        @Override
        public int getItemCount() {
            return main_list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView txt_info, txt_descripcion;
            private ImageView img_activo;
            public ViewHolder(View itemView) {
                super(itemView);
                txt_info = itemView.findViewById(R.id.txt_info);
                txt_descripcion = itemView.findViewById(R.id.txt_descripcion_activo);
                img_activo = itemView.findViewById(R.id.img_activo);
            }
        }
    }
    
    private class Model{
        private String IdHistorial;
        private String IdCAF;
        private String NumeroActivo;
        private String EPC;
        private String DescripcionActivo;
        private String IdUsuario;
        private String NombreUsuario;
        private String Fecha;
        private String NombreArea;
        private String NombreOficina;
        private int Tipo;

        public String getIdHistorial() {
            return IdHistorial;
        }

        public void setIdHistorial(String idHistorial) {
            IdHistorial = idHistorial;
        }

        public String getIdCAF() {
            return IdCAF;
        }

        public void setIdCAF(String idCAF) {
            IdCAF = idCAF;
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

        public String getDescripcionActivo() {
            return DescripcionActivo;
        }

        public void setDescripcionActivo(String descripcionActivo) {
            DescripcionActivo = descripcionActivo;
        }

        public String getIdUsuario() {
            return IdUsuario;
        }

        public void setIdUsuario(String idUsuario) {
            IdUsuario = idUsuario;
        }

        public String getNombreUsuario() {
            return NombreUsuario;
        }

        public void setNombreUsuario(String nombreUsuario) {
            NombreUsuario = nombreUsuario;
        }

        public String getFecha() {
            return Fecha;
        }

        public void setFecha(String fecha) {
            Fecha = fecha;
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

        public int getTipo() {
            return Tipo;
        }

        public void setTipo(int tipo) {
            Tipo = tipo;
        }
    }

}