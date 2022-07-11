package com.SCAF.CAFv2.BuscadorEPC;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.Addons.ProgressBarAnimation;
import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.uhf.uhf.R;

public class Buscador extends AppCompatActivity {

    String PORT = "dev/ttyS4";
    RFIDReaderHelper mReader;
    ToneGenerator mToneGenerator;

    public static ConstraintLayout Buscador;
    public static ProgressBar pb_potencia;
    public static TextView CurrentEPC;
    private SeekBar sb_potencia;
    private TextView txt_indicador_potencia;

    private RXObserver rx_observer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.main);
        setContentView(R.layout.activity_buscador2);
        findViewById(R.id.btn_volver).setOnClickListener(v -> {this.onBackPressed();});

        initViews();
        rx_observer= new RXObserver(){
            @Override
            protected void onInventoryTag(RXInventoryTag tag) {
                super.onInventoryTag(tag);
                Log.e("EPC",tag.strEPC);
                if(tag.strEPC.replaceAll(" ", "").substring(0,24).equals(GlobalPreferences.CURRENT_TAG)){
                    Buscador.this.runOnUiThread(() -> {
                        pb_potencia.startAnimation(new ProgressBarAnimation(pb_potencia, Math.round(Float.parseFloat(tag.strRSSI)), 0));
                    });
                    mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100);
                }
            }
        };
        connectToAntenna();
    }

    private void initViews() {
        Buscador = findViewById(R.id.layout_buscador);
        CurrentEPC = findViewById(R.id.txt_epc);
        CurrentEPC.setText(GlobalPreferences.CURRENT_TAG);
        txt_indicador_potencia = findViewById(R.id.txt_indicador_potencia);
        sb_potencia = findViewById(R.id.sb_potencia);
        sb_potencia.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txt_indicador_potencia.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                byte btOutputPower = (byte)Integer.parseInt(String.valueOf(seekBar.getProgress()));
                mReader.setOutputPower((byte)0xff, btOutputPower);
            }
        });
        pb_potencia = findViewById(R.id.TrackPower);
    }



    @Override
    public void onBackPressed() {
        mReader.unRegisterObserver(rx_observer);
        this.finish();

    }

    private void connectToAntenna() {
        mToneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        ModuleConnector connector = new ReaderConnector();
        if (connector.connectCom(PORT, 115200)) {
            ModuleManager.newInstance().setUHFStatus(true);
            try {
                mReader = RFIDReaderHelper.getDefaultHelper();
                mReader.registerObserver(rx_observer);
            } catch (Exception e) {
                e.printStackTrace();
            }
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