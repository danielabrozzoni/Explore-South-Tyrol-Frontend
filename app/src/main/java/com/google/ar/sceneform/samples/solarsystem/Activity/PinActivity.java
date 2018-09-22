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
package com.google.ar.sceneform.samples.solarsystem.Activity;

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
import com.google.ar.sceneform.samples.solarsystem.*;
import com.google.ar.sceneform.samples.solarsystem.Helper.CompassHelper;
import com.google.ar.sceneform.samples.solarsystem.Helper.DemoUtils;
import com.google.ar.sceneform.samples.solarsystem.Helper.GestureHelper;
import com.google.ar.sceneform.samples.solarsystem.Helper.LocationHelper;
import com.google.ar.sceneform.samples.solarsystem.Model.PlaceModel;
import com.google.ar.sceneform.samples.solarsystem.Nodes.AcceleratingNode;
import com.google.ar.sceneform.samples.solarsystem.Nodes.PlaceNode;
import com.google.ar.sceneform.samples.solarsystem.Thread.GetPointsThread;
import com.google.ar.sceneform.samples.solarsystem.Widget.Tutorial;

import java.util.*;
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
    private GetPointsThread mGetPoints;

    private LocationHelper mLocationHelper;

    private final float INITIAL_SCALE = 0.3f;
    private final float SCALE_FACTOR = 0.05f;

    // True once the scene has been placed.
    private boolean hasPlacedPins = false;

    private List<Vector3> queuedStars = new LinkedList<>();


    private void setupGesture() {
        GestureHelper gestureHelper = new GestureHelper();
        gestureHelper.setTouchListener(findViewById(R.id.ar_scene_view),
                force -> {
                    Log.d("PinActivityForce", force.toString());
                    queuedStars.add(force);
                    //Toast.makeText(this, "Touched", Toast.LENGTH_SHORT).show();
                });
    }

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

        final View rootView = getWindow().getDecorView().getRootView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(this::setupGesture);

        mLocationHelper = LocationHelper.getInstance(this);
        mLocationHelper.initLocationService();
        setContentView(R.layout.activity_solar);
        arSceneView = findViewById(R.id.ar_scene_view);

        initRenderable();

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

                            if (mGetPoints == null) {
                                mGetPoints = new GetPointsThread((float) mLocationHelper.getLastLocation().getLatitude(),
                                        (float) mLocationHelper.getLastLocation().getLongitude());
                                return;
                            }

                            Pose cameraPose = frame.getCamera().getPose();

                            Vector3 initialForce = Quaternion.rotateVector(
                                    new Quaternion(cameraPose.qw(), cameraPose.qx(), cameraPose.qy(), cameraPose.qz()),
                                    Vector3.one()
                            ).scaled(1000);

                            initialForce.y = 500f;

                            AnchorNode anchorNode = null;

                            if (queuedStars.size() > 0) {
                                Anchor anchor = arSceneView.getSession().createAnchor(frame.getCamera().getPose());
                                anchorNode = new AnchorNode(anchor);
                                anchorNode.setParent(arSceneView.getScene());
                            }

                            // Try to place the stars
                            ListIterator<Vector3> iter = queuedStars.listIterator();
                            while(iter.hasNext()){
                                Vector3 swipeForce = iter.next();
                                //Vector3 initialForce = new Vector3(swipeForce.x, 300f, 0);

                                Log.d("InitialForce", initialForce.toString());

                                Node mStar = new AcceleratingNode(100, initialForce, 10f);
                                mStar.setParent(anchorNode);
                                mStar.setRenderable(starRenderable);
                                mStar.setLocalPosition(new Vector3(0f, -0.045f, 0f));
                                mStar.setLocalScale(new Vector3(0.1f, 0.1f, 0.1f));

                                iter.remove();
                            }

                            if (hasPlacedPins) {
                                return;
                            }

                            for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                                if (plane.getTrackingState() == TrackingState.TRACKING) {
                                    if (mGetPoints.hasPlaces()) {
                                        displayPins(mGetPoints.getPlaces(), plane);
                                    } else {
                                        // We have a plane, let's query the server
                                        displayDontMoveMessage(); // Warn the user to avoid movements

                                        mCompassHelper = CompassHelper.getInstance(this);
                                        mCompassHelper.init(() -> {
                                            if (mGetPoints.isAlive()) {
                                                return;
                                            }

                                            mGetPoints.setHeading(mCompassHelper.getCurrentDegree());
                                            mGetPoints.start();
                                        });
                                    }
                                }
                            }
                        });

    }

    private void displayPins(List<PlaceModel> placeModels, Plane plane) {
        Node worldView = placePins(placeModels);

        Pose pose = new Pose(new float[]{0.0f, 0.0f, 0.0f}, new float[]{0.0f, 0.0f, 0.0f, 0.0f});
        Anchor initialAnchor = plane.createAnchor(pose);

        AnchorNode anchorNode = new AnchorNode(initialAnchor);
        anchorNode.setParent(arSceneView.getScene());
        anchorNode.addChild(worldView);

        hideLoadingMessage();
        hasPlacedPins = true;
    }

    private void displayDontMoveMessage() {
        ((Tutorial) findViewById(R.id.tutorial)).dontMove();
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


    private Node placePins(List<PlaceModel> placeModels) {
        Node base = new Node();
        base.setLocalPosition(arSceneView.getScene().getCamera().getWorldPosition());

        for (PlaceModel placeModel : placeModels) {
            createPlace(placeModel, base, pinRenderable);
        }

        return base;
    }

    private Node createPlace(
            PlaceModel placeModel,
            Node parent,
            ModelRenderable renderable) {
        float placeScale = INITIAL_SCALE + SCALE_FACTOR / placeModel.distance;

        PlaceNode placeNode = new PlaceNode(this, placeModel, placeScale, renderable);
        placeNode.setParent(parent);
        placeNode.setLocalPosition(new Vector3(placeModel.x, placeModel.y, placeModel.z));

        return placeNode;
    }

    private void showLoadingMessage() {
        findViewById(R.id.tutorial).setVisibility(View.VISIBLE);
    }

    private void hideLoadingMessage() {
        ((Tutorial) findViewById(R.id.tutorial)).collapse();
    }

    private void initRenderable() {
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

                            } catch (InterruptedException | ExecutionException ex) {
                                DemoUtils.displayError(this, "Unable to load renderable", ex);
                            }

                            return null;
                        });
    }
}
