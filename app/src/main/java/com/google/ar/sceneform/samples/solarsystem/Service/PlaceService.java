package com.google.ar.sceneform.samples.solarsystem.Service;

import android.util.Log;

import java.util.List;

import com.google.ar.sceneform.samples.solarsystem.API.GetPlaceApi;
import com.google.ar.sceneform.samples.solarsystem.PlaceModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlaceService {

    static final String BASE_URL = "https://1a490957.ngrok.io/";

    public void start(float latitude, float longitude, Callback<List<PlaceModel>> callback) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        GetPlaceApi getPlaceApi = retrofit.create(GetPlaceApi.class);

        Call<List<PlaceModel>> call = getPlaceApi.loadPlaces(latitude, longitude);
        call.enqueue(callback);

    }
}