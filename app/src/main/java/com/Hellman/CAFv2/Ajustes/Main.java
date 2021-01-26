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

    // android built in classes for bluetooth operations
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    // needed for communication to bluetooth device / network
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
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
            try {
                String msg = "! U1 setvar \"media.type\" \"journal\"\n" +
                        "! U1 setvar \"media.sense_mode\" \"gap\"\n" +
                        "! 200 203 203 1215 1\n" +
                        "PCX 24 94\n" +
                        "\n" +
                        "       ] W , ,    \uF8F8\uF8F8\uF8F8                                            ,                                                             霞    \uEF17擱跧8    \uEF17擱跧8    \uEF17擱跧8    \uEF17擱跧8    \uEF17擱蒢?   \uEF17擱跧8    \uEF17擱跧8    \uEF17擱跧8    \uEF17擱跧8    \uEF17擱蒢?   \uEF17擱跧8    \uEF17擱跧8    \uEF17擱跧8    \uEF17擱跧8    \uEF17擱蜵\uF8F8???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????? ?闋 ?謊  ?闋 闋 ??賽$\uE58D闌#?轄I'?謝G? ?? ?鍵 ?謊  ?醜  ???\uE44C \uF0EE??謐! ?賺? \uE68B\uF8F8??@ 闋\uE682\uF8F8謠 !?謊 ? ??    鍵 ?謊 \t?\uEFB5 ? ??賽\"  闌 ?賺I ?\uE44F?R ??  \uF08F醜#?謝 @?      ??    錘 ?謊??騁?A   ??轅J?闋\uF2C5\uE2D3\uF8F8蹊 ?( 霞@\uE416\uF8F8?騁 鍵#颶 謊 \"?\uE485 ?  ??颶 闋 颶?謐N I$ x?????颶 闋\uF2C5?謊 D   騁 ?賺G??騁 鍵 闌 蹊/    ?醜 ??颶 闌 闋 謊 ?HA?鍍 ??颶\uF0EE詿鍵O謐 ?  ?霞 ??闋? 闋 醜 謊?D \uEF19\uF8F8闋C??闋?隱'謊\uF2C5?謊   ?韓 ??隱@  鍍 ?謠 謐\uE40D??霞 ??闋\t(@闋  \t賺/醜 闋C?霞\uEF20\uF8F8?闌?闋&\t 謊 鎂'鎂 ?霞 ??闋\" $闌  @A/闋 醜 ?韓%??隱 ?醜B@   闋\uF2C5??霞 ??闋? 隱   ?隱#謐 ?闋A??闋 $ 醜 ? \uEDDD? _?闈 ??霜z轂@闋 \uF307  鞠 ??闋#??騁 隱 ? /颶H ??鍍 ??颶 醜 ! ! 颶  ?? ?賺 ??颶 闌  @  颶 ?騁 ?謊'??颶 闋  ?\uEDDD?  颶 ????餵 鍵 %   騁   霞 騁  ??顆闊颶@闋?B\"/?  ?韓A霞 /?? ? 闋@ P  ?  ?醜  \t ?? ?鎂 ,?\uE40D\uF8F8??蹈  @ ??賽  \uE4AA? T  ? !?謐?  ?? A  闋 鍬? ?謊 ?醜  $??謊   鍵 r. O???闈\"A ??? @闋!鍵蹉H ?謊 ?霞?C??? 闌 鍾/  ?鎂G?騁D  ??謊 \"$醜\t鍬O  ?醜 ? B ??醞鄹邂?霜轄紐\uF8F8韓純\uF8F8霞 ??鍊 ??錘o??鍬\uE421\uF8F8?轄耿\uF8F8?鍵O??隸/??鍾O??鍊/??螟浯顆??螟纀霜??\uEDDD?鍛??岐霂避??避鍊O賸??民?W??\uF3FF燋?\uF8F8?觴奚??泥J\uE694\uF8F8?謐?W???T忍\uF8F8?轄R% ??鍵\uE765O??鞠\"R_??隸T\uE71C??餵? ??騁點 ??枓??$\uE695\uF8F8?賽'??鍬耿\uF8F8?闌/??韓諾\uF8F8?韓???騁??騁???餵 ??闈_??隱耿\uF8F8?鎂\uE421\uF8F8?購W??轂忱\uF8F8?轄\uE909\uF8F8????賺耿\uF8F8?隸 ??鎂耿\uF8F8?隱_??餵諾\uF8F8\n" +
                        "COUNTRY LATIN1\n" +
                        "SETMAG 1 1\n" +
                        "T180 5 0 259 1075 CAME : $ 2,750.00\n" +
                        "SETMAG 1 1\n" +
                        "T180 5 0 242 1045 Interciclo :   $ 0.00\n" +
                        "SETMAG 1 1\n" +
                        "T180 5 0 222 1015 Ahorro : $ 65.00\n" +
                        "SETMAG 1 1\n" +
                        "T180 5 0 297 985 Mora CAME  :   $ 0.00\n" +
                        "SETMAG 1 1\n" +
                        "T180 5 0 232 955 Mora PI :   $ 0.00\n" +
                        "SETMAG 1 1\n" +
                        "T180 5 0 202 925 Otros :   $ 0.00\n" +
                        "LINE 24 1086 374 1086 2\n" +
                        "LINE 24 886 374 886 2\n" +
                        "SETMAG 1 1\n" +
                        "T180 5 0 373 859 Entradas : $ 2,848.00\n" +
                        "INVERSE-LINE 373 830 139 830 29\n" +
                        "SETMAG 1 1\n" +
                        "T180 5 0 275 804   PAGAR? \n" +
                        "INVERSE-LINE 275 775 135 775 29\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 759 \"EN\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 320 759 MI\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 284 759 CARACTER\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 199 759 DE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 162 759 COTITULAR\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 70 759 DEL\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 728 CREDITO\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 291 728 OTORGADO\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 210 728 POR\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 169 728 CAME\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 119 728 AL\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 86 728 GRUPO\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 696 GENERADOR\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 281 696 DE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 253 696 INGRESOS\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 177 696 \"LAS\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 134 696 ESTRELLITAS\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 665 DE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 321 665 HILZILZINGO\",\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 189 665 RECIBI\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 113 665 DE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 70 665 LOS\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 634 COTITULARES\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 264 634 LA\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 236 634 CANTIDAD\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 159 634 DE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 130 634 $\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 110 634 2,848.00\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 603 (DOS\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 318 603 MIL\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 280 603 OCHOCIENTOS\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 178 603 CUARENTA\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 100 603 Y\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 78 603 OCHO\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 572 PESOS\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 314 572 00/100\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 255 572 M.N.)\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 204 572 PARA\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 161 572 SER\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 126 572 DEPOSITADO\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 541 EN\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 339 541 LA\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 313 541 CUENTA\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 255 541 DEL\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 222 541 MISMO\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 172 541 A\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 154 541 MAS\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 120 541 TARDAR\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 62 541 EL\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 510 DIA\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 332 510 DE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 307 510 HOY\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 274 510 .\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 258 510 DE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 233 510 NO\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 208 510 HACERSE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 143 510 EL\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 118 510 DEPOSITO,\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 479 DEBO\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 323 479 Y\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 305 479 PAGARE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 247 479 A\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 230 479 LA\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 204 479 ORDEN\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 154 479 DE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 128 479 CONSEJO\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 62 479 DE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 448 ASISTENCIA\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 273 448 AL\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 245 448 MICROEMPRENDEDOR\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 105 448 S.A.\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 62 448 DE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 417 C.V.\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 323 417 S.F.P.\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 265 417 ,\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 247 417 EN\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 222 417 MEXICO\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 164 417 D.F.\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 122 417 EL\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 96 417 DIA\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 62 417 23\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 386 DE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 336 386 AGOSTO\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 276 386 DEL\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 240 386 2013,\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 187 386 LA\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 159 386 CANTIDAD\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 83 386 DE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 54 386 $\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 355 2,848.00\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 289 355 (DOS\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 245 355 MIL\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 209 355 OCHOCIENTOS\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 110 355 CUARENTA\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 324 Y\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 335 324 OCHO\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 280 324 PESOS\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 218 324 00/100\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 148 324 M.N.)\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 86 324 VALOR\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 293 RECIBIDO\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 290 293 A\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 271 293 MI\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 244 293 ENTERA\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 185 293 SATISFACCION\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 78 293 ESTE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 262 PAGARE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 304 262 CAUSARA\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 234 262 UN\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 205 262 INTERES\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 136 262 DE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 107 262 $\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 86 262 28.48\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 231 (VEINTIOCHO\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 263 231 PESOS\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 209 231 48/100\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 148 231 M.N.)\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 94 231 DIARIO\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 200 DESDE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 314 200 LA\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 288 200 FECHA\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 237 200 DE\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 211 200 VENCIMIENTO\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 112 200 HASTA\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 62 200 SU\n" +
                        "SETMAG 1 2\n" +
                        "T180 0 0 365 173 TOTAL LIQUIDACION. \n" +
                        "LINE 24 119 374 119 1\n" +
                        "FORM\n" +
                        "PRINT";
                msg = "! U1 setvar \"media.type\" \"journal\"\n" +
                        "! U1 setvar \"media.sense_mode\" \"gap\"\n" +
                        "! 200 203 203 1215 1\n" +
                        "SETMAG 1 1\n" +
                        "T180 5 0 373 859 Entradas : $ 2,848.00\n";
                //msg += "\n";
                socket.getOutputStream().write(msg.getBytes());
            } catch (IOException e) {
                Log.e("BTM", "msg->"+e.getMessage());
            }
            //new SendPrint().execute();

            /*new SendPrint().execute();*/
            /*new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmanCAF/webservices/Loaders/").build().create(api_network_blank.class).setData("", new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    Toast.makeText(Main.this, "Reboot Complete", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(Main.this, "Error, sin conexión al servidor", Toast.LENGTH_SHORT).show();
                }
            });*/
        });
        initViews();
        preferences = loadPreferences();
        preferences_editor = loadEditor();
        et_direccion_ip.setText(preferences.getString("SERVER_IP", ""));
        et_direccion_ip_impresora.setText(preferences.getString("SERVER_PRINTER_IP", ""));
    }

    void findBT() {

        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(mBluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth no disponible", Toast.LENGTH_SHORT).show();
            }

            if(!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if(pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {

                    // RPP300 is the name of the bluetooth printer device
                    // we got this name from the list of paired devices
                    if (device.getName().equals("SW-LKP12")) {
                        mmDevice = device;
                        break;
                    }
                }
            }

            Toast.makeText(this, "dispositivo Bluetooth encontrado", Toast.LENGTH_SHORT).show();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void openBT() throws IOException {
        try {

            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();

            Toast.makeText(this, "Bluetooth abierto", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = mmInputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                Toast.makeText(Main.this, "Datos enviados", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendData() throws IOException {
        try {

            // the text typed by the user
            String msg = "Hola mundo";
            msg += "\n";

            mmOutputStream.write(msg.getBytes());

            // tell the user data were sent
            Toast.makeText(this, "Datos enviados", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
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