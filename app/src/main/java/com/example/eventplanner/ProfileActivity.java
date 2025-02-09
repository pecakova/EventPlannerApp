package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPhone, etRole;
    private Button btnSaveProfile;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        etRole = findViewById(R.id.etRole);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        Button btnLogout = findViewById(R.id.btnLogout);
        AppCompatImageButton btnBack = findViewById(R.id.btnBack);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadUserProfile();

        etFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                btnSaveProfile.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        etLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                btnSaveProfile.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                btnSaveProfile.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void loadUserProfile() {
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        firestore.collection("attendees").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String phone = documentSnapshot.getString("phone");
                        String role = documentSnapshot.getString("role");

                        etFirstName.setText(firstName);
                        etLastName.setText(lastName);
                        etPhone.setText(phone);
                        etRole.setText(role);
                    } else {
                        fetchUserDataFromOrganizers(userId);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Error loading profile from attendees", Toast.LENGTH_SHORT).show());
    }

    private void fetchUserDataFromOrganizers(String userId) {
        firestore.collection("organizers").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String phone = documentSnapshot.getString("phone");
                        String role = documentSnapshot.getString("role");

                        etFirstName.setText(firstName);
                        etLastName.setText(lastName);
                        etPhone.setText(phone);
                        etRole.setText(role);
                    } else {
                        Toast.makeText(ProfileActivity.this, "User not found in attendees or organizers", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Error loading profile from organizers", Toast.LENGTH_SHORT).show());
    }

    private void saveProfileChanges() {
        String updatedFirstName = etFirstName.getText().toString().trim();
        String updatedLastName = etLastName.getText().toString().trim();
        String updatedPhone = etPhone.getText().toString().trim();

        if (!updatedFirstName.isEmpty() && !updatedLastName.isEmpty()) {
            String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
            String role = etRole.getText().toString();

            if (role.equals("Attendee")) {
                firestore.collection("attendees").document(userId)
                        .update("firstName", updatedFirstName, "lastName", updatedLastName, "phone", updatedPhone)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            btnSaveProfile.setVisibility(View.GONE);
                        })
                        .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Error updating profile in attendees", Toast.LENGTH_SHORT).show());
            } else if (role.equals("Organizer")) {
                firestore.collection("organizers").document(userId)
                        .update("firstName", updatedFirstName, "lastName", updatedLastName, "phone", updatedPhone)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            btnSaveProfile.setVisibility(View.GONE);
                        })
                        .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Error updating profile in organizers", Toast.LENGTH_SHORT).show());
            }
        } else {
            Toast.makeText(ProfileActivity.this, "First name and last name cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }
}