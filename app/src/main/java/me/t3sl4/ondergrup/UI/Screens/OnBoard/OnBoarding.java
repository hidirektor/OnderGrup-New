package me.t3sl4.ondergrup.UI.Screens.OnBoard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.t3sl4.countrypicker.CountryPicker;
import me.t3sl4.ondergrup.Model.OnBoard.Adapter.OnBoardAdapter;
import me.t3sl4.ondergrup.Model.OnBoard.OnBoard;
import me.t3sl4.ondergrup.R;
import me.t3sl4.ondergrup.UI.BaseActivity;
import me.t3sl4.ondergrup.UI.Components.HorizontalStepper.HorizontalStepper;
import me.t3sl4.ondergrup.UI.Components.Sneaker.Sneaker;
import me.t3sl4.ondergrup.UI.Screens.Auth.LoginScreen;
import me.t3sl4.ondergrup.Util.Util;
import me.t3sl4.ondergrup.Util.Web.WebViewBottomSheetFragment;

public class OnBoarding extends BaseActivity {

    HorizontalStepper horizontalStepper;

    ConstraintLayout mainLayout;
    CountryPicker countryPicker;

    Button createAccount;
    Button login;

    TextView userTerms;
    TextView privacyPolicy;

    ViewPager2 viewPager;
    List<OnBoard> onBoardList;
    OnBoardAdapter adapter;

    private int previousPosition = 0;

    private List<String> countryBlacklist = Arrays.asList("IL");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        initializeComponents();

        updateSpinnerBasedOnLocation();

        componentListeners();
    }

    private void initializeComponents() {
        horizontalStepper = findViewById(R.id.horizontalStepper);

        mainLayout = findViewById(R.id.mainConstraint);
        countryPicker = findViewById(R.id.countryPicker);

        viewPager = findViewById(R.id.viewPager);

        createAccount = findViewById(R.id.registerButton);
        login = findViewById(R.id.loginButton);

        userTerms = findViewById(R.id.userTerms);
        privacyPolicy = findViewById(R.id.privacyPolicy);

        setupOnBoards();

        adapter = new OnBoardAdapter(onBoardList);
        viewPager.setAdapter(adapter);
    }

    private void componentListeners() {
        String errorTitleMsg = getString(R.string.errorTitle);
        String countryErrorMsg = getString(R.string.country_error);
        createAccount.setOnClickListener(v -> {
            if(countryBlacklist.contains(countryPicker.getSelectedCountry().getCode())) {
                Sneaker.with(this).setTitle(errorTitleMsg).setMessage(countryErrorMsg).sneakError();
            } else {
                Intent createAccountIntent = new Intent(OnBoarding.this, LoginScreen.class);
                finish();
                startActivity(createAccountIntent);
            }
        });

        login.setOnClickListener(v -> {
            if(countryBlacklist.contains(countryPicker.getSelectedCountry().getCode())) {
                Sneaker.with(this).setTitle(errorTitleMsg).setMessage(countryErrorMsg).sneakError();
            } else {
                Intent loginIntent = new Intent(OnBoarding.this, LoginScreen.class);
                finish();
                startActivity(loginIntent);
            }
        });

        userTerms.setOnClickListener(v -> {
            WebViewBottomSheetFragment webViewBottomSheet = new WebViewBottomSheetFragment("https://www.ondergrup.com/veri-saklama-ve-imha-politikasi/");
            webViewBottomSheet.show(getSupportFragmentManager(), "WebViewBottomSheet");
        });

        privacyPolicy.setOnClickListener(v -> {
            WebViewBottomSheetFragment webViewBottomSheet = new WebViewBottomSheetFragment("https://www.ondergrup.com/gizlilik-politikasi/");
            webViewBottomSheet.show(getSupportFragmentManager(), "WebViewBottomSheet");
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                mainLayout.setBackgroundColor(onBoardList.get(position).getBackgroundColor());

                if (position > previousPosition) {
                    horizontalStepper.setCurrentStep(horizontalStepper.getCurrentStep() + 1);
                } else if (position < previousPosition) {
                    horizontalStepper.setCurrentStep(horizontalStepper.getCurrentStep() - 1);
                }
                previousPosition = position;
            }
        });
    }

    private void setupOnBoards() {
        String onboardTitle1 = getString(R.string.onboard_1_title_1);
        String onboardTitle2 = getString(R.string.onboard_2_title_1);
        String onboardTitle3 = getString(R.string.onboard_3_title_1);
        onBoardList = new ArrayList<>();
        onBoardList.add(new OnBoard(ContextCompat.getColor(this, R.color.tanitimekrani1), onboardTitle1, R.drawable.onboard_1));
        onBoardList.add(new OnBoard(ContextCompat.getColor(this, R.color.tanitimekrani2), onboardTitle2, R.drawable.onboard_2));
        onBoardList.add(new OnBoard(ContextCompat.getColor(this, R.color.tanitimekrani3), onboardTitle3, R.drawable.onboard_3));
    }

    private void updateSpinnerBasedOnLocation() {
        String errorTitleMsg = getString(R.string.errorTitle);
        String countryErrorMsg = getString(R.string.country_error);

        Util.getUserCountryCode(this, this, userCountryCode -> {
            if (userCountryCode != null && !userCountryCode.isEmpty()) {
                countryPicker.setDefaultCountryByCode(userCountryCode);
                if(countryBlacklist.contains(userCountryCode)) {
                    Sneaker.with(this).setTitle(errorTitleMsg).setMessage(countryErrorMsg).sneakError();
                }
            } else {
                countryPicker.setDefaultCountryByCode("US");
            }
        });
    }
}