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

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.samples.solarsystem.Helper.LocationHelper;
import com.google.ar.sceneform.samples.solarsystem.Helper.SensorHelper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore and Sceneform APIs.
 */
public class SolarActivity extends AppCompatActivity {
    public static final int RC_PERMISSIONS = 0x123;
    private boolean installRequested;

    private GestureDetector gestureDetector;
    private Snackbar loadingMessageSnackbar = null;

    private ArSceneView arSceneView;

    private ModelRenderable sunRenderable;
    private ModelRenderable mercuryRenderable;
    private ModelRenderable venusRenderable;
    private ModelRenderable earthRenderable;
    private ModelRenderable lunaRenderable;
    private ModelRenderable marsRenderable;
    private ModelRenderable jupiterRenderable;
    private ModelRenderable saturnRenderable;
    private ModelRenderable uranusRenderable;
    private ModelRenderable neptuneRenderable;
    private ViewRenderable solarControlsRenderable;

    private final SolarSettings solarSettings = new SolarSettings();

    private SensorHelper mSensorHelper;

    private LocationHelper mLocationHelper;

    // True once scene is loaded
    private boolean hasFinishedLoading = false;

    // True once the scene has been placed.
    private boolean hasPlacedSolarSystem = false;

    // Astronomical units to meters ratio. Used for positioning the planets of the solar system.
    private static final float AU_TO_METERS = 0.5f;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DemoUtils.requestCameraPermission(this, RC_PERMISSIONS);
        DemoUtils.requestLocationPermission(this, RC_PERMISSIONS);

        mLocationHelper = LocationHelper.getInstance(this);
        mSensorHelper = SensorHelper.getInstance(this);

        if (!DemoUtils.checkIsSupportedDeviceOrFinish(this)) {
            // Not a supported device.
            return;
        }

        setContentView(R.layout.activity_solar);
        arSceneView = findViewById(R.id.ar_scene_view);

        // Build all the planet models.
        CompletableFuture<ModelRenderable> sunStage =
                ModelRenderable.builder().setSource(this, Uri.parse("Sol.sfb")).build();
        CompletableFuture<ModelRenderable> mercuryStage =
                ModelRenderable.builder().setSource(this, Uri.parse("Mercury.sfb")).build();
        CompletableFuture<ModelRenderable> venusStage =
                ModelRenderable.builder().setSource(this, Uri.parse("Venus.sfb")).build();
        CompletableFuture<ModelRenderable> earthStage =
                ModelRenderable.builder().setSource(this, Uri.parse("Earth.sfb")).build();
        CompletableFuture<ModelRenderable> lunaStage =
                ModelRenderable.builder().setSource(this, Uri.parse("Luna.sfb")).build();
        CompletableFuture<ModelRenderable> marsStage =
                ModelRenderable.builder().setSource(this, Uri.parse("Mars.sfb")).build();
        CompletableFuture<ModelRenderable> jupiterStage =
                ModelRenderable.builder().setSource(this, Uri.parse("Jupiter.sfb")).build();
        CompletableFuture<ModelRenderable> saturnStage =
                ModelRenderable.builder().setSource(this, Uri.parse("Saturn.sfb")).build();
        CompletableFuture<ModelRenderable> uranusStage =
                ModelRenderable.builder().setSource(this, Uri.parse("Uranus.sfb")).build();
        CompletableFuture<ModelRenderable> neptuneStage =
                ModelRenderable.builder().setSource(this, Uri.parse("Neptune.sfb")).build();

        // Build a renderable from a 2D View.
        CompletableFuture<ViewRenderable> solarControlsStage =
                ViewRenderable.builder().setView(this, R.layout.solar_controls).build();

        CompletableFuture.allOf(
                sunStage,
                mercuryStage,
                venusStage,
                earthStage,
                lunaStage,
                marsStage,
                jupiterStage,
                saturnStage,
                uranusStage,
                neptuneStage,
                solarControlsStage)
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
                                sunRenderable = sunStage.get();
                                mercuryRenderable = mercuryStage.get();
                                venusRenderable = venusStage.get();
                                earthRenderable = earthStage.get();
                                lunaRenderable = lunaStage.get();
                                marsRenderable = marsStage.get();
                                jupiterRenderable = jupiterStage.get();
                                saturnRenderable = saturnStage.get();
                                uranusRenderable = uranusStage.get();
                                neptuneRenderable = neptuneStage.get();
                                solarControlsRenderable = solarControlsStage.get();

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
                            if (loadingMessageSnackbar == null) {
                                return;
                            }

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
                                    hasPlacedSolarSystem = true;
                                    Node solarSystem = createSolarSystem();
                                    Pose pose = new Pose(new float[]{0.0f, 0.0f, 0.0f}, new float[]{0.0f, 0.0f, 0.0f, 0.0f});
                                    Anchor anchor = plane.createAnchor(pose);
                                    AnchorNode anchorNode = new AnchorNode(anchor);
                                    anchorNode.setParent(arSceneView.getScene());
                                    anchorNode.addChild(solarSystem);
                                }
                            }
                        });

        // Lastly request CAMERA permission which is required by ARCore.
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


    private Node createSolarSystem() {
        Node base = new Node();
        base.setLocalPosition(arSceneView.getScene().getCamera().getWorldPosition());
        Node sun = new Node();
        sun.setParent(base);
        sun.setLocalPosition(new Vector3(0.0f, 0.0f, 0.0f));

        Node sunVisual = new Node();
        sunVisual.setParent(sun);
        sunVisual.setRenderable(sunRenderable);
        sunVisual.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));


        createPlanet("Mercury", sun, 0.4f, mercuryRenderable, 0.019f);

        createPlanet("Venus", sun, 0.7f, venusRenderable, 0.0475f);

        Node earth = createPlanet("Earth", sun, 1.0f, earthRenderable, 0.05f);

        createPlanet("Moon", earth, 0.15f, lunaRenderable, 0.018f);

        createPlanet("Mars", sun, 1.5f, marsRenderable, 0.0265f);

        createPlanet("Jupiter", sun, 2.2f, jupiterRenderable, 0.16f);

        createPlanet("Saturn", sun, 3.5f, saturnRenderable, 0.1325f);

        createPlanet("Uranus", sun, 5.2f, uranusRenderable, 0.1f);

        createPlanet("Neptune", sun, 6.1f, neptuneRenderable, 0.074f);

        return base;
    }

    private Node createPlanet(
            String name,
            Node parent,
            float auFromParent,
            ModelRenderable renderable,
            float planetScale) {

        // Create the planet and position it relative to the sun.
        Planet planet = new Planet(this, name, planetScale, renderable, solarSettings);
        planet.setParent(parent);
        planet.setLocalPosition(new Vector3(auFromParent * AU_TO_METERS, (float) Math.random(), (float) Math.random()));

        return planet;
    }

    private void showLoadingMessage() {
        if (loadingMessageSnackbar != null && loadingMessageSnackbar.isShownOrQueued()) {
            return;
        }

        loadingMessageSnackbar =
                Snackbar.make(
                        SolarActivity.this.findViewById(android.R.id.content),
                        R.string.plane_finding,
                        Snackbar.LENGTH_INDEFINITE);
        loadingMessageSnackbar.getView().setBackgroundColor(0xbf323232);
        loadingMessageSnackbar.show();
    }

    private void hideLoadingMessage() {
        if (loadingMessageSnackbar == null) {
            return;
        }

        loadingMessageSnackbar.dismiss();
        loadingMessageSnackbar = null;
    }
}
