package ir.nogram.SQLite;

import ir.nogram.messanger.ApplicationLoader;

public class FaveHide {

    public static final String hide= "hide" ;
    public static final String notHide= "notHide" ;
    public static final String fave= "fave" ;
    public static final String notfave= "notfave" ;

    long id;
    long chat_id;
    public FaveHide(){
    }

    public FaveHide(long id, long chat_id){
        this.id = id;
        this.chat_id = chat_id;
    }

    public FaveHide(long chat_id){
        this.chat_id = chat_id;
    }

    public long getChatID(){
        return this.chat_id;
    }

    public long getID(){
        return this.id;
    }

    public void setChatID(long chat_id){
        this.chat_id = chat_id;
    }

    public void setID(long id){
        this.id = id;
    }
//    public static void addFavourite(Long id){
//        FaveHide faveHide = new FaveHide(id);
//        ApplicationLoader.databaseHandler.addFavourite(faveHide);
//    }

//    public static boolean isHide(Long id){
//        try{
//            FaveHide faveHide = ApplicationLoader.databaseHandler.getBlockByChatId(id);
//            if(faveHide == null){
//                return false;
//            }else{
//                return true;
//            }
//        } catch (Exception e) {
//            FileLog.e("tmessages", e);
//            return false;
//        }
//    }
    public static void addFave(int selectedDialog){
        ApplicationLoader.databaseHandler.addFavorite(selectedDialog) ;
        ApplicationLoader.sharedPreferencesMain.edit().putString(fave+selectedDialog ,fave+selectedDialog ).apply();


    }
    public static void deleteFavourite(int selectedDialog){
        ApplicationLoader.databaseHandler.deleteFavorite( selectedDialog) ;
        ApplicationLoader.sharedPreferencesMain.edit().putString(fave+selectedDialog ,notfave ).apply();

    }
    public static boolean isFavourite(int selectedDialog){
         String faveS = ApplicationLoader.sharedPreferencesMain.getString(fave+selectedDialog , notfave) ;
        return faveS.equals(fave+selectedDialog) ;

    }


    public static void addHide(int selectedDialog){
        ApplicationLoader.databaseHandler.addHideDialog(selectedDialog) ;
        ApplicationLoader.sharedPreferencesMain.edit().putString(hide+selectedDialog ,hide+selectedDialog ).apply();

    }
    public static void deleteHide(int selectedDialog){
        ApplicationLoader.databaseHandler.deleteHide( selectedDialog) ;
        ApplicationLoader.sharedPreferencesMain.edit().putString(hide+selectedDialog ,notHide ).apply();

    }
    public static boolean isHidenDialog(int selectedDialog){
        String faveS = ApplicationLoader.sharedPreferencesMain.getString(hide+selectedDialog , notHide ) ;
        return faveS.equals(hide+selectedDialog) ;

    }
}

