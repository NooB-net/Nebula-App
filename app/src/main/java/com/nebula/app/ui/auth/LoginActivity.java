package com.nebula.app.ui.auth;

import com.nebula.app.ui.BaseActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;


import com.nebula.app.databinding.ActivityLoginBinding;
import com.nebula.app.ui.MainActivity;
import com.nebula.app.util.AuthErrors;
import com.nebula.app.util.Cloud;
import com.nebula.app.util.LocalStore;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends BaseActivity {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.progressBar.setVisibility(View.GONE);
        binding.btnLogin.setOnClickListener(v -> loginUser());
        binding.tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        binding.tvSkip.setOnClickListener(v -> {
            // A guest is a brand-new, empty identity: never the previous account or a previous guest
            Cloud.signOut(this);
            LocalStore.guest(this);
            goToMain();
        });
    }

    private void loginUser() {
        final String email = binding.etEmail.getText().toString().trim();
        final String password = binding.etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Cloud.isConfigured(this)) {
            // Offline/local-only mode (no Firebase set up yet)
            if (!LocalStore.hasAccount(this)) {
                Toast.makeText(this, "No account on this device yet. Tap Register first.", Toast.LENGTH_LONG).show();
                return;
            }
            if (LocalStore.login(this, email, password)) goToMain();
            else Toast.makeText(this, AuthErrors.INCORRECT, Toast.LENGTH_SHORT).show();
            return;
        }

        // Cloud account: works on any phone
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);
            if (task.isSuccessful()) {
                FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
                String name = u != null && u.getDisplayName() != null && !u.getDisplayName().isEmpty()
                        ? u.getDisplayName() : email.split("@")[0];
                LocalStore.saveSession(this, name, email);
                goToMain();
            } else if (LocalStore.hasAccount(this) && LocalStore.login(this, email, password)) {
                // Older on-device account (or no connection): let them in
                goToMain();
            } else {
                Toast.makeText(this, AuthErrors.forLogin(task.getException()), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
