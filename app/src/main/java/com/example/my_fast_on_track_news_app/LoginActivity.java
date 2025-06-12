package com.example.my_fast_on_track_news_app;

import com.example.my_fast_on_track_news_app.NewsActivity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etEmail); // Actually used for username now
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        TextView tvSignUp = findViewById(R.id.tvSignUp);
        tvSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        btnLogin.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim().toLowerCase(); // ensure lowercase
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                etUsername.setError("Enter username");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Enter password");
                return;
            }

            progressDialog.setMessage("Logging in...");
            progressDialog.show();

            // Lookup email by username in Realtime Database
            DatabaseReference usernamesRef = FirebaseDatabase.getInstance().getReference("usernames");
            usernamesRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String email = snapshot.getValue(String.class);
                        // TEMP: Debug toast to see what email was found
                        Toast.makeText(LoginActivity.this, "Debug: Email = " + email, Toast.LENGTH_LONG).show();

                        if (email == null || email.isEmpty()) {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "No email found for username.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        // Now login with found email and password
                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        progressDialog.dismiss();
                                        if (task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(LoginActivity.this, NewsActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Username not found.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
