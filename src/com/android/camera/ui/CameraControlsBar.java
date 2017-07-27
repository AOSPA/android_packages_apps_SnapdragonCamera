/*
 * Copyright (C) 2017 Paranoid Android
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

package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import org.codeaurora.snapcam.R;

public class CameraControlsBar extends FrameLayout implements Rotatable {

    private ModuleSwitcher mModuleSwitcher;
    private CameraSettings mCameraSettings;

    public CameraControlsBar(Context context) {
        this(context, null, 0);
    }

    public CameraControlsBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraControlsBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mModuleSwitcher = (ModuleSwitcher) findViewById(R.id.camera_switcher);
        mCameraSettings = (CameraSettings) findViewById(R.id.camera_settings);

        ((View) mCameraSettings.getParent()).setVisibility(INVISIBLE);
    }

    @Override
    public void setOrientation(int orientation, boolean animation) {
        mCameraSettings.setOrientation(orientation, animation);
    }

}
