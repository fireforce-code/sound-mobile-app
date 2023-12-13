package com.example.finalmobileproject;

import static android.provider.UserDictionary.Words._ID;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DecibelDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "decibeldata.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "Decibel";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_CURRENT_DECIBEL = "current";
    public static final String COLUMN_LAEQ_DECIBEL = "laeq";
    public static final String COLUMN_LMAX_DECIBEL = "lmax";
    public static final String COLUMN_LOUD_LEVEL = "loudlevel";



    public DecibelDBHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase myDB) {
        final String SQL_CREATE_DECIBEL_DATA_TABLE =
                "CREATE TABLE " + TABLE_NAME + "(" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_CURRENT_DECIBEL + " REAL NOT NULL, " +
                        COLUMN_LAEQ_DECIBEL + " REAL NOT NULL, " +
                        COLUMN_LMAX_DECIBEL + " REAL NOT NULL, " +
                        COLUMN_LOUD_LEVEL + " TEXT NOT NULL);";
        myDB.execSQL(SQL_CREATE_DECIBEL_DATA_TABLE);
        Log.d("Database", "Table created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase myDB, int i, int i1) {
        myDB.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(myDB);
    }

    public void removeItem(long id) {
        SQLiteDatabase myDB = getWritableDatabase();
        // Define 'where' part of query.
        String selection = _ID + "= ? ";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(id)};
        // Issue SQL statement.
        int deletedRows = myDB.delete(TABLE_NAME, selection, selectionArgs);
        //    myDB.close();
        //myDB.delete(UniversityContract.Student.TABLE_NAME, UniversityContract.Student._ID + "=" + id, null);
    }

    public ArrayList<DecibelDB> getAllItems() {
        ArrayList<DecibelDB> decibels = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                DecibelDBHelper.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DecibelDBHelper.COLUMN_CURRENT_DECIBEL + " DESC"
        );

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
            double currentDecibel = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CURRENT_DECIBEL));
            double currentLAeq = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LAEQ_DECIBEL));
            double currentLmax = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LMAX_DECIBEL));
            String loudLevel = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOUD_LEVEL));

            DecibelDB aDecibel = new DecibelDB(id, currentDecibel, currentLAeq , currentLmax, loudLevel);
            decibels.add(aDecibel);
        }

        cursor.close();
        return decibels;
    }

}
