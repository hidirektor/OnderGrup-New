package me.t3sl4.ondergrup.UI.Screens.SubUser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import me.t3sl4.ondergrup.Model.User.User;
import me.t3sl4.ondergrup.R;

public class SubUserScreen extends AppCompatActivity {
    public User receivedUser;

    private Button subUserListeleButton;
    private Button subUsterEkleButton;
    private ImageView backButton;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_subuser);

        Intent intent = getIntent();
        receivedUser = intent.getParcelableExtra("user");

        initializeComponents();

        buttonClickListeners();
    }

    private void initializeComponents() {
        subUserListeleButton = findViewById(R.id.showButton);
        subUsterEkleButton = findViewById(R.id.addSubUserButton);
        backButton = findViewById(R.id.backButton);
    }

    private void buttonClickListeners() {
        subUserListeleButton.setOnClickListener(v -> {
            Intent addIntent = new Intent(SubUserScreen.this, SubUserListScreen.class);
            addIntent.putExtra("user", receivedUser);
            startActivity(addIntent);
        });

        subUsterEkleButton.setOnClickListener(v -> {
            Intent addIntent = new Intent(SubUserScreen.this, SubUserAddScreen.class);
            addIntent.putExtra("user", receivedUser);
            startActivity(addIntent);
        });

        backButton.setOnClickListener(v -> {
            finish();
        });
    }
}
