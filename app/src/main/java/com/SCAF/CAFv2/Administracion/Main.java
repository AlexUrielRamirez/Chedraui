package com.SCAF.CAFv2.Administracion;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.SCAF.CAFv2.Administracion.Activo.admin_activo;
import com.SCAF.CAFv2.Administracion.Impresion.admin_impresion;
import com.SCAF.CAFv2.Administracion.Incidences.admin_incidences;
import com.SCAF.CAFv2.Administracion.Traspasos.admin_traspasos;
import com.SCAF.CAFv2.Administracion.Ubicaciones.admin_ubicaciones;
import com.SCAF.CAFv2.ToolEPCLauncher;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.uhf.uhf.R;

import org.apache.commons.codec.binary.Hex;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE;
import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE_IDLE;
import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE_INCIDENCES;
import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE_ITEM;
import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE_PRINT;
import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE_TRANSFER;
import static com.Etiflex.Splash.GlobalPreferences.ADMIN_PAGE_STATE_UBICATIONS;
import static com.Etiflex.Splash.GlobalPreferences.CODE_BAR_READER;
import static com.Etiflex.Splash.GlobalPreferences.db_manager;
import static com.SCAF.CAFv2.Administracion.Traspasos.admin_traspasos.HolderCamera;
import static com.SCAF.CAFv2.Administracion.Traspasos.admin_traspasos.Panel_crear;
import static com.SCAF.CAFv2.Administracion.Traspasos.admin_traspasos.scanner;

public class Main extends AppCompatActivity {

    Methods methods;

    private ImageView btn_on_back_pressed;
    private CardView menu_excel, menu_incidencias, menu_activo, menu_usuarios;
    private TextView NombreUsuario;

    private interface check_admin_pass{
        @FormUrlEncoded
        @POST("/check_admin_acces.php")
        void setData(
                @Field("pass") String Password,
                Callback<Response> callback
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        methods = new Methods();
        methods.CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_main_administracion);
        initViews();
        setUpViews();
        //checkUserLevel();
    }

    private void checkUserLevel() {
        if(GlobalPreferences.NIVEL_USUARIO != 1){
            BottomSheetDialog bsd = new BottomSheetDialog(this);
            bsd.setContentView(R.layout.bsd_admin_password);
            bsd.setCancelable(false);
            EditText et_contrasena = bsd.findViewById(R.id.et_contrasena);
            bsd.findViewById(R.id.btn_continuar).setOnClickListener(v->{
                if(et_contrasena.getText().length() != 0){
                    new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmannCAF/webservices/Administracion").build().create(check_admin_pass.class).setData(et_contrasena.getText().toString(), new Callback<Response>() {
                        @Override
                        public void success(Response response, Response response2) {
                            try {
                                if(new BufferedReader(new InputStreamReader(response.getBody().in())).readLine().equals("ok")){
                                    Toast.makeText(Main.this, "¡Gracias!", Toast.LENGTH_SHORT).show();
                                    bsd.dismiss();
                                }else{
                                    et_contrasena.setText("");
                                    Toast.makeText(Main.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                                }
                            }catch (IOException e){
                                Toast.makeText(Main.this, "Algo salió mal, intente nuevamente por favor", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(Main.this, "Error, revise su conexión", Toast.LENGTH_SHORT).show();
                            Log.e("Administracion", error.getMessage());
                        }
                    });
                }else{
                    Toast.makeText(this, "Por favor, ingrese una contraseña válida", Toast.LENGTH_SHORT).show();
                }
            });
            bsd.findViewById(R.id.btn_volver).setOnClickListener(v->{
                this.finish();
            });
            bsd.show();
        }
    }

    private void initViews() {
        NombreUsuario = findViewById(R.id.textView34);

        btn_on_back_pressed = findViewById(R.id.btn_onbackpressed);
        menu_excel = findViewById(R.id.btn_generar_excel);
        menu_incidencias = findViewById(R.id.btn_admin_incidencias);
        menu_activo = findViewById(R.id.btn_administrar_activo);
        menu_usuarios = findViewById(R.id.btn_admin_usuarios);

        findViewById(R.id.btn_radar_sonoro).setOnClickListener(v -> launch_tool_manager("RADAR"));
        findViewById(R.id.btn_consultar_activo).setOnClickListener(v -> launch_tool_manager("CONSULTA"));

    }

    private void launch_tool_manager(String use_case){
        Intent i = new Intent(this, ToolEPCLauncher.class);
        switch (use_case){
            case "RADAR":
                i.putExtra("use_case","RADAR");
                break;
            case "CONSULTA":
                i.putExtra("use_case","CONSULTA");
                break;
        }
        startActivity(i);
    }

    private void setUpViews() {
        NombreUsuario.setText(GlobalPreferences.NOMBRE_USUARIO);
        btn_on_back_pressed.setOnClickListener(v-> this.onBackPressed());
        menu_excel.setOnClickListener(v->{
            BottomSheetDialog bsd = new BottomSheetDialog(this);
            bsd.setContentView(R.layout.bsd_generate_excel);

            EditText et_file_name = bsd.findViewById(R.id.et_nombre);
            Button btn_aceptar = bsd.findViewById(R.id.btn_aceptar);
            Button btn_volver = bsd.findViewById(R.id.btn_volver);

            btn_aceptar.setOnClickListener(a->{
                if(et_file_name.getText().toString().length() > 0){
                    ProgressDialog pd = new ProgressDialog(this);
                    pd.setMessage("Generando archivo, por favor espere...");
                    pd.setCancelable(false);
                    pd.show();

                    HSSFWorkbook workbook = new HSSFWorkbook();
                    HSSFSheet firstSheet = workbook.createSheet("LAYOUT");
                    HSSFRow rows_ = firstSheet.createRow(1);

                    rows_.createCell(1).setCellValue(new HSSFRichTextString("Sociedad"));
                    rows_.createCell(2).setCellValue(new HSSFRichTextString("Activo Fijo"));
                    rows_.createCell(3).setCellValue(new HSSFRichTextString("SN°"));
                    rows_.createCell(4).setCellValue(new HSSFRichTextString("Fecha Capitalización"));
                    rows_.createCell(5).setCellValue(new HSSFRichTextString("Denominación del Activo Fijo"));
                    rows_.createCell(6).setCellValue(new HSSFRichTextString("Valor Adquisición"));
                    rows_.createCell(7).setCellValue(new HSSFRichTextString("Valor Libros"));
                    rows_.createCell(8).setCellValue(new HSSFRichTextString("Supra AF"));
                    rows_.createCell(9).setCellValue(new HSSFRichTextString("No Serie"));
                    rows_.createCell(10).setCellValue(new HSSFRichTextString("No Invent"));
                    rows_.createCell(11).setCellValue(new HSSFRichTextString("Nota Inventario"));
                    rows_.createCell(12).setCellValue(new HSSFRichTextString("Emplazamiento"));
                    rows_.createCell(13).setCellValue(new HSSFRichTextString("Local"));
                    rows_.createCell(14).setCellValue(new HSSFRichTextString("Clase AF"));
                    rows_.createCell(15).setCellValue(new HSSFRichTextString("Centro Coste"));
                    rows_.createCell(16).setCellValue(new HSSFRichTextString("Status"));
                    rows_.createCell(17).setCellValue(new HSSFRichTextString("Fecha de Lectura"));
                    rows_.createCell(18).setCellValue(new HSSFRichTextString("Motivo"));

                    SQLiteDatabase db = db_manager.getWritableDatabase();
                    Cursor c = db.rawQuery("SELECT A.*, B.Motivo FROM tb_activo A " +
                            "LEFT JOIN tb_incidences B ON B.IdActivo = A.IdActivo AND B.Status = 1", null);

                    if (c != null) {
                        c.moveToFirst();
                        int i = 3;
                        do {
                            HSSFRow rows = firstSheet.createRow(i);

                            rows.createCell(1).setCellValue(new HSSFRichTextString(c.getString(c.getColumnIndex("Sociedad"))));
                            rows.createCell(2).setCellValue(new HSSFRichTextString(c.getString(c.getColumnIndex("ActivoFijo"))));
                            rows.createCell(3).setCellValue(new HSSFRichTextString(c.getString(c.getColumnIndex("SN"))));
                            rows.createCell(4).setCellValue(new HSSFRichTextString(c.getString(c.getColumnIndex("FechaCapitalizacion"))));
                            rows.createCell(5).setCellValue(new HSSFRichTextString(c.getString(c.getColumnIndex("DenominacionDelActivoFijo"))));
                            rows.createCell(6).setCellValue(new HSSFRichTextString(c.getString(c.getColumnIndex("ValorAdquisicion"))));
                            rows.createCell(7).setCellValue(new HSSFRichTextString(c.getString(c.getColumnIndex("ValorLibros"))));
                            rows.createCell(8).setCellValue(new HSSFRichTextString(c.getString(c.getColumnIndex("SupraAF"))));
                            rows.createCell(9).setCellValue(new HSSFRichTextString(c.getString(c.getColumnIndex("NoSerie"))));
                            rows.createCell(10).setCellValue(new HSSFRichTextString(c.getString(c.getColumnIndex("NoInvent"))));
                            rows.createCell(11).setCellValue(new HSSFRichTextString(c.getString(c.getColumnIndex("NotaInventario"))));
                            rows.createCell(12).setCellValue(new HSSFRichTextString(c.getString(c.getColumnIndex("Emplazamiento"))));
                            rows.createCell(13).setCellValue(new HSSFRichTextString(c.getString(c.getColumnIndex("Local"))));
                            rows.createCell(14).setCellValue(new HSSFRichTextString(c.getString(c.getColumnIndex("ClaseAF"))));
                            rows.createCell(15).setCellValue(new HSSFRichTextString(c.getString(c.getColumnIndex("CentroCoste"))));

                            String estado = null, fecha_lectura = null, motivo = " ";

                            switch (c.getString(c.getColumnIndex("Status"))){
                                case "0":
                                    estado = "FALTANTE";
                                    fecha_lectura = "No especificado";
                                    break;
                                case "1":
                                    estado = "ENCONTRADO";
                                    fecha_lectura = c.getString(c.getColumnIndex("FechaLectura"));
                                    break;
                                case "2":
                                    estado = "INCIDENCIA";
                                    fecha_lectura = "No especificado";
                                    motivo = getMotivo(c.getString(c.getColumnIndex("Motivo")));
                                    break;
                            }

                            rows.createCell(16).setCellValue(new HSSFRichTextString(estado));
                            rows.createCell(17).setCellValue(new HSSFRichTextString(fecha_lectura));
                            rows.createCell(18).setCellValue(new HSSFRichTextString(motivo));

                            i++;
                        } while (c.moveToNext());
                    }

                    FileOutputStream fos = null;
                    try {
                        String str_path = Environment.getExternalStorageDirectory().toString()+"/Download/";
                        //String str_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

                        File file ;
                        file = new File(str_path,et_file_name.getText().toString() + ".xls");
                        fos = new FileOutputStream(file);
                        workbook.write(fos);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al generar el archivo, intente nuevamente", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    } finally {
                        if (fos != null) {
                            try {
                                fos.flush();
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(this, "Archivo guardado con exito!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                        bsd.dismiss();
                    }
                }else{
                    Toast.makeText(this, "Error, ingrese un nombre válido", Toast.LENGTH_SHORT).show();
                }
            });
            btn_volver.setOnClickListener(a->bsd.dismiss());
            bsd.show();
        });
        menu_activo.setOnClickListener(v->{

        });
        menu_incidencias.setOnClickListener(v->{
            startActivity(new Intent(this, com.SCAF.CAFv2.Administracion.Incidences.Main.class));
        });
        menu_usuarios.setOnClickListener(v->{
            startActivity(new Intent(this, com.SCAF.CAFv2.Administracion.Usuarios.Main.class));
        });
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

}