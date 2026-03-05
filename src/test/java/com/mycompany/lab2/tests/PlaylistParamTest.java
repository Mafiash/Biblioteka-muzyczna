package com.mycompany.lab2.tests;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import com.mycompany.lab2.model.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional parameterized tests for Playlist public methods.
 * Checks valid flows, invalid inputs (exceptions), and simple boundaries.
 */
class PlaylistParamTest {

    /** Invalid names for constructor/addPlaylist. */
    static Stream<String> invalidNames() { return Stream.of(null, " "); }

    /** Title/artist invalid pairs to force IAE in removeSong. */
    static Stream<Arguments> invalidTitleArtist() {
        return Stream.of(
                Arguments.of(null, "A"),
                Arguments.of(" ", "A"),
                Arguments.of("T", null),
                Arguments.of("T", " ")
        );
    }

    /** Different songs for add/remove scenarios. */
    static Stream<Song> songs() {
        return Stream.of(
                new Song("Africa", "Toto", 1982),
                new Song("Hurt", "Johnny Cash", 2002)
        );
    }

    /**
     * Constructor invalid: name null/blank -> IAE.
     */
    @ParameterizedTest
    @MethodSource("invalidNames")
    void constructor_invalidName_throwsIAE(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new Playlist(invalid));
    }

    /**
     * addSong: null -> IAE; valid songs are appended.
     */
    @ParameterizedTest
    @MethodSource("songs")
    void addSong_valid_and_null(Song s) {
        Playlist pl = new Playlist("P");
        pl.addSong(s);
        assertEquals(1, pl.getSongs().size());
        assertSame(s, pl.getSongs().get(0));
        assertThrows(IllegalArgumentException.class, () -> pl.addSong(null));
    }

    /** removeSong invalid parameters. */
    @ParameterizedTest
    @MethodSource("invalidTitleArtist")
    void removeSong_invalidParams_throwIAE(String title, String artist) {
        Playlist pl = new Playlist("Mix");
        assertThrows(IllegalArgumentException.class, () -> pl.removeSong(title, artist));
    }

    /** removeSong returns true when present, false when absent. */
    @ParameterizedTest
    @MethodSource("songs")
    void removeSong_trueThenFalse(Song s) {
        Playlist pl = new Playlist("Hits");
        pl.addSong(s);
        assertTrue(pl.removeSong(s.getTitle(), s.getArtist()));
        assertFalse(pl.removeSong(s.getTitle(), s.getArtist()));
    }

}
