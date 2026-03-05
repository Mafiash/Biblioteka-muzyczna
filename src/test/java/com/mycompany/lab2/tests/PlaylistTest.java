package com.mycompany.lab2.tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import com.mycompany.lab2.model.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PlaylistTest {

    /**
     * Provides invalid title/artist pairs to trigger IllegalArgumentException in removeSong.
     */
    static Stream<Arguments> invalidTitleArtist() {
        return Stream.of(
                Arguments.of(null, "A"),
                Arguments.of(" ", "A"),
                Arguments.of("T", null),
                Arguments.of("T", " ")
        );
    }

    /**
     * Valid playlist name creates an empty playlist with that name.
     */
    @Test
    void constructor_validName_createsPlaylist() {
        Playlist pl = new Playlist("My Playlist");
        assertEquals("My Playlist", pl.getName());
        assertTrue(pl.isEmpty());
        assertEquals(0, pl.getSongs().size());
    }

    /**
     * Null name in constructor should throw IllegalArgumentException.
     */
    @Test
    void constructor_nullName_throwsIAE() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Playlist(null));
    }

    /**
     * Adding a valid song appends it to the playlist.
     */
    @Test
    void addSong_addsValidSong() {
        Playlist pl = new Playlist("Road Trip");
        Song s = new Song("Africa", "Toto", 1982);
        pl.addSong(s);
        assertFalse(pl.isEmpty());
        assertEquals(1, pl.getSongs().size());
        assertSame(s, pl.getSongs().get(0));
    }

    /**
     * Adding null should throw IllegalArgumentException.
     */
    @Test
    void addSong_null_throwsIAE() {
        Playlist pl = new Playlist("Favourites");
        assertThrows(IllegalArgumentException.class, () -> pl.addSong(null));
    }

    /**
     * Removing an existing song returns true and leaves the playlist empty.
     */
    @Test
    void removeSong_removesExisting_returnsTrue() {
        Playlist pl = new Playlist("Hits");
        Song s = new Song("Billie Jean", "Michael Jackson", 1983);
        pl.addSong(s);
        assertTrue(pl.removeSong("Billie Jean", "Michael Jackson"));
        assertTrue(pl.isEmpty());
    }

    /**
     * Removing a non-existing song returns false and does not change the playlist.
     */
    @Test
    void removeSong_notFound_returnsFalse() {
        Playlist pl = new Playlist("Chill");
        new Song("Hurt", "Johnny Cash", 2002); // unrelated
        assertFalse(pl.removeSong("Hurt", "Johnny Cash"));
    }


    /**
     * Verifies isEmpty and clear reflect playlist state changes.
     */
    @Test
    void isEmpty_and_clear_behaveAsExpected() {
        Playlist pl = new Playlist("Workout");
        assertTrue(pl.isEmpty());
        pl.addSong(new Song("Stronger", "Kanye West", 2007));
        assertFalse(pl.isEmpty());
        pl.clear();
        assertTrue(pl.isEmpty());
    }
    
    /**
     * removeSong with null/blank title or artist should throw IllegalArgumentException.
     */
    @ParameterizedTest
    @MethodSource("invalidTitleArtist")
    void removeSong_invalidParams_throwIAE_param(String title, String artist) {
        Playlist pl = new Playlist("Mix");
        assertThrows(IllegalArgumentException.class, () -> pl.removeSong(title, artist));
    }
    
    /**
     * removeSong should be case-insensitive for title and artist.
     */
    @Test
    void removeSong_caseInsensitive_match() {
        Playlist pl = new Playlist("Mix");
        pl.addSong(new Song("Thunderstruck", "ACDC", 1990));
        assertTrue(pl.removeSong("thunderstruck", "acdc"));
    }

}
