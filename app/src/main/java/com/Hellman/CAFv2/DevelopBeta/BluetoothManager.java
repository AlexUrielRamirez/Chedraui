package com.Hellman.CAFv2.DevelopBeta;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;

public class BluetoothManager {

    private OutputStream outputStream;
    private InputStream inStream;
    String cpcl = "! U1 setvar \"media.type\" \"label\"\n" +
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
    public BluetoothSocket init() throws IOException {
        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (blueAdapter != null) {
            if (blueAdapter.isEnabled()) {
                Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();

                if(bondedDevices.size() > 0) {
                    Object[] devices = (Object []) bondedDevices.toArray();
                    for (int position = 0; position < devices.length; position++){
                        Log.e("BTM","Returned->"+devices[position]);
                        BluetoothDevice device = (BluetoothDevice) devices[position];
                        ParcelUuid[] uuids = device.getUuids();
                        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                        socket.connect();
                        return socket;
                    }
                }
            } else {
                Log.e("error", "Por favor, primero encienda el bluetooth.");
            }
        }
        return null;
    }

    public void write(String s) throws IOException {
        outputStream.write(s.getBytes());
    }

    public void run() {
        final int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytes = 0;
        int b = BUFFER_SIZE;

        while (true) {
            try {
                bytes = inStream.read(buffer, bytes, BUFFER_SIZE - bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
