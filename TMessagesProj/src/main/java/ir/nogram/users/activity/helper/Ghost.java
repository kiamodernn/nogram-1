package ir.nogram.users.activity.helper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.users.activity.GhostControlActivity;
import ir.nogram.users.activity.db.DatabaseMain;
import ir.nogram.users.activity.holder.HoldConst;
import ir.nogram.users.activity.holder.Pref;

/**
 * Created by MhkDeveloper on 17/04/2017.
 */

public class Ghost {

    public static boolean ghostIsEnable(){
        return ApplicationLoader.sharedPreferencesMain.getBoolean(Pref.GHOST_ENABLE , false) ;
    }
    public static void disableGhost(){
          ApplicationLoader.sharedPreferencesMain.edit().putBoolean(Pref.GHOST_ENABLE , false).apply(); ;
    }

    public static boolean changeGhost() {
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(HoldConst.PREF_MAIN, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = plusPreferences.edit();
        boolean ghostEn = plusPreferences.getBoolean(Pref.GHOST_ENABLE, false);
        ghostEn = !ghostEn ;
        editor.putBoolean(Pref.GHOST_ENABLE , ghostEn).apply();
        return ghostEn ;
    }
    public static boolean ghostByUser(int user_id){
        boolean ghost = false ;
        int ghost_type = ApplicationLoader.sharedPreferencesMain.getInt(Pref.GHOST_TYPE , GhostControlActivity.GHOST_TYPE_NEVER) ;
        boolean isInExeptUser = DatabaseMain.getInstance(ApplicationLoader.applicationContext).userIsInGhostExeptByIdAndType(user_id , ghost_type) ;
       if(isInExeptUser){
           Log.i("LOGGGG" , "INEEEEE AND TYPE "+ ghost_type) ;
       }else{
           Log.i("LOGGGG" , "!!!!!! INEEEEE"+ ghost_type) ;
       }
        if(ghost_type == GhostControlActivity.GHOST_TYPE_NEVER){
            if (isInExeptUser){
                ghost = true ;
            }else{
                ghost = false ;
            }
        }else{
            if (isInExeptUser){
                ghost = false ;
            }else{
                ghost = true ;
            }
        }
        if(ghost){
            Log.i("LOGGGG" , "ghost enable") ;
        }else{
            Log.i("LOGGGG" , "ghost disable") ;
        }
        ApplicationLoader.sharedPreferencesMain.edit().putBoolean(Pref.GHOST_ENABLE , ghost).apply();
        return ghost ;
    }

    public static boolean ghostForDialogsIcon(){
        int currentType =  ApplicationLoader.sharedPreferencesMain.getInt(Pref.GHOST_TYPE , GhostControlActivity.GHOST_TYPE_NEVER) ;
        if(currentType == GhostControlActivity.GHOST_TYPE_NEVER){
            return false ;
        }else {
            return true ;
        }
    }
}
