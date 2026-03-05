package com.mycompany.lab2.tests;

import org.junit.jupiter.api.Test;
import com.mycompany.lab2.model.*;

import static org.junit.jupiter.api.Assertions.*;

class SongSummaryTest {

    /**
     * Verifies the 3-arg constructor sets title, artist, and favorite flag.
     */
    @Test
    void constructor_threeParams_createsSummary() {
        SongSummary summary = new SongSummary("Test Title", "Test Artist", true);
        assertEquals("Test Title", summary.title());
        assertEquals("Test Artist", summary.artist());
        assertTrue(summary.favorite());
    }

    /**
     * Ensures SongSummary(Song) copies title/artist and favorite flag from a Song instance.
     */
    @Test
    void constructor_fromSong_copiesFields() {
        Song song = new Song("Original", "Artist Name", 2020);
        song.setFavorite(false);
        SongSummary summary = new SongSummary(song);
        assertEquals("Original", summary.title());
        assertEquals("Artist Name", summary.artist());
        assertFalse(summary.favorite());
    }

    /**
     * When source Song is favorite, SongSummary should preserve favorite=true.
     */
    @Test
    void constructor_fromFavoriteSong_preservesFavoriteStatus() {
        Song song = new Song("Favorite Song", "Popular Artist", 2021);
        song.setFavorite(true);
        SongSummary summary = new SongSummary(song);
        assertTrue(summary.favorite());
    }

    /**
     * Checks the toString output uses the expected "Title — Artist" format.
     */
    @Test
    void toString_formatsAsExpected() {
        SongSummary summary = new SongSummary("Bad Guy", "Billie Eilish", true);
        assertEquals("Bad Guy — Billie Eilish", summary.toString());
    }

    
    @Test
    void toString_usesEmDashSeparator() {
        SongSummary summary = new SongSummary("Imagine", "John Lennon", false);
        String text = summary.toString();
        assertTrue(text.contains(" — "));
        assertFalse(text.contains(" - "));
    }
}
