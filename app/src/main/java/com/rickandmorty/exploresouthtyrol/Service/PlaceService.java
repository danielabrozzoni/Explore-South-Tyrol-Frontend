package com.rickandmorty.exploresouthtyrol.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rickandmorty.exploresouthtyrol.API.GetPlaceApi;
import com.rickandmorty.exploresouthtyrol.API.StarAPI;
import com.rickandmorty.exploresouthtyrol.Model.LocationModel;
import com.rickandmorty.exploresouthtyrol.Model.PlaceModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlaceService {

    static final String BASE_URL = "https://378f00d8.ngrok.io/";

    public void getPlaces(float latitude, float longitude, float heading, Callback<List<PlaceModel>> callback) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        GetPlaceApi getPlaceApi = retrofit.create(GetPlaceApi.class);

        Call<List<PlaceModel>> call = getPlaceApi.loadPlaces(latitude, longitude, heading);
        call.enqueue(callback);

    }

    public void starPlace(Float latitude, Float longitude, Callback<LocationModel> callback) {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        LocationModel locationModel = new LocationModel();
        locationModel.latitude = latitude;
        locationModel.longitude = longitude;

        StarAPI starAPI = retrofit.create(StarAPI.class);

        Call<LocationModel> call = starAPI.sendLocation(locationModel);
        call.enqueue(callback);
    }
}
