package com.SCAF.SQLiteTools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String USERS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS tb_users(" +
            "IdUser INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "NombreUsuario TEXT, " +
            "MailUsuario TEXT, " +
            "Password TEXT, "+
            "Type INTEGER, "+
            "Status INTEGER)";
    private static final String ACTIVO_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS tb_activo(" +
            "IdActivo INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "Sociedad TEXT, " +
            "ActivoFijo TEXT, "+
            "SN TEXT, "+
            "FechaCapitalizacion TEXT, "+
            "DenominacionDelActivoFijo TEXT, "+
            "ValorAdquisicion TEXT, "+
            "ValorLibros TEXT, "+
            "SupraAF INTEGER, "+
            "NoSerie TEXT, "+
            "NoInvent TEXT, "+
            "NotaInventario TEXT, "+
            "Emplazamiento TEXT, "+
            "Local TEXT, "+
            "ClaseAF TEXT, "+
            "CentroCoste TEXT, "+
            "FechaLectura TEXT, "+
            "Status INTEGER)";
    private static final String INCIDENCES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS tb_incidences(" +
            "IdIncidencia INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "IdActivo INTEGER, " +
            "NombreAlta TEXT, "+
            "FechaAlta TEXT, "+
            "NombreBaja TEXT, "+
            "FechaBaja TEXT, "+
            "Motivo TEXT,"+
            "Comentario TEXT,"+
            "Status INTEGER)";
    private static final String UBICATIONS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS tb_ubications(" +
            "IdUbication INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "Centro INTEGER, " +
            "Emplazamiento TEXT, "+
            "Denominacion TEXT, "+
            "Status INTEGER)";
    private static final String STATUS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS tb_status(" +
            "IdStatus INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "SupranumAF INTEGER, " +
            "Denominacion TEXT, "+
            "Status INTEGER)";
    private static final String TIPO_ACTIVO_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS tb_tipo_activo(" +
            "IdTipo INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "Clase TEXT, " +
            "Denominacion TEXT, "+
            "Status INTEGER)";
    private static final String DB_NAME = "pepsico.sqlite";
    private static final int DB_VERSION = 7;
    public SQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(USERS_TABLE_CREATE);
        db.execSQL(ACTIVO_TABLE_CREATE);
        db.execSQL(UBICATIONS_TABLE_CREATE);
        db.execSQL(STATUS_TABLE_CREATE);
        db.execSQL(TIPO_ACTIVO_TABLE_CREATE);
        db.execSQL(INCIDENCES_TABLE_CREATE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
