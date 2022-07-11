package com.SCAF.CAFv2.Administracion.Usuarios;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.SCAF.SQLiteTools.SQLiteHelper;
import com.uhf.uhf.R;

import static com.Etiflex.Splash.GlobalPreferences.db_manager;

public class EditUser extends AppCompatActivity {

    private EditText et_nombre, et_mail, et_password;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_edit_user);

        Main.model_usuarios mModel = GlobalPreferences.current_user_item;

        db_manager = new SQLiteHelper(this);
        db = db_manager.getWritableDatabase();

        et_nombre = findViewById(R.id.et_nombre);
        et_mail = findViewById(R.id.et_mail);
        et_password = findViewById(R.id.et_password);

        et_nombre.setText(mModel.getNombreUsuario());
        et_mail.setText(mModel.getMail());
        et_password.setText(mModel.getPassword());

        findViewById(R.id.btn_guardar).setOnClickListener(v -> {
            if(et_nombre.getText().toString().length() > 0 && et_mail.getText().toString().length() > 0 && et_password.getText().toString().length() > 0){
                db.execSQL("UPDATE tb_users SET " +
                        "NombreUsuario = " + "\"" + et_nombre.getText().toString() + "\"," +
                        "MailUsuario = " + "\"" + et_mail.getText().toString() + "\"," +
                        "Password = " + "\"" + et_password.getText().toString() + "\"" +
                        "WHERE IdUser = " + mModel.getIdUsuario());
                Intent data = new Intent();
                data.setData(Uri.parse("done"));
                setResult(RESULT_OK, data);
                finish();
            }else{
                Toast.makeText(this, "Por favor, ingrese información válida", Toast.LENGTH_SHORT).show();
            }
        });

    }
}