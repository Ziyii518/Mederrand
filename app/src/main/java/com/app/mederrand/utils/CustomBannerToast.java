package com.app.mederrand.utils;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.app.mederrand.R;
import com.shasin.notificationbanner.Banner;

public class CustomBannerToast {
    public static final int DURATION = 3000;

    public static void showRequiredToast(View view, Activity context, String pleaseEnter) {
        Banner.make(view, context, Banner.TOP, R.layout.custom_popup_banner);

        RelativeLayout ll_cookie = Banner.getInstance().getBannerView().findViewById(R.id.ll_cookie);
        TextView tv_message = Banner.getInstance().getBannerView().findViewById(R.id.tv_message);
        TextView tv_title = Banner.getInstance().getBannerView().findViewById(R.id.tv_title);

        tv_message.setText(pleaseEnter);
        tv_title.setText(context.getString(R.string.text_required));

        Banner.getInstance().setCustomAnimationStyle(R.style.topAnimation);
        Banner.getInstance().setDuration(DURATION);
        Banner.getInstance().show();

        ll_cookie.setOnClickListener(view1 -> Banner.getInstance().dismissBanner());
    }

    public static void showSuccessToast(View view, Activity context, String pleaseEnter) {
        Banner.make(view, context, Banner.TOP, R.layout.custom_popup_banner);

        RelativeLayout ll_cookie = Banner.getInstance().getBannerView().findViewById(R.id.ll_cookie);

        TextView tv_message = Banner.getInstance().getBannerView().findViewById(R.id.tv_message);
        TextView tv_title = Banner.getInstance().getBannerView().findViewById(R.id.tv_title);
        ImageView iv_icon = Banner.getInstance().getBannerView().findViewById(R.id.iv_icon);
        iv_icon.setImageResource(R.drawable.ic_baseline_sentiment_satisfied);
        tv_message.setText(pleaseEnter);
        tv_title.setText(R.string.text_success);

        Banner.getInstance().setCustomAnimationStyle(R.style.topAnimation);
        Banner.getInstance().setDuration(DURATION);
        Banner.getInstance().show();

        ll_cookie.setOnClickListener(view1 -> Banner.getInstance().dismissBanner());
    }


    public static void showSuccessToastWithTitle(View view, Activity context, String title, String pleaseEnter) {
        Banner.make(view, context, Banner.TOP, R.layout.custom_popup_banner);

        RelativeLayout ll_cookie = Banner.getInstance().getBannerView().findViewById(R.id.ll_cookie);

        TextView tv_message = Banner.getInstance().getBannerView().findViewById(R.id.tv_message);
        TextView tv_title = Banner.getInstance().getBannerView().findViewById(R.id.tv_title);
        ImageView iv_icon = Banner.getInstance().getBannerView().findViewById(R.id.iv_icon);
        iv_icon.setImageResource(R.drawable.ic_baseline_sentiment_satisfied);
        tv_message.setText(pleaseEnter);
        tv_title.setText(title);

        Banner.getInstance().setCustomAnimationStyle(R.style.topAnimation);
        Banner.getInstance().setDuration(DURATION);
        Banner.getInstance().show();

        ll_cookie.setOnClickListener(view1 -> Banner.getInstance().dismissBanner());
    }


    public static void showToastWithTitle(View view, Activity context, String title, String pleaseEnter) {

        Banner.make(view, context, Banner.TOP, R.layout.custom_popup_banner);

        CardView ll_cookie = Banner.getInstance().getBannerView().findViewById(R.id.ll_cookie);
        TextView tv_message = Banner.getInstance().getBannerView().findViewById(R.id.tv_message);
        TextView tv_title = Banner.getInstance().getBannerView().findViewById(R.id.tv_title);
        tv_message.setText(pleaseEnter);
        tv_title.setText(title);

        Banner.getInstance().setCustomAnimationStyle(R.style.topAnimation);
        Banner.getInstance().setDuration(DURATION);
        Banner.getInstance().show();

        ll_cookie.setOnClickListener(view1 -> Banner.getInstance().dismissBanner());
    }

    public static void showFailureToast(View view, Activity context, String title, String pleaseEnter) {
        Banner.make(view, context, Banner.TOP, R.layout.custom_popup_banner);

        RelativeLayout ll_cookie = Banner.getInstance().getBannerView().findViewById(R.id.ll_cookie);
        TextView tv_message = Banner.getInstance().getBannerView().findViewById(R.id.tv_message);
        TextView tv_title = Banner.getInstance().getBannerView().findViewById(R.id.tv_title);

        tv_message.setText(pleaseEnter);
        tv_title.setText(title);

        Banner.getInstance().setCustomAnimationStyle(R.style.topAnimation);
        Banner.getInstance().setDuration(DURATION);
        Banner.getInstance().show();

        ll_cookie.setOnClickListener(view1 -> Banner.getInstance().dismissBanner());
    }
}
