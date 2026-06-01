package com.example.muzfit.model;

public class DashboardCalendarDay {

    private final int dayNumber;
    private final ActivityLevel level;
    private final boolean currentMonth;

    public DashboardCalendarDay(int dayNumber, ActivityLevel level, boolean currentMonth) {
        this.dayNumber = dayNumber;
        this.level = level;
        this.currentMonth = currentMonth;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public ActivityLevel getLevel() {
        return level;
    }

    public boolean isCurrentMonth() {
        return currentMonth;
    }

    public enum ActivityLevel {
        NONE,
        PARTIAL,
        GOAL,
        EMPTY
    }
}
