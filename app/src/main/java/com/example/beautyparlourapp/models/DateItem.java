package com.example.beautyparlourapp.models;

public class DateItem {
    private String dayName;
    private String dayNumber;
    private String fullDateString;

    public DateItem(String dayName, String dayNumber, String fullDateString) {
        this.dayName = dayName;
        this.dayNumber = dayNumber;
        this.fullDateString = fullDateString;
    }

    public String getDayName() { return dayName; }
    public String getDayNumber() { return dayNumber; }
    public String getFullDateString() { return fullDateString; }
}