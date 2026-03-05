package com.mycompany.lab2.tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

import com.mycompany.lab2.model.Song;

/**
 * Non-parameterized tests for the Song model.
 */
public class SongTest {
    /**
     * Verifies that a Song is created correctly when provided with valid parameters.
     * Asserts title, artist, year, and default favorite flag state.
     */
    @Test
    void constructor_validParameters_createsSong() {
        Song song = new Song("Imagine", "John Lennon", 1971);
        assertEquals("Imagine", song.getTitle());
        assertEquals("John Lennon", song.getArtist());
        assertEquals(1971, song.getYear());
        assertFalse(song.isFavorite());
    }

    /**
     * Ensures that passing a null title to the constructor throws IllegalArgumentException.
     */
    @Test
    void constructor_nullTitle_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> new Song(null, "Artist", 2000));
    }

    /**
     * Ensures that passing an empty/blank title to the constructor throws IllegalArgumentException.
     */
    @Test
    void constructor_emptyTitle_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> new Song("  ", "Artist", 2000));
    }

    /**
     * Ensures that passing a null artist to the constructor throws IllegalArgumentException.
     */
    @Test
    void constructor_nullArtist_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> new Song("Title", null, 2000));
    }

    /**
     * Ensures that passing an empty/blank artist to the constructor throws IllegalArgumentException.
     */
    @Test
    void constructor_emptyArtist_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> new Song("Title", "  ", 2000));
    }

    /**
     * Verifies that a year below the lower bound (1800) throws IllegalArgumentException.
     */
    @Test
    void constructor_yearTooEarly_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> new Song("Title", "Artist", 1799));
    }

    /**
     * Verifies that a year above the upper bound (current year) throws IllegalArgumentException.
     */
    @Test
    void constructor_yearTooLate_throwsIAE() {
        int futureYear = java.time.Year.now().getValue() + 1;
        assertThrows(IllegalArgumentException.class,
                () -> new Song("Title", "Artist", futureYear));
    }

    /**
     * Confirms that the lower boundary year 1800 is accepted by the constructor.
     */
    @Test
    void constructor_yearBoundary1800_succeeds() {
        Song song = new Song("Old Song", "Ancient Artist", 1800);
        assertEquals(1800, song.getYear());
    }

    /**
     * Confirms that the current year boundary is accepted by the constructor.
     */
    @Test
    void constructor_yearBoundaryCurrentYear_succeeds() {
        int currentYear = java.time.Year.now().getValue();
        Song song = new Song("New Song", "Modern Artist", currentYear);
        assertEquals(currentYear, song.getYear());
    }

    /**
     * Checks that toString follows the "Title - Artist Year" format for a sample song.
     */
    @Test
    void toString_formatsAsExpected() {
        Song song = new Song("Imagine", "John Lennon", 1971);
        assertEquals("Imagine - John Lennon 1971", song.toString());
    }

    /**
     * Parameterized: invalid title values (null, empty, blank) must cause IllegalArgumentException.
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void constructor_invalidTitle_throwsIAE(String invalidTitle) {
        assertThrows(IllegalArgumentException.class,
                () -> new Song(invalidTitle, "Artist", 2000));
    }

    /**
     * Parameterized: invalid artist values (null, empty, blank) must cause IllegalArgumentException.
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void constructor_invalidArtist_throwsIAE(String invalidArtist) {
        assertThrows(IllegalArgumentException.class,
                () -> new Song("Title", invalidArtist, 2000));
    }
}
