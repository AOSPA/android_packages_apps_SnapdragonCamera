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
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import org.codeaurora.snapcam.R;

public class CameraControlsUtils extends ViewGroup {

    private int mItemPadding;

    public CameraControlsUtils(Context context) {
        this(context, null, 0);
    }

    public CameraControlsUtils(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraControlsUtils(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundColor(getResources().getColor(R.color.camera_control_bg_transparent));

        mItemPadding = getResources().getDimensionPixelSize(R.dimen.camera_controls_utils_item_padding);
    }

    private void setOrientation(View child, int orientation, boolean animation) {
        if (child instanceof RotateImageView) {
	    ((RotateImageView) child).setOrientation(orientation, animation);
        }
    }

    public void setOrientation(int orientation, boolean animation) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof ViewGroup) {
                ViewGroup childGroup = (ViewGroup) child;
                for (int x = 0; x < childGroup.getChildCount(); x++) {
                    setOrientation(childGroup.getChildAt(x),  orientation, animation);
                }
            } else {
                setOrientation(child, orientation, animation);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        List<List<View>> arrangement = new ArrayList<>();
        List<View> childrenRow = null;

        int childrenCount = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            if (childrenCount % 5 == 0) {
                childrenRow = new ArrayList<>();
                arrangement.add(0, childrenRow);
            }

            childrenRow.add(child);
            childrenCount++;
        }

        int adjustedWidth = width - 2 * mItemPadding;
        int widthAvailable = Math.round(adjustedWidth / 5f);

        int top = mItemPadding;
        for (int row = 0; row < arrangement.size(); row++) {
            List<View> columnViews = arrangement.get(row);
            int maxHeight = 0;

            for (View view : columnViews) {
                int viewHeight = view.getMeasuredHeight();
                if (viewHeight > maxHeight) {
                    maxHeight = viewHeight;
                }
            }

            for (int i = 0; i < columnViews.size(); i++) {
                View child = columnViews.get(i);
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                int widthOffset = 0;
                int heightOffset = 0;

                if (widthAvailable > childWidth) {
                    widthOffset = Math.round((widthAvailable - childWidth) / 2f);
                }
                if (maxHeight > childHeight) {
                    heightOffset = Math.round((maxHeight - childHeight) / 2f);
                }

                int left = mItemPadding + widthAvailable * i;
                child.layout(left + widthOffset,
                        top + heightOffset,
                        left + widthOffset + childWidth,
                        top + heightOffset + childHeight);
            }

            top += maxHeight + mItemPadding;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int childrenCount = 0;

        int currentWidth = 0;

        int childrenHeightCount = 0;
        int currentMaxHeight = 0;

        int childrenWidth = 0;
        int childrenHeight = 0;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            if (childrenCount % 5 == 0) {
                if (currentWidth > childrenWidth) {
                    childrenWidth = currentWidth;
                }

                childrenHeightCount++;
                childrenHeight += currentMaxHeight;
                currentMaxHeight = 0;
            }

            currentWidth += child.getMeasuredWidth();

            int childHeight = child.getMeasuredHeight();
            if (childHeight > currentMaxHeight) {
                currentMaxHeight = childHeight;
            }

            childrenCount++;
        }

        childrenWidth += 2 * mItemPadding;
        childrenHeight += (childrenHeightCount + 1) * mItemPadding + currentMaxHeight;

        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            childrenWidth = getMeasuredWidth();
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            childrenHeight = getMeasuredHeight();
        }

        setMeasuredDimension(childrenWidth, childrenHeight);
    }
}
