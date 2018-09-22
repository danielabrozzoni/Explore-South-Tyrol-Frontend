package com.rickandmorty.exploresouthtyrol.API;

import android.location.LocationManager;

import com.google.android.gms.tasks.Task;
import com.rickandmorty.exploresouthtyrol.Model.LocationModel;
import com.rickandmorty.exploresouthtyrol.Model.PlaceModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GetPlaceApi {

    @GET("poi/")
    Call<List<PlaceModel>> loadPlaces(@Query("latitude") float latitude,
                                      @Query("longitude") float longitude,
                                      @Query("heading") float heading);
}
