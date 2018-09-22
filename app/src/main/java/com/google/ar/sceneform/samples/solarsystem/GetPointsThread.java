package com.google.ar.sceneform.samples.solarsystem;

import android.util.Log;
import android.widget.Toast;
import com.google.ar.sceneform.samples.solarsystem.API.GetPlaceApi;
import com.google.ar.sceneform.samples.solarsystem.Activity.PinActivity;
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

    private final float latitude;
    private final float longitude;
    private final float heading;

    private boolean loading = false;

    public GetPointsThread(float latitude, float longitude, float heading) {
        super("GetPointsThread");
        this.latitude = latitude;
        this.longitude = longitude;
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
