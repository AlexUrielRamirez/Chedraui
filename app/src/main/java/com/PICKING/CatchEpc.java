package com.PICKING;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.PICKING.Create.Picking;
import com.PICKING.Create.Picking_items;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.uhf.uhf.R;

import java.util.Arrays;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;

import static com.Etiflex.Splash.GlobalPreferences.DESDE;

public class CatchEpc extends AppCompatActivity {

    String PORT = "dev/ttyS4";
    RXObserver rx = null;
    RFIDReaderHelper mReader;
    TextView txt_epc;
    String EPC;
    String filtro;

    ProgressDialog pd;

    interface upload_op{
        @Multipart
        @POST("/setBox.php")
        void setData(
                @Part("data") String data,
                @Part("epc_pallete") String nop,
                Callback<Response> callback
        );
    }

    interface upload_op_pallete{
        @Multipart
        @POST("/setPallete.php")
        void setData(
                @Part("data") String data,
                @Part("destino") String destino,
                @Part("epc_pallete") String nop,
                Callback<Response> callback
        );
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catch_epc);
        new Methods().CambiarColorStatusBar(this, R.color.main);

        pd = new ProgressDialog(this);
        pd.setMessage("Cargando, por favor espere...");
        pd.setCancelable(false);

        //TextView txt_cabecera = findViewById(R.id.txt_cabecera);

        txt_epc = findViewById(R.id.txt_box_epc);
        if(DESDE.equals("PALLETS")){
            filtro = GlobalPreferences.FILTRO_PALLET;
            //txt_cabecera.setText("Leer RFID Pallet");
            txt_epc.setText("Esperando RFID de Pallet...");
        }else if(DESDE.equals("ITEMS")){
            filtro = GlobalPreferences.FILTRO_CAJA;
            //txt_cabecera.setText("Leer RFID Caja");
            txt_epc.setText("Esperando RFID de Caja...");
        }
        findViewById(R.id.btn_aceptar).setOnClickListener(v -> {
            upload_data_items();
        });

        setUpReader();
    }

    void upload_data_items(){
        pd.show();
        String data = "{";
        for(int position = 0; position < GlobalPreferences.tag_list_global.size(); position++){
            String row = "\""+position+"\":\""+GlobalPreferences.tag_list_global.get(position)+"\"";
            Log.i("CatchEPC: ", row);
            if(position < GlobalPreferences.tag_list_global.size() - 1){
                row = row + ",";
            }

            data = data + row;
        }

        data = data + "}";

        if(DESDE.equals("ITEMS")){
            new RestAdapter.Builder().setEndpoint("http://" + GlobalPreferences.TMP_IP + "/Chedraui/webservices/").build().create(upload_op.class).setData(data, EPC, new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    Toast.makeText(CatchEpc.this, "Caja ingresada correctamente", Toast.LENGTH_SHORT).show();
                    mReader.unRegisterObserver(rx);
                    CatchEpc.this.finish();
                }

                @Override
                public void failure(RetrofitError error) {
                    //Toast.makeText(CatchEpc.this, "Algo salió mal, revise su conexión", Toast.LENGTH_SHORT).show();
                    Toast.makeText(CatchEpc.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if(DESDE.equals("PALLETS")){
            Spinner sp = findViewById(R.id.spinner_destino);
            String destino = sp.getSelectedItem().toString();
            new RestAdapter.Builder().setEndpoint("http://" + GlobalPreferences.TMP_IP + "/Chedraui/webservices/").build().create(upload_op_pallete.class).setData(data, destino, EPC, new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    Toast.makeText(CatchEpc.this, "Pallet ingresado correctamente", Toast.LENGTH_SHORT).show();
                    mReader.unRegisterObserver(rx);
                    pd.dismiss();
                    CatchEpc.this.finish();
                }

                @Override
                public void failure(RetrofitError error) {
                    //Toast.makeText(CatchEpc.this, "Algo salió mal, revise su conexión", Toast.LENGTH_SHORT).show();
                    Toast.makeText(CatchEpc.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("CatchEPC: " , error.getMessage());
                    pd.hide();
                }
            });
        }


    }

    private void setUpReader() {
        rx = new RXObserver(){
            @Override
            protected void onInventoryTag(RXInventoryTag tag) {
                super.onInventoryTag(tag);
                new Thread(() -> {
                    if(tag.strEPC.replaceAll(" ", "").contains(filtro)){
                        EPC = tag.strEPC.replaceAll(" ", "");
                        CatchEpc.this.runOnUiThread(() -> txt_epc.setText(EPC));
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

}