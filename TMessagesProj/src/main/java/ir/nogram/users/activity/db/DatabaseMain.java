package ir.nogram.users.activity.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import ir.nogram.users.activity.holder.HoldCatDialog;
import ir.nogram.users.activity.holder.HoldCatFav;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by MhkDeveloper on 19/06/2017.
 */

public class DatabaseMain extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "mainDb";

    private static final String TABLE_FAVE_CAT = "TABLE_FAVE_CAT";
    private static final String CAT_ID = "CAT_ID";
    private static final String CAT_NAME = "CAT_NAME";
    private static final String CAT_CHAT_ID = "CAT_CHAT_ID";
    private static final String TAG = "DATABASEMAIN";

    private static final String TABLE_GHOT = "TABLE_GHOT";
    private static final String GHOT_ID = "GHOT_ID";
    private static final String GHOT_USER_ID = "GHOT_USER_ID";
    private static final String GHOT_TYPE = "GHOT_TYPE";

    private static final String TABLE_ADV = "TABLE_ADV";
    private static final String ADV_ID = "ADV_ID";
    private static final String ADV_MID = "ADV_MID";
    private static final String ADV_CH_ID = "ADV_CH_ID";
    private static final String ADV_UID = "ADV_UID";
    private static final String ADV_CHAT_USER_NAME = "ADV_CHAT_USER_NAME";

    private static final String TABLE_CAT_DIALOG = "TABLE_CAT_DIALOG";
    private static final String CAT_DIALOG__ID = "CAT_DIALOG__ID";
    private static final String CAT_DIALOG_NAME = "CAT_DIALOG_NAME";

    private static final String TABLE_CAT_DIALOG_SUB = "TABLE_CAT_DIALOG_SUB";
    private static final String CAT_DIALOG_SUB_DIALOG_ID = "CAT_DIALOG_SUB_DIALOG_ID";
    private static final String CAT_DIALOG_SUB_CAT_ID = "CAT_DIALOG_SUB_CAT_ID";

    private static final String TABLE_ADS_EX_DIALOG= "TABLE_ADS_EX_DIALOG";
    private static final String ADS_EX_DIALOG_ID = "ADS_EX_DIALOG_ID";

    private SQLiteDatabase mDatabase;
    private static DatabaseMain Instance;

    public static DatabaseMain getInstance(Context context) {

        DatabaseMain localInstance = Instance;
        if (localInstance == null) {
            synchronized (DatabaseMain.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new DatabaseMain(context);
                }
            }
        }
        return localInstance;
    }

    public DatabaseMain(@NonNull Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        String CREATE_TABLE_CATS = "CREATE TABLE " + TABLE_FAVE_CAT + '(' +
                CAT_ID + " INTEGER PRIMARY KEY," +
                CAT_CHAT_ID + " INTEGER," +
                CAT_NAME + " TEXT" +
                 ')';
        db.execSQL(CREATE_TABLE_CATS);
        String CREATE_TABLE_GHSOTS = "CREATE TABLE " + TABLE_GHOT + '(' +
                GHOT_ID + " INTEGER PRIMARY KEY," +
                GHOT_USER_ID + " INTEGER," +
                GHOT_TYPE + " INTEGER," +
                "UNIQUE("+ GHOT_USER_ID +","+ GHOT_TYPE +") ON CONFLICT REPLACE"+
                 ')';
         db.execSQL(CREATE_TABLE_GHSOTS);
        String CREATE_TABLE_ADV = "CREATE TABLE " + TABLE_ADV + '(' +
                ADV_ID + " INTEGER PRIMARY KEY," +
                ADV_CH_ID + " INTEGER," +
                ADV_MID + " INTEGER," +
                ADV_UID + " INTEGER," +
                ADV_CHAT_USER_NAME + " TEXT," +
                "UNIQUE("+ ADV_UID +","+ ADV_MID +") ON CONFLICT REPLACE"+
                 ')';
         db.execSQL(CREATE_TABLE_ADV);

        String CREATE_TABLE_CAT_DIALOG = "CREATE TABLE " + TABLE_CAT_DIALOG + '(' +
                CAT_DIALOG__ID + " INTEGER PRIMARY KEY," +
                CAT_DIALOG_NAME + " TEXT" +
                ')';
        db.execSQL(CREATE_TABLE_CAT_DIALOG);
        String CREATE_TABLE_CAT_DIALOG_SUB = "CREATE TABLE " + TABLE_CAT_DIALOG_SUB+ '(' +
                CAT_DIALOG_SUB_CAT_ID + " INTEGER," +
                CAT_DIALOG_SUB_DIALOG_ID + " INTEGER," +
                "UNIQUE("+ CAT_DIALOG_SUB_CAT_ID +","+ CAT_DIALOG_SUB_DIALOG_ID +") ON CONFLICT REPLACE"+
                ')';
        db.execSQL(CREATE_TABLE_CAT_DIALOG_SUB);

        String CREATE_TABLE_ADS_EX= "CREATE TABLE " + TABLE_ADS_EX_DIALOG+ '(' +
                ADS_EX_DIALOG_ID + " INTEGER," +
                "UNIQUE("+ ADS_EX_DIALOG_ID +") ON CONFLICT REPLACE"+
                ')';
        db.execSQL(CREATE_TABLE_ADS_EX);
     }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void openDbIfNeedToWrite() {
        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = this.getWritableDatabase();
        }
    }

    private void openDbIfNeedToRead() {
        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = this.getReadableDatabase();
        }
    }

    @Override
    public synchronized void close() {
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
        super.close();
    }

    public long insertAdv(long chat_id , int mid) {
        openDbIfNeedToWrite();
        ContentValues values = new ContentValues();
        values.put(ADV_CH_ID, chat_id) ;
        values.put(ADV_MID, mid) ;
        long i = mDatabase.insert(TABLE_ADV, null, values);
//        close();
        return i;
    }
    public long insertCatFave(int chat_id , String name) {
        openDbIfNeedToWrite();
        ContentValues values = new ContentValues();
        values.put(CAT_NAME, name);
        values.put(CAT_CHAT_ID, chat_id);
        long i = mDatabase.insert(TABLE_FAVE_CAT, null, values);
//        close();
        return i;
    }

    public boolean chanelIsCat(int ch_id){
        try {
            boolean isCat = false ;
            String select = "SELECT " + CAT_CHAT_ID +" FROM "+ TABLE_FAVE_CAT +
                    " WHERE "+  CAT_CHAT_ID + "="+ch_id ;
            SQLiteDatabase db = this.getWritableDatabase() ;
            Cursor cursor =db.rawQuery(select , null) ;
            if (cursor != null){
                if (cursor.moveToFirst()){
                    isCat = true ;
                }
            }
            assert cursor != null;
            cursor.close();
//            db.close();
            return isCat ;
        }catch (Exception e){
            return  false ;
        }

    }
    public long insertGhost(int user_id , int ghost_type) {
        openDbIfNeedToWrite();
        ContentValues values = new ContentValues();
        values.put(GHOT_USER_ID, user_id);
        values.put(GHOT_TYPE, ghost_type);
        long i = mDatabase.insert(TABLE_GHOT, null, values);
        return i;
    }
    public long deleteGhost(int user_id , int ghost_type) {
        openDbIfNeedToWrite();
         return mDatabase.delete(TABLE_GHOT , GHOT_USER_ID +"=? AND "+GHOT_TYPE+"=?" ,
                new String[]{String.valueOf(user_id) ,String.valueOf(ghost_type)}
                );
    }


    public ArrayList<HoldCatFav> loadCatFave() {
        ArrayList<HoldCatFav> cats = new ArrayList<>();
        openDbIfNeedToRead();
        Cursor cursor = mDatabase.query(
                TABLE_FAVE_CAT, new String[]{CAT_ID,
                        CAT_CHAT_ID,
                        CAT_NAME}, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(CAT_ID));
                    int chatId = cursor.getInt(cursor.getColumnIndex(CAT_CHAT_ID));
                    String name = cursor.getString(cursor.getColumnIndex(CAT_NAME));
                    HoldCatFav holdCatFav = new HoldCatFav(id , chatId , name) ;
                    cats.add(holdCatFav) ;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
//        close();
        return cats;
    }
    public HashMap<Integer ,String> loadCatsChannel() {
        HashMap<Integer ,String> cats = new HashMap<>();
        openDbIfNeedToRead();
        Cursor cursor = mDatabase.query(
                TABLE_FAVE_CAT, new String[]{CAT_ID,
                        CAT_CHAT_ID,
                        CAT_NAME }, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int chatId = cursor.getInt(cursor.getColumnIndex(CAT_CHAT_ID));
                    String name = cursor.getString(cursor.getColumnIndex(CAT_NAME));
                    cats.put(chatId , name) ;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
//        close();
        return cats;
    }
    public long deletGhostsByType(int currentType) {
        openDbIfNeedToWrite();
        return mDatabase.delete(TABLE_GHOT ,  GHOT_TYPE+"=?" ,
                new String[]{String.valueOf(currentType) }
        );
    }

    public boolean userIsInGhostExeptByIdAndType(int user_id , int type){
        boolean is = false ;
        openDbIfNeedToRead();
        Cursor cursor = mDatabase.query(
                TABLE_GHOT, new String[]{GHOT_USER_ID, GHOT_TYPE}, GHOT_USER_ID + "=? AND " +GHOT_TYPE +"=?", new String[]{String.valueOf(user_id) ,String.valueOf(type)}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                is = true ;
            }
            cursor.close();
        }
        return is;
    }
    public ArrayList<Integer> getListGhostsByType(int type) {
        ArrayList<Integer> ghosts = new ArrayList<>();
        openDbIfNeedToRead();
        Cursor cursor = mDatabase.query(
                TABLE_GHOT, new String[]{GHOT_USER_ID, GHOT_TYPE}, GHOT_TYPE +"=?", new String[]{String.valueOf(type)}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int ghost_user_id = cursor.getInt(cursor.getColumnIndex(GHOT_USER_ID));
                    ghosts.add(ghost_user_id) ;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
//        close();
        return ghosts;
    }

    //CatDialogs
    public long addCatDialog(String name) {
        openDbIfNeedToWrite();
        ContentValues values = new ContentValues();
        values.put(CAT_DIALOG_NAME, name);
        long i = mDatabase.insert(TABLE_CAT_DIALOG , null, values);
//        close();
        return i;
    }
    //CatDialogs
    public long updateCatDialog(int id , String name) {
        openDbIfNeedToWrite();
        ContentValues values = new ContentValues();
        values.put(CAT_DIALOG_NAME, name);
        long i = mDatabase.update(TABLE_CAT_DIALOG , values , CAT_DIALOG__ID +"="+id , null) ;
        return i;
    }
    public long addDialogToCatDialog(int catId , long dialogId) {
        openDbIfNeedToWrite();
        ContentValues values = new ContentValues();
        values.put(CAT_DIALOG_SUB_CAT_ID, catId);
        values.put(CAT_DIALOG_SUB_DIALOG_ID, dialogId);
        long i = mDatabase.insert(TABLE_CAT_DIALOG_SUB , null, values);
//        close();
        return i;
    }
    public ArrayList<HoldCatDialog> loadCatDialogs() {
        ArrayList<HoldCatDialog> cats = new ArrayList<>();
        openDbIfNeedToRead();
        Cursor cursor = mDatabase.query(
                TABLE_CAT_DIALOG, new String[]{CAT_DIALOG__ID,
                        CAT_DIALOG_NAME
                }, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(CAT_DIALOG__ID));
                    String name = cursor.getString(cursor.getColumnIndex(CAT_DIALOG_NAME));
                    HoldCatDialog holdCatDialog = new HoldCatDialog(id , name) ;
                    cats.add(holdCatDialog) ;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return cats;
    }

    public HashMap<Long ,Integer> loadDialogsByCat(int catId) {
        HashMap<Long ,Integer> dialogsByCat = new HashMap<>() ;
        openDbIfNeedToRead();
        Cursor cursor = mDatabase.query(
                TABLE_CAT_DIALOG_SUB , new String[]{CAT_DIALOG_SUB_DIALOG_ID ,
                CAT_DIALOG_SUB_CAT_ID
                }, CAT_DIALOG_SUB_CAT_ID +"=?", new String[]{String.valueOf(catId)}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int cat_id = cursor.getInt(cursor.getColumnIndex(CAT_DIALOG_SUB_CAT_ID));
                    long dialogId = cursor.getLong(cursor.getColumnIndex(CAT_DIALOG_SUB_DIALOG_ID));
                    dialogsByCat.put(dialogId ,cat_id) ;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return dialogsByCat;
    }

    public long deleteDialogFromCat(int catId , long dialogId) {
        openDbIfNeedToWrite();
        return mDatabase.delete(TABLE_CAT_DIALOG_SUB , CAT_DIALOG_SUB_CAT_ID +"=? AND "+CAT_DIALOG_SUB_DIALOG_ID+"=?" ,
                new String[]{String.valueOf(catId) ,String.valueOf(dialogId)}
        );
    }
    public long deleteCatDialog(int catId ) {
        openDbIfNeedToWrite();
        return mDatabase.delete(TABLE_CAT_DIALOG , CAT_DIALOG__ID +"=?" ,
                new String[]{String.valueOf(catId) }
        );
    }


    public long deleteAdsEx(int adsId ) {
        openDbIfNeedToWrite();
        return mDatabase.delete(TABLE_ADS_EX_DIALOG , ADS_EX_DIALOG_ID +"=?" ,
                new String[]{String.valueOf(adsId) }
        );
    }


    public long addAdsEx( int dialogId) {
        openDbIfNeedToWrite();
        ContentValues values = new ContentValues();
        values.put(ADS_EX_DIALOG_ID , dialogId);
        long i = mDatabase.insert(TABLE_ADS_EX_DIALOG , null, values);
        return i;
    }

    public HashMap<Integer ,Integer> loadDialogEx() {
        HashMap<Integer ,Integer> dialogsSelected = new HashMap<>() ;
        openDbIfNeedToRead();
        Cursor cursor = mDatabase.query(
                TABLE_ADS_EX_DIALOG , new String[]{ADS_EX_DIALOG_ID}, null, null, null, null , null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int dialogId = cursor.getInt(cursor.getColumnIndex(ADS_EX_DIALOG_ID));
                    dialogsSelected.put(dialogId , dialogId) ;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return dialogsSelected;
    }

}