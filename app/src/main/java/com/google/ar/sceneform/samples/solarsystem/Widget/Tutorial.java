package com.google.ar.sceneform.samples.solarsystem.Widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

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
        addView(view);
    }
}
