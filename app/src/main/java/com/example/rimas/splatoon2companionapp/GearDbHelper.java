package com.example.rimas.splatoon2companionapp;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Rimas on 9/25/2017.
 */

public class GearDbHelper extends SQLiteOpenHelper {
    private static final String SQL_CREATE_GEAR_TABLE =  "CREATE TABLE "
            + GearContract.GearEntry.TABLE_GEAR + " ("
            + GearContract.GearEntry._ID + " INTEGER PRIMARY KEY, "
            + GearContract.GearEntry.COLUMN_GNAME + " TEXT, "
            + GearContract.GearEntry.COLUMN_TYPE_ID + " INTEGER, "
            + GearContract.GearEntry.COLUMN_BRAND_ID + " INTEGER, "
            + GearContract.GearEntry.COLUMN_ABILITY_ID + " INTEGER, "
            + GearContract.GearEntry.COLUMN_ACQUISITION_METHOD_ID + " INTEGER, "
            + GearContract.GearEntry.COLUMN_RARITY + " INTEGER, "
            + GearContract.GearEntry.COLUMN_AVAILABLE + " INTEGER, "
            + GearContract.GearEntry.COLUMN_SELECTED + " INTEGER)";

    private static final String SQL_CREATE_ABILITIES_TABLE = "CREATE TABLE "
            + GearContract.GearEntry.TABLE_ABILITIES + " ("
            + GearContract.GearEntry._ID + " INTEGER PRIMARY KEY, "
            + GearContract.GearEntry.COLUMN_ABILITY + " TEXT)";

    private static final String SQL_CREATE_ACQUISITION_METHOD_TABLE = "CREATE TABLE "
            + GearContract.GearEntry.TABLE_ACQUISITION_METHODS + " ("
            + GearContract.GearEntry._ID + " INTEGER PRIMARY KEY, "
            + GearContract.GearEntry.COLUMN_ACQUISITION + " TEXT)";

    private static final String SQL_CREATE_BRANDS_TABLE = "CREATE TABLE "
            + GearContract.GearEntry.TABLE_BRANDS + " ("
            + GearContract.GearEntry._ID + " INTEGER PRIMARY KEY, "
            + GearContract.GearEntry.COLUMN_BRAND + " TEXT, "
            + GearContract.GearEntry.COLUMN_COMMON_ABILITY + " INTEGER, "
            + GearContract.GearEntry.COLUMN_UNCOMMON_ABILITY + " INTEGER)";

    private static final String SQL_CREATE_GEAR_TO_ABILITIES_TABLE = "CREATE TABLE "
            + GearContract.GearEntry.TABLE_GEAR_TO_ABILITIES + " ("
            + GearContract.GearEntry._ID + " INTEGER PRIMARY KEY, "
            + GearContract.GearEntry.COLUMN_GEAR_ID + " INTEGER, "
            + GearContract.GearEntry.COLUMN_ABILITY_ID + " INTEGER)";

    private static final String SQL_CREATE_TYPES_TABLE = "CREATE TABLE "
            + GearContract.GearEntry.TABLE_TYPES + " ("
            + GearContract.GearEntry._ID + " INTEGER PRIMARY KEY, "
            + GearContract.GearEntry.COLUMN_TYPE_NAME + " TEXT)";

    private static final String SQL_WIPE_DATABASE_CLEAN =
            "DROP TABLE IF EXISTS " + GearContract.GearEntry.TABLE_ABILITIES + "; " +
            "DROP TABLE IF EXISTS " + GearContract.GearEntry.TABLE_ACQUISITION_METHODS + "; " +
            "DROP TABLE IF EXISTS " + GearContract.GearEntry.TABLE_BRANDS + "; " +
            "DROP TABLE IF EXISTS " + GearContract.GearEntry.TABLE_GEAR_TO_ABILITIES + "; " +
            "DROP TABLE IF EXISTS " + GearContract.GearEntry.TABLE_TYPES + "; " +
            "DROP TABLE IF EXISTS " + GearContract.GearEntry.TABLE_GEAR + ";";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Splatoon2Companion.db";

    public GearDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_GEAR_TABLE);
        db.execSQL(SQL_CREATE_ABILITIES_TABLE);
        db.execSQL(SQL_CREATE_ACQUISITION_METHOD_TABLE);
        db.execSQL(SQL_CREATE_BRANDS_TABLE);
        db.execSQL(SQL_CREATE_GEAR_TO_ABILITIES_TABLE);
        db.execSQL(SQL_CREATE_TYPES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onDowngrade(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_WIPE_DATABASE_CLEAN);
        onCreate(db);
    }

    public boolean doesTableExist(SQLiteDatabase db, String tableName)
    {
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        else {
            cursor.close();
            return false;
        }
    }
}