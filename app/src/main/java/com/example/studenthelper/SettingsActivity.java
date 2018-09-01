package com.example.studenthelper;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SettingsActivity extends AppCompatActivity {

    private EditText text_username;
    private EditText old_password_field;
    private EditText new_password_field;
    private Button btn_change_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        text_username = findViewById(R.id.text_username);
        old_password_field = findViewById(R.id.old_password_field);
        new_password_field = findViewById(R.id.new_password_field);
        btn_change_password = findViewById(R.id.btn_change_password);

        setUsernameToField();

        btn_change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });

    }

    private void setUsernameToField(){
        SharedPreferences prefs = getSharedPreferences("PREFERENCES", MODE_PRIVATE);
        final String user_id = prefs.getString("user_id", "null");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                text_username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void changePassword(){
        SharedPreferences prefs = getSharedPreferences("PREFERENCES", MODE_PRIVATE);
        final String user_id = prefs.getString("user_id", "null");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                String old_password = old_password_field.getText().toString().trim();
                String new_password = new_password_field.getText().toString().trim();

                if(old_password.isEmpty() || new_password.isEmpty()){
                    Toast.makeText(SettingsActivity.this, "All fields required", Toast.LENGTH_LONG).show();
                    return;
                }

                if(hashPassword(old_password).equals(user.getPassword())){
                    // Correct old password
                    try {
                        validatePassword(new_password);
                    } catch (ExceptionHandler e) {
                        Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Change password
                    FirebaseDatabase.getInstance().getReference().child("users").child(user_id).child("password").setValue(hashPassword(new_password));

                    Toast.makeText(SettingsActivity.this, "Password changed!", Toast.LENGTH_LONG).show();
                } else {
                     Toast.makeText(SettingsActivity.this, "Incorrect old password", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void validatePassword(String password) throws ExceptionHandler {
        if(TextUtils.isEmpty(password)){
            throw new ExceptionHandler("Password is empty");
        } else if(password.length() < 5){
            throw new ExceptionHandler("Password must contain 5 or more characters");
        }
    }

    public String hashPassword(String password){
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Add password bytes to digest
            md.update(password.getBytes());

            // Get the hash's bytes
            byte[] bytes = md.digest();

            // This bytes[] has bytes in decimal format;

            // Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();

            for(int i=0; i< bytes.length ;i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            // Get complete hashed password in hex format
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }
}
