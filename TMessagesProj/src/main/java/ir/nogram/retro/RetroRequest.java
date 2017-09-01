package ir.nogram.retro;

import android.util.Log;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by MhkDeveloper on 04/07/2017.
 */

public class RetroRequest {

    private static boolean block = false ;

    public static void sendMessageIdToServer(String baseurl , String ch , String uid , String hash , String mid ){
//        RetroRequest.sendMessageIdToServerGet(HoldConst.BASE_URL_FOR_ADDS, chatName ,userId , String.valueOf(hash) ,
        if(block){
            return;
        }
        String cr = "sd";

        String post ="{" +
                getStrWithBlock("ch" , false , true) +
                getStrWithBlock(ch , true , false) +

                getStrWithBlock("mid" , false , true) +
                getStrWithBlock(mid , true , false) +

                getStrWithBlock("hash" , false , true) +
                getStrWithBlock(hash , true , false) +

                getStrWithBlock("uid" , false , true) +
                getStrWithBlock(uid , true , false) +

                getStrWithBlock("cr" , false , true) +
                getStrWithBlock(cr , false , false) +
                "}" ;

        Log.i("LOG" , post) ;

        // cr=md5(GET("ch").GET("uid").GET("hash").GET("mid")."antitabligh");
//        Log.i("RESPONSE" , ch + " "+ mid + " "+ uid + " " +cr) ;
        ClientTel clientRetro = Client.clientTel(baseurl) ;
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"),post);

        Call<ResponseBody> callAuth  = clientRetro.sendMessageIdToServerPost(requestBody) ;
//        callAuth.
//        Call<ResponseBody> callAuth = clientRetro.sendMessageIdToServerGet(ch ,mid , hash , uid , cr) ;
        callAuth.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    Log.i("LOG" , "SUCCESS ADS") ;

//                    try {
//                        String res = response.body().string() ;
//                       if(res.equals("block")){
//                           block = true ;
//                       }
//                    } catch (IOException e) {
//                    }
                }else{
                    Log.i("LOG" , "ERROR ADS") ;
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i("LOG" , "Failure ADS") ;
            }
        });

    }
//



//    public static String md5(String s) {
//        try {
//            // Create MD5 Hash
//            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
//            digest.update(s.getBytes());
//            byte messageDigest[] = digest.digest();
//            // Create Hex String
//            StringBuilder hexString = new StringBuilder();
//            for (byte aMessageDigest : messageDigest)
//                hexString.append(Integer.toHexString(0xFF & aMessageDigest));
//            return hexString.toString();
//
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }

    private static String getStrWithBlock(String str , boolean withComa , boolean withdot){
        String ret =  "[" + str + "]" ;
        if (withComa){
            ret += "," ;
        }
        if (withdot){
            ret += ":" ;
        }
        return ret ;
    }
}
