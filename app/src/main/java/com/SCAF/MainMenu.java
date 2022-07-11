package com.SCAF;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.SCAF.CAFv2.Inventario.Main;
import com.SCAF.SQLiteTools.SQLiteHelper;
import com.bumptech.glide.Glide;
import com.reader.helper.ReaderHelper;
import com.uhf.uhf.R;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import static com.Etiflex.Splash.GlobalPreferences.db_manager;

public class MainMenu extends AppCompatActivity {
    private ImageView logo;
    private CardView btn_inventario, btn_alta, btn_ajustes, btn_administracion;
    private LinearLayout contents;
    private TextView btn_excel;
    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.setProperty("javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
        try {
            ReaderHelper.setContext(getApplicationContext());
        }catch (Exception e){
            Toast.makeText(this, "Ocurrió un error al iniciar los recursos, contacte a un desarrollador", Toast.LENGTH_SHORT).show();
        }

        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_main_menu);

        initViews();

        Glide.with(this).load(R.drawable.large_logo).override(480, 360).into(logo);

        btn_inventario.setOnClickListener(v->{
            startActivity(new Intent(this, Main.class));
        });

        btn_administracion.setOnClickListener(v->{
            startActivity(new Intent(this, com.SCAF.CAFv2.Administracion.Main.class));
        });

        btn_alta.setOnClickListener(v->{
            startActivity(new Intent(this, com.SCAF.CAFv2.Alta.Main.class));
        });

        btn_ajustes.setOnClickListener(v->{
            //startActivity(new Intent(this, com.SCAF.CAFv2.Ajustes.Main.class));
        });

        btn_excel.setOnClickListener(v->{
            String[] mimeTypes = {"application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.setType(mimeTypes[0]);
                if (mimeTypes.length > 0) {
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                }
            } else {
                String mimeTypesStr = "";
                for (String mimeType : mimeTypes) {
                    mimeTypesStr += mimeType + "|";
                }
                intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
            }
            startActivityForResult(Intent.createChooser(intent,"Seleccionando archivo excel"), GlobalPreferences.INTENT_RESULT_ADD_FILE_EXCEL);
        });
    }

    private void checkIncidences() {
        contents.removeAllViews();
        db_manager = new SQLiteHelper(this);
        SQLiteDatabase db = db_manager.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " +
                "A.IdIncidencia," +
                "A.IdActivo," +
                "A.NombreAlta," +
                "A.FechaAlta," +
                "B.NoInvent," +
                "B.ActivoFijo," +
                "B.DenominacionDelActivoFijo "+
                "FROM tb_incidences A " +
                "INNER JOIN tb_activo B ON B.IdActivo = A.IdActivo " +
                "WHERE A.Status = 1", null);

        if (c != null) {
            try{
                c.moveToFirst();
                int i = 1;
                do {
                    View view = getLayoutInflater().inflate(getResources().getLayout(R.layout.incidence_feed_item), null);
                    TextView txt_name_indicator = view.findViewById(R.id.txt_name_indicator);
                    TextView txt_message = view.findViewById(R.id.txt_message);

                    txt_name_indicator.setBackgroundColor(getResources().getColor(getRandomColor()));

                    String name = c.getString(c.getColumnIndex("NombreAlta"));

                    txt_name_indicator.setText(name.substring(0,1));
                    txt_message.setText(name + " creó una incidencia "+getDate(c.getString(c.getColumnIndex("FechaAlta"))));

                    contents.addView(view);

                    i++;
                } while (c.moveToNext() && i < 3);
            }catch (CursorIndexOutOfBoundsException e){
                contents.addView(getLayoutInflater().inflate(getResources().getLayout(R.layout.empty_layout), null));
            }
        }else{
            contents.addView(getLayoutInflater().inflate(getResources().getLayout(R.layout.empty_layout), null));
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkIncidences();
    }

    private void initViews() {
        logo = findViewById(R.id.logo);
        contents = findViewById(R.id.contents);
        btn_excel =  findViewById(R.id.btn_add_file_xls);
        btn_alta = findViewById(R.id.btn_alta);
        btn_ajustes = findViewById(R.id.btn_ajustes);
        btn_inventario = findViewById(R.id.btn_inventario);
        btn_administracion = findViewById(R.id.btn_administracion);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)  {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GlobalPreferences.INTENT_RESULT_ADD_FILE_EXCEL && resultCode == RESULT_OK && data != null) {

            SQLiteDatabase db = db_manager.getWritableDatabase();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Los datos anteriores serán eliminados de forma permanente. ¿Desea continuar?")
                    .setPositiveButton("Aceptar", (dialog, id) -> {
                        db.execSQL("delete from tb_incidences");
                        db.execSQL("delete from tb_activo");
                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/" + getFileName(data.getData()));
                        pd = new ProgressDialog(MainMenu.this);
                        pd.setMessage("Creando datos, por favor espere...");
                        pd.setCancelable(false);
                        pd.show();
                        new Thread(() -> {
                            WorkbookSettings ws = new WorkbookSettings();
                            Workbook workbook;
                            ws.setGCDisabled(true);
                            if(file != null){
                                try {
                                    workbook = Workbook.getWorkbook(file);
                                    //Sheet sheet_activo = workbook.getSheet(0);
                                    Sheet sheet_activo = workbook.getSheet(0);

                                    /**Ingresamos activos*/
                                    for(int i = 0;i< sheet_activo.getRows();i++){
                                        Cell[] row = sheet_activo.getRow(i);
                                        if(row.length > 0 && i > 2){
                                            db.execSQL("INSERT INTO tb_activo (" +
                                                    "Sociedad," +
                                                    "ActivoFijo," +
                                                    "SN," +
                                                    "FechaCapitalizacion," +
                                                    "DenominacionDelActivoFijo," +
                                                    "ValorAdquisicion," +
                                                    "ValorLibros," +
                                                    "SupraAF," +
                                                    "NoSerie," +
                                                    "NoInvent," +
                                                    "NotaInventario," +
                                                    "Emplazamiento," +
                                                    "Local," +
                                                    "ClaseAF," +
                                                    "CentroCoste," +
                                                    "Status) VALUES (" +
                                                    "'"+row[1].getContents()+"'," +
                                                    "'"+row[2].getContents()+"'," +
                                                    "'"+row[3].getContents()+"'," +
                                                    "'"+row[4].getContents()+"'," +
                                                    "'"+row[5].getContents()+"'," +
                                                    "'"+row[6].getContents()+"'," +
                                                    "'"+row[7].getContents()+"'," +
                                                    "'"+row[8].getContents()+"'," +
                                                    "'"+row[9].getContents()+"'," +
                                                    "'"+row[10].getContents()+"'," +
                                                    "'"+row[11].getContents()+"'," +
                                                    "'"+row[12].getContents()+"'," +
                                                    "'"+row[13].getContents()+"'," +
                                                    "'"+row[14].getContents()+"'," +
                                                    "'"+row[15].getContents()+"'," +
                                                    "0)");

                                        }
                                    }

                                    /**Generamos ubicaciones*/
                                    /*Sheet sheet_ubicaciones = workbook.getSheet(0);
                                    for(int i = 0;i< sheet_ubicaciones.getRows();i++){
                                        Cell[] row = sheet_ubicaciones.getRow(i);
                                        if(row.length > 0 && i > 2){
                                            String centro = row[1].getContents();
                                            String emplazamiento = row[2].getContents();
                                            String denominacion = row[3].getContents();

                                            db.execSQL("INSERT INTO tb_ubications (Centro, Emplazamiento, Denominacion, Status) " +
                                                    "VALUES (" + centro + ", '" + emplazamiento + "', '" + denominacion + "', 1)");
                                        }
                                    }*/

                                    /**Generamos status*/
                                    /*Sheet sheet_status = workbook.getSheet(1);
                                    for(int i = 0;i< sheet_status.getRows();i++){
                                        Cell[] row = sheet_status.getRow(i);
                                        if(row.length > 0 && i > 2){
                                            String supranumAF = row[1].getContents();
                                            String denominacion = row[2].getContents();

                                            db.execSQL("INSERT INTO tb_status (SupranumAF, Denominacion, Status) " +
                                                    "VALUES (" + supranumAF + ", '" + denominacion + "', 1)");
                                        }
                                    }*/

                                    /**Generamos Tipos*/
                                    /*Sheet sheet_tipos = workbook.getSheet(2);
                                    for(int i = 0;i< sheet_tipos .getRows();i++){
                                        Cell[] row = sheet_tipos .getRow(i);
                                        if(row.length > 0 && i > 2){
                                            String clase = row[1].getContents();
                                            String denominacion;
                                            try{
                                                denominacion = row[2].getContents();
                                            }catch (ArrayIndexOutOfBoundsException e){
                                                denominacion = "";
                                            }


                                            db.execSQL("INSERT INTO tb_tipo_activo (Clase, Denominacion, Status) " +
                                                    "VALUES ('" + clase + "', '" + denominacion + "', 1)");
                                        }
                                    }*/

                                    runOnUiThread(() -> {
                                        pd.dismiss();
                                        Toast.makeText(MainMenu.this, "Datos creados correctamente.", Toast.LENGTH_SHORT).show();
                                    });
                                } catch (IOException | BiffException e) {
                                    e.printStackTrace();
                                    Log.e("MainMenu", e.getMessage());
                                    runOnUiThread(() -> {
                                        pd.dismiss();
                                        Toast.makeText(MainMenu.this, "Algo salió mal, intente nuevamente o revise su archivo.", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }
                        }).start();
                    })
                    .setNegativeButton("Cancelar", (dialog, id) -> {});
            builder.create();
            builder.show();
            return;

        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
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

    int getRandomColor(){
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int rand = random.nextInt(0, 6);
        switch (rand){
            case 1: return R.color.rojo_etiflex;
            case 2: return R.color.green;
            case 3: return R.color.blue_selected;
            case 4: return R.color.menu_orange;
            case 51: return R.color.menu_purple;
            default: return R.color.rojo_etiflex;
        }
    }
}