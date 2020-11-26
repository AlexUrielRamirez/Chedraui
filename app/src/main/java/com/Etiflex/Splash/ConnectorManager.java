package com.Etiflex.Splash;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.Etiflex.Splash.Principal.Principal;
import com.Etiflex.Splash.ROC.ReciboOrdenCompra;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;

import static com.Etiflex.Splash.GlobalPreferences.DEVICE;

public class ConnectorManager {

    public static ModuleConnector connector = new ReaderConnector();
    public static RFIDReaderHelper mReader;

    private String PORT;

    public void Connect(RXObserver rx){

        switch (DEVICE){
            case "HORCA":
                PORT = "dev/ttyS4";
                break;
            case "SPIDER":
                PORT = "dev/ttymxc2";
                break;
            default:

        }

        PORT = "dev/ttyS4";

        if (connector.connectCom(PORT, 115200)) {
            ModuleManager.newInstance().setUHFStatus(true);
            try {
                mReader = RFIDReaderHelper.getDefaultHelper();
                mReader.registerObserver(rx);
                Thread.currentThread().sleep(500);
                mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



}