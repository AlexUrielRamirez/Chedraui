package com.SCAF.CAFv2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.SCAF.CAFv2.BuscadorEPC.Buscador;
import com.SCAF.CAFv2.Inventario.Details;
import com.SCAF.CAFv2.Inventario.Main;
import com.SCAF.SQLiteTools.SQLiteHelper;
import com.bumptech.glide.Glide;
import com.uhf.uhf.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import static com.Etiflex.Splash.GlobalPreferences.CODE_BAR_READER;
import static com.Etiflex.Splash.GlobalPreferences.PAGE_STATE_SETTING_UBICATION;
import static com.Etiflex.Splash.GlobalPreferences.PICK_IMAGE_FROM_CAMERA;
import static com.Etiflex.Splash.GlobalPreferences.PICK_IMAGE_FROM_GALLERY;
import static com.Etiflex.Splash.GlobalPreferences.db_manager;

public class ToolEPCLauncher extends AppCompatActivity {

    EditText et_epc;
    String use_case;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_tool_e_p_c_launcher);

        use_case = getIntent().getExtras().getString("use_case");

        et_epc = findViewById(R.id.et_epc);
        findViewById(R.id.btn_code_bar).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.etiflex.sdl", "com.zebra.sdl.SDLguiActivity"));
            startActivityForResult(intent, CODE_BAR_READER);
        });
        findViewById(R.id.btn_buscar).setOnClickListener(v->{
            String epc = et_epc.getText().toString();
            if(epc.length() > 0){
                search_content(epc);
            }else{
                Toast.makeText(this, "Por favor, ingrese un EPC válido", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void search_content(String epc) {
        db_manager = new SQLiteHelper(this);
        db = db_manager.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " +
                "A.IdActivo," +
                "A.NoInvent," +
                "A.Sociedad," +
                "A.ActivoFijo," +
                "A.SN," +
                "A.FechaCapitalizacion," +
                "A.DenominacionDelActivoFijo," +
                "A.ValorAdquisicion," +
                "A.ValorLibros," +
                "A.SupraAF," +
                "A.NoSerie," +
                "A.NoInvent," +
                "A.NotaInventario," +
                "A.Emplazamiento," +
                "A.Local," +
                "A.ClaseAF," +
                "A.CentroCoste,"+
                "A.Status " +
                "FROM tb_activo A " +
                "WHERE A.NoInvent = " + "\"" + epc.substring(0,24) + "\"", null);

        Main.ModelInventario model;
        if (c != null) {
            try {
                c.moveToFirst();
                do {
                    model = new Main.ModelInventario();
                    model.setId(c.getString(c.getColumnIndex("IdActivo")));
                    model.setEPC(c.getString(c.getColumnIndex("NoInvent")));
                    model.setNumero(c.getString(c.getColumnIndex("ActivoFijo")));
                    model.setNombre(c.getString(c.getColumnIndex("DenominacionDelActivoFijo")));
                    model.setDescripcion(c.getString(c.getColumnIndex("DenominacionDelActivoFijo")));
                    model.setSociedad(c.getString(c.getColumnIndex("Sociedad")));
                    model.setActivoFijo(c.getString(c.getColumnIndex("ActivoFijo")));
                    model.setSN(c.getString(c.getColumnIndex("SN")));
                    model.setFechaCapitalizacion(c.getString(c.getColumnIndex("FechaCapitalizacion")));
                    model.setDenominacionDelActivoFijo(c.getString(c.getColumnIndex("DenominacionDelActivoFijo")));
                    model.setValorAdquisicion(c.getString(c.getColumnIndex("ValorAdquisicion")));
                    model.setValorLibros(c.getString(c.getColumnIndex("ValorLibros")));
                    model.setSupraAF(c.getString(c.getColumnIndex("SupraAF")));
                    model.setNoSerie (c.getString(c.getColumnIndex("NoSerie")));
                    model.setNoInvent(c.getString(c.getColumnIndex("NoInvent")));
                    model.setNotaInventario(c.getString(c.getColumnIndex("NotaInventario")));
                    model.setEmplazamiento(c.getString(c.getColumnIndex("Emplazamiento")));
                    model.setLocal(c.getString(c.getColumnIndex("Local")));
                    model.setClaseAF(c.getString(c.getColumnIndex("ClaseAF")));
                    model.setCentroCoste(c.getString(c.getColumnIndex("CentroCoste")));
                    model.setStatus(c.getInt(c.getColumnIndex("Status")));
                } while (c.moveToNext());

                switch (use_case){
                    case "RADAR":
                        GlobalPreferences.CURRENT_TAG = model.getEPC();
                        startActivity(new Intent(this, Buscador.class));
                        break;
                    case "CONSULTA":
                        GlobalPreferences.current_inventory_item = model;
                        startActivity(new Intent(this, Details.class).putExtra("OnlyConsult", true));
                        break;
                }
            }catch (CursorIndexOutOfBoundsException e){
                Log.e("TEPCL", "Error->"+e.getMessage());
                Toast.makeText(this, "No se encontraron resultados", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "No se encontraron resultados", Toast.LENGTH_SHORT).show();
            Log.e("TEPCL", "Error->no data found");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            et_epc.setText(data.getDataString());
        }
    }
}