package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddActivity extends AppCompatActivity {

    private EditText etEventName, etEventDescription, etEventDate, etEventLocation;
    private Button btnSaveEvent;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        etEventName = findViewById(R.id.etEventName);
        etEventDescription = findViewById(R.id.etEventDescription);
        etEventDate = findViewById(R.id.etEventDate);
        etEventLocation = findViewById(R.id.etEventLocation);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);

        // Button Click Listener to save event
        btnSaveEvent.setOnClickListener(v -> saveEventToFirestore());
    }

    private void saveEventToFirestore() {
        String name = etEventName.getText().toString().trim();
        String description = etEventDescription.getText().toString().trim();
        String date = etEventDate.getText().toString().trim();
        String location = etEventLocation.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || date.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create event object
//        Map<String, Object> event = new HashMap<>();
//        event.put("name", name);
//        event.put("description", description);
//        event.put("date", date);
//        event.put("location", location);
//        event.put("organizerId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        String organizerEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail(); // Get organizer's email

        Event event = new Event(name, description, date, location, organizerEmail);


        // Save to Firestore under "events" collection
        db.collection("events")
                .add(event)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddActivity.this, "Event added successfully!", Toast.LENGTH_SHORT).show();
                    Intent addEventIntent = new Intent(AddActivity.this, MainActivity.class);
                    startActivity(addEventIntent);
                    finish(); // Close activity after saving
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddActivity.this, "Failed to add event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
