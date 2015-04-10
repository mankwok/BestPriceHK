package com.oufyp.bestpricehk.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.oufyp.bestpricehk.model.Product;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseHandler extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    private static DatabaseHandler dbInstance;
    private static final String DATABASE_NAME = "BestPriceHK";
    private static final String TABLE_FAVPRODUCT = "favProduct";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "Name";
    private static final String KEY_TYPE = "Type";
    private static final String KEY_BRAND = "Brand";
    // Login table name
    //field KEY_NAME = "Name";
    private static final String TABLE_LOGIN = "login";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UID = "uid";
    private static final String KEY_RANK = "rank";
    private static final String KEY_CREATED_AT = "created_at";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAVPRODUCT_TABLE = "CREATE TABLE " + TABLE_FAVPRODUCT + "("
                + KEY_UID + " TEXT,"
                + KEY_ID + " TEXT," + KEY_NAME + " TEXT,"
                + KEY_TYPE + " TEXT," + KEY_BRAND + " TEXT,"
                + " PRIMARY KEY ( " + KEY_UID + " , " + KEY_ID + "))";
        db.execSQL(CREATE_FAVPRODUCT_TABLE);
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_UID + " TEXT,"
                + KEY_RANK + " TEXT, "
                + KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVPRODUCT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
        // Create tables again
        onCreate(db);
    }
    public static DatabaseHandler getInstance(Context context) {
        if (dbInstance == null) {
            dbInstance = new DatabaseHandler(context.getApplicationContext());
        }
        return dbInstance;
    }
    public void addFavProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        HashMap<String, String> user = this.getUserDetails();
        String uid = user.get("uid");
        values.put(KEY_UID, uid);
        values.put(KEY_ID, product.getId());
        values.put(KEY_NAME, product.getName());
        values.put(KEY_TYPE, product.getType());
        values.put(KEY_BRAND, product.getBrand());
        db.insert(TABLE_FAVPRODUCT, null, values);
        db.close(); // Closing database connection
    }

    public ArrayList<Product> getFavPoducts() {
        ArrayList<Product> productList = new ArrayList<Product>();
        HashMap<String, String> user = this.getUserDetails();
        String uid = user.get("uid");
        String selectQuery = "SELECT  * FROM " + TABLE_FAVPRODUCT + " WHERE " + KEY_UID + " = '" + uid + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getString(1));
                product.setName(cursor.getString(2));
                product.setType(cursor.getString(3));
                product.setBrand(cursor.getString(4));
                // Adding contact to list
                productList.add(product);
            } while (cursor.moveToNext());
        }
        return productList;
    }

    public void deleteFavProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        HashMap<String, String> user = this.getUserDetails();
        String uid = user.get("uid");
        db.delete(TABLE_FAVPRODUCT, KEY_ID + " = ? and " + KEY_UID + " = ?", new String[]{String.valueOf(product.getId()), uid});
    }

    public void clearFavProduct() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVPRODUCT, null, null);
    }

    // Getting contacts Count
    public int getFavProductsCount() {
        HashMap<String, String> user = this.getUserDetails();
        String uid = user.get("uid");
        String countQuery = "SELECT  * FROM " + TABLE_FAVPRODUCT + " WHERE uid = '" + uid + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public boolean isFavourited(String pid) {
        HashMap<String, String> user = this.getUserDetails();
        String uid = user.get("uid");
        String query = "SELECT  * FROM " + TABLE_FAVPRODUCT + " WHERE " + KEY_ID + " = '" + pid + "' AND " + KEY_UID + " = '" + uid + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        } else {
            cursor.close();
            return true;
        }
    }

    /**
     * Storing user details in database
     */
    public void addUser(String name, String email, String uid, String rank,String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_UID, uid); // uid
        values.put(KEY_RANK,rank);
        values.put(KEY_CREATED_AT, created_at); // Created At

        // Inserting Row
        db.insert(TABLE_LOGIN, null, values);
    }

    /**
     * Getting user data from database
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_LOGIN;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("uid", cursor.getString(3));
            user.put("rank", cursor.getString(4));
            user.put("created_at", cursor.getString(5));
        }
        cursor.close();
        return user;
    }

    /**
     * Getting user login status
     * return true if rows are there in table
     */
    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + TABLE_LOGIN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        cursor.close();
        // return row count
        return rowCount;
    }

    /**
     * Re crate database
     * Delete all tables and create them again
     */
    public void resetTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_LOGIN, null, null);
        db.delete(TABLE_FAVPRODUCT, null, null);
    }

}
