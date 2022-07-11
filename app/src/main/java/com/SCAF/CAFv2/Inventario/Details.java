package com.SCAF.CAFv2.Inventario;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.SCAF.SQLiteTools.SQLiteHelper;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.uhf.uhf.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.Etiflex.Splash.GlobalPreferences.db_manager;

public class Details extends AppCompatActivity {

    private Main.ModelInventario model;

    private ImageView img_item;
    private TextView Denominacion, Sociedad,ActivoFijo, SN, FechaCapitalizacion, ValorAdquisicion, ValorLibros, SupraAF, NoSerie, NoInventario, NotaInventario, Emplazamiento,  Local, ClaseAF, CentroCoste;
    private Button btn_buscar, btn_incidencia;
    private String motivo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_details);
        if(getIntent().getExtras() != null){
            if(getIntent().getExtras().getBoolean("OnlyConsult")){
                findViewById(R.id.constraintLayout4).setVisibility(View.GONE);
            }
        }
        model = GlobalPreferences.current_inventory_item;
        initViews();
        setUpViews();
    }

    private void initViews() {
        Denominacion  = findViewById(R.id.txt_denominacion);
        Sociedad = findViewById(R.id.txt_sociedad);
        ActivoFijo = findViewById(R.id.txt_activo_fijo);
        SN = findViewById(R.id.txt_sn);
        FechaCapitalizacion = findViewById(R.id.txt_fecha_capitalizacion);
        ValorAdquisicion = findViewById(R.id.txt_valor_adquisicion);
        ValorLibros = findViewById(R.id.txt_valor_libros);
        SupraAF = findViewById(R.id.txt_supra_af);
        NoSerie = findViewById(R.id.txt_no_serie);
        NoInventario = findViewById(R.id.txt_no_inventario);
        NotaInventario = findViewById(R.id.txt_nota_inventario);
        Emplazamiento = findViewById(R.id.txt_emplazamiento);
        Local = findViewById(R.id.txt_local);
        ClaseAF = findViewById(R.id.txt_clase_af);
        CentroCoste = findViewById(R.id.txt_centro_coste);
        btn_buscar = findViewById(R.id.btn_buscar);
        btn_incidencia = findViewById(R.id.btn_incidencia);
        findViewById(R.id.btn_onbackpressed).setOnClickListener(v->{
            this.onBackPressed();
        });
    }

    private void setUpViews() {
        Denominacion.setText(model.getDenominacionDelActivoFijo());
        Sociedad.setText("Sociedad: " + model.getSociedad());
        ActivoFijo.setText("Activo fijo: " + model.getActivoFijo());
        SN.setText("SN°: " + model.getSN());
        FechaCapitalizacion.setText("Fecha capitalización : " + model.getFechaCapitalizacion());
        ValorAdquisicion.setText("Valor adquisicion: $" + model.getValorAdquisicion());
        ValorLibros .setText("Valor libros: " + model.getValorLibros());
        SupraAF.setText("Supra AF: " + model.getSupraAF());
        NoSerie.setText("No. Serie: " + model.getNoSerie());
        NoInventario.setText("No. Inventario: " + model.getNoInvent());
        NotaInventario.setText("Nota inventario: " + model.getNotaInventario());
        Emplazamiento.setText("Emplazamiento: " + model.getEmplazamiento());
        Local.setText("Local: " + model.getLocal());
        ClaseAF.setText("Clase AF: " + model.getClaseAF());
        CentroCoste.setText("Centro coste: " + model.getCentroCoste());
        btn_buscar.setOnClickListener(v->{
            GlobalPreferences.CURRENT_TAG = model.getEPC();
            startActivity(new Intent(Details.this, com.SCAF.CAFv2.BuscadorEPC.Buscador.class));
        });

        btn_incidencia.setOnClickListener(v->{
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.alert_insidence);
            TextView NombreCreador = dialog.findViewById(R.id.et_persona_alta);
            EditText et_comentario = dialog.findViewById(R.id.et_comentario);

            Spinner spinner = dialog.findViewById(R.id.txt_spinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.status_strings, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {motivo = parent.getItemAtPosition(position).toString();}
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });

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
                    ProgressDialog pd_insidencia = new ProgressDialog(Details.this);
                    pd_insidencia.setMessage("Por favor espere...");
                    pd_insidencia.show();
                    File f = null;
                    try{
                        f = new File(Details.this.getCacheDir(), "tmp_bitmap");
                        f.createNewFile();
                        Bitmap bitmap = signaturePad.getSignatureBitmap();
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 0 , bos);
                        byte[] bitmapdata = bos.toByteArray();
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(bitmapdata);
                        fos.flush();
                        fos.close();

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        String currentDateandTime = sdf.format(new Date());

                        //Guardar imagen de la firma
                        db_manager = new SQLiteHelper(this);
                        SQLiteDatabase db = db_manager.getWritableDatabase();
                        db.execSQL("INSERT INTO tb_incidences (" +
                                "IdActivo," +
                                "NombreAlta," +
                                "FechaAlta," +
                                "NombreBaja," +
                                "FechaBaja," +
                                "Motivo," +
                                "Comentario," +
                                "Status) VALUES (" +
                                model.getId()+","+
                                "'"+GlobalPreferences.NOMBRE_USUARIO+"',"+
                                "'"+currentDateandTime+"',"+
                                "'Indefinido'," +
                                "'Indefinido'," +
                                "'"+getMotivo(motivo)+"'," +
                                "'"+et_comentario.getText().toString()+"'," +
                                "1)");

                        db.execSQL("UPDATE tb_activo SET Status = 2 WHERE IdActivo = " + model.getId());

                        pd_insidencia.dismiss();
                        dialog.dismiss();

                        Intent data = new Intent();
                        data.setData(Uri.parse("incidence-ok"));
                        setResult(RESULT_OK, data);
                        finish();


                    }catch (IOException e){
                        pd_insidencia.dismiss();
                        Toast.makeText(Details.this, "Por favor, revise los permisos de la aplicación", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(Details.this, "Por favor, ingrese los datos solicitados", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        });
    }

    private String getMotivo(String motivo) {
        switch (motivo){
            case "Ocioso":
                return "10";
            case "Obsoleto":
                return "20";
            case "Proceso de Vta":
                return "30";
            case "Inventariado":
                return "40";
            case "Faltante":
                return "50";
            case "Sobrante":
                return "60";
            case "Depreciación Acelerada":
                return "70";
            case "Reactivación":
                return "80";
            case "Escisión":
                return "90";
        }
        return "No especificado";
    }

}