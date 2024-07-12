package com.daon.fido.sdk.sample.fragments.pattern;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daon.fido.sdk.sample.fragments.BaseCaptureFragment;
import com.daon.sdk.authenticator.controller.PatternControllerProtocol;
import com.daon.fido.sdk.sample.R;

public abstract class PatternFragment extends BaseCaptureFragment implements
        PatternCollect.PatternCollectResultReceiver, IPatternManager {
    private PatternCollect patternCollect;
    private PatternParameters parameters;

    public PatternControllerProtocol getController() {
        return (PatternControllerProtocol) super.getController();
    }

    public void setController(PatternControllerProtocol controller) {
        super.setController(controller);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        patternCollect = new PatternCollect(getContext(), null);
        parameters = new PatternParameters();

        ViewGroup rootView = createView(inflater, container, savedInstanceState);

        patternCollect.setParameters(parameters);
        patternCollect.setEnabled(true);
        return rootView;
    }

    private ViewGroup createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(getLayoutResource(), container, false);

        ViewGroup layout = rootView.findViewById(R.id.layout);
        if (layout != null)
            layout.addView(patternCollect);

        return rootView;
    }

    @Override
    protected void start() {
        super.start();

        patternCollect.setController(getController());
        patternCollect.startCapture(getPatternCollectMode(), this, parameters, this);
    }

    protected abstract int getLayoutResource();

    protected abstract PatternCollect.Mode getPatternCollectMode();
}
