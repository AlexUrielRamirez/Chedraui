package com.SCAF.CAFv2.Administracion.Incidences;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.SCAF.SQLiteTools.SQLiteHelper;
import com.uhf.uhf.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.Etiflex.Splash.GlobalPreferences.db_manager;

public class ManageIncidence extends AppCompatActivity {

    private TextView txt_denominacion, txt_alta, txt_baja, txt_estado;
    private Button btn_resolver;
    private Main.model_incidencia model;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_manage_incidence);

        model = GlobalPreferences.current_incidencia_model;

        txt_denominacion = findViewById(R.id.txt_denominacion);
        txt_alta = findViewById(R.id.txt_alta);
        txt_baja = findViewById(R.id.txt_baja);
        txt_estado = findViewById(R.id.txt_estado);
        btn_resolver = findViewById(R.id.btn_resolver);

        txt_denominacion.setText(model.getDenominacionDelActivoFijo());
        txt_alta.setText("Creada por "+ model.getNombreAlta() + " " + getDate(model.getFechaAlta()));
        txt_baja.setText("Resuelta por "+ model.getNombreBaja() + " " + getDate(model.getFechaBaja()));

        btn_resolver.setOnClickListener(v->{
            db_manager = new SQLiteHelper(this);
            db = db_manager.getWritableDatabase();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());

            db.execSQL("UPDATE tb_activo SET Status = 1, FechaLectura = '"+currentDateandTime+"' WHERE IdActivo = " + model.getIdActivo());
            db.execSQL("UPDATE tb_incidences SET Status = 0 WHERE IdIncidencia = " + model.getIdIncidencia());
            Intent data = new Intent();
            data.setData(Uri.parse("done"));
            setResult(RESULT_OK, data);
            finish();
        });

        if(model.getStatusIncidencia() == 1){
            txt_estado.setText("ACTIVA");
            txt_estado.setTextColor(getResources().getColor(R.color.menu_orange));
            btn_resolver.setEnabled(true);
        }else if(model.getStatusIncidencia() == 0){
            txt_estado.setText("RESUELTA");
            txt_estado.setTextColor(getResources().getColor(R.color.green));
        }else{
            txt_estado.setText("INDEFINIDO");
            txt_estado.setTextColor(getResources().getColor(R.color.black));
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
            return "aún sin fecha especificada";
        }
    }

}