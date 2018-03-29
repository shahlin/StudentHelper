package com.example.studenthelper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button loginBtn;
    private TextView registerLabel;
    private TextView login_error_field;
    private ProgressBar loadingSign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.usernameField);
        password = findViewById(R.id.passwordField);
        loginBtn = findViewById(R.id.loginBtn);
        registerLabel = findViewById(R.id.registerLabel);
        login_error_field = findViewById(R.id.login_error_field);
        loadingSign = findViewById(R.id.loading_sign);

        // Hide progress bar by default
        loadingSign.setVisibility(View.GONE);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show loading sign
                loadingSign.setVisibility(View.VISIBLE);

                handleLogin();
            }
        });
    }

    public void openRegister(View v){
        Intent intent = new Intent(MainActivity.this, Register.class);
        MainActivity.this.startActivity(intent);
    }

    private void handleLogin(){

        if(TextUtils.isEmpty(username.getText().toString()) || TextUtils.isEmpty(password.getText().toString())){
            // Username or password is empty
            loadingSign.setVisibility(View.GONE);
            login_error_field.setText("Both fields required");
        } else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");

            String hashedPassword = null;

            try {
                // Create MessageDigest instance for MD5
                MessageDigest md = MessageDigest.getInstance("MD5");

                // Add password bytes to digest
                md.update(password.getText().toString().getBytes());

                // Get the hash's bytes
                byte[] bytes = md.digest();

                // This bytes[] has bytes in decimal format;

                // Convert it to hexadecimal format
                StringBuilder sb = new StringBuilder();

                for(int i=0; i< bytes.length ;i++) {
                    sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                }
                // Get complete hashed password in hex format
                hashedPassword = sb.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            final String hashedPasswordFinal = hashedPassword;

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot user: dataSnapshot.getChildren()){
                        if(user.child("username").getValue(String.class).toLowerCase().equals(username.getText().toString().trim().toLowerCase()) && user.child("password").getValue(String.class).equals(hashedPasswordFinal)){
                            // User found

                            // Disable login button
                            loginBtn.setEnabled(false);

                            String user_id = user.getKey();
                            SharedPreferences prefs = getSharedPreferences("PREFERENCES", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();

                            editor.putString("user_id", user_id);
                            editor.apply();

                            Intent folderPage = new Intent(MainActivity.this, Folders.class);
                            folderPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(folderPage);

                            return;
                        }
                    }

                    loadingSign.setVisibility(View.GONE);
                    login_error_field.setText("Invalid username/password");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
