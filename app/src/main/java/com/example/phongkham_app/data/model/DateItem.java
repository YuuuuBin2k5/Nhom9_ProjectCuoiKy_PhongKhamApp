package com.example.phongkham_app.data.model;

public class DateItem {
    private String dayOfWeek;
    private String dayNumber;
    private int month;
    private int year;
    private boolean isSelected;

    public DateItem() {
    }

    public DateItem(String dayOfWeek, String dayNumber, int month, int year, boolean isSelected) {
        this.dayOfWeek = dayOfWeek;
        this.dayNumber = dayNumber;
        this.month = month;
        this.year = year;
        this.isSelected = isSelected;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(String dayNumber) {
        this.dayNumber = dayNumber;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
