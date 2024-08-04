package me.t3sl4.ondergrup.Util;

import static me.t3sl4.ondergrup.Service.UserDataService.getUserFromPreferences;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import me.t3sl4.ondergrup.R;
import me.t3sl4.ondergrup.Screens.Auth.LoginScreen;
import me.t3sl4.ondergrup.Screens.Dashboard.Engineer;
import me.t3sl4.ondergrup.Screens.Dashboard.SysOp;
import me.t3sl4.ondergrup.Screens.Dashboard.Technician;
import me.t3sl4.ondergrup.Service.UserDataService;
import me.t3sl4.ondergrup.Util.Component.SharedPreferencesManager;

public class Util {
    public Context context;

    public static String userManualURL = "https://hidirektor.com.tr/manual/manual.pdf";

    public static String profilePhotoPath;
    public Util(Context context) {
        this.context = context;
        this.profilePhotoPath = context.getFilesDir() + "/profilePhoto/";
    }

    public static boolean isEmpty(TextInputEditText etText) {
        if (Objects.requireNonNull(etText.getText()).toString().trim().length() > 0)
            return false;

        return true;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void showErrorPopup(Dialog diyalog, String hataMesaji) {
        diyalog.setContentView(R.layout.activity_popup_warning);
        Button close = diyalog.findViewById(R.id.kapatButton);
        TextView hataMesajiTextView = diyalog.findViewById(R.id.uyariMesaji);

        hataMesajiTextView.setText(hataMesaji);
        close.setOnClickListener(v -> diyalog.dismiss());

        Objects.requireNonNull(diyalog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        diyalog.show();
    }

    public static void showSuccessPopup(Dialog diyalog, String hataMesaji) {
        diyalog.setContentView(R.layout.activity_popup_success);
        Button close = diyalog.findViewById(R.id.kapatButton);
        TextView hataMesajiTextView = diyalog.findViewById(R.id.uyariMesaji);

        hataMesajiTextView.setText(hataMesaji);
        close.setOnClickListener(v -> diyalog.dismiss());

        Objects.requireNonNull(diyalog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        diyalog.show();
    }

    public static String dateTimeConvert(String inputDate) {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("MM.dd.yyyy hh:mm:ss");

        try {
            Date date = inputDateFormat.parse(inputDate);
            assert date != null;
            return outputDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String convertUnixTimestampToDateString(long unixTimestamp) {
        Date date = new Date(unixTimestamp * 1000L); // Convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date);
    }

    public static void setLocale(Context context, String newLanguage) {
        Resources activityRes = context.getResources();
        Configuration activityConf = activityRes.getConfiguration();
        Locale newLocale = new Locale(newLanguage);
        activityConf.setLocale(newLocale);
        activityRes.updateConfiguration(activityConf, activityRes.getDisplayMetrics());

        Resources applicationRes = context.getResources();
        Configuration applicationConf = applicationRes.getConfiguration();
        applicationConf.setLocale(newLocale);
        applicationRes.updateConfiguration(applicationConf,
                applicationRes.getDisplayMetrics());
    }

    public static void loadNewTranslations(Context context) {
        String currentLanguage = SharedPreferencesManager.getSharedPref("language", context, "en");

        Util.setLocale(context, currentLanguage);
    }

    public static void redirectBasedRole(Activity activity, boolean splashStatus) {
        Intent intent = null;

        String userRole = UserDataService.getUserRole(activity.getApplicationContext());

        Log.d("ROLL", userRole);

        switch (userRole) {
            case "NORMAL":
                intent = new Intent(activity, me.t3sl4.ondergrup.Screens.Dashboard.User.class);
                intent.putExtra("user", getUserFromPreferences(activity.getApplicationContext()));
                break;
            case "TECHNICIAN":
                intent = new Intent(activity, Technician.class);
                intent.putExtra("user", getUserFromPreferences(activity.getApplicationContext()));
                break;
            case "ENGINEER":
                intent = new Intent(activity, Engineer
                        .class);
                intent.putExtra("user", getUserFromPreferences(activity.getApplicationContext()));
                break;
            case "SYSOP":
                intent = new Intent(activity, SysOp.class);
                intent.putExtra("user", getUserFromPreferences(activity.getApplicationContext()));
                break;
            default:
                if(splashStatus) {
                    intent = new Intent(activity, LoginScreen.class);
                    activity.startActivity(intent);
                    activity.finish();
                } else {
                    Util.showErrorPopup(new Dialog(activity.getApplicationContext()), "Desteklenmeyen bir kullanıcı rolüne sahipsin. Lütfen iletişime geç.");
                }
                break;
        }

        if (intent != null) {
            activity.startActivity(intent);
            activity.finish();
        }
    }
}
