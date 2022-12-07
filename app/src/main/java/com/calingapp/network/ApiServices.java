package com.calingapp.network;

import java.util.HashMap;
import java.util.List;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface ApiServices {

//    String BASE_URL = "https://furniturestore.pythonanywhere.com";

    @POST("send")
    Call<String> sendRemoteMessage(
            @HeaderMap HashMap<String ,String> headers,
            @Body String remoteBody
            );

//    @GET("/product")
//    Call<List<ResponseBody>> getAllProduct();

}
