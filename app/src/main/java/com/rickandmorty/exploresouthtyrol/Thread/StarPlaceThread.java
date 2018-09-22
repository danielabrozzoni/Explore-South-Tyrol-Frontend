package com.rickandmorty.exploresouthtyrol.Thread;

import android.util.Log;

import com.rickandmorty.exploresouthtyrol.Model.LocationModel;
import com.rickandmorty.exploresouthtyrol.Model.PlaceModel;
import com.rickandmorty.exploresouthtyrol.Service.PlaceService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StarPlaceThread extends Thread {
    private boolean started = false;

    private float latitude, longitude;

    public StarPlaceThread(float latitude, float longitude) {
        super("StarPlaceThread");
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void run() {

        Callback<LocationModel> callback = new Callback<LocationModel>() {
            @Override
            public void onResponse(Call<LocationModel> call, Response<LocationModel> response) {

            }

            @Override
            public void onFailure(Call<LocationModel> call, Throwable t) {

            }
        };

        PlaceService placeService = new PlaceService();
        placeService.starPlace(latitude, longitude, callback);
    }

    @Override
    public synchronized void start() {
        if (this.started) {
            return;
        }

        this.started = true;
        super.start();
    }
}
