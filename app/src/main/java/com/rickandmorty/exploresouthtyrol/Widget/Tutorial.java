package com.rickandmorty.exploresouthtyrol.Widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rickandmorty.exploresouthtyrol.Helper.AnimationHelper;
import com.google.ar.sceneform.samples.solarsystem.R;

public class Tutorial extends LinearLayout {
    public Tutorial(Context context) {
        super(context);
        init();
    }

    public Tutorial(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Tutorial(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public Tutorial(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.tutorial_view, this, false);
        ((TextView) view.findViewById(R.id.hint)).setText(R.string.tutorial_surface);
        addView(view);
    }

    public void expand() {
        AnimationHelper.expand(findViewById(R.id.view1), null);
        AnimationHelper.expand(findViewById(R.id.view3), new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.hint).setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void collapse() {
        AnimationHelper.collapse(findViewById(R.id.view1), null);
        AnimationHelper.collapse(findViewById(R.id.view3), new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.hint).setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void dontMove() {
        ((TextView) findViewById(R.id.hint)).setText(R.string.tutorial_dontmove);
    }
}
