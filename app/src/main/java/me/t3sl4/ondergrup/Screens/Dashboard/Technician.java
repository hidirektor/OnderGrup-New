package me.t3sl4.ondergrup.Screens.Dashboard;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;

import me.t3sl4.ondergrup.Model.Machine.Adapter.MachineAdapter;
import me.t3sl4.ondergrup.Model.Machine.Machine;
import me.t3sl4.ondergrup.R;
import me.t3sl4.ondergrup.Screens.Log.Error.ErrorLogAll;
import me.t3sl4.ondergrup.Screens.Log.Maintenance.MaintenanceLogAll;
import me.t3sl4.ondergrup.Screens.Machine.MachineListScreen;
import me.t3sl4.ondergrup.Screens.Machine.RestrictedMachineScreen;
import me.t3sl4.ondergrup.Screens.Profile.EditProfileScreen;
import me.t3sl4.ondergrup.Screens.Profile.ProfileScreen;
import me.t3sl4.ondergrup.Screens.QR.QRScanner;
import me.t3sl4.ondergrup.Service.UserDataService;
import me.t3sl4.ondergrup.Util.Component.SharedPreferencesManager;
import me.t3sl4.ondergrup.Util.HTTP.Requests.Authorized.OPMachineService;
import me.t3sl4.ondergrup.Util.Util;

public class Technician extends AppCompatActivity {
    private static final String TARGET_WIFI_SSID = "OnderGrup_IoT";

    private TextView isimSoyisim;

    private ConstraintLayout subLanguage;
    private ConstraintLayout profileButton;
    private ConstraintLayout settingsButton;
    private ConstraintLayout myMachineButton;
    private FloatingActionButton qrButton;

    //Top Buttons:
    private ImageView logoutButton;
    private ConstraintLayout allMaintenancesButton;
    private ConstraintLayout allErrorsButton;


    //Machine List View Section:
    private ListView machineListView;
    private MachineAdapter machineListAdapter;
    private ArrayList<Machine> machineList;

    public me.t3sl4.ondergrup.Model.User.User receivedUser;

    private Dialog uyariDiyalog;
    private Dialog qrDiyalog;

    public static String scannedQRCode;
    public static EditText scannedQRCodeEditText;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Intent originalIntent = result.getOriginalIntent();
                    if (originalIntent == null) {
                        Log.d("MainActivity", "Tarama iptal edildi");
                        Toast.makeText(Technician.this, "Tarama kapatıldı", Toast.LENGTH_LONG).show();
                    } else if(originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                        Log.d("MainActivity", "Kamera yetkisi eksik");
                        String cameraPerm = getResources().getString(R.string.camera_permission_error);
                        Util.showErrorPopup(uyariDiyalog, cameraPerm);
                    }
                } else {
                    Log.d("MainActivity", "Taramam tamamlandı");
                    scannedQRCode = result.getContents();
                    scannedQRCodeEditText.setText(scannedQRCode);
                    Toast.makeText(Technician.this, "ID: " + result.getContents(), Toast.LENGTH_LONG).show();
                }
            });

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_technician);

        uyariDiyalog = new Dialog(this);
        qrDiyalog = new Dialog(this);

        Intent intent = getIntent();
        receivedUser = intent.getParcelableExtra("user");

        isimSoyisim = findViewById(R.id.loggedUserName);

        subLanguage = findViewById(R.id.languageConstraint);
        profileButton = findViewById(R.id.profileConstraint);
        settingsButton = findViewById(R.id.settingsConstraint);
        myMachineButton = findViewById(R.id.myMachine);
        qrButton = findViewById(R.id.qrConstraint);

        //Top buttons:
        logoutButton = findViewById(R.id.logoutButton);
        allErrorsButton = findViewById(R.id.allErrorsConstraint);
        allMaintenancesButton = findViewById(R.id.allMaintenancesConstraint);

        //ListView Definitians:
        machineListView = findViewById(R.id.machineListView);
        machineList = getMachineList();
        machineListView.setOnItemClickListener((parent, view, position, id) -> {
            Machine selectedMachine = machineList.get(position);

            Intent machineIntent = new Intent(Technician.this, RestrictedMachineScreen.class);
            machineIntent.putExtra("machine", selectedMachine);
            machineIntent.putExtra("user", receivedUser);
            startActivity(machineIntent);
        });

        //Top Buttons:
        logoutButton.setOnClickListener(v -> UserDataService.logout(this));

        allMaintenancesButton.setOnClickListener(v -> {
            Intent profileIntent = new Intent(Technician.this, MaintenanceLogAll.class);
            profileIntent.putExtra("user", receivedUser);
            startActivity(profileIntent);
        });

        allErrorsButton.setOnClickListener(v -> {
            Intent profileIntent = new Intent(Technician.this, ErrorLogAll.class);
            profileIntent.putExtra("user", receivedUser);
            startActivity(profileIntent);
        });

        //Bottom buttons:
        myMachineButton.setOnClickListener(v -> {
            Intent manageMachineIntent = new Intent(Technician.this, MachineListScreen.class);
            manageMachineIntent.putExtra("user", receivedUser);
            startActivity(manageMachineIntent);
        });

        profileButton.setOnClickListener(v -> {
            Intent profileIntent = new Intent(Technician.this, ProfileScreen.class);
            profileIntent.putExtra("user", receivedUser);
            startActivity(profileIntent);
        });

        qrButton.setOnClickListener(v -> addMachine());

        subLanguage.setOnClickListener(v -> {
            switchLanguage();
        });

        settingsButton.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(Technician.this, EditProfileScreen.class);
            settingsIntent.putExtra("user", receivedUser);
            startActivity(settingsIntent);
        });

        setUserInfo();
    }

    public void setUserInfo() {
        isimSoyisim.setText(this.getResources().getString(R.string.hello_prefix) + receivedUser.getNameSurname());
    }

    public void makineEkle(String machineType, String machineID) {
        /*String reqURL = util.BASE_URL + util.addMachineURL;

        String userName = receivedUser.getUserName();
        String companyName = receivedUser.getCompanyName();
        String jsonAddMachineBody = "{\"Username\": \"" + userName + "\", \"CompanyName\": \"" + companyName + "\", \"MachineID\": \"" + machineID + "\"}";

        HTTP.sendRequest(reqURL, jsonAddMachineBody, new HTTP.HttpRequestCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                qrDiyalog.dismiss();
                util.showSuccessPopup(uyariDiyalog, "Makine başarılı bir şekilde eklendi.");
            }

            @Override
            public void onFailure(String errorMessage) {
                util.showErrorPopup(uyariDiyalog, "Kullanıcı adı veya şifreniz hatalı. \nLütfen bilgilerinizi kontrol edip tekrar deneyin.");
            }
        }, Volley.newRequestQueue(this));*/
    }

    private boolean isConnectedToTargetWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    String ssid = wifiManager.getConnectionInfo().getSSID().replace("\"", ""); // Remove quotes from SSID
                    return ssid.equals(TARGET_WIFI_SSID);
                }
            }
        }
        return false;
    }

    private void openWifiSettings() {
        Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
        startActivity(intent);
    }

    private ArrayList<Machine> getMachineList() {
        ArrayList<Machine> machines = new ArrayList<>();

        OPMachineService.getAllMachines(this, machines, () -> {
            // onSuccess callback
            updateListView(machines);
        }, () -> {
            //onFailure callback
        });

        return null;
    }

    private void updateListView(ArrayList<Machine> machines) {
        machineList = machines;
        machineListAdapter = new MachineAdapter(this, machineList);
        machineListView.setAdapter(machineListAdapter);
    }

    public void scanBarcodeCustomLayout() {
        ScanOptions options = new ScanOptions().setOrientationLocked(false).setCaptureActivity(QRScanner.class);
        barcodeLauncher.launch(options);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addMachine() {
        if(receivedUser.getRole().equals("NORMAL")) {
            qrDiyalog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            qrDiyalog.setContentView(R.layout.activity_machine_add);

            ImageView cancelButton = qrDiyalog.findViewById(R.id.cancelButton);
            ImageView wifiButton = qrDiyalog.findViewById(R.id.wifiButton);
            Button addButton = qrDiyalog.findViewById(R.id.makineEkleButton);
            Spinner machineTypeSpinner = qrDiyalog.findViewById(R.id.machineTypeSpinner);

            scannedQRCodeEditText = qrDiyalog.findViewById(R.id.editTextID);
            if (scannedQRCode != null) {
                scannedQRCodeEditText.setText(scannedQRCode);
            }

            scannedQRCodeEditText.setOnTouchListener((vi, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        if (event.getRawX() >= (scannedQRCodeEditText.getRight() - scannedQRCodeEditText.getCompoundDrawables()[2].getBounds().width())) {
                            scanBarcodeCustomLayout();
                            return true;
                        }
                }
                return false;
            });

            cancelButton.setOnClickListener(view -> qrDiyalog.dismiss());

            wifiButton.setOnClickListener(view -> {
                if (!isConnectedToTargetWifi()) {
                    openWifiSettings();

                    new Thread(() -> {
                        while (!isConnectedToTargetWifi()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        runOnUiThread(() -> {
                            wifiButton.setImageDrawable(getResources().getDrawable(R.drawable.ikon_wifi_green));
                        });
                    }).start();
                } else {
                    wifiButton.setImageDrawable(getResources().getDrawable(R.drawable.ikon_wifi_green));
                }
            });


            addButton.setOnClickListener(view -> makineEkle(machineTypeSpinner.getSelectedItem().toString(), scannedQRCode));

            String[] machineTypes = getResources().getStringArray(R.array.machineType);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, machineTypes);
            machineTypeSpinner.setAdapter(adapter);

            qrDiyalog.show();
            qrDiyalog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            qrDiyalog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            qrDiyalog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            qrDiyalog.getWindow().setGravity(Gravity.BOTTOM);
        } else {
            Util.showErrorPopup(uyariDiyalog, "Sadece NORMAL kullanıcılar doğrudan makine ekleyebilir.");
        }
    }

    private void switchLanguage() {
        String currentLanguage = SharedPreferencesManager.getSharedPref("language", Technician.this, "en");
        String nextLang = "";

        if (currentLanguage.equals("tr")) {
            SharedPreferencesManager.writeSharedPref("language", "en", Technician.this);
            nextLang = "en";
        } else {
            SharedPreferencesManager.writeSharedPref("language", "tr", Technician.this);
            nextLang = "tr";
        }

        Util.setLocale(Technician.this, nextLang);
        recreate();
    }
}