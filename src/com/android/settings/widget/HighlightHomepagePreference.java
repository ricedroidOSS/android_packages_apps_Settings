/*
 * Copyright (C) 2023-2024 The risingOS Android Project
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
import android.content.Intent;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;

public class HighlightHomepagePreference extends HomepagePreference implements
        HomepagePreferenceLayoutHelper.HomepagePreferenceLayout {

    private final HomepagePreferenceLayoutHelper mHelper;
    private Context mContext;

    public HighlightHomepagePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mHelper = new HomepagePreferenceLayoutHelper(this);
        mContext = context;
        init();
    }

    public HighlightHomepagePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHelper = new HomepagePreferenceLayoutHelper(this);
        mContext = context;
        init();
    }

    public HighlightHomepagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHelper = new HomepagePreferenceLayoutHelper(this);
        mContext = context;
        init();
    }

    public HighlightHomepagePreference(Context context) {
        super(context);
        mHelper = new HomepagePreferenceLayoutHelper(this);
        mContext = context;
        init();
    }

    private void init() {
        setLayoutResource(R.layout.top_level_preference_highlight_card);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        ViewPager2 viewPager = holder.itemView.findViewById(R.id.card_holders);
        List<HighlightCard> highlightCards = new ArrayList<>();
        highlightCards.add(new HighlightCard(mContext.getString(R.string.personalize_highlight_settings_title), mContext.getString(R.string.rising_settings_summary), R.drawable.ic_custom_settings_wallpaper_white));
        highlightCards.add(new HighlightCard(mContext.getString(R.string.network_dashboard_title), mContext.getString(R.string.network_highlight_settings_summary), R.drawable.ic_custom_wireless));
        highlightCards.add(new HighlightCard(mContext.getString(R.string.bt_highlight_settings_title), mContext.getString(R.string.bt_highlight_settings_summary), R.drawable.ic_custom_devices));
        HighlightCardAdapter adapter = new HighlightCardAdapter(highlightCards);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPageTransformer(new MarginPageTransformer((int) mContext.getResources().getDimension(R.dimen.page_margin)));
        viewPager.post(new Runnable() {
            @Override
            public void run() {
                viewPager.beginFakeDrag();
                viewPager.fakeDragBy(-10f);
                viewPager.endFakeDrag();
                viewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        viewPager.beginFakeDrag();
                        viewPager.fakeDragBy(10f);
                        viewPager.endFakeDrag();
                    }
                });
            }
        });
    }

    @Override
    public HomepagePreferenceLayoutHelper getHelper() {
        return mHelper;
    }

    private static class HighlightCard {
        private final String title;
        private final String summary;
        private final int iconResId;

        public HighlightCard(String title, String summary, int iconResId) {
            this.title = title;
            this.summary = summary;
            this.iconResId = iconResId;
        }

        public String getTitle() {
            return title;
        }

        public String getSummary() {
            return summary;
        }

        public int getIconResId() {
            return iconResId;
        }
    }

    private static class HighlightCardAdapter extends RecyclerView.Adapter<HighlightCardAdapter.HighlightCardViewHolder> {

        private final List<HighlightCard> mHighlightCards;

        public HighlightCardAdapter(List<HighlightCard> highlightCards) {
            mHighlightCards = highlightCards;
        }

        @NonNull
        @Override
        public HighlightCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.highlight_card, parent, false);
            return new HighlightCardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HighlightCardViewHolder holder, int position) {
            HighlightCard card = mHighlightCards.get(position);
            holder.title.setText(card.getTitle());
            holder.summary.setText(card.getSummary());
            holder.icon.setImageResource(card.getIconResId());
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            holder.itemView.setLayoutParams(layoutParams);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    if (position == 0) {
                        intent = new Intent();
                        intent.setClassName("com.android.settings",
                            "com.android.settings.Settings$crDroidSettingsLayoutActivity");
                    } else if (position == 1) {
                        intent = new Intent();
                        intent.setClassName("com.android.settings",
                            "com.android.settings.Settings$NetworkDashboardActivity");
                    } else if (position == 2) {
                        intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                    } else {
                        return;
                    }
                    v.getContext().startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mHighlightCards.size();
        }

        static class HighlightCardViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            TextView summary;
            ImageView icon;

            HighlightCardViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(android.R.id.title);
                summary = itemView.findViewById(android.R.id.summary);
                icon = itemView.findViewById(android.R.id.icon);
            }
        }
    }
}
