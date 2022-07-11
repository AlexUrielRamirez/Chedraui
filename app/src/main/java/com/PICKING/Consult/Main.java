package com.PICKING.Consult;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.uhf.uhf.R;

import static com.Etiflex.Splash.GlobalPreferences.CODE_BAR_READER;

public class Main extends AppCompatActivity {
    private EditText et_epc_pallete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consult);
        new Methods().CambiarColorStatusBar(this, R.color.main);
        et_epc_pallete = findViewById(R.id.et_epc_pallete);
        findViewById(R.id.btn_volver).setOnClickListener(v -> this.onBackPressed());

        findViewById(R.id.imageView10).setOnClickListener(v->{
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.etiflex.sdl", "com.zebra.sdl.SDLguiActivity"));
            startActivityForResult(intent, 0);
        });

        findViewById(R.id.btn_buscar).setOnClickListener(v -> {
            if(et_epc_pallete.getText().toString().length() > 0){
                Intent intent = getIntent();

                if (intent.hasExtra("Search")) {
                    GlobalPreferences.CURRENT_TAG = et_epc_pallete.getText().toString();
                    startActivity(new Intent(this, com.SCAF.CAFv2.BuscadorEPC.Buscador.class));
                } else {
                    Intent i = new Intent(this, Inventory.class);
                    i.putExtra("EPC_pallete", et_epc_pallete.getText().toString());
                    startActivity(i);
                }
            }else{
                Toast.makeText(this, "Ingrese un epc válido", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if (requestCode == 0) {
                et_epc_pallete.setText(data.getDataString().substring(0, 24));
            }
        }
    }

}