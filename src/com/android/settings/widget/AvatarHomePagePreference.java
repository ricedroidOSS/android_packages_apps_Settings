/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.settings.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.utils.UserUtils;

/** A customized layout for homepage preference. */
public class AvatarHomePagePreference extends HomepagePreference implements
        HomepagePreferenceLayoutHelper.HomepagePreferenceLayout {

    private ImageView mAvatarIcon;
    private View mUserCard;
    private final UserUtils mUserUtils;

    private final Handler mHandler = new Handler();
    private final Runnable mUpdateTileRunnable = new Runnable() {
        @Override
        public void run() {
            updateTile();
            mHandler.postDelayed(this, 1000);
        }
    };

    private Drawable mCurrentAvatarDrawable;
    private boolean isUpdatesRunning = false;

    public AvatarHomePagePreference(Context context, AttributeSet attrs, int defStyleAttr,
                                    int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mUserUtils = UserUtils.Companion.getInstance(context);
    }

    public AvatarHomePagePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mUserUtils = UserUtils.Companion.getInstance(context);
    }

    public AvatarHomePagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mUserUtils = UserUtils.Companion.getInstance(context);
    }

    public AvatarHomePagePreference(Context context) {
        super(context);
        mUserUtils = UserUtils.Companion.getInstance(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mAvatarIcon = (ImageView) holder.findViewById(R.id.user_avatar);
        mUserCard = holder.findViewById(R.id.user_card_holder);
        if (mAvatarIcon != null) {
            mUserUtils.setClick((View) mAvatarIcon);
        }
        if (mUserCard != null) {
            mUserUtils.setClick(mUserCard);
        }
        holder.itemView.post(() -> {
            String userName = mUserUtils.getUserName();
            if (userName != null && !userName.equals(getTitle())) {
                setTitle(userName);
            }
        });
        if (!isUpdatesRunning) {
            isUpdatesRunning = true;
            mHandler.post(mUpdateTileRunnable);
        }
    }

    @Override
    public HomepagePreferenceLayoutHelper getHelper() {
        return null;
    }

    private void updateTile() {
        if (mAvatarIcon != null) {
            Drawable newAvatarDrawable = mUserUtils.getCircularUserIcon();
            if (mCurrentAvatarDrawable == null || !mCurrentAvatarDrawable.equals(newAvatarDrawable)) {
                mAvatarIcon.setImageDrawable(newAvatarDrawable);
                mCurrentAvatarDrawable = newAvatarDrawable;
            }
        }
        if (getTitle() != null) {
            String userName = mUserUtils.getUserName();
            if (userName != null && !userName.equals(getTitle())) {
                setTitle(userName);
            }
        }
    }

    @Override
    public void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        if (isUpdatesRunning) {
            isUpdatesRunning = false;
            mHandler.removeCallbacks(mUpdateTileRunnable);
        }
    }
}
