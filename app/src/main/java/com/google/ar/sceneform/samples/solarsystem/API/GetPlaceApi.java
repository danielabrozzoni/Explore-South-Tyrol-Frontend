package com.google.ar.sceneform.samples.solarsystem.API;

import com.google.ar.sceneform.samples.solarsystem.Model.PlaceModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetPlaceApi {

    @GET("poi/")
    Call<List<PlaceModel>> loadPlaces(@Query("latitude") float latitude,
                                      @Query("longitude") float longitude,
                                      @Query("heading") float heading);
}
