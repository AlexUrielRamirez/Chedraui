package com.SCAF.CAFv2.Administracion.Usuarios;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.SCAF.CAFv2.Inventario.Details;
import com.SCAF.SQLiteTools.SQLiteHelper;
import com.uhf.uhf.R;

import java.util.ArrayList;

import static com.Etiflex.Splash.GlobalPreferences.PAGE_STATE_SETTING_UBICATION;
import static com.Etiflex.Splash.GlobalPreferences.current_user_item;
import static com.Etiflex.Splash.GlobalPreferences.db_manager;

public class Main extends AppCompatActivity {

    private RecyclerView rv_usuarios;
    private ArrayList<model_usuarios> main_list;
    SQLiteDatabase db;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_main_usuarios);

        db_manager = new SQLiteHelper(this);
        db = db_manager.getWritableDatabase();

        findViewById(R.id.imageView9).setOnClickListener(v -> {
            startActivityForResult(new Intent(this, NewUser.class), 0);
        });

        rv_usuarios = findViewById(R.id.rv_usuarios);
        rv_usuarios.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        loadData();
    }

    private void loadData() {
        main_list = new ArrayList<>();

        Cursor c = db.rawQuery("SELECT * FROM tb_users WHERE Status = 1", null);

        if (c != null) {
            try {
                c.moveToFirst();
                do {
                    model_usuarios model = new model_usuarios();
                    model.setIdUsuario(c.getInt(c.getColumnIndex("IdUser")));
                    model.setNombreUsuario(c.getString(c.getColumnIndex("NombreUsuario")));
                    model.setMail(c.getString(c.getColumnIndex("MailUsuario")));
                    model.setPassword(c.getString(c.getColumnIndex("Password")));
                    model.setType(c.getInt(c.getColumnIndex("Type")));
                    model.setStatus(c.getInt(c.getColumnIndex("Status")));
                    main_list.add(model);
                } while (c.moveToNext());
            }catch (CursorIndexOutOfBoundsException e){

            }
        }
        rv_usuarios.setAdapter(new rv_adapter());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case 0:
                    if(data.getDataString().equals("done")){
                        loadData();
                    }
                    break;
                case 1:
                    loadData();
                    break;
            }
        }
    }

    public class rv_adapter extends RecyclerView.Adapter<rv_adapter.ViewHolder>{

        Context context;
        @Override
        public rv_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_usuario,parent,false);
            context = parent.getContext();
            return new rv_adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(rv_adapter.ViewHolder holder, int position) {
            holder.txt_nombre_usuario.setText(main_list.get(position).getNombreUsuario());
            holder.btn_eliminar.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.btn_eliminar.getContext());
                builder.setMessage("¿Realmente desea eliminar al siguiente usuario?\n"+main_list.get(position).getNombreUsuario())
                        .setPositiveButton("Aceptar", (dialog, id) -> {
                            db.execSQL("UPDATE tb_users SET Status = 0 WHERE IdUser = "+ main_list.get(position).getIdUsuario());
                            loadData();
                        })
                        .setNegativeButton("Cancelar", (dialog, id) -> {

                        });
                builder.create();
                builder.show();
            });
            holder.btn_editar.setOnClickListener(v -> {
                current_user_item = main_list.get(position);
                startActivityForResult(new Intent(holder.btn_editar.getContext(), EditUser.class), 1);
            });
        }

        @Override
        public int getItemCount() {
            return main_list.size();
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

            TextView txt_nombre_usuario;
            ImageView btn_editar, btn_eliminar;

            public ViewHolder(View itemView) {
                super(itemView);
                txt_nombre_usuario = itemView.findViewById(R.id.txt_nombre_usuario);
                btn_editar = itemView.findViewById(R.id.btn_editar);
                btn_eliminar = itemView.findViewById(R.id.btn_eliminar);
            }
        }
    }

    public class model_usuarios{

        private int IdUsuario;
        private String NombreUsuario;
        private String Mail;
        private String Password;
        private int Type;
        private int Status;

        public int getIdUsuario() {
            return IdUsuario;
        }

        public void setIdUsuario(int idUsuario) {
            IdUsuario = idUsuario;
        }

        public String getNombreUsuario() {
            return NombreUsuario;
        }

        public void setNombreUsuario(String nombreUsuario) {
            NombreUsuario = nombreUsuario;
        }

        public String getMail() {
            return Mail;
        }

        public void setMail(String mail) {
            Mail = mail;
        }

        public String getPassword() {
            return Password;
        }

        public void setPassword(String password) {
            Password = password;
        }

        public int getType() {
            return Type;
        }

        public void setType(int type) {
            Type = type;
        }

        public int getStatus() {
            return Status;
        }

        public void setStatus(int status) {
            Status = status;
        }
    }

}