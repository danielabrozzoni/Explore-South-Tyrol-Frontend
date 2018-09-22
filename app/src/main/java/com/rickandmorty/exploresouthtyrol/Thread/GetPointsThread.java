package com.rickandmorty.exploresouthtyrol.Thread;

import android.util.Log;

import com.rickandmorty.exploresouthtyrol.Model.PlaceModel;
import com.rickandmorty.exploresouthtyrol.Service.PlaceService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class GetPointsThread extends Thread {
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

        Callback<List<PlaceModel>> callback = new Callback<List<PlaceModel>>() {
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
        };

        PlaceService placeService = new PlaceService();
        placeService.getPlaces(latitude, longitude, heading, callback);
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
