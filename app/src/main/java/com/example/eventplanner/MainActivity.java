package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnProfile, fabAddEvent;
    private FirebaseFirestore db;
    private String userRole;
    private String userId;
    private boolean roleChecked = false;
    private RecyclerView organizerRecyclerView, allEventsRecyclerView;
    private EventAdapter eventAdapter, organizerAdapter;
    private List<Event> eventList = new ArrayList<>();
    private List<Event> organizerEvents = new ArrayList<>();
    private EditText etFilter;
    private Button filterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnProfile = findViewById(R.id.btnProfile);
        fabAddEvent = findViewById(R.id.fabAddEvent);
        organizerRecyclerView = findViewById(R.id.organizerEventsRecyclerView);
        allEventsRecyclerView = findViewById(R.id.allEventsRecyclerView);
        etFilter = findViewById(R.id.nameFilter);
        filterButton = findViewById(R.id.filterButton);

        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        userId = currentUser.getUid();

        checkUserRole();

        organizerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        organizerAdapter = new EventAdapter(organizerEvents, currentUser.getEmail());
        eventAdapter = new EventAdapter(eventList, currentUser.getEmail());


        organizerRecyclerView.setAdapter(organizerAdapter);
        allEventsRecyclerView.setAdapter(eventAdapter);

        fetchEvents();

        btnProfile.setOnClickListener(v -> {
            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        fabAddEvent.setOnClickListener(v -> {
            if (roleChecked) {
                if (userRole != null && userRole.equals("organizer")) {
                    Intent addEventIntent = new Intent(MainActivity.this, AddActivity.class);
                    startActivity(addEventIntent);
                } else {
                    Toast.makeText(MainActivity.this, "You must be an organizer to add an event.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Please wait while we check your role.", Toast.LENGTH_SHORT).show();
            }
        });

        filterButton.setOnClickListener(v -> {
            String filterText = etFilter.getText().toString().trim();
            if (!filterText.isEmpty()) {
                filterEvents(filterText);
            } else {
                Toast.makeText(MainActivity.this, "Please enter a valid filter.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserRole() {
        db.collection("attendees").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userRole = "attendee";
                        fabAddEvent.setVisibility(View.GONE);
                        filterButton.setVisibility(View.VISIBLE);
                        etFilter.setVisibility(View.VISIBLE);
                    } else {
                        db.collection("organizers").document(userId).get()
                                .addOnSuccessListener(doc -> {
                                    if (doc.exists()) {
                                        userRole = "organizer";
                                        fabAddEvent.setVisibility(View.VISIBLE);
                                    }
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(MainActivity.this, "Failed to retrieve user role.", Toast.LENGTH_SHORT).show();
                                });
                    }
                    roleChecked = true;
                }).addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to retrieve user role.", Toast.LENGTH_SHORT).show();
                    roleChecked = true;
                });
    }

    private void fetchEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserEmail = currentUser.getEmail();
            db.collection("events").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        eventList.clear();
                        organizerEvents.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Event event = document.toObject(Event.class);
                            if (event.getOrganizerEmail() != null && event.getOrganizerEmail().equals(currentUserEmail)) {
                                organizerEvents.add(event);
                            } else {
                                eventList.add(event);
                            }
                        }

                        organizerAdapter.notifyDataSetChanged();
                        eventAdapter.notifyDataSetChanged();

                        if (!organizerEvents.isEmpty()) {
                            findViewById(R.id.organizerHeader).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.organizerHeader).setVisibility(View.GONE);
                        }

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void filterEvents(String filterText) {
        List<Event> filteredList = new ArrayList<>();
        for (Event event : eventList) {
            if (event.getTitle() != null && event.getTitle().toLowerCase().contains(filterText.toLowerCase())) {
                filteredList.add(event);
            }
        }
        eventAdapter.updateEventList(filteredList);
    }
}
