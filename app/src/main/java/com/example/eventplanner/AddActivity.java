package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddActivity extends AppCompatActivity {

    private EditText etEventName, etEventDescription, etEventDate, etEventLocation;
    private Button btnSaveEvent;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        db = FirebaseFirestore.getInstance();

        etEventName = findViewById(R.id.etEventName);
        etEventDescription = findViewById(R.id.etEventDescription);
        etEventDate = findViewById(R.id.etEventDate);
        etEventLocation = findViewById(R.id.etEventLocation);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);

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
        String organizerEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Event event = new Event(name, description, date, location, organizerEmail);


        db.collection("events")
                .add(event)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddActivity.this, "Event added successfully!", Toast.LENGTH_SHORT).show();
                    Intent addEventIntent = new Intent(AddActivity.this, MainActivity.class);
                    startActivity(addEventIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddActivity.this, "Failed to add event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
