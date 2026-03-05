package com.mycompany.lab2.tests;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import com.mycompany.lab2.model.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parameterized tests for Song model.
 * Check valid, invalid (exceptions), and boundary scenarios.
 */
class SongParamTest {

    /** Valid constructor scenarios. */
    static Stream<Arguments> validConstructorArgs() {
        int current = java.time.Year.now().getValue();
        return Stream.of(
                Arguments.of("Imagine", "John Lennon", 1971),
                Arguments.of("Old Song", "Ancient Artist", 1800),
                Arguments.of("New Song", "Modern Artist", current)
        );
    }

    /** Invalid constructor scenarios for year. */
    static Stream<Integer> invalidYears() {
        return Stream.of(1799, java.time.Year.now().getValue() + 1);
    }

    /**
     * Constructor path and boundaries.
     */
    @ParameterizedTest
    @MethodSource("validConstructorArgs")
    void constructor_validParameters_createsSong(String title, String artist, int year) {
        Song song = new Song(title, artist, year);
        assertEquals(title, song.getTitle());
        assertEquals(artist, song.getArtist());
        assertEquals(year, song.getYear());
        assertFalse(song.isFavorite());
    }

    /**
     * Constructor invalid titles -> IllegalArgumentException.
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void constructor_invalidTitle_throwsIAE(String invalidTitle) {
        assertThrows(IllegalArgumentException.class,
                () -> new Song(invalidTitle, "Artist", 2000));
    }

    /**
     * Constructor invalid artists -> IllegalArgumentException.
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void constructor_invalidArtist_throwsIAE(String invalidArtist) {
        assertThrows(IllegalArgumentException.class,
                () -> new Song("Title", invalidArtist, 2000));
    }

    /**
     * Constructor invalid years (below/above bounds) -> IllegalArgumentException.
     */
    @ParameterizedTest
    @MethodSource("invalidYears")
    void constructor_invalidYear_throwsIAE(int invalidYear) {
        assertThrows(IllegalArgumentException.class,
                () -> new Song("Title", "Artist", invalidYear));
    }

    /**
     * toString format verification using different inputs.
     */
    static Stream<Arguments> toStringArgs() {
        return Stream.of(
                Arguments.of(new Song("Imagine", "John Lennon", 1971), "Imagine - John Lennon 1971"),
                Arguments.of(new Song("Bad", "Michael Jackson", 1987), "Bad - Michael Jackson 1987")
        );
    }

    /**
     * toString should render as "Title - Artist Year" for various inputs.
     */
    @ParameterizedTest
    @MethodSource("toStringArgs")
    void toString_formatsAsExpected(Song song, String expected) {
        assertEquals(expected, song.toString());
    }

    /**
     * setTitle validation (non-trivial mutator).
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void setTitle_invalid_throwsIAE(String invalid) {
        Song s = new Song("A", "B", 2000);
        assertThrows(IllegalArgumentException.class, () -> s.setTitle(invalid));
    }

    /**
     * setTitle should trim and set a non-blank value.
     */
    @ParameterizedTest
    @ValueSource(strings = {" Title ", "Hello"})
    void setTitle_valid_trimsAndSets(String value) {
        Song s = new Song("A", "B", 2000);
        s.setTitle(value);
        assertFalse(s.getTitle().isEmpty());
        assertEquals(value.trim(), s.getTitle());
    }

    /** setArtist validation. */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void setArtist_invalid_throwsIAE(String invalid) {
        Song s = new Song("A", "B", 2000);
        assertThrows(IllegalArgumentException.class, () -> s.setArtist(invalid));
    }

    /**
     * setArtist should trim and set a non-blank value.
     */
    @ParameterizedTest
    @ValueSource(strings = {" Artist ", "Queen"})
    void setArtist_valid_trimsAndSets(String value) {
        Song s = new Song("A", "B", 2000);
        s.setArtist(value);
        assertEquals(value.trim(), s.getArtist());
    }

    /** setYear boundary and invalid checks. */
    static Stream<Arguments> validYears() {
        int current = java.time.Year.now().getValue();
        return Stream.of(
                Arguments.of(1800),
                Arguments.of(current)
        );
    }

    /**
     * setYear accepts boundary values (e.g., 1800 and current year).
     */
    @ParameterizedTest
    @MethodSource("validYears")
    void setYear_validBoundaries_ok(int y) {
        Song s = new Song("A", "B", 2000);
        s.setYear(y);
        assertEquals(y, s.getYear());
    }

    @ParameterizedTest
    @MethodSource("invalidYears")
    void setYear_invalid_throwsIAE(int y) {
        Song s = new Song("A", "B", 2000);
        assertThrows(IllegalArgumentException.class, () -> s.setYear(y));
    }
}