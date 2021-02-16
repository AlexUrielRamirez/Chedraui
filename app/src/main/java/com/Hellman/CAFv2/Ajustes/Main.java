package com.Hellman.CAFv2.Ajustes;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.Hellman.CAFv2.DevelopBeta.BluetoothManager;
import com.uhf.uhf.R;

import org.w3c.dom.Text;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public class Main extends AppCompatActivity {

    private SharedPreferences preferences;
    private SharedPreferences.Editor preferences_editor;
    private EditText et_direccion_ip, et_direccion_ip_impresora;

    private interface api_network_blank{
        @FormUrlEncoded
        @POST("/blank.php")
        void setData(
                @Field("fake") String FakeVal,
                Callback<Response> callback
        );
    }

    BluetoothSocket socket;
    BluetoothManager btm = new BluetoothManager();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_main_ajustes);
        try {
            socket = btm.init();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("BTM", "msg->"+e.getMessage());
        }
        findViewById(R.id.btn_test).setOnClickListener(v->{

        });
        initViews();
        preferences = loadPreferences();
        preferences_editor = loadEditor();
        et_direccion_ip.setText(preferences.getString("SERVER_IP", ""));
        et_direccion_ip_impresora.setText(preferences.getString("SERVER_PRINTER_IP", ""));
    }

    private void initViews() {
        et_direccion_ip = findViewById(R.id.et_direccion_ip);
        et_direccion_ip_impresora = findViewById(R.id.et_direccion_ip_impresora);
        findViewById(R.id.btn_volver).setOnClickListener(v->{
            onBackPressed();
        });
        findViewById(R.id.btn_guardar).setOnClickListener(v->onBackPressed());
    }

    private SharedPreferences loadPreferences(){
        return getSharedPreferences("Global", Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor loadEditor(){
        return preferences.edit();
    }

    private void savePreferences(){
        preferences_editor = preferences.edit();
        preferences_editor.putString("SERVER_IP", et_direccion_ip.getText().toString());
        preferences_editor.putString("SERVER_PRINTER_IP", et_direccion_ip_impresora.getText().toString());
        preferences_editor.apply();

        GlobalPreferences.SERVER_PRINTER_IP = et_direccion_ip_impresora.getText().toString();
        GlobalPreferences.URL = et_direccion_ip.getText().toString();

        Toast.makeText(this, "Ajustes guardados correctamente", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        savePreferences();
        super.onBackPressed();
    }

    private class SendPrint extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String EPC = "189900000000000000000007";
                String TipoActivo = "Papel";
                String Oficina = "Johannes Wattendorff";
                String DescripcionActivo = "TEST";
                String desc_1 = "",desc_2 = "" ,desc_3 = "" ,desc_4 = "";
                if(DescripcionActivo.length() <= 50){
                    desc_1 = DescripcionActivo;
                    desc_2 = "";
                    desc_3 = "";
                    desc_4 = "";
                }else if(DescripcionActivo.length() >= 50 && DescripcionActivo.length() <= 100){
                    desc_1 = DescripcionActivo.substring(0,50);
                    desc_2 = DescripcionActivo.substring(50,DescripcionActivo.length());
                    desc_3 = "";
                    desc_4 = "";
                }else if(DescripcionActivo.length() >= 100){
                    desc_1 = DescripcionActivo.substring(0,50);
                    desc_2 = DescripcionActivo.substring(50,100);
                    desc_3 = DescripcionActivo.substring(100,DescripcionActivo.length());
                    desc_4 = "";
                }

                String zpl_2 = "^XA\n" +
                        "^RS,,,3,N,,,2\n" +
                        "^RR10\n" +
                        "^XZ\n" +
                        "<xpml><page quantity='0' pitch='24.0 mm'></xpml>^XA\n" +
                        "^SZ2^JMA\n" +
                        "^MCY^PMN\n" +
                        "^PW573\n" +
                        "~JSN\n" +
                        "^JZY\n" +
                        "^LH0,0^LRN\n" +
                        "^XZ\n" +
                        "<xpml></page></xpml><xpml><page quantity='1' pitch='24.0 mm'></xpml>~DGR:SSGFX000.GRF,790,10,:Z64:eJxNU7Fq21AUPc9qrUISmdChDlZlOnUrwhnymjiuP8DQfoAhhgwdI5NCJLCM2qmTmg8o7o+Y+JVCvQSU0UOoVDx4KcjFBRtMk94rpRANjwPSOefdc48AQCbIH91fBjk6SC9WOeqOzW4Giot0uFCMSjG02GF0GgKhZDSjN0aPgBjxkdKhmfxiTIq6bDKNjq2EFZ6SYfkrW23OgZfvz/lr8jsRbCqGwHe4TCaFS/xh1IBwxQ9GeyjY4hOjx0HREQOW0YOHCdYsbTSFQpcHetDZUDi94psnpQBHLKglBpov6oyCKjqWx3bn+3hTSVlmVYMpxizt1lDXIkarHs50nwWvWzj+kGXVauPsm8d2bReN3ymjngPTy7iOi/oi49oTPIl9RbaNj9BCqVCeGhPoA+LuzK0VimvyPVH7DudMWcGmAIgrEtQmAHH1AMSFIenmIC4sb0kxWDRNJduRJO722HQyX1SiutLhtIEq7fJQtVvMnULOLy3mzoSvriXvd6jdYGVn+6XEaN5Hi0jWgHkVr2L/5wTiqoTjUE4DysrA24G3pEGCjaC/TkecqVDm9tjinHXV340o1E2ld94d+XSpUlCwO6Gk5e4Gwn028Ci158Cvwpqz6tOkZpeL8Rl4PeSseOeHy7iX96A8De28G1uzmHLcIbo+4m7sqfu9Ehe8vyxquhz0XtZJErWyTpa+/O9pcX17k9XjXp9xENXnd73/e6vufoH8X/gHTBHUuQ==:F2F6\n" +
                        "^XA\n" +
                        "^FO36,233\n" +
                        "^BY3^BCN,22,N,N^FD>;"+EPC+"^FS\n" +
                        "^FO4,84\n" +
                        "^BQN,2,6^FDLA,"+EPC+"^FS\n" +
                        "^FT118,276\n" +
                        "^CI0\n" +
                        "^A0N,21,28^FD"+EPC+"^FS\n" +
                        "^FT146,43\n" +
                        "^A0N,21,27^FDMobiliario y Equipo^FS\n" +
                        "^FT146,99\n" +
                        "^A0N,21,27^FDJohannes Wattendorff^FS\n" +
                        "^FT146,71\n" +
                        "^A0N,21,27^FDOficinas^FS\n" +
                        "^FT417,65\n" +
                        "^A0N,54,72^FD"+EPC.substring(0, 4)+"^FS\n" +
                        "^FO432,71\n" +
                        "^BY2^BCN,18,N,N^FD>;"+EPC.substring(0, 4)+"^FS\n" +
                        "^FT146,139\n" +
                        "^A0N,29,20^FD"+desc_1+"\n" +
                        "^FT146,169\n" +
                        "^A0N,29,20^F"+desc_2+"^FS\n" +
                        "^FT146,199\n" +
                        "^A0N,29,20^FD"+desc_3+"^FS\n" +
                        "^FO30,10\n" +
                        "^XGR:SSGFX000.GRF,1,1^FS\n" +
                        "^PQ1,0,1,Y\n" +
                        "^RFW,H,2,12,1^FD"+EPC+"^FS\n"+
                        "^XZ\n" +
                        "<xpml></page></xpml>^XA\n" +
                        "^IDR:SSGFX000.GRF^XZ\n" +
                        "<xpml><end/></xpml>";
                /*"^XA\n" +
                        "^RS,,,3,N,,,2\n" +
                        "^RR10\n" +
                        "^XZ\n" +
                        "<xpml><page quantity='0' pitch='24.0 mm'></xpml>^XA\n" +
                        "^SZ2^JMA\n" +
                        "^MCY^PMN\n" +
                        "^PW573\n" +
                        "~JSN\n" +
                        "^JZY\n" +
                        "^LH0,0^LRN\n" +
                        "^XZ\n" +
                        "<xpml></page></xpml><xpml><page quantity='1' pitch='24.0 mm'></xpml>~DGR:SSGFX000.GRF,790,10,:Z64:eJxNk7+L02AYx7+vLzaDZ8LRRTDX1sm1coOvd72YzX/ikEKHupmC0AbbGnHpUIo4der9GzdICSjWQS4dHYoJ9vAWIYWALVSvPs8bBDN9X9738/z8BgCe2Mg/4886zJWplkMtxDd/PdGqsN1db/W13Qr3Wgmr2gY4/syqTTdWl9UyAKQiIX/w0aejMXZJPeYnlTrnqQBllyMYVeDk9ZxfUyglBoxcAl2xYEURfgodtQ8RwtM5IefIdB2BURcxxYcVFCpixElkaLo400nCWwFerrjyZD9A6yOpG14xcA8vGF6VUS+/4g7nJjxTURgxcPDM9LnHsULfSFltPNhixoTnofYm0uoU7Q89vj0dovFOsTIXaJ5pttSBs0u5LEVsi1jjbbUDO4sCWE71K4yY2IOVM4AcEasScww5IXaDEk1hm4YiwRE1Syx1TSwOo8oxQCzKvfVvwBnqHQ10Xpj+VaLz4m46DQ10id2f2WgG3gugGJ0gUlQzsb/ELlyYzH4vKAwd4Kl/aVG/R5z3nM6bB7pmiuUVUcuiEk1wZQXtuEfekImJxoWiFcvAdJsT/4rnfDPsH6fvKagrEmLJTbYr5yqLFO9IdO7FPSrvIc2D+iXVoP0aEzYG7ff59Tal0RN+3+YdyTtUZy1Lcm/cXsc0xwM67S2/8EST/30lpry/VHuBVi4+aX+s/nnSiiFj7RnKm41yP1MfuZ/xKJ1uciVmjOsv/xf+AqFTzgc=:1C98\n" +
                        "^XA\n" +
                        "^FO36,233\n" +
                        "^BY3^BCN,22,N,N^FD>;044000000000000000000004^FS\n" +
                        "^FO4,80\n" +
                        "^BQN,2,6^FDLA,044000000000000000000004^FS\n" +
                        "^FT153,111\n" +
                        "^CI0\n" +
                        "^A0N,33,22^FDRemodelacion banos almacen Puma (incluye:^FS\n" +
                        "^FT153,145\n" +
                        "^A0N,33,22^FDmuebles, red^FS\n" +
                        "^FT153,179\n" +
                        "^A0N,33,22^FDsanitaria,demolicion,mamparas,azulejo y area^FS\n" +
                        "^FT153,213\n" +
                        "^A0N,33,22^FDexhaustivas)^FS\n" +
                        "^FT118,276\n" +
                        "^A0N,21,29^FD044000000000000000000004^FS\n" +
                        "^FT153,40\n" +
                        "^A0N,21,27^FDTEOPE^FS\n" +
                        "^FT153,79\n" +
                        "^A0N,21,27^FDAlmacen Puma ^FS\n" +
                        "^FT282,40\n" +
                        "^A0N,21,27^FDAlmacen ^FS\n" +
                        "^FT411,65\n" +
                        "^A0N,58,78^FD0440^FS\n" +
                        "^FO458,71\n" +
                        "^BY1^BCN,18,N,N^FD>;0440^FS\n" +
                        "^FO30,2\n" +
                        "^XGR:SSGFX000.GRF,1,1^FS\n" +
                        "^PQ1,0,1,Y\n" +
                        "^RFW,H,2,12,1^FD044000000000300000000001^FS\n"+
                        "^XZ\n" +
                        "<xpml></page></xpml>^XA\n" +
                        "^IDR:SSGFX000.GRF^XZ\n" +
                        "<xpml><end/></xpml>"*/
                Log.e("main_ajustes", "Starting process");
                Socket clientSocket = new Socket(GlobalPreferences.SERVER_PRINTER_IP, 9100);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                outToServer.writeBytes(zpl_2);
                clientSocket.close();
                Log.e("main_ajustes", "Process ended succesfully");
            } catch (IOException e) {
                Log.e("main_ajustes", "Error -> "+e.getMessage());
            }
            return null;
        }
    }
}