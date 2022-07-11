package com.PICKING.Create;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.PICKING.CatchEpc;
import com.PICKING.Consult.Inventory;
import com.SCAF.CAFv2.Administracion.Incidences.Main;
import com.SCAF.CAFv2.Administracion.Incidences.ManageIncidence;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.uhf.uhf.R;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;

import static com.Etiflex.Splash.GlobalPreferences.CODE_BAR_READER;
import static com.Etiflex.Splash.GlobalPreferences.DESDE;

public class Picking extends AppCompatActivity {

    RecyclerView rv_tags;
    ArrayList<String> main_list;
    final int CODE_BAR_FOR_PALLETE = 33;
    String EPC_Pallete = "";

    String PORT = "dev/ttyS4";
    RXObserver rx = null;
    RFIDReaderHelper mReader;
    Button continuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picking_items);
        new Methods().CambiarColorStatusBar(this, R.color.main);
        findViewById(R.id.btn_volver).setOnClickListener(v->this.onBackPressed());

        //((TextView)findViewById(R.id.txt_header)).setText("Lectura de cajas");
        ((Button)findViewById(R.id.btn_agregar_items)).setText("¿Terminaste?... Crear Pallet");

        rv_tags = findViewById(R.id.rv_tags);
        rv_tags.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rv_tags.getItemAnimator().setChangeDuration(0);

        main_list = new ArrayList<>();

        rv_tags.setAdapter(new rv_adapter(main_list));

        findViewById(R.id.btn_agregar_items).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.etiflex.sdl", "com.zebra.sdl.SDLguiActivity"));
            startActivityForResult(intent, CODE_BAR_READER);
        });

        continuar = findViewById(R.id.btn_crear_orden);
        continuar.setText("CREAR PALLET");
        continuar.setOnClickListener(v -> {
            if(main_list.size() > 0){
                /*Toast.makeText(this, "Ingrese el número de caja", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.etiflex.sdl", "com.zebra.sdl.SDLguiActivity"));
                startActivityForResult(intent, CODE_BAR_FOR_PALLETE);*/
                GlobalPreferences.tag_list_global = main_list;
                DESDE = "PALLETS";
                mReader.unRegisterObserver(rx);
                startActivity(new Intent(this, CatchEpc.class));
                Picking.this.finish();
            }else{
                Toast.makeText(this, "Ingrese almenos un elemento", Toast.LENGTH_SHORT).show();
            }
        });
        setUpReader();
    }

    private void setUpReader() {
        rx = new RXObserver(){
            @Override
            protected void onInventoryTag(RXInventoryTag tag) {
                super.onInventoryTag(tag);
                new Thread(() -> {
                    String EPC = tag.strEPC.replaceAll(" ", "");
                    //EditText et_filtros = findViewById(R.id.et_filtros);
                    //String filtro = et_filtros.getText().toString();
                    if(!main_list.contains(EPC)){
                        if(GlobalPreferences.FILTRO_CAJA.length() > 0){
                            List<String> items = Arrays.asList(GlobalPreferences.FILTRO_CAJA.split("\\s*,\\s*"));
                            Log.e("Pickeo", "items: " + items);
                            for(int i = 0; i < items.size(); i++){
                                if(EPC.contains(items.get(i))){
                                    main_list.add(EPC);
                                    update_recycler();
                                    break;
                                }
                            }
                        }else{
                            main_list.add(EPC);
                            update_recycler();
                        }
                    }

                }).run();
            }
        };
        connectToAntenna();
    }

    private void connectToAntenna(){
        ModuleConnector connector = new ReaderConnector();
        if (connector.connectCom(PORT, 115200)) {
            ModuleManager.newInstance().setUHFStatus(true);
            try {
                mReader = RFIDReaderHelper.getDefaultHelper();
                mReader.setOutputPower((byte)0xff, (byte)5);
                mReader.registerObserver(rx);
            } catch (Exception e) {
                Log.e("Main", "error connecting to antenna->"+e.getMessage());
                e.printStackTrace();
            }
        }else{
            Log.e("Main", "error connecting to antenna");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == 134){
            mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
        }
        return super.onKeyDown(keyCode, event);
    }

    void update_recycler(){
        Picking.this.runOnUiThread(() -> {
            rv_tags.getAdapter().notifyDataSetChanged();
        });
    }

    private class rv_adapter extends RecyclerView.Adapter<rv_adapter.ViewHolder>{
        Context context;
        ArrayList<String> child_list;
        public rv_adapter(ArrayList<String> list){
            this.child_list = list;
        }

        @Override
        public rv_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_simple_tag,parent,false);
            context = parent.getContext();
            return new rv_adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(rv_adapter.ViewHolder holder, int position) {

            holder.tag.setText(child_list.get(position));
            holder.btn_delete.setOnClickListener(v->{
                main_list.remove(position);
                update_recycler();
            });

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

            TextView tag;
            ImageView btn_delete;

            public ViewHolder(View itemView) {
                super(itemView);
                tag = itemView.findViewById(R.id.txt_tag);
                btn_delete = itemView.findViewById(R.id.delete_item);
            }
        }
    }

}