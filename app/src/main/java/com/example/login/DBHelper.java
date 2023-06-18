package com.example.login;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG="DEBUG_DBHelper";
    public static final String DBNAME = "FYP.db";
    public DBHelper(Context context) {
        super(context, "FYP.db", null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase MyDB) {
        MyDB.execSQL("create Table users(password TEXT, username TEXT primary key)");
        MyDB.execSQL("create Table contacts(ename TEXT, econtact TEXT, username TEXT, foreign key(username) references users(username))");
    }
    @Override
    public void onUpgrade(SQLiteDatabase MyDB, int i, int i1) {
        MyDB.execSQL("drop Table if exists users");
        MyDB.execSQL("drop Table if exists contacts");
    }
    public Boolean insertData(String username, String password){
        SQLiteDatabase MyDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("username", username);
        contentValues.put("password", password);
        long result = MyDB.insert("users",null,contentValues);
        if(result==-1) {
            return false;
        }
        else {
            return true;
        }
    }
    public Boolean checkusername(String username){
        SQLiteDatabase MyDB = this.getWritableDatabase();
        Cursor cursor = MyDB.rawQuery("Select * from users where username = ?", new String[]{username});
        if(cursor.getCount()>0)
            return true;
        else
            return false;
    }
    public Boolean checkusernamepassword(String username, String password){
        SQLiteDatabase MyDB = this.getWritableDatabase();
        Cursor cursor = MyDB.rawQuery("Select * from users where username = ? and password = ?", new String[]{username,password});
        if(cursor.getCount()>0)
            return true;
        else
            return false;
    }
    public Boolean insertuserdata(String name, String contact, String username)
    {
        SQLiteDatabase MyDB=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("econtact",contact);
        contentValues.put("ename",name);
        contentValues.put("username",username);
        long result= MyDB.insert("contacts",null,contentValues);
        if(result==-1){
            return false;
        }else{
            return true;
        }
    }
    public Cursor getdata(String un)
    {
        SQLiteDatabase MyDB=this.getWritableDatabase();
        Cursor cursor=MyDB.rawQuery("Select * from contacts where username = ?", new String[]{un});
        return cursor;
    }
    public Boolean deletedata(String un)
    {
        SQLiteDatabase MyDB = this.getWritableDatabase();
        Cursor cursor = MyDB.rawQuery("Select * from contacts where username = ?", new String[]{un});
        if(cursor.getCount()>0){
            long result = MyDB.delete("contacts", "username=?", new String[]{un});
            if(result==-1){
                return false;
            }else{
                return true;
            }
        }
        else{
            return false;
        }
    }
}