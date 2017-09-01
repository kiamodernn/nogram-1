package ir.nogram.retro;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by MhkDeveloper on 19/04/2017.
 */

public interface ClientTel {
//    @FormUrlEncoded
//    @POST("histrousertouser.php")
//    Call<ResponseBody> archiveUserByUser(
//            @Field(HoldRetro.num1) String num1,
//            @Field(HoldRetro.num2) String num2,
//            @Field(HoldRetro.timesent) long timestamp
//    ) ;
//    @FormUrlEncoded
//    @POST("searchusercreatecode.php")
//    Call<ResponseBody> createcode(
//            @Field(HoldRetro.username) String phone,
//            @Field(HoldRetro.password) String password
//    ) ;
//
////    http://boshrapardaz.ir/at/set.php?ch=test&mid=17&uid=123&cr=7a2ac93ac7a6ba57484ba8988ba6132e
//    @FormUrlEncoded
//    @POST("set.php")
//    Call<ResponseBody> sendMessageIdToServerGet(
//            @Field(HoldRetro.username) String phone,
//            @Field(HoldRetro.password) String password
//    ) ;
//    @FormUrlEncoded
//    @POST("createUserOnserver.php")
//    Call<ResponseBody> createUserOnserver(
//            @Field(HoldRetro.username) String phone,
//            @Field(HoldRetro.password) String password,
//            @Field(HoldRetro.firstName) String firstName,
//            @Field(HoldRetro.lastName) String lastName
//    ) ;
//    @FormUrlEncoded
//    @POST("authcodefile.php")
//    Call<ResponseBody> auth_code(
//            @Field(HoldRetro.username) String username,
//            @Field(HoldRetro.code) String code
//    ) ;
//    @Headers( "Content-Type: application/json" )
//    @POST("synccontacts.php")
//    Call<ResponseBody> synccontacts(
//            @Body JSONArray jsonArray
//    ) ;

    // http://boshrapardaz.ir/at/set.php?ch=test&mid=107&hash=96969&uid=123&cr=0b9ee62c8ee2a8695cbd6a4519ce3abb
//    @FormUrlEncoded
    @GET("set.php")
    Call<ResponseBody> sendMessageIdToServerGet(
            @Query(HoldRetro.ch) String ch,
            @Query(HoldRetro.mid) String mid,
            @Query(HoldRetro.hash) String hash,
            @Query(HoldRetro.uid) String uid,
            @Query(HoldRetro.cr) String cr
    ) ;

    @POST("tel7020860")
    Call<ResponseBody> sendMessageIdToServerPost(@Body RequestBody post) ;

}
