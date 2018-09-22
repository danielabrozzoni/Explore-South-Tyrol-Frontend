package com.rickandmorty.exploresouthtyrol.API;

import com.rickandmorty.exploresouthtyrol.Model.LocationModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface StarAPI {

    @POST("star/")
    Call<LocationModel> sendLocation(@Body LocationModel locationModel);
}
