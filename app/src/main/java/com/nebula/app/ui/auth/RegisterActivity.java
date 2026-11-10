package com.nebula.app.ui.auth;

import com.nebula.app.ui.BaseActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;


import com.nebula.app.databinding.ActivityRegisterBinding;
import com.nebula.app.ui.MainActivity;
import com.nebula.app.util.AuthErrors;
import com.nebula.app.util.Cloud;
import com.nebula.app.util.LocalStore;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends BaseActivity {
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.progressBar.setVisibility(View.GONE);
        binding.btnRegister.setOnClickListener(v -> registerUser());
        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String username = binding.etUsername.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password needs at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Cloud.isConfigured(this)) {
            if (LocalStore.accountExists(this, email)) {
                Toast.makeText(this, "An account with this email already exists on this device.", Toast.LENGTH_LONG).show();
                return;
            }
            LocalStore.register(this, username, email, password);
            goToMain();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRegister.setEnabled(false);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnRegister.setEnabled(true);
                Toast.makeText(this, AuthErrors.forRegister(task.getException()), Toast.LENGTH_LONG).show();
                return;
            }
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            LocalStore.register(this, username, email, password); // local copy for offline login
            if (u == null) { goToMain(); return; }
            u.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(username).build())
                    .addOnCompleteListener(t -> goToMain());
        });
    }

    private void goToMain() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
