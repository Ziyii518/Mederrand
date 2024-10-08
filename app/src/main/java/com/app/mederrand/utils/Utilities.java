package com.app.mederrand.utils;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.app.mederrand.R;
import com.app.mederrand.activities.WebViewActivity;
import com.app.mederrand.models.local.CountriesModel;
import com.app.mederrand.models.local.EmergencyContact;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class Utilities {
    private static final String SHARED_STORAGE = "android-mederrand";
    final Context context;


    public Utilities(Context context) {
        this.context = context;
    }

    //---------------------------------- SHARED PREFERENCE -----------------------------------------

    public static void saveInt(Context context, String key, int value) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getInt(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_STORAGE, Context.MODE_PRIVATE);
        return sharedPref.getInt(key, 0);

    }

    public static void saveLong(Context context, String key, long value) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static long getLong(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_STORAGE, Context.MODE_PRIVATE);
        return sharedPref.getLong(key, 0);
    }

    public static void saveString(Context context, String key, String value) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_STORAGE, Context.MODE_PRIVATE);
        return sharedPref.getString(key, "");

    }

    public static void saveEmergencyContactsList(Context context,  String key, List<EmergencyContact> list) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    public static List<EmergencyContact> getEmergencyContactsList(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<List<EmergencyContact>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public static void saveStringList(Context context, String key, List<String> value) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Set<String> set = new HashSet<>(value);
        editor.putStringSet(key, set);
        editor.apply();
    }

    public static List<String> getStringList(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_STORAGE, Context.MODE_PRIVATE);
        Set<String> set = sharedPref.getStringSet(key, new HashSet<>());
        return new ArrayList<>(set);
    }


    public static void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();

    }

    public static Boolean getBoolean(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_STORAGE, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(key, false);
    }


    public static void clearSharedPref(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
    }

    // STATUS BAR AND NAVIGATION BARS

    public static void setCustomStatusAndNavColor(Activity activity, int statusColor, int navColor) {
        Window window = activity.getWindow();
        View view = window.getDecorView();
        view.setSystemUiVisibility(view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        view.setSystemUiVisibility(view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        window.setNavigationBarColor(ContextCompat.getColor(activity, navColor));
        window.setStatusBarColor(ContextCompat.getColor(activity, statusColor));
    }

    public static void hideNavAndStatusBarNew(Activity activity) {
        WindowCompat.setDecorFitsSystemWindows( activity.getWindow(), false);
    }


    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            return (mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting());
        } else
            return false;


    }
    public static void moveToClickedContentAndHideKeyBoard(Activity activity, View view, Class<?> className) {
        view.setOnClickListener(v -> {
            hideKeyboard(view, activity);
            Intent mainIntent = new Intent(activity, className);
            activity.startActivity(mainIntent);
            activity.overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
        });
    }

    public static void openWebView(Activity activity, String pageTitle, String pageUrl){
        Intent webViewIntent = new Intent(activity, WebViewActivity.class);
        webViewIntent.putExtra("pageTitle", pageTitle);
        webViewIntent.putExtra("pageUrl", pageUrl);
        activity.startActivity(webViewIntent);
        activity.overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
    }

    public static void showHidePassword(ImageView imageView, EditText editText){
        imageView.setImageResource(R.drawable.ic_close_eye_new);
        imageView.setOnClickListener(v -> {
            if (editText.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                imageView.setImageResource(R.drawable.ic_eye_on);
                editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                imageView.setImageResource(R.drawable.ic_close_eye_new);
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            editText.setSelection(editText.getText().toString().length());
        });
    }

    public static boolean isValidEmail(CharSequence target) {
        if (target != null) {
            if (!isEmpty(target)) {
                return checkEmail(target);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );

    public static boolean checkEmail(CharSequence email) {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }


    public static void hideKeyboard(View view, Context context) {
        InputMethodManager in = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getCountryName(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            String countryCode = telephonyManager.getSimCountryIso();
            if (countryCode != null && !countryCode.isEmpty()) {
                // Convert the country code to country name
                Locale locale = new Locale("", countryCode);
                return locale.getDisplayCountry();
            }
        }
        return "";
    }

    public static List<CountriesModel> getCountriesList(Context context){
        List<CountriesModel> countriesList = new ArrayList<>();
        try {
            JSONArray countriesJsonArray = new JSONArray(loadCountriesJSONFromAsset(context));
            for (int i = 0; i < countriesJsonArray.length(); i++) {
                JSONObject jo_inside = countriesJsonArray.getJSONObject(i);
                String name = jo_inside.optString("name");
                String flag = jo_inside.optString("flag");
                String format = jo_inside.optString("format");
                String dial_code = jo_inside.optString("dial_code");
                String digit1 = jo_inside.optString("digit1");
                countriesList.add(new CountriesModel(name, format, flag, dial_code, digit1));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("SignUpActivity", e.getMessage());
        }

        return countriesList;
    }

    public static String loadCountriesJSONFromAsset(Context context) {
        String json;
        try {
            InputStream is = context.getAssets().open("countries.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }


    public static void keyBoardVisibilityListener(View root, View keyBoardVisibilityManager) {

        final View rootView = root; // Replace with your root layout
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) { // Adjust this threshold as needed
                // Keyboard is visible
                keyBoardVisibilityManager.setVisibility(View.VISIBLE);
            } else {
                // Keyboard is hidden
                keyBoardVisibilityManager.setVisibility(View.GONE);
            }
        });
    }

    public static String getSelectedDateForTextView(int dayOfMonth, int monthOfYear, int year) {
        String day;
        String month;
        if (dayOfMonth < 10) {
            day = "0" + dayOfMonth;
        } else {
            day = "" + dayOfMonth;
        }

        if (monthOfYear < 10) {
            month = "0" + monthOfYear;
        } else {
            month = "" + monthOfYear;
        }

        return day + " " + getMonthNumberForCustomCalendar(Integer.parseInt(month)) + " " + year;
    }

    public static String getMonthNumberForCustomCalendar(int month) {

        return switch (month) {
            case 1 -> "January";
            case 2 -> "February";
            case 3 -> "March";
            case 4 -> "April";
            case 5 -> "May";
            case 6 -> "June";
            case 7 -> "July";
            case 8 -> "August";
            case 9 -> "September";
            case 10 -> "October";
            case 11 -> "November";
            case 12 -> "December";
            default -> "";
        };
    }

    public static String getSelectedDateForApi(int dayOfMonth, int monthOfYear, int year) {
        String day;
        String month;
        if (dayOfMonth < 10) {
            day = "0" + dayOfMonth;
        } else {
            day = "" + dayOfMonth;
        }
        if (monthOfYear < 10) {
            month = "0" + monthOfYear;
        } else {
            month = "" + monthOfYear;
        }
        return year + "-" + month + "-" + day;
    }

    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // IMAGE LOADING

    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof final Activity activity) {
            return !activity.isDestroyed() && !activity.isFinishing();
        }
        return true;
    }

    public static void loadImage(Context context, String image, ImageView imageView) {
        if (image.isEmpty()){
            imageView.setImageResource(R.drawable.colored_place_holder_ic);
        }else {
            if (!isValidContextForGlide(context)) return;
            try {
                Glide.with(context)
                        .applyDefaultRequestOptions(new RequestOptions().error(R.drawable.colored_place_holder_ic))
                        .load(image)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(imageView);
            } catch (Exception e) {

            }
        }
    }

    public static void loadImage(Context context, String image, ImageView imageView, View loadingImage) {
        if (image.isEmpty()){
            loadingImage.setVisibility(View.GONE);
            imageView.setImageResource(R.drawable.colored_place_holder_ic);
        }else {
            if (!isValidContextForGlide(context)) return;

            try {
                loadingImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(image)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .placeholder(R.drawable.colored_place_holder_ic)
                        .error(R.drawable.colored_place_holder_ic)
                        .signature(new MediaStoreSignature("*/*", java.util.Calendar.DATE, 0))
                        .listener(new RequestListener<>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                loadingImage.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                loadingImage.setVisibility(View.GONE);
                                return false;
                            }
                        }).into(imageView);


            } catch (Exception ignored) {

            }
        }
    }

    public static void sendNotification(String recipientToken, String title, String body) {
        RemoteMessage.Builder messageBuilder = new RemoteMessage.Builder(recipientToken);
        messageBuilder.setMessageId(Integer.toString((int) (System.currentTimeMillis() / 1000)));
        messageBuilder.addData("title", title);
        messageBuilder.addData("body", body);
        FirebaseMessaging.getInstance().send(messageBuilder.build());
    }


    public static void createMarker(GoogleMap googleMap, Context context, double latitude, double longitude, String snippet, int iconResID) {
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title("Your Location")
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(context, iconResID))));
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        Bitmap bitmap = Bitmap.createBitmap(Objects.requireNonNull(drawable).getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}


