package me.skipr.skipr.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Navjot on 6/13/2015.
 */
public interface SkiprApi {
    /*Join room*/
    @POST("/{roomId}/join")
    public void join(@Path("roomId") String roomId, @Body String userId, Callback<String> callback);

    /*Create room*/
    @POST("/create")
    public void create(@Body String userId, @Body String token, Callback<String> callback);
}
