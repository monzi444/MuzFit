package com.example.muzfit.utils;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DateParserTest {

    @Test
    public void parseApiDate_validString_returnsPositiveMillis() {
        long result = DateParser.parseApiDate("2026-05-27 18:30:00");
        assertTrue(result > 0L);
    }

    @Test
    public void parseApiDate_null_returnsZero() {
        assertEquals(0L, DateParser.parseApiDate(null));
    }

    @Test
    public void parseApiDate_empty_returnsZero() {
        assertEquals(0L, DateParser.parseApiDate(""));
    }

    @Test
    public void parseApiDate_invalidFormat_returnsZero() {
        assertEquals(0L, DateParser.parseApiDate("not-a-date"));
    }

    @Test
    public void isSameDay_sameCalendarDay_returnsTrue() {
        long morning = DateParser.parseApiDate("2026-05-27 08:00:00");
        long evening = DateParser.parseApiDate("2026-05-27 22:15:00");
        assertTrue(DateParser.isSameDay(morning, evening));
    }

    @Test
    public void isSameDay_differentDays_returnsFalse() {
        long dayOne = DateParser.parseApiDate("2026-05-27 12:00:00");
        long dayTwo = DateParser.parseApiDate("2026-05-28 12:00:00");
        assertFalse(DateParser.isSameDay(dayOne, dayTwo));
    }

    @Test
    public void isSameDay_zeroMillis_returnsFalse() {
        long valid = DateParser.parseApiDate("2026-05-27 12:00:00");
        assertFalse(DateParser.isSameDay(0L, valid));
        assertFalse(DateParser.isSameDay(valid, 0L));
    }

    @Test
    public void isSameMonth_matchingYearAndMonth_returnsTrue() {
        long dateMillis = DateParser.parseApiDate("2026-05-27 18:30:00");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateMillis);
        assertTrue(DateParser.isSameMonth(dateMillis, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)));
    }

    @Test
    public void isSameMonth_differentMonth_returnsFalse() {
        long dateMillis = DateParser.parseApiDate("2026-05-27 18:30:00");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateMillis);
        int otherMonth = calendar.get(Calendar.MONTH) == Calendar.JANUARY
                ? Calendar.FEBRUARY
                : Calendar.JANUARY;
        assertFalse(DateParser.isSameMonth(dateMillis, calendar.get(Calendar.YEAR), otherMonth));
    }

    @Test
    public void isSameMonth_zeroMillis_returnsFalse() {
        assertFalse(DateParser.isSameMonth(0L, 2026, Calendar.MAY));
    }
}
