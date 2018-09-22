/*
 * Copyright 2018 Google LLC
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
package com.rickandmorty.exploresouthtyrol.Nodes;

import android.content.Context;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.rickandmorty.exploresouthtyrol.Model.PlaceModel;
import com.google.ar.sceneform.samples.solarsystem.R;

/**
 * Node that represents a place.
 * <p>
 * <p>The place creates a child nodes when it is activated:
 * <p>
 * <ul>
 * <li>The visual of the place.
 * <li>An info card, renders an Android View that displays the name of the place and a short description.
 * This can be toggled on and off.
 * </ul>
 */
public class PlaceNode extends Node implements Node.OnTouchListener {

    private PlaceModel placeModel;

    private final float placeScale;

    private final ModelRenderable placeRenderable;

    private Node infoCard;

    private Node placeVisual;

    private final Context context;

    private static final float INFO_CARD_Y_POS_COEFF = 3.55f;

    public PlaceNode(
            Context context,
            PlaceModel placeModel,
            float placeScale,
            ModelRenderable placeRenderable) {
        this.context = context;
        this.placeModel = placeModel;
        this.placeScale = placeScale;
        this.placeRenderable = placeRenderable;
        setOnTouchListener(this);
    }

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void onActivate() {

        if (getScene() == null) {
            throw new IllegalStateException("Scene is null!");
        }

        if (infoCard == null) {
            infoCard = new Node();
            infoCard.setParent(this);
            infoCard.setEnabled(false);
            infoCard.setLocalPosition(new Vector3(0.0f, placeScale * INFO_CARD_Y_POS_COEFF, 0.0f));
            infoCard.setLocalScale(new Vector3(placeScale * 15, placeScale * 15, placeScale * 15));
            infoCard.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {
                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        infoCard.setEnabled(false);
                        return true;
                    }

                    return false;
                }
            });

            ViewRenderable.builder()
                    .setView(context, R.layout.card_layout)
                    .build()
                    .thenAccept(
                            (renderable) -> {
                                infoCard.setRenderable(renderable);
                                View view = renderable.getView();
                                ((TextView) view.findViewById(R.id.Title)).setText(placeModel.title);
                                ((TextView) view.findViewById(R.id.description)).setText(Html.fromHtml(placeModel.description));
                            })
                    .exceptionally(
                            (throwable) -> {
                                throw new AssertionError("Could not load plane card view.", throwable);
                            });
        }

        if (placeVisual == null) {
            placeVisual = new Node();
            placeVisual.setParent(this);
            placeVisual.setRenderable(placeRenderable);
            placeVisual.setLocalScale(new Vector3(placeScale, placeScale, placeScale));
        }
    }

    @Override
    public boolean onTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {
        if (infoCard == null) {
            return false;
        }

        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            infoCard.setEnabled(!infoCard.isEnabled());
            return true;
        }

        return false;
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        if (infoCard == null) {
            return;
        }

        // Typically, getScene() will never return null because onUpdate() is only called when the node
        // is in the scene.
        // However, if onUpdate is called explicitly or if the node is removed from the scene on a
        // different thread during onUpdate, then getScene may be null.
        if (getScene() == null) {
            return;
        }

        Vector3 cameraPosition = getScene().getCamera().getWorldPosition();
        Vector3 cardPosition = infoCard.getWorldPosition();
        Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
        Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
        infoCard.setWorldRotation(lookRotation);
    }
}
