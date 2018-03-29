package com.example.studenthelper;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Created by DELL on 3/25/2018.
 */

public class Register extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText usernameField;
    private EditText passwordField;
    private Spinner accountTypeSpinner;
    private Button registerBtn;
    private String selectedType;
    private TextView errorField;

    // True if error exists; if true, can't register
    private boolean error = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        accountTypeSpinner = findViewById(R.id.accountTypeSpinner);
        registerBtn = findViewById(R.id.registerBtn);
        errorField = findViewById(R.id.errorField);

        // Populate spinner with values from strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.account_type, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountTypeSpinner.setAdapter(adapter);

        accountTypeSpinner.setOnItemSelectedListener(this);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateRegister()){

                    String username = usernameField.getText().toString().trim().toLowerCase();
                    String password = passwordField.getText().toString();

                    String hashedPassword = null;

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
                        hashedPassword = sb.toString();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                    final String hashedPasswordFinal = hashedPassword;
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");

                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String username = usernameField.getText().toString().trim().toLowerCase();

                            for(DataSnapshot user: dataSnapshot.getChildren()){
                                if(user.child("username").getValue(String.class).equals(username)){
                                    // Username found
                                    Toast.makeText(Register.this, "User already exists", Toast.LENGTH_LONG).show();
                                    error = true;
                                    break;
                                } else {
                                    error = false;
                                }
                            }

                            if(error == false){
                                HashMap<String, String> dataMap = new HashMap<>();
                                dataMap.put("username", username);
                                dataMap.put("password", hashedPasswordFinal);
                                dataMap.put("type", selectedType);

                                // Store user in database
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");

                                ref.push().setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(Register.this, "Registered!", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(Register.this, "Error registering", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });


    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selectedType = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    // Returns true if successful
    private boolean validateRegister(){
        boolean valid = true;

        if(selectedType.equals("Account Type...")){
            // No type selected
            errorField.setText("Select an account type");
            valid = false;
        }

        try {
            String password = passwordField.getText().toString();

            validatePassword(password);
        } catch (ExceptionHandler e){
            errorField.setText(e.getMessage());
            valid = false;
        }

        try {
            String username = usernameField.getText().toString().trim();

            validateUsername(username);
        } catch (ExceptionHandler e){
            errorField.setText(e.getMessage());
            valid = false;
        }

        return valid;
    }

    public void validateUsername(String username) throws ExceptionHandler {
        if(TextUtils.isEmpty(username)){
            throw new ExceptionHandler("Username cannot be empty");
        } else if(TextUtils.isDigitsOnly(username)){
            throw new ExceptionHandler("Username cannot contain digits only");
        } else if(!username.matches("[a-zA-Z0-9]*")){
            throw new ExceptionHandler("Username cannot contain special characters");
        } else if(username.length() < 3){
            throw new ExceptionHandler("Username cannot contain less than 3 characters");
        } else if(username.length() > 8){
            throw new ExceptionHandler("Username cannot contain more than 8 characters");
        }
    }

    public void validatePassword(String password) throws ExceptionHandler {
        if(TextUtils.isEmpty(password)){
            throw new ExceptionHandler("Password is empty");
        } else if(password.length() < 5){
            throw new ExceptionHandler("Password must contain 5 or more characters");
        }
    }
}
