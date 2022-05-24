package com.example.pruebadogapp;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

public class BaseDatosRazasHelper extends SQLiteOpenHelper{

    String crearBaseDatos = "CREATE TABLE Razas (id INTEGER, nombre TEXT)"; //id para identificar a cada raza de manera única.

    public BaseDatosRazasHelper(Context contexto, String nombre, CursorFactory factory, int version){
        super(contexto, nombre, factory, version);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(crearBaseDatos);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        /*Nota: Lo correcto sería migrar los datos de la tabla antigua. Pero como es un ejercicio en el que al iniciar se
            extraen los valores mediante el webAPI, se elimina y crea de nuevo.
         */
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Razas");
        sqLiteDatabase.execSQL(crearBaseDatos);
    }


}
