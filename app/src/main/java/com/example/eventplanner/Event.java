package com.example.eventplanner;

public class Event {
    private String title, description, date, location, organizerEmail;

    public Event() {
    }

    public Event(String title, String description, String date, String location, String organizerEmail) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.organizerEmail = organizerEmail;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getLocation() {return  location; }
    public String getOrganizerEmail() {return organizerEmail; }

}
