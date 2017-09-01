package ir.nogram.retro;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by MhkDeveloper on 19/04/2017.
 */

public class Client {
//    static String API_BASE_URL_TEL = "http://192.168.1.9/zaeimphp/";
    public static Retrofit instance(String API_BASE_URL){
        return
                new Retrofit.Builder()
                        .baseUrl(API_BASE_URL)
                        .addConverterFactory(
                                GsonConverterFactory.create()
                        )
                        .build();
    }

    public static ClientTel clientTel(String baseUrl){
        return instance(baseUrl).create(ClientTel.class) ;
    }
}
