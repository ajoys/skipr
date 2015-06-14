package me.skipr.skipr.api;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Navjot on 6/13/2015.
 */
public interface SkiprApi {
    /*Join room*/
    @PUT("/{roomId}/join")
    public void join(@Path("roomId") String roomId, @Body String userId, Callback<String> callback);

    /*Create room*/
    @Multipart
    @POST("/room")
    public void create(@Part("userId") String userId, @Part("token") String token, Callback<RoomCreateResponse> callback);
}
