package com.readertranslator.usilitel.readertranslator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// класс для работы с БД
public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dictionaryDb";
    public static final String TABLE_DICTIONARY = "dictionary";

    public static final String KEY_ID = "_id";
    public static final String KEY_WORD = "word";
    public static final String KEY_TRANSCRIPTION = "transcription";
    public static final String KEY_TRANSLATION = "translation";
    public static final String KEY_DATE = "callDate";
    public static final String KEY_TIME = "callTime";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // если БД отсутствует - создаем таблицу со словарем
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_DICTIONARY + "("
                +  KEY_ID + " integer primary key, "
                + KEY_WORD + " text, "
                + KEY_TRANSCRIPTION + " text, "
                + KEY_TRANSLATION + " text, "
                + KEY_DATE + " text DEFAULT CURRENT_DATE, "
                + KEY_TIME + " text DEFAULT CURRENT_TIME) ");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
