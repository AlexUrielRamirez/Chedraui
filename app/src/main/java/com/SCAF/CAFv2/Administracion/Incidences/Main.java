package com.SCAF.CAFv2.Administracion.Incidences;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.SCAF.CAFv2.Inventario.Details;
import com.SCAF.SQLiteTools.SQLiteHelper;
import com.uhf.uhf.R;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static com.Etiflex.Splash.GlobalPreferences.db_manager;

public class Main extends AppCompatActivity {

    private RecyclerView rv_incidencias;
    private rv_adapter adapter;
    private ArrayList<model_incidencia> main_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_main_incidencias);

        rv_incidencias = findViewById(R.id.rv_incidencias);
        rv_incidencias.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        downladData();

    }

    private void downladData() {
        main_list = new ArrayList<>();
        db_manager = new SQLiteHelper(this);
        SQLiteDatabase db = db_manager.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " +
                "A.IdIncidencia," +
                "A.IdActivo," +
                "A.NombreAlta," +
                "A.FechaAlta," +
                "A.NombreBaja," +
                "A.FechaBaja," +
                "A.Motivo," +
                "A.Comentario," +
                "B.NoInvent," +
                "B.ActivoFijo," +
                "B.DenominacionDelActivoFijo,"+
                "A.Status,"+
                "B.NoInvent "+
                "FROM tb_incidences A " +
                "INNER JOIN tb_activo B ON B.IdActivo = A.IdActivo ", null);

        if (c != null) {
            try{
                c.moveToFirst();
                do {

                    model_incidencia model = new model_incidencia();
                    model.setIdIncidencia(c.getString(c.getColumnIndex("IdIncidencia")));
                    model.setIdActivo(c.getString(c.getColumnIndex("IdActivo")));
                    model.setNombreActivo(c.getString(c.getColumnIndex("DenominacionDelActivoFijo")));
                    model.setNombreAlta(c.getString(c.getColumnIndex("NombreAlta")));
                    model.setFechaAlta(c.getString(c.getColumnIndex("FechaAlta")));
                    model.setMotivo(c.getString(c.getColumnIndex("Motivo")));
                    model.setComentario(c.getString(c.getColumnIndex("Comentario")));
                    model.setNombreBaja(c.getString(c.getColumnIndex("NombreBaja")));
                    model.setFechaBaja(c.getString(c.getColumnIndex("FechaBaja")));
                    model.setEPC(c.getString(c.getColumnIndex("NoInvent")));
                    model.setActivoFijo(c.getString(c.getColumnIndex("ActivoFijo")));
                    model.setDenominacionDelActivoFijo(c.getString(c.getColumnIndex("DenominacionDelActivoFijo")));
                    model.setStatusIncidencia(c.getInt(c.getColumnIndex("Status")));
                    main_list.add(model);

                } while (c.moveToNext());

            }catch (CursorIndexOutOfBoundsException e){

            }
            if(main_list.size() > 0){
                adapter = new rv_adapter(main_list);
                rv_incidencias.setAdapter(adapter);
            }

        }else{
            //Mostrar pantalla vacía
        }
    }

    private class rv_adapter extends RecyclerView.Adapter<rv_adapter.ViewHolder>{
        Context context;
        ArrayList<model_incidencia> child_list;
        public rv_adapter(ArrayList<model_incidencia> list){
            this.child_list = list;
        }

        @Override
        public rv_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_incidencias,parent,false);
            context = parent.getContext();
            return new rv_adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(rv_adapter.ViewHolder holder, int position) {
            holder.parent_holder.setOnClickListener(v->{
                GlobalPreferences.current_incidencia_model = child_list.get(position);
                startActivityForResult(new Intent(Main.this, ManageIncidence.class), 0);
            });
            holder.txt_nombre_activo.setText(child_list.get(position).getDenominacionDelActivoFijo());
            holder.txt_epc.setText("EPC: " + child_list.get(position).getEPC());
            if(child_list.get(position).getStatusIncidencia() == 1){
                holder.txt_alta.setText("Creada por "+ child_list.get(position).getNombreAlta() + " " + getDate(child_list.get(position).getFechaAlta()));
                holder.txt_motivo.setText(getMotivo(child_list.get(position).getMotivo()));
                holder.txt_comentario.setText(child_list.get(position).getComentario());
            }else{
                holder.txt_alta.setText("Resuelta por "+ child_list.get(position).getNombreBaja() + " " + getDate(child_list.get(position).getFechaBaja()));
                holder.txt_motivo.setText(getMotivo(child_list.get(position).getMotivo()));
                holder.txt_comentario.setText(child_list.get(position).getComentario());
            }

            if(child_list.get(position).getStatusIncidencia() == 1){
                holder.txt_estado.setText("ACTIVA");
                holder.txt_estado.setTextColor(getResources().getColor(R.color.menu_orange));
            }else if(child_list.get(position).getStatusIncidencia() == 0){
                holder.txt_estado.setText("RESUELTA");
                holder.txt_estado.setTextColor(getResources().getColor(R.color.green));
            }else{
                holder.txt_estado.setText("INDEFINIDO");
                holder.txt_estado.setTextColor(getResources().getColor(R.color.black));
            }

        }

        @Override
        public int getItemCount() {
            return child_list.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ConstraintLayout parent_holder;
            TextView txt_nombre_activo, txt_epc, txt_alta, txt_motivo, txt_comentario, txt_estado;

            public ViewHolder(View itemView) {
                super(itemView);

                parent_holder = itemView.findViewById(R.id.parent_holder);
                txt_nombre_activo = itemView.findViewById(R.id.txt_nombre);
                txt_epc = itemView.findViewById(R.id.txt_epc);
                txt_alta = itemView.findViewById(R.id.txt_alta);
                txt_motivo = itemView.findViewById(R.id.txt_motivo);
                txt_comentario = itemView.findViewById(R.id.txt_comentario);
                txt_estado = itemView.findViewById(R.id.txt_estado);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case 0:
                    if(data.getDataString().equals("done")){
                        downladData();
                    }
                    break;
            }
        }
    }

    String getDate(String date){
        try {
            String[] parts = date.split(" ");
            String Fecha = parts[0];
            String Hora = parts[1];

            String dia = Fecha.substring(0, 2);
            String mes = Fecha.substring(3, 5);
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
            String anio = Fecha.substring(6, 10);

            return " el " + dia + " del " + mes + " de " + anio + " a las " + Hora + " horas";
        }catch (ArrayIndexOutOfBoundsException  e){
            return ", fecha no especificada";
        }
    }

    private String getMotivo(String motivo) {
        switch (motivo){
            case "10":
                return "Ocioso";
            case "20":
                return "Obsoleto";
            case "30":
                return "Proceso de Vta";
            case "40":
                return "Inventariado";
            case "50":
                return "Faltante";
            case "60":
                return "Sobrante";
            case "70":
                return "Depreciación Acelerada";
            case "80":
                return "Reactivación";
            case "90":
                return "Escisión";
        }
        return "No especificado";
    }

    public static class model_incidencia{
        private String IdIncidencia;
        private String IdActivo;
        private String NombreActivo;
        private String NombreAlta;
        private String Motivo;
        private String Comentario;
        private String FechaAlta;
        private String NombreBaja;
        private String FechaBaja;
        private String EPC;
        private String ActivoFijo;
        private String DenominacionDelActivoFijo;
        private int StatusIncidencia;

        public String getIdIncidencia() {
            return IdIncidencia;
        }

        public void setIdIncidencia(String idIncidencia) {
            IdIncidencia = idIncidencia;
        }

        public String getIdActivo() {
            return IdActivo;
        }

        public void setIdActivo(String idActivo) {
            IdActivo = idActivo;
        }

        public String getNombreActivo() {
            return NombreActivo;
        }

        public void setNombreActivo(String nombreActivo) {
            NombreActivo = nombreActivo;
        }

        public String getNombreAlta() {
            return NombreAlta;
        }

        public void setNombreAlta(String nombreAlta) {
            NombreAlta = nombreAlta;
        }

        public String getMotivo() {
            return Motivo;
        }

        public void setMotivo(String motivo) {
            Motivo = motivo;
        }

        public String getComentario() {
            return Comentario;
        }

        public void setComentario(String comentario) {
            Comentario = comentario;
        }

        public String getFechaAlta() {
            return FechaAlta;
        }

        public String getNombreBaja() {
            return NombreBaja;
        }

        public void setNombreBaja(String nombreBaja) {
            NombreBaja = nombreBaja;
        }

        public String getFechaBaja() {
            return FechaBaja;
        }

        public void setFechaBaja(String fechaBaja) {
            FechaBaja = fechaBaja;
        }

        public void setFechaAlta(String fechaAlta) {
            FechaAlta = fechaAlta;
        }

        public String getEPC() {
            return EPC;
        }

        public void setEPC(String EPC) {
            this.EPC = EPC;
        }

        public String getActivoFijo() {
            return ActivoFijo;
        }

        public void setActivoFijo(String activoFijo) {
            ActivoFijo = activoFijo;
        }

        public String getDenominacionDelActivoFijo() {
            return DenominacionDelActivoFijo;
        }

        public void setDenominacionDelActivoFijo(String denominacionDelActivoFijo) {
            DenominacionDelActivoFijo = denominacionDelActivoFijo;
        }

        public int getStatusIncidencia() {
            return StatusIncidencia;
        }

        public void setStatusIncidencia(int statusIncidencia) {
            StatusIncidencia = statusIncidencia;
        }
    }

}