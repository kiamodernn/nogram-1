package ir.nogram.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ir.nogram.messanger.FileLog;
import ir.nogram.users.activity.holder.HoldChangeContact;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "favourites";
    private static final String TABLE_FAVS = "tbl_favs";

    private static final String KEY_ID = "id";
    private static final String KEY_FAVE_CHAT_ID = "chat_id";
    private static final String KEY_FAVE_user_id = "user_id";


    public static final int PATTERN_FOR_LOCKED = 1 ;
    public static final int PATTERN_FOR_HIDE= 2 ;
    private static final String TABLE_LOCK_PATTERN = "lock";
    private static final String Lock_PATTERN_id = "id";
    private static final String Lock_PATTERN_type = "Lock_PATTERN_type";
    private static final String Lock_PATTERN_Number = "lock";


    private static final String TABLE_HIDE = "Ids";
//    private static final String TABLE_WORD_USER = "word_user";
private static final String TABLE_LOCKED = "Ids_locked";

    // TABLE_WORD_USER   Columns names
    private static final String hide_id = "id";
    private static final String hide_ch_id = "ch_id";
    private static final String hide_user_id = "user_id";

    // TABLE_WORD_USER   Columns names
    private static final String Ids_locked_id = "id";
    private static final String Ids_locked__ch_id = "ch_id";
    private static final String Ids_locked_user_id = "user_id";

    // HistoryItems table name
    private static final String TABLE_ContactsChanged = "contactsChanged";
    // TABLE_WORD_USER   Columns names
    private static final String CH_ID = "id";
    private static final String CH_USER_ID = "user_id";
    private static final String  CH_DATE= "date";
    private static final String CH_TYPE = "type";

    static DatabaseHandler instance ;
    public static DatabaseHandler getInstance(Context  context){
        DatabaseHandler localInstance = instance ;
        if(localInstance == null){
            synchronized (DatabaseHandler.class){
                localInstance = instance ;
                if(localInstance == null){
                    instance =localInstance   = new DatabaseHandler(context) ;
                }
            }
        }
        return localInstance ;
    }
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAVS_TABLE = "CREATE TABLE " + TABLE_FAVS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                  +KEY_FAVE_user_id + " INTEGER," +

                  KEY_FAVE_CHAT_ID + " INTEGER," +
                "UNIQUE("+ KEY_FAVE_user_id +","+ KEY_FAVE_CHAT_ID +") ON CONFLICT REPLACE"+

                    ")";
        db.execSQL(CREATE_FAVS_TABLE);
        //
        String CREATE_TABLE_IDS_Blocked = "CREATE TABLE " + TABLE_HIDE + '(' +
                hide_id + " INTEGER PRIMARY KEY," +
                hide_ch_id + " INTEGER," +
                hide_user_id + " INTEGER," +
                "UNIQUE("+ hide_ch_id +","+ hide_user_id +") ON CONFLICT REPLACE"+
                ')';
        db.execSQL(CREATE_TABLE_IDS_Blocked);
        //
        String CREATE_IDS_LOCK_TABLE = "CREATE TABLE " + TABLE_LOCKED + '(' +
                Ids_locked_id + " INTEGER PRIMARY KEY," +
                Ids_locked__ch_id + " INTEGER," +
                Ids_locked_user_id + " INTEGER," +
                "UNIQUE("+ Ids_locked__ch_id +","+ Ids_locked_user_id +") ON CONFLICT REPLACE"+
                ')';
        db.execSQL(CREATE_IDS_LOCK_TABLE);

        //
        String CREATE_CHANGE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_ContactsChanged + '(' +
                CH_ID + " INTEGER PRIMARY KEY," +
                CH_USER_ID + " INTEGER," +
                CH_DATE + " INTEGER," +
                CH_TYPE + " INTEGER" +
                ')';
        db.execSQL(CREATE_CHANGE_CONTACTS_TABLE);

        String CREATE_TABLE_PATTERN_LOCK = "CREATE TABLE " + TABLE_LOCK_PATTERN + '(' +
                Lock_PATTERN_id + " INTEGER PRIMARY KEY," +
                Lock_PATTERN_Number + " INTEGER," +
                Lock_PATTERN_type+ " INTEGER," +
                "UNIQUE("+ Lock_PATTERN_Number  +","+ Lock_PATTERN_type+") ON CONFLICT REPLACE"+
                ')';
        db.execSQL(CREATE_TABLE_PATTERN_LOCK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVS);
//        onCreate(db);
    }

    public void addFavourite(FaveHide faveHide) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FAVE_CHAT_ID, faveHide.getChatID());
        db.insert(TABLE_FAVS, null, values);
//        db.close();
    }
    public FaveHide getFavouriteByChatId(long chat_id ) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {

            String [] projection = {
                    KEY_ID,
                    KEY_FAVE_CHAT_ID,
                    KEY_FAVE_user_id
            };

            String whereClause = KEY_FAVE_CHAT_ID +"=?";
            String [] whereArgs = {String.valueOf(chat_id)};

            cursor = db.query(
                    TABLE_FAVS,
                    projection,
                    whereClause,
                    whereArgs,
                    null,
                    null,
                    null
            );

            if( cursor != null && cursor.moveToFirst() ){
                return new FaveHide(cursor.getLong(1));
            }
        } catch (Exception e) {
            if(cursor != null)
                cursor.close();
            FileLog.e("tmessages", e);
            return null;
        } finally {
            if(cursor != null)
                cursor.close();
        }
        return null;
    }

    public void deleteFavourite(Long chat_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVS, KEY_FAVE_CHAT_ID + " = ?", new String[] { String.valueOf(chat_id) });
//        db.close();
    }
    public List<FaveHide> getAllFavourites(int user_id) {
        List<FaveHide> favsList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_FAVS +" WHERE "+KEY_FAVE_user_id +"="+user_id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                FaveHide faveHide = new FaveHide();
                faveHide.setID(Integer.parseInt(cursor.getString(0)));
                faveHide.setChatID(cursor.getLong(1));
                favsList.add(faveHide);
            } while (cursor.moveToNext());
        }
        return favsList;
    }
    public ArrayList<Integer> getAllFavouritesIdByUser() {
        ArrayList<Integer> favsList = new ArrayList<>();
        String selectQuery = "SELECT "+KEY_FAVE_CHAT_ID + " FROM " + TABLE_FAVS  ;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                int ch_id = cursor.getInt(cursor.getColumnIndex(KEY_FAVE_CHAT_ID)) ;
                favsList.add(ch_id);
            } while (cursor.moveToNext());
        }
        cursor.close();
//        db.close();
        return favsList;
    }
    /*
        public int getFavouritesCount() {
            String countQuery = "SELECT  * FROM " + TABLE_FAVS;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(countQuery, null);
            cursor.close();
            return cursor.getCount();
        }*/
    public long addHideDialog(int ch_id ) {
        SQLiteDatabase db = this.getWritableDatabase() ;
        ContentValues values = new ContentValues();
        values.put(hide_ch_id, ch_id);
        long i = db.insert(TABLE_HIDE, null, values) ;
//        db.close();
        return  i ;
    }
    public long addLockDialog(int ch_id ) {
        SQLiteDatabase db = this.getWritableDatabase() ;
        ContentValues values = new ContentValues();
        values.put(Ids_locked__ch_id, ch_id);
        long i = db.insert(TABLE_LOCKED, null, values) ;
        Log.i("log", "ch_id add");
//        db.close();
        return  i ;
    }
    public String countLock(){
        SQLiteDatabase database = this.getReadableDatabase() ;
        int count = 0 ;
        String select = "SELECT " + Ids_locked_id +" FROM "+ TABLE_LOCKED;
        Cursor cursor = database.rawQuery(select ,  null) ;

        if (cursor != null){
            if(cursor.moveToFirst()){
                do{
                    count++ ;
                }while (cursor.moveToNext()) ;
            }

        }
        cursor.close();
//        database.close();
        return String.valueOf(count) ;
    }

    public String countHide(){
        SQLiteDatabase database = this.getReadableDatabase() ;
        int count = 0 ;
        String select = "SELECT " + hide_id +" FROM "+ TABLE_HIDE ;
        Cursor cursor = database.rawQuery(select ,  null) ;
        if (cursor != null){
            if(cursor.moveToFirst()){
                do{
                    count++ ;
                }while (cursor.moveToNext()) ;
            }

        }
        cursor.close();
//        database.close();
        return String.valueOf(count) ;
    }


    public long addFavorite(int ch_id  ) {
        SQLiteDatabase db = this.getWritableDatabase() ;
        ContentValues values = new ContentValues();
        values.put(KEY_FAVE_CHAT_ID, ch_id);
        long i = db.insert(TABLE_FAVS, null, values) ;
        Log.i("log", "ch_id add");
//        db.close();
        return  i ;
    }
    public int deleteFavorite(int ch_id){
        SQLiteDatabase db = this.getWritableDatabase() ;
        int i = db.delete(TABLE_FAVS, KEY_FAVE_CHAT_ID +"="+ ch_id  , null) ;
//        db.close();
        return i ;
    }
    public int deleteHide(int ch_id){
        SQLiteDatabase db = this.getWritableDatabase() ;
        int i = db.delete(TABLE_HIDE, hide_ch_id +"="+ ch_id  , null) ;
//        db.close();
        return i ;
    }
    public int deleteLockDialog(int ch_id){
        SQLiteDatabase db = this.getWritableDatabase() ;
        int i = db.delete(TABLE_LOCKED, Ids_locked__ch_id +"="+ ch_id  , null) ;
//        db.close();
        return i ;
    }
    public boolean isHide(int ch_id){
        try {
            boolean isBlocked = false ;
            String select = "SELECT " + hide_id +" FROM "+ TABLE_HIDE +
                    " WHERE "+  hide_ch_id + "="+ch_id ;
            SQLiteDatabase db = this.getWritableDatabase() ;
            Cursor cursor =db.rawQuery(select , null) ;
            if (cursor != null){
                if (cursor.moveToFirst()){
                    isBlocked = true ;
                }
            }
            assert cursor != null;
            cursor.close();
//            db.close();
            return isBlocked ;
        }catch (Exception e){
            Log.e("log" , e.getMessage()) ;
            return  false ;
        }

    }
    public boolean islocked(int ch_id){
        try {
            boolean isLocked = false ;
            String select = "SELECT " + Ids_locked_id +" FROM "+ TABLE_LOCKED +
                    " WHERE "+ Ids_locked__ch_id+ "="+ch_id ;
            SQLiteDatabase db = this.getWritableDatabase() ;
            Cursor cursor =db.rawQuery(select , null) ;
            if (cursor != null){
                if (cursor.moveToFirst()){
                    isLocked = true ;
                }
            }
            assert cursor != null;
            cursor.close();
//            db.close();
            return isLocked ;
        }catch (Exception e){
            return  false ;
        }

    }
    public boolean isFavorite(int ch_id ){
        try {
            boolean isFave = false ;
            String select = "SELECT " + KEY_ID +" FROM "+ TABLE_FAVS +
                    " WHERE " + KEY_FAVE_CHAT_ID + "="+ch_id ;
            SQLiteDatabase db = this.getWritableDatabase() ;
            Cursor cursor =db.rawQuery(select , null) ;
            if (cursor != null){
                if (cursor.moveToFirst()){
                    isFave = true ;
                }
            }
            assert cursor != null;
            cursor.close();
//            db.close();
            return isFave ;
        }catch (Exception e){
            Log.e("log" , e.getMessage()) ;
            return  false ;
        }

    }

    public FaveHide getBlockByChatId(long chat_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String [] projection = {
                    hide_ch_id,
                    hide_user_id
            };
            String whereClause = hide_ch_id +"=?";
            String [] whereArgs = {String.valueOf(chat_id) };
            cursor = db.query(
                    TABLE_HIDE,
                    projection,
                    whereClause,
                    whereArgs,
                    null,
                    null,
                    null
            );

            if( cursor != null && cursor.moveToFirst() ){
                return new FaveHide(cursor.getLong(1));
            }
        } catch (Exception e) {
            if(cursor != null)
                cursor.close();
            FileLog.e("tmessages", e);
            return null;
        } finally {
            if(cursor != null)
                cursor.close();
        }
        return null;
    }

    //ChangeContacts
    public long addChange(int user_id , int type , int date ) {
        SQLiteDatabase db = this.getWritableDatabase() ;
        ContentValues values = new ContentValues();
        values.put(CH_USER_ID, user_id);
        values.put(CH_TYPE, type);
        values.put(CH_DATE, date);
        long i = db.insert(TABLE_ContactsChanged, null, values);
        Log.i("LogUpdateAdd", "change add");
//        db.close();
        return  i ;
    }
    public int deleteChange(int id){
        SQLiteDatabase db = this.getWritableDatabase() ;
        int i = db.delete(TABLE_ContactsChanged, CH_ID +"="+ id  , null) ;
//        db.close();
        return i ;
    }
    public int deleteAllChange(){
        SQLiteDatabase db = this.getWritableDatabase() ;
        int i = db.delete(TABLE_ContactsChanged, null , null) ;
//        db.close();
        return i ;
    }

    public ArrayList<HoldChangeContact> listChanges(){
        ArrayList<HoldChangeContact> holdChanges = new ArrayList<>() ;
        String select = "SELECT * FROM "+ TABLE_ContactsChanged +
                " ORDER BY "+ CH_ID + " DESC" ;
        SQLiteDatabase db = this.getWritableDatabase() ;
        Cursor cursor =db.rawQuery(select , null) ;
        if (cursor != null){
            if (cursor.moveToFirst()){
                do{
                    int id = cursor.getInt(cursor.getColumnIndex(CH_ID)) ;
                    int user_id = cursor.getInt(cursor.getColumnIndex(CH_USER_ID)) ;
                    int type = cursor.getInt(cursor.getColumnIndex(CH_TYPE)) ;
                    int date = cursor.getInt(cursor.getColumnIndex(CH_DATE)) ;
                    holdChanges.add(new HoldChangeContact(user_id , id , type , date)) ;
                }while (cursor.moveToNext()) ;
            }
        }
        cursor.close();
//        db.close();
        return holdChanges ;
    }
    public int lastIdChange(){
        int last_id = 0 ;
        String select = "SELECT "+ CH_ID + " FROM "+ TABLE_ContactsChanged +
                " ORDER BY "+ CH_ID + " DESC LIMIT 1" ;
        SQLiteDatabase db = this.getWritableDatabase() ;
        Cursor cursor =db.rawQuery(select , null) ;
        if (cursor != null){
            if (cursor.moveToFirst()){
                last_id = cursor.getInt(cursor.getColumnIndex(CH_ID)) ;
            }
        }
        Log.i("loglast_id"  , last_id +"") ;
        return last_id ;
    }
    public int  countChangeUnReaded(int last_id){
        int count = 0 ;
        String select = "SELECT "+  CH_ID  +" FROM "+ TABLE_ContactsChanged +
                " WHERE "+CH_ID + " >"+ last_id ;
        SQLiteDatabase db = this.getWritableDatabase() ;
        Cursor cursor =db.rawQuery(select , null) ;
        if (cursor != null){
            if (cursor.moveToFirst()){
                do{
                    count++ ;
                }while (cursor.moveToNext()) ;
            }
        }
        Log.e("last_idDb" , count +"") ;
        cursor.close();
//        db.close();
        return count ;
    }

    public long addLock(int lock , int type) {
        deleteLock(type) ;
        SQLiteDatabase db = this.getWritableDatabase() ;
        ContentValues values = new ContentValues();
        values.put(Lock_PATTERN_Number, lock);
        values.put(Lock_PATTERN_type, type);
        long i = db.insert(TABLE_LOCK_PATTERN, null, values) ;
        Log.i("log", "ch_id add");
//        db.close();
        return  i ;
    }
    public int deleteLock(int type){
        SQLiteDatabase db = this.getWritableDatabase() ;
        int i = db.delete(TABLE_LOCK_PATTERN, Lock_PATTERN_type +"="+type , null) ;
//        db.close();
        return i ;
    }
    public int getLock(int type){
        int lock = 0 ;
        String select = "SELECT * FROM "+ TABLE_LOCK_PATTERN +" WHERE "+Lock_PATTERN_type +"="+type;
        SQLiteDatabase db = this.getWritableDatabase() ;
        Cursor cursor =db.rawQuery(select , null) ;
        if (cursor != null){
            if (cursor.moveToFirst()){
                lock = cursor.getInt(cursor.getColumnIndex(Lock_PATTERN_Number)) ;
            }
        }
        cursor.close();
//        db.close();
        return lock ;
    }

}
