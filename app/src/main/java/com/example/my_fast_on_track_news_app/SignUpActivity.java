package com.example.my_fast_on_track_news_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword, etEmail;
    private Button btnSignUp;
    private CheckBox chkRememberUsername;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etEmail = findViewById(R.id.etEmail);
        btnSignUp = findViewById(R.id.btnSignUp);
        chkRememberUsername = findViewById(R.id.chkRememberUsername);

        mAuth = FirebaseAuth.getInstance();
        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Restore username if remembered
        String savedUsername = sharedPref.getString("username", "");
        if (!TextUtils.isEmpty(savedUsername)) {
            etUsername.setText(savedUsername);
            chkRememberUsername.setChecked(true);
        }

        btnSignUp.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String usernameLower = username.toLowerCase(); // Force lowercase for db mapping!
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            // Validation
            if (TextUtils.isEmpty(username)) {
                etUsername.setError("Enter username");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Enter password");
                return;
            }
            if (TextUtils.isEmpty(confirmPassword)) {
                etConfirmPassword.setError("Confirm your password");
                return;
            }
            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                return;
            }
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Enter email");
                return;
            }

            // Remember Username
            if (chkRememberUsername.isChecked()) {
                sharedPref.edit().putString("username", username).apply();
            } else {
                sharedPref.edit().remove("username").apply();
            }

            // Firebase Auth: create user with email and password
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Store username-to-email mapping in lowercase!
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference usernamesRef = database.getReference("usernames");
                                usernamesRef.child(usernameLower).setValue(email);

                                Toast.makeText(SignUpActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                // Go back to login
                                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                Toast.makeText(SignUpActivity.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });
    }
}
