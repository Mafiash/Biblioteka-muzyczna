package com.mycompany.lab2.tests;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import com.mycompany.lab2.model.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parameterized tests for SongSummary record: constructors and toString.
 */
class SongSummaryParamTest {

    /**
     * Provides sample inputs for the 3-arg SongSummary constructor.
     */
    static Stream<Arguments> threeArgCtorCases() {
        return Stream.of(
                Arguments.of("Test Title", "Test Artist", true),
                Arguments.of("Bad Guy", "Billie Eilish", false)
        );
    }

    /**
     * Verifies 3-arg constructor copies given fields into record components.
     */
    @ParameterizedTest
    @MethodSource("threeArgCtorCases")
    void constructor_threeParams_copiesFields(String title, String artist, boolean fav) {
        SongSummary summary = new SongSummary(title, artist, fav);
        assertEquals(title, summary.title());
        assertEquals(artist, summary.artist());
        assertEquals(fav, summary.favorite());
    }

    /**
     * Supplies Song instances to verify SongSummary(Song) constructor mirrors fields and favorite flag.
     */
    static Stream<Arguments> fromSongCases() {
        return Stream.of(
                Arguments.of(new Song("Original", "Artist Name", 2020), false),
                Arguments.of(new Song("Favorite Song", "Popular Artist", 2021), true)
        );
    }

    /**
     * Ensures SongSummary(Song) copies title/artist and favorite flag from Song.
     */
    @ParameterizedTest
    @MethodSource("fromSongCases")
    void constructor_fromSong_reflectsSongFields(Song s, boolean fav) {
        s.setFavorite(fav);
        SongSummary summary = new SongSummary(s);
        assertEquals(s.getTitle(), summary.title());
        assertEquals(s.getArtist(), summary.artist());
        assertEquals(fav, summary.favorite());
    }

    /**
     * Provides cases to verify pretty toString formatting.
     */
    static Stream<Arguments> toStringCases() {
        return Stream.of(
                Arguments.of(new SongSummary("Bad Guy", "Billie Eilish", true), "Bad Guy — Billie Eilish"),
                Arguments.of(new SongSummary("Imagine", "John Lennon", false), "Imagine — John Lennon")
        );
    }

    /**
     * Checks that toString returns the expected "Title — Artist" format.
     */
    @ParameterizedTest
    @MethodSource("toStringCases")
    void toString_formatsAsExpected(SongSummary summary, String expected) {
        assertEquals(expected, summary.toString());
    }
}
