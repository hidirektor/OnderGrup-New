package me.t3sl4.ondergrup.Screens.Dashboard;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;

import me.t3sl4.ondergrup.Model.Machine.Adapter.MachineAdapter;
import me.t3sl4.ondergrup.Model.Machine.Machine;
import me.t3sl4.ondergrup.Model.User.Adapter.UserAdapter;
import me.t3sl4.ondergrup.Model.User.User;
import me.t3sl4.ondergrup.R;
import me.t3sl4.ondergrup.Screens.Machine.RestrictedMachineScreen;
import me.t3sl4.ondergrup.Screens.Profile.EditProfileScreen;
import me.t3sl4.ondergrup.Screens.Profile.ProfileScreen;
import me.t3sl4.ondergrup.Service.UserDataService;
import me.t3sl4.ondergrup.Util.Component.Button.ButtonManager;
import me.t3sl4.ondergrup.Util.Component.SharedPreferencesManager;
import me.t3sl4.ondergrup.Util.Util;

public class SysOp extends AppCompatActivity {

    public me.t3sl4.ondergrup.Model.User.User receivedUser;
    private Dialog uyariDiyalog;

    private TextView isimSoyisim;
    private TextView allText;

    private ImageView subLanguage;
    private ConstraintLayout profileButton;
    private ConstraintLayout settingsButton;

    private ImageView logoutButton;

    private ListView machineListView;
    private MachineAdapter machineListAdapter;
    private ArrayList<Machine> machineList;

    private ArrayList<me.t3sl4.ondergrup.Model.User.User> userList;
    private ListView userListView;
    private UserAdapter userListAdapter;

    private Button allMachinesButton;
    private Button allUsersButton;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sysop);

        uyariDiyalog = new Dialog(this);

        Intent intent = getIntent();
        receivedUser = intent.getParcelableExtra("user");

        isimSoyisim = findViewById(R.id.loggedUserName);
        allText = findViewById(R.id.allText);

        subLanguage = findViewById(R.id.languageImageView);
        profileButton = findViewById(R.id.profileConstraint);
        settingsButton = findViewById(R.id.profileUpdateConstraint);

        //Top buttons:
        logoutButton = findViewById(R.id.logoutButton);

        allMachinesButton = findViewById(R.id.allMachinesButton);
        allUsersButton = findViewById(R.id.allUsersButton);

        //ListView Definitians:
        machineListView = findViewById(R.id.machineListView);
        userListView = findViewById(R.id.userListView);

        machineList = getMachineList();
        userList = getUserList();

        userListView.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = (User) parent.getItemAtPosition(position);

            PopupMenu popup = new PopupMenu(SysOp.this, view);
            popup.getMenuInflater().inflate(R.menu.user_operations_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                if(item.getItemId() == R.id.deleteUser) {
                    //Kullanıcı silme işlemi
                    deleteUser(selectedUser.getUserName());

                } else if(item.getItemId() == R.id.editUser) {
                    //Kullanıcı düzenleme işlemi
                    Intent userEditIntent = new Intent(SysOp.this, EditProfileScreen.class);
                    userEditIntent.putExtra("user", selectedUser);
                    userEditIntent.putExtra("mainUser", receivedUser);
                    userEditIntent.putExtra("incomeScreen", "sysop");

                    startActivity(userEditIntent);

                } else if(item.getItemId() == R.id.editRole) {
                    //Kullanıcı rol yükseltme işlemi
                    roleUpdate(selectedUser);

                } else {
                    return false;
                }

                return true;
            });

            popup.show();
        });

        machineListView.setOnItemClickListener((parent, view, position, id) -> {
            Machine selectedMachine = machineList.get(position);

            Intent machineIntent = new Intent(SysOp.this, RestrictedMachineScreen.class);
            machineIntent.putExtra("machine", selectedMachine);
            machineIntent.putExtra("user", receivedUser);
            startActivity(machineIntent);
        });

        profileButton.setOnClickListener(v -> {
            Intent profileIntent = new Intent(SysOp.this, ProfileScreen.class);
            profileIntent.putExtra("user", receivedUser);
            startActivity(profileIntent);
        });

        settingsButton.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(SysOp.this, EditProfileScreen.class);
            settingsIntent.putExtra("user", receivedUser);
            startActivity(settingsIntent);
        });

        logoutButton.setOnClickListener(v -> UserDataService.logout(this));

        subLanguage.setOnClickListener(v -> {
            switchLanguage();
        });

        sectionPager();
        setUserInfo();
    }

    @Override
    public void onResume() {
        super.onResume();

        machineList = getMachineList();
        updateListView(machineList);
    }

    public void setUserInfo() {
        isimSoyisim.setText(this.getResources().getString(R.string.hello_prefix) + receivedUser.getNameSurname());
    }

    private void sectionPager() {
        allMachinesButton.setOnClickListener(v -> {
            ButtonManager.orderButtonColorEffect(1, allMachinesButton, allUsersButton, this);
            machineListView.setVisibility(View.VISIBLE);
            userListView.setVisibility(View.GONE);
            allText.setText(this.getResources().getString(R.string.allmachines));
            machineList = getMachineList();
            updateListView(machineList);
        });

        allUsersButton.setOnClickListener(v -> {
            ButtonManager.orderButtonColorEffect(2, allMachinesButton, allUsersButton, this);
            userListView.setVisibility(View.VISIBLE);
            machineListView.setVisibility(View.GONE);
            allText.setText(this.getResources().getString(R.string.allusers));
            userList = getUserList();
            updateUserListView(userList);
        });
    }

    private void deleteUser(String userName) {
        /*String reqURL = util.BASE_URL + util.deleteUser;
        String jsonDeleteBody = "{\"Username\": \"" + userName + "\"}";

        HTTP.sendRequest(reqURL, jsonDeleteBody, new HTTP.HttpRequestCallback() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                if(response.getString("error").contains("silinemez")) {
                    util.showErrorPopup(uyariDiyalog, "Bu kullanıcının makinesi olduğu için kullanıcı silinemez.");
                } else {
                    util.showSuccessPopup(uyariDiyalog, "Kullanıcı silindi !");
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                util.showErrorPopup(uyariDiyalog, "Kullanıcı silinemedi.");
            }
        }, Volley.newRequestQueue(this));*/
    }

    private void roleUpdate(User selectedUser) {
        Dialog roleDialog = new Dialog(this);
        roleDialog.setContentView(R.layout.activity_popup_role);
        roleDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button normalRoleButton = roleDialog.findViewById(R.id.normalRole);
        Button technicianRoleButton = roleDialog.findViewById(R.id.technicianRole);
        Button engineerRoleButton = roleDialog.findViewById(R.id.engineerRole);
        Button sysopRoleButton = roleDialog.findViewById(R.id.sysopRole);

        normalRoleButton.setOnClickListener(v -> {
            String updateRoleBody = "{\"Username\": \"" + selectedUser.getUserName() + "\", \"Role\": \"" + "NORMAL" + "\"}";
            sendRoleRequest(updateRoleBody);
            userList = getUserList();
            updateUserListView(userList);
            roleDialog.dismiss();
        });

        technicianRoleButton.setOnClickListener(v -> {
            String updateRoleBody = "{\"Username\": \"" + selectedUser.getUserName() + "\", \"Role\": \"" + "TECHNICIAN" + "\"}";
            sendRoleRequest(updateRoleBody);
            userList = getUserList();
            updateUserListView(userList);
            roleDialog.dismiss();
        });

        engineerRoleButton.setOnClickListener(v -> {
            String updateRoleBody = "{\"Username\": \"" + selectedUser.getUserName() + "\", \"Role\": \"" + "ENGINEER" + "\"}";
            sendRoleRequest(updateRoleBody);
            userList = getUserList();
            updateUserListView(userList);
            roleDialog.dismiss();
        });

        sysopRoleButton.setOnClickListener(v -> {
            String updateRoleBody = "{\"Username\": \"" + selectedUser.getUserName() + "\", \"Role\": \"" + "SYSOP" + "\"}";
            sendRoleRequest(updateRoleBody);
            userList = getUserList();
            updateUserListView(userList);
            roleDialog.dismiss();
        });

        roleDialog.show();
    }

    private void sendRoleRequest(String updateBody) {
        /*String updateRoleURL = util.BASE_URL + util.updateRolePrefix;
        HTTP.sendRequest(updateRoleURL, updateBody, new HTTP.HttpRequestCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                util.showSuccessPopup(uyariDiyalog, "Rol başarılı bir şekilde güncellendi.");
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("updateRole", updateRoleURL + " " + updateBody);
                util.showErrorPopup(uyariDiyalog, "Rol güncellenemedi. \nLütfen bilgilerinizi kontrol edip tekrar deneyin.");
            }
        }, Volley.newRequestQueue(this));*/
    }

    private ArrayList<Machine> getMachineList() {
        /*ArrayList<Machine> machines = new ArrayList<>();
        String reqURL = util.BASE_URL + util.getAllMachinesURL;

        HTTP.sendRequestNormal(reqURL, new HTTP.HttpRequestCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONArray machineArray = response.getJSONArray("machines");
                    for (int i = 0; i < machineArray.length(); i++) {
                        JSONObject machineInfoObj = machineArray.getJSONObject(i).getJSONObject("MachineInfo");
                        JSONObject machineDataObj = machineArray.getJSONObject(i).getJSONArray("MachineData").getJSONObject(0);

                        String ownerUser = machineInfoObj.getString("Owner_UserName");
                        String lastUpdate = machineInfoObj.getString("LastUpdate");
                        String machineType = machineInfoObj.getString("MachineType");
                        String machineID = machineInfoObj.getString("MachineID");
                        String devirmeYuruyusSecim = machineDataObj.getString("devirmeYuruyusSecim");
                        String calismaSekli = machineDataObj.getString("calismaSekli");
                        String emniyetCercevesi = machineDataObj.getString("emniyetCercevesi");
                        String yavaslamaLimit = machineDataObj.getString("yavaslamaLimit");
                        String altLimit = machineDataObj.getString("altLimit");
                        String kapiTablaAcKonum = machineDataObj.getString("kapiTablaAcKonum");
                        String basincSalteri = machineDataObj.getString("basincSalteri");
                        String kapiSecimleri = machineDataObj.getString("kapiSecimleri");
                        String kapiAcTipi = machineDataObj.getString("kapiAcTipi");
                        String kapi1Tip = machineDataObj.getString("kapi1Tip");
                        String kapi1AcSure = machineDataObj.getString("kapi1AcSure");
                        String kapi2Tip = machineDataObj.getString("kapi2Tip");
                        String kapi2AcSure = machineDataObj.getString("kapi2AcSure");
                        String kapitablaTip = machineDataObj.getString("kapitablaTip");
                        String kapiTablaAcSure = machineDataObj.getString("kapiTablaAcSure");
                        String yukariYavasLimit = machineDataObj.getString("yukariYavasLimit");
                        String devirmeYukariIleriLimit = machineDataObj.getString("devirmeYukariIleriLimit");
                        String devirmeAsagiGeriLimit = machineDataObj.getString("devirmeAsagiGeriLimit");
                        String devirmeSilindirTipi = machineDataObj.getString("devirmeSilindirTipi");
                        String platformSilindirTipi = machineDataObj.getString("platformSilindirTipi");
                        String yukariValfTmr = machineDataObj.getString("yukariValfTmr");
                        String asagiValfTmr = machineDataObj.getString("asagiValfTmr");
                        String devirmeYukariIleriTmr = machineDataObj.getString("devirmeYukariIleriTmr");
                        String devirmeAsagiGeriTmr = machineDataObj.getString("devirmeAsagiGeriTmr");
                        String makineCalismaTmr = machineDataObj.getString("makineCalismaTmr");
                        String buzzer = machineDataObj.getString("buzzer");
                        String demoMode = machineDataObj.getString("demoMode");
                        String calismaSayisi1 = machineDataObj.getString("calismaSayisi1");
                        String calismaSayisi10 = machineDataObj.getString("calismaSayisi10");
                        String calismaSayisi100 = machineDataObj.getString("calismaSayisi100");
                        String calismaSayisi1000 = machineDataObj.getString("calismaSayisi1000");
                        String calismaSayisi10000 = machineDataObj.getString("calismaSayisi10000");
                        String dilSecim = machineDataObj.getString("dilSecim");
                        String eepromData38 = machineDataObj.getString("eepromData38");
                        String eepromData39 = machineDataObj.getString("eepromData39");
                        String eepromData40 = machineDataObj.getString("eepromData40");
                        String eepromData41 = machineDataObj.getString("eepromData41");
                        String eepromData42 = machineDataObj.getString("eepromData42");
                        String eepromData43 = machineDataObj.getString("eepromData43");
                        String eepromData44 = machineDataObj.getString("eepromData44");
                        String eepromData45 = machineDataObj.getString("eepromData45");
                        String eepromData46 = machineDataObj.getString("eepromData46");
                        String eepromData47 = machineDataObj.getString("eepromData47");
                        String lcdBacklightSure = machineDataObj.getString("lcdBacklightSure");

                        Machine selectedMachine = new Machine(ownerUser, lastUpdate, machineType, machineID, devirmeYuruyusSecim, calismaSekli, emniyetCercevesi,
                                yavaslamaLimit, altLimit, kapiTablaAcKonum, basincSalteri, kapiSecimleri,
                                kapiAcTipi, kapi1Tip, kapi1AcSure, kapi2Tip, kapi2AcSure, kapitablaTip,
                                kapiTablaAcSure, yukariYavasLimit, devirmeYukariIleriLimit, devirmeAsagiGeriLimit,
                                devirmeSilindirTipi, platformSilindirTipi, yukariValfTmr, asagiValfTmr,
                                devirmeYukariIleriTmr, devirmeAsagiGeriTmr, makineCalismaTmr, buzzer, demoMode,
                                calismaSayisi1, calismaSayisi10, calismaSayisi100, calismaSayisi1000, calismaSayisi10000,
                                dilSecim, eepromData38, eepromData39, eepromData40, eepromData41,
                                eepromData42, eepromData43, eepromData44, eepromData45, eepromData46, eepromData47,
                                lcdBacklightSure);

                        machines.add(selectedMachine);
                    }

                    updateListView(machines);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                util.showErrorPopup(uyariDiyalog, "Herhangi bir alt kullanıcı bulunamadı.");
            }
        }, Volley.newRequestQueue(this));
        return machines;*/
        return null;
    }

    private ArrayList<me.t3sl4.ondergrup.Model.User.User> getUserList() {
        /*ArrayList<me.t3sl4.ondergrup.Model.User.User> users = new ArrayList<>();
        String reqURL = util.BASE_URL + util.getAllUsersURL;

        HTTP.sendRequestNormal(reqURL, new HTTP.HttpRequestCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONArray userArray = response.getJSONArray("users");
                    for (int i = 0; i < userArray.length(); i++) {
                        JSONObject userInfoObj = userArray.getJSONObject(i).getJSONObject("UserInfo");

                        String userRole = userInfoObj.getString("Role");
                        String userName = userInfoObj.getString("UserName");
                        String userEmail = userInfoObj.getString("Email");
                        String userNameSurname = userInfoObj.getString("NameSurname");
                        String userPhone = userInfoObj.getString("Phone");
                        String userCompany = userInfoObj.getString("CompanyName");
                        String userOwner = userInfoObj.getString("Owner");
                        String userCreatedAt = userInfoObj.getString("Created_At");

                        User selectedUser = new User(userRole, userName, userEmail, userNameSurname, userPhone, userCompany, userCreatedAt);
                        selectedUser.setOwnerName(userOwner);
                        users.add(selectedUser);
                    }

                    updateUserListView(users);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                util.showErrorPopup(uyariDiyalog, "Herhangi bir alt kullanıcı bulunamadı.");
            }
        }, Volley.newRequestQueue(this));
        return users;*/
        return null;
    }

    private void updateListView(ArrayList<Machine> machines) {
        machineList = machines;
        machineListAdapter = new MachineAdapter(this, machineList);
        machineListView.setAdapter(machineListAdapter);
    }

    private void updateUserListView(ArrayList<User> users) {
        userList = users;
        userListAdapter = new UserAdapter(this, userList);
        userListView.setAdapter(userListAdapter);
    }

    private void switchLanguage() {
        String currentLanguage = SharedPreferencesManager.getSharedPref("language", SysOp.this, "en");
        String nextLang = "";

        if (currentLanguage.equals("tr")) {
            SharedPreferencesManager.writeSharedPref("language", "en", SysOp.this);
            nextLang = "en";
        } else {
            SharedPreferencesManager.writeSharedPref("language", "tr", SysOp.this);
            nextLang = "tr";
        }

        Util.setLocale(SysOp.this, nextLang);
        recreate();
    }
}
