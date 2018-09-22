package com.rickandmorty.exploresouthtyrol.Thread;

import android.util.Log;

import com.rickandmorty.exploresouthtyrol.API.GetPlaceApi;
import com.rickandmorty.exploresouthtyrol.Model.PlaceModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.List;

public class GetPointsThread extends Thread {
    private static final String BASE_URL = "https://1a490957.ngrok.io/";
    private List<PlaceModel> places = new ArrayList<>();

    private float latitude;
    private float longitude;
    private float heading;

    private boolean started = false;

    public GetPointsThread(float latitude, float longitude) {
        super("GetPointsThread");
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setHeading(float heading) {
        this.heading = heading;
    }

    public void run() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        GetPlaceApi getPlaceApi = retrofit.create(GetPlaceApi.class);

        Call<List<PlaceModel>> call = getPlaceApi.loadPlaces(latitude, longitude, heading);
        call.enqueue(new Callback<List<PlaceModel>>() {
            @Override
            public void onResponse(Call<List<PlaceModel>> call, Response<List<PlaceModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setPlaces(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<PlaceModel>> call, Throwable t) {
                Log.d("PinActivity", t.getMessage());
            }
        });
    }

    @Override
    public synchronized void start() {
        if (this.started) {
            return;
        }

        this.started = true;
        super.start();
    }

    public synchronized boolean hasPlaces() {
        return places.size() > 0;
    }

    private synchronized void setPlaces(List<PlaceModel> places) {
        this.places = places;
    }

    public synchronized List<PlaceModel> getPlaces() {
        return places;
    }
}
