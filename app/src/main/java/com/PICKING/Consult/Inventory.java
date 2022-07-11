package com.PICKING.Consult;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.SCAF.CAFv2.Inventario.Main;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.uhf.uhf.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Inventory extends AppCompatActivity {

    String PORT = "dev/ttyS4";
    RXObserver rx = null;
    RFIDReaderHelper mReader;

    String url = "http://" + GlobalPreferences.TMP_IP + "/Chedraui/getPallete.php";

    RecyclerView rv_tags;
    ArrayList<ModelTags> main_list;
    ArrayList<String> tag_list;

    TextView txt_contador;
    ProgressBar progress;
    Button btn_terminar;

    rv_adapter adapter;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.main);
        setContentView(R.layout.activity_inventory);

        url = "http://" + GlobalPreferences.TMP_IP + "/Chedraui/webservices/getPallete.php?EPCPallete="+getIntent().getStringExtra("EPC_pallete");

        rv_tags = findViewById(R.id.rv_tags);
        rv_tags.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        txt_contador = findViewById(R.id.txt_contador);
        progress = findViewById(R.id.progress_bar);
        btn_terminar = findViewById(R.id.btn_terminar);

        download_tags();
    }

    private void download_tags() {
        Volley.newRequestQueue(this).add(new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            JSONArray json = response.optJSONArray("Data");
            main_list = new ArrayList<>();
            tag_list = new ArrayList<>();
            try {
                for (int i = 0; i < json.length(); i++) {
                    ModelTags model = new ModelTags();
                    JSONObject jsonObject = null;
                    jsonObject = json.getJSONObject(i);
                    model.setId(jsonObject.optString("Id"));
                    model.setEPC(jsonObject.optString("EPC"));
                    model.setIdCaja(jsonObject.optString("IdPallete"));
                    model.setStatus(jsonObject.optString("Status"));
                    model.setReaded(0);

                    main_list.add(model);
                    tag_list.add(model.getEPC());
                }

                adapter = new rv_adapter(main_list);
                rv_tags.setAdapter(adapter);
                setUpProgress(0);
                progress.setMax(main_list.size());
                btn_terminar.setOnClickListener(v -> Inventory.this.finish());
                setUpReader();
            } catch (JSONException | NullPointerException e) {
                Log.e("Validacion", "JSON | Null Exception" + e);
            }
        }, error -> {
            Log.e("Validacion", "Volley error" + error);
        }));
    }

    void setUpProgress(int items_counted){
        txt_contador.setText(items_counted + "/" + main_list.size());
        progress.setProgress(items_counted);
    }

    private void setUpReader() {
        rx = new RXObserver(){
            @Override
            protected void onInventoryTag(RXInventoryTag tag) {
                super.onInventoryTag(tag);
                String EPC = tag.strEPC.replaceAll(" ", "").substring(0, 24);
                try {
                    if(tag_list.contains(EPC)){
                        new Thread(() -> {
                            for(int position = 0; position < tag_list.size(); position++){
                                if(main_list.get(position).getEPC().equals(EPC) && main_list.get(position).getReaded() == 0){
                                    main_list.get(position).setReaded(1);
                                    Inventory.this.runOnUiThread(() -> {
                                        adapter.notifyDataSetChanged();
                                        counter++;
                                        setUpProgress(counter);
                                    });
                                    break;
                                }
                            }
                        }).run();
                    }
                }catch (NullPointerException e){
                    Log.e("Truper", "Lecturas"+e.getMessage());
                }
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
            if(main_list != null && main_list.size() > 0){
                mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public class rv_adapter extends RecyclerView.Adapter<rv_adapter.ViewHolder> {
        Context context;
        private ArrayList<ModelTags> child_list;
        public rv_adapter(ArrayList<ModelTags> list){
            child_list = list;
        }
        @Override
        public rv_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_picking_inventory,parent,false);
            context = parent.getContext();
            return new rv_adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(rv_adapter.ViewHolder holder, int position) {
            holder.epc.setText(child_list.get(position).getEPC());
            holder.epc.setOnClickListener(v -> {
                GlobalPreferences.CURRENT_TAG = main_list.get(position).getEPC();
                startActivity(new Intent(context, com.SCAF.CAFv2.BuscadorEPC.Buscador.class));
            });
            if(child_list.get(position).getReaded() == 0){
                Glide.with(context).load(R.drawable.cancel_circle_red).into(holder.img_status);
            }else{
                Glide.with(context).load(R.drawable.done_circle_green).into(holder.img_status);
            }
        }

        @Override
        public int getItemCount() {
            return child_list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView epc;
            ImageView img_status;

            public ViewHolder(View itemView) {
                super(itemView);
                epc = itemView.findViewById(R.id.txt_epc);
                img_status = itemView.findViewById(R.id.img_status);
            }
        }
    }

    private class ModelTags{
        private String Id;
        private String EPC;
        private String IdCaja;
        private String Status;
        private int Readed;

        public String getId() {
            return Id;
        }

        public void setId(String id) {
            Id = id;
        }

        public String getEPC() {
            return EPC;
        }

        public void setEPC(String EPC) {
            this.EPC = EPC;
        }

        public String getIdCaja() {
            return IdCaja;
        }

        public void setIdCaja(String idCaja) {
            IdCaja = idCaja;
        }

        public String getStatus() {
            return Status;
        }

        public void setStatus(String status) {
            Status = status;
        }

        public int getReaded() {
            return Readed;
        }

        public void setReaded(int readed) {
            Readed = readed;
        }
    }

}