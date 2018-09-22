/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.solarsystem;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.samples.solarsystem.Helper.LocationHelper;
import com.google.ar.sceneform.samples.solarsystem.Helper.CompassHelper;
import com.google.ar.sceneform.samples.solarsystem.Widget.Tutorial;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore and Sceneform APIs.
 */
public class PinActivity extends AppCompatActivity {
    public static final int RC_PERMISSIONS = 0x123;
    private boolean installRequested;

    private ArSceneView arSceneView;

    private ModelRenderable pinRenderable;
    private ModelRenderable starRenderable;

    private CompassHelper mCompassHelper;

    private LocationHelper mLocationHelper;

    // True once scene is loaded
    private boolean hasFinishedLoading = false;

    // True once the scene has been placed.
    private boolean hasPlacedSolarSystem = false;


    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!DemoUtils.checkIsSupportedDeviceOrFinish(this)) {
            // Not a supported device.
            return;
        }

        DemoUtils.requestCameraPermission(this, RC_PERMISSIONS);
        DemoUtils.requestLocationPermission(this, RC_PERMISSIONS);

        mLocationHelper = LocationHelper.getInstance(this);
        setContentView(R.layout.activity_solar);
        arSceneView = findViewById(R.id.ar_scene_view);

        CompletableFuture<ModelRenderable> pinStage =
                ModelRenderable.builder().setSource(this, Uri.parse("Pin.sfb")).build();
        CompletableFuture<ModelRenderable> starStage =
                ModelRenderable.builder().setSource(this, Uri.parse("Star.sfb")).build();

        CompletableFuture.allOf(
                pinStage,
                starStage)
                .handle(
                        (notUsed, throwable) -> {
                            // When you build a Renderable, Sceneform loads its resources in the background while
                            // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                            // before calling get().

                            if (throwable != null) {
                                DemoUtils.displayError(this, "Unable to load renderable", throwable);
                                return null;
                            }

                            try {
                                pinRenderable = pinStage.get();
                                starRenderable = starStage.get();

                                // Everything finished loading successfully.
                                hasFinishedLoading = true;

                            } catch (InterruptedException | ExecutionException ex) {
                                DemoUtils.displayError(this, "Unable to load renderable", ex);
                            }

                            return null;
                        });


        // Set an update listener on the Scene that will hide the loading message once a Plane is
        // detected.
        arSceneView
            .getScene()
            .addOnUpdateListener(
                frameTime -> {

                    Frame frame = arSceneView.getArFrame();
                    if (frame == null) {
                        return;
                    }

                    if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                        return;
                    }

                    if (hasPlacedSolarSystem)
                        return;

                    for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {

                        if (plane.getTrackingState() == TrackingState.TRACKING) {
                            hideLoadingMessage();
                            mCompassHelper = CompassHelper.getInstance(this, new Runnable() {
                                @Override
                                public void run() {
                                    Node worldView = placePins();
                                    Pose pose = new Pose(new float[]{0.0f, 0.0f, 0.0f}, new float[]{0.0f, 0.0f, 0.0f, 0.0f});
                                    Log.d("PinActivityL", "" + worldView.getWorldRotation().toString());
                                    worldView.setWorldRotation(new Quaternion(Vector3.up(),  360 + mCompassHelper.getCurrentDegree()));
                                    Log.d("PinActivityL", "" + worldView.getWorldRotation().toString());
                                    Toast.makeText(PinActivity.this, "Rotation: " + (int)(360 + mCompassHelper.getCurrentDegree()), Toast.LENGTH_SHORT).show();
                                    Anchor anchor = plane.createAnchor(pose);
                                    AnchorNode anchorNode = new AnchorNode(anchor);
                                    anchorNode.setParent(arSceneView.getScene());
                                    anchorNode.addChild(worldView);
                                }
                            });
                            hasPlacedSolarSystem = true;
                        }
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (arSceneView == null) {
            return;
        }

        if (arSceneView.getSession() == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                Session session = DemoUtils.createArSession(this, installRequested);
                if (session == null) {
                    installRequested = DemoUtils.hasCameraPermission(this);
                    return;
                } else {
                    arSceneView.setupSession(session);
                }
            } catch (UnavailableException e) {
                DemoUtils.handleSessionException(this, e);
            }
        }

        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            DemoUtils.displayError(this, "Unable to get camera", ex);
            finish();
            return;
        }

        if (arSceneView.getSession() != null) {
            showLoadingMessage();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (arSceneView != null) {
            arSceneView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (arSceneView != null) {
            arSceneView.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        if (!DemoUtils.hasCameraPermission(this)) {
            if (!DemoUtils.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                DemoUtils.launchPermissionSettings(this);
            } else {
                Toast.makeText(
                        this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                        .show();
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }


    private Node placePins() {
        Node base = new Node();
        base.setLocalPosition(arSceneView.getScene().getCamera().getWorldPosition());
        Node sun = new Node();
        sun.setParent(base);
        sun.setLocalPosition(new Vector3(0.0f, 0.0f, 0.0f));

        createPlace("Place", "Place", sun, pinRenderable, 0.19f, 0.2f, 0.1f, 0.5f);

        createPlace("Star", "Star", sun, starRenderable, 0.019f, 0.4f, 0.2f, 1f);

        return base;
    }

    private Node createPlace(
            String name,
            String description,
            Node parent,
            ModelRenderable renderable,
            float planetScale,
            float x,
            float y,
            float z) {

        // Create the place and position it relative to the sun.
        Place place = new Place(this, name, description, planetScale, renderable);
        place.setParent(parent);
        place.setLocalPosition(new Vector3(x,y,z));

        return place;
    }

    private void showLoadingMessage() {
        findViewById(R.id.tutorial).setVisibility(View.VISIBLE);
    }

    private void hideLoadingMessage() {
        ((Tutorial) findViewById(R.id.tutorial)).collapse();
    }
}
