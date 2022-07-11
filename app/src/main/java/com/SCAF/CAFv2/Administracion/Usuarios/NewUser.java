package com.SCAF.CAFv2.Administracion.Usuarios;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.Etiflex.Splash.Methods;
import com.SCAF.SQLiteTools.SQLiteHelper;
import com.uhf.uhf.R;

import static com.Etiflex.Splash.GlobalPreferences.db_manager;

public class NewUser extends AppCompatActivity {

    private EditText et_nombre, et_mail, et_password;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_new_user);

        et_nombre = findViewById(R.id.et_nombre);
        et_mail = findViewById(R.id.et_mail);
        et_password = findViewById(R.id.et_password);

        findViewById(R.id.btn_add_user).setOnClickListener(v -> {
            if(et_nombre.getText().toString().length() > 0 && et_mail.getText().toString().length() > 0 && et_password.getText().toString().length() > 0){
                db_manager = new SQLiteHelper(this);
                db = db_manager.getWritableDatabase();
                db.execSQL("INSERT INTO tb_users (NombreUsuario, MailUsuario, Password, Type, Status) VALUES ('"+et_nombre.getText().toString()+"','"+et_mail.getText().toString()+"', '"+et_password.getText().toString()+"', 1, 1)");
                Intent data = new Intent();
                data.setData(Uri.parse("done"));
                setResult(RESULT_OK, data);
                finish();
            }else{
                Toast.makeText(this, "Por favor, ingrese información válida", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent data = new Intent();
        data.setData(Uri.parse("undone"));
        setResult(RESULT_OK, data);
        finish();
    }
}