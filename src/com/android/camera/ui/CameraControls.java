/*
 * Copyright (C) 2013 The Android Open Source Project
 * Copyright (C) 2017 ParanoidAndroid Project
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

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

import com.android.camera.CameraActivity;
import com.android.camera.ui.ModuleSwitcher;
import com.android.camera.ui.RotateImageView;
import com.android.camera.ShutterButton;
import com.android.camera.Storage;

import org.codeaurora.snapcam.R;

public class CameraControls extends RotatableLayout {

    private static final String TAG = "CAM_Controls";

    private CameraControlsUtils mCameraControlsUtils;

    private View mControlsParent;
    private View mShutter;
    private View mVideoShutter;
    private View mExitPanorama;
    private View mIndicators;
    private View mPreview;
    private View mReviewDoneButton;
    private View mReviewCancelButton;
    private View mReviewRetakeButton;
    private ArrowTextView mRefocusToast;

    private CameraActivity mActivity;

    private static final int WIDTH_GRID = 5;
    private static final int HEIGHT_GRID = 7;
    private static boolean isAnimating = false;
    private ArrayList<View> mViewList;
    private static final int ANIME_DURATION = 300;
    private boolean mHideRemainingPhoto = false;
    private LinearLayout mRemainingPhotos;
    private TextView mRemainingPhotosText;
    private int mCurrentRemaining = -1;
    private int mOrientation;

    private static final int LOW_REMAINING_PHOTOS = 20;
    private static final int HIGH_REMAINING_PHOTOS = 1000000;
    private static final int BACKGROUND_ALPHA = (int) (0.6f * 255);

    AnimatorListener outlistener = new AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            for (View view : mViewList) {
                view.setVisibility(View.INVISIBLE);
            }
            isAnimating = false;
            enableTouch(true);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            onAnimationEnd(animation);
        }
    };

    public View getPanoramaExitButton() {
        return mExitPanorama;
    }

    AnimatorListener inlistener = new AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            isAnimating = false;
            enableTouch(true);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            onAnimationEnd(animation);
        }
    };

    public CameraControls(Context context) {
        this(context, null);
    }

    public CameraControls(Context context, AttributeSet attrs) {
        super(context, attrs);

        mActivity = (CameraActivity) context;

        mRefocusToast = new ArrowTextView(context);
        addView(mRefocusToast);
        setClipChildren(false);

        setMeasureAllChildren(true);
    }

    public static boolean isAnimating() {
        return isAnimating;
    }

    public void enableTouch(boolean enable) {
        if (enable) {
            ((ShutterButton) mShutter).setPressed(false);
            mVideoShutter.setPressed(false);
            mExitPanorama.setPressed(false);
        }

        ((ShutterButton) mShutter).enableTouch(enable);
        mVideoShutter.setClickable(enable);
        mExitPanorama.setEnabled(enable);
        mPreview.setEnabled(enable);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mViewList = new ArrayList<>();

        mCameraControlsUtils = (CameraControlsUtils)
                findViewById(R.id.camera_controls_utils);

        mControlsParent = findViewById(R.id.camera_controls_row);

        mShutter = findViewById(R.id.shutter_button);
        mVideoShutter = findViewById(R.id.video_button);
        mExitPanorama = findViewById(R.id.exit_panorama);
        mIndicators = findViewById(R.id.on_screen_indicators);
        mPreview = findViewById(R.id.preview_thumb);
        mReviewDoneButton = findViewById(R.id.btn_done);
        mReviewCancelButton = findViewById(R.id.btn_cancel);
        mReviewRetakeButton = findViewById(R.id.btn_retake);

        mRemainingPhotos = (LinearLayout) findViewById(R.id.remaining_photos);
        mRemainingPhotosText = (TextView) findViewById(R.id.remaining_photos_text);

        mExitPanorama.setVisibility(View.GONE);

        mViewList.add(mShutter);
        mViewList.add(mVideoShutter);
        mViewList.add(mExitPanorama);
        mViewList.add(mIndicators);
        mViewList.add(mPreview);
        mViewList.add(mReviewDoneButton);
        mViewList.add(mReviewCancelButton);
        mViewList.add(mReviewRetakeButton);
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // As l,t,r,b are positions relative to parents, we need to convert them
        // to child's coordinates
        r = r - l;
        b = b - t;
        layoutToast(mRefocusToast, r, b);
        layoutRemaingPhotos();
    }

    private void layoutToast(final View v, int w, int h) {
        int tw = v.getMeasuredWidth();
        int th = v.getMeasuredHeight();
        int l = w / WIDTH_GRID / 4;
        int b = (int) (h / HEIGHT_GRID * (HEIGHT_GRID - 1.25));
        int r = l + tw;
        int t = b - th;
        mRefocusToast.setArrow(0, th, th / 2, th, 0, th * 3 / 2);
        mRefocusToast.layout(l, t, r, b);
    }

    public void setTitleBarVisibility(int status){
    }

    public void hideUI() {
        if (!isAnimating) {
            enableTouch(false);
        }
        isAnimating = true;
        for (View view : mViewList) {
            view.animate().cancel();
        }
        mActivity.setGridVisibility(View.GONE);
        mRemainingPhotos.setVisibility(View.INVISIBLE);
        mRefocusToast.setVisibility(View.GONE);
    }

    public void showUI() {
        if (!isAnimating) {
            enableTouch(false);
        }
        isAnimating = true;
        for (View view : mViewList) {
            view.animate().cancel();
            view.setVisibility(View.VISIBLE);
        }
        AnimationDrawable shutterAnim = (AnimationDrawable) mShutter.getBackground();
        if (shutterAnim != null) shutterAnim.stop();

        if ((mRemainingPhotos.getVisibility() == View.INVISIBLE) &&
                !mHideRemainingPhoto){
            mRemainingPhotos.setVisibility(View.VISIBLE);
        }
        if (mActivity.isGridEnabled()) {
            mActivity.setGridVisibility(View.VISIBLE);
        }
        mRefocusToast.setVisibility(View.GONE);
    }

    private void layoutRemaingPhotos() {
        int rl = mPreview.getLeft();
        int rt = mPreview.getTop();
        int rr = mPreview.getRight();
        int rb = mPreview.getBottom();
        int w = mRemainingPhotos.getMeasuredWidth();
        int h = mRemainingPhotos.getMeasuredHeight();
        int m = getResources().getDimensionPixelSize(R.dimen.remaining_photos_margin);

        int hc = (rl + rr) / 2;
        int vc = (rt + rb) / 2 - m;
        if (mOrientation == 90 || mOrientation == 270) {
            vc -= w / 2;
        }
        if(hc < w / 2) {
            mRemainingPhotos.layout(0, vc - h / 2, w, vc + h / 2);
        } else {
            mRemainingPhotos.layout(hc - w / 2, vc - h / 2, hc + w / 2, vc + h / 2);
        }
        mRemainingPhotos.setRotation(-mOrientation);
    }

    public void updateRemainingPhotos(int remaining) {
        long remainingStorage = Storage.getAvailableSpace() - Storage.LOW_STORAGE_THRESHOLD_BYTES;
        if ((remaining < 0 && remainingStorage <= 0) || mHideRemainingPhoto) {
            mRemainingPhotos.setVisibility(View.GONE);
        } else {
            for (int i = mRemainingPhotos.getChildCount() - 1; i >= 0; --i) {
                mRemainingPhotos.getChildAt(i).setVisibility(View.VISIBLE);
            }
            if (remaining < LOW_REMAINING_PHOTOS) {
                mRemainingPhotosText.setText("<" + LOW_REMAINING_PHOTOS + " ");
            } else if (remaining >= HIGH_REMAINING_PHOTOS) {
                mRemainingPhotosText.setText(">" + HIGH_REMAINING_PHOTOS);
            } else {
                mRemainingPhotosText.setText(remaining + " ");
            }
        }
        mCurrentRemaining = remaining;
    }

    public void setControlHeight(int height) {
        mControlsParent.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, height));
    }

    public void setTransparency(boolean transparent) {
        mControlsParent.setBackgroundColor(getResources().getColor(transparent ?
                R.color.camera_control_bg_transparent :
                R.color.camera_control_bg_opaque));
    }

    public void showRefocusToast(boolean show) {
        mRefocusToast.setVisibility(show ? View.VISIBLE : View.GONE);
        if ((mCurrentRemaining > 0 ) && !mHideRemainingPhoto) {
            mRemainingPhotos.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void setOrientation(int orientation, boolean animation) {
        mOrientation = orientation;
        for (View v : mViewList) {
            if (v instanceof RotateImageView) {
                ((RotateImageView) v).setOrientation(orientation, animation);
            }
        }
        mCameraControlsUtils.setOrientation(orientation, animation);
        layoutRemaingPhotos();
    }

    public void hideCameraSettings() {
    }

    public void showCameraSettings() {
    }

    public void hideRemainingPhotoCnt() {
        mHideRemainingPhoto = true;
        mRemainingPhotos.setVisibility(View.GONE);
        mRemainingPhotosText.setVisibility(View.GONE);
    }

    private class ArrowTextView extends TextView {
        private static final int TEXT_SIZE = 14;
        private static final int PADDING_SIZE = 18;
        private static final int BACKGROUND = 0x80000000;

        private Paint mPaint;
        private Path mPath;

        public ArrowTextView(Context context) {
            super(context);

            setText(context.getString(R.string.refocus_toast));
            setBackgroundColor(BACKGROUND);
            setVisibility(View.GONE);
            setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
            setTextSize(TEXT_SIZE);
            setPadding(PADDING_SIZE, PADDING_SIZE, PADDING_SIZE, PADDING_SIZE);

            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(BACKGROUND);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mPath != null) {
                canvas.drawPath(mPath, mPaint);
            }
        }

        public void setArrow(float x1, float y1, float x2, float y2, float x3, float y3) {
            mPath = new Path();
            mPath.reset();
            mPath.moveTo(x1, y1);
            mPath.lineTo(x2, y2);
            mPath.lineTo(x3, y3);
            mPath.lineTo(x1, y1);
        }
    }
}
