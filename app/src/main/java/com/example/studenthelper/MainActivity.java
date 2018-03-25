package com.example.studenthelper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.usernameField);
        password = findViewById(R.id.passwordField);
        loginBtn = findViewById(R.id.loginBtn);
        registerLabel = findViewById(R.id.registerLabel);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            Toast.makeText(MainActivity.this, "Both fields required", Toast.LENGTH_LONG).show();
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
                        if(user.child("username").getValue(String.class).equals(username.getText().toString().trim().toLowerCase()) && user.child("password").getValue(String.class).equals(hashedPasswordFinal)){
                            // User found
                            Toast.makeText(MainActivity.this, "Logged in!", Toast.LENGTH_LONG).show();
                            break;
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid username/password", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
