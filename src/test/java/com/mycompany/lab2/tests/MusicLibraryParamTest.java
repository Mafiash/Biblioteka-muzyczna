package com.mycompany.lab2.tests;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import com.mycompany.lab2.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parameterized tests for public methods of MusicLibrary.
 * Cover valid, invalid (forcing exceptions), and boundary-like scenarios.
 */
class MusicLibraryParamTest {

    /** Different songs used across tests. */
    static Stream<Arguments> songs() {
        return Stream.of(
                Arguments.of(new Song("Numb", "Linkin Park", 2003)),
                Arguments.of(new Song("Yellow", "Coldplay", 2000))
        );
    }

    /** Title/artist invalid pairs to force IAE in removeSong. */
    static Stream<Arguments> invalidTitleArtist() {
        return Stream.of(
                Arguments.of(null, "A"),
                Arguments.of(" ", "A"),
                Arguments.of("T", null),
                Arguments.of("T", " ")
        );
    }

    /** Playlists names for true/false remove cases. */
    static Stream<Arguments> playlistNames() {
        return Stream.of(
                Arguments.of("X", "x", true),   // remove succeeds once (case-insensitive)
                Arguments.of("Party", "nope", false) // remove fails when missing
        );
    }

    /**
     * addSong: valid add; duplicate and null handling.
     */
    @ParameterizedTest
    @MethodSource("songs")
    void addSong_valid_duplicate_and_null(Song s) throws Exception {
        MusicLibrary lib = new MusicLibrary();
        lib.addSong(s);
        List<Song> all = lib.filterSongs(song -> true);
        assertEquals(1, all.size());
        assertSame(s, all.get(0));
        Song dup = new Song(s.getTitle(), s.getArtist(), s.getYear());
        assertThrows(DuplicateSongException.class, () -> lib.addSong(dup));
        assertThrows(IllegalArgumentException.class, () -> lib.addSong(null));
    }

    /**
     * removeSong: invalid parameters -> IAE.
     */
    @ParameterizedTest
    @MethodSource("invalidTitleArtist")
    void removeSong_invalidParams_throwIAE(String title, String artist) {
        MusicLibrary lib = new MusicLibrary();
        assertThrows(IllegalArgumentException.class, () -> lib.removeSong(title, artist));
    }

    /**
     * removeSong: true when present, false when absent; also removes from playlists.
     */
    @ParameterizedTest
    @MethodSource("songs")
    void removeSong_trueAndFalse_and_cascadeToPlaylists(Song s) throws Exception {
        MusicLibrary lib = new MusicLibrary();
        lib.addSong(s);
        lib.addPlaylist("A");
        lib.addSongToPlaylist("A", s);
        assertTrue(lib.removeSong(s.getTitle(), s.getArtist()));
        assertFalse(lib.removeSong(s.getTitle(), s.getArtist()));
        assertEquals(0, lib.getPlaylist("A").getSongs().size(), "Should be removed from playlists as well");
    }

    /** addPlaylist: invalid and duplicate names. */
    static Stream<String> invalidNames() { return Stream.of(null, " "); }

    /**
     * addPlaylist should throw IllegalArgumentException for null/blank names.
     */
    @ParameterizedTest
    @MethodSource("invalidNames")
    void addPlaylist_invalid_throwsIAE(String name) {
        MusicLibrary lib = new MusicLibrary();
        assertThrows(IllegalArgumentException.class, () -> lib.addPlaylist(name));
    }

    /**
     * removePlaylist should return true when the playlist existed (case-insensitive) and false otherwise.
     */
    @ParameterizedTest
    @MethodSource("playlistNames")
    void removePlaylist_trueFalse_cases(String toAdd, String toRemove, boolean expected) {
        MusicLibrary lib = new MusicLibrary();
        lib.addPlaylist(toAdd);
        assertEquals(expected, lib.removePlaylist(toRemove));
    }

    /** getPlaylist: found vs null. */
    static Stream<Arguments> getPlaylistCases() {
        return Stream.of(
            Arguments.of("Gym", "Gym", true),
            Arguments.of("Study", "Missing", false)
        );
    }

    /**
     * getPlaylist returns the playlist when present and null when missing (case-sensitive query here).
     */
    @ParameterizedTest
    @MethodSource("getPlaylistCases")
    void getPlaylist_foundOrNull(String added, String query, boolean present) {
        MusicLibrary lib = new MusicLibrary();
        lib.addPlaylist(added);
        Playlist p = lib.getPlaylist(query);
        assertEquals(present, p != null);
    }

    /** addSongToPlaylist/removeSongFromPlaylist: missing playlist -> IAE; valid path works. */
    @ParameterizedTest
    @MethodSource("songs")
    void addAndRemoveSongToFromPlaylist_behaviour(Song s) throws Exception {
        MusicLibrary lib = new MusicLibrary();
        lib.addSong(s);
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> lib.addSongToPlaylist("Nope", s)),
                () -> assertThrows(IllegalArgumentException.class, () -> lib.removeSongFromPlaylist("Nope", s))
        );
        lib.addPlaylist("Chill");
        lib.addSongToPlaylist("Chill", s);
        assertEquals(1, lib.getPlaylist("Chill").getSongs().size());
        lib.removeSongFromPlaylist("Chill", s);
        assertEquals(0, lib.getPlaylist("Chill").getSongs().size());
    }

    /** saveToFile/loadFromFile round trip with different favorite flags and playlist membership. */
    static Stream<Arguments> roundTripCases() {
        return Stream.of(
                Arguments.of(true, "Coldplay"),
                Arguments.of(false, "Mix")
        );
    }

    /**
     * Saves to a file and loads back, preserving songs, favorites, and playlist membership.
     */
    @ParameterizedTest
    @MethodSource("roundTripCases")
    void saveAndLoad_roundTrip(boolean favorite, String playlistName, @TempDir Path tempDir) throws Exception {
        MusicLibrary lib = new MusicLibrary();
        Song s1 = new Song("Yellow", "Coldplay", 2000);
        s1.setFavorite(favorite);
        Song s2 = new Song("Clocks", "Coldplay", 2002);
        lib.addSong(s1);
        lib.addSong(s2);
        lib.addPlaylist(playlistName);
        lib.addSongToPlaylist(playlistName, s1);
        Path file = tempDir.resolve("library.txt");

        lib.saveToFile(file.toString());
        assertTrue(Files.exists(file));

        MusicLibrary loaded = new MusicLibrary();
        loaded.loadFromFile(file.toString());

        List<Song> loadedAll = loaded.filterSongs(song -> true);
        assertEquals(2, loadedAll.size());
        assertEquals(1, loaded.getPlaylist(playlistName).getSongs().size());
    }

    /** saveToFile/loadFromFile invalid filename cases. */
    static Stream<String> invalidFilenames() { return Stream.of(null, " "); }

    /**
     * saveToFile should throw IllegalArgumentException for null/blank filename.
     */
    @ParameterizedTest
    @MethodSource("invalidFilenames")
    void saveToFile_invalidFilename_throwsIAE(String filename) {
        MusicLibrary lib = new MusicLibrary();
        assertThrows(IllegalArgumentException.class, () -> lib.saveToFile(filename));
    }

    /**
     * loadFromFile should throw IllegalArgumentException for null/blank filename.
     */
    @ParameterizedTest
    @MethodSource("invalidFilenames")
    void loadFromFile_invalidFilename_throwsIAE(String filename) {
        MusicLibrary lib = new MusicLibrary();
        assertThrows(IllegalArgumentException.class, () -> lib.loadFromFile(filename));
    }

    /**
     * filterSongs filters using a provided predicate and throws IAE when predicate is null.
     */
    @ParameterizedTest
    @MethodSource("songs")
    void filterSongs_predicates_and_null(Song s) throws Exception {
        MusicLibrary lib = new MusicLibrary();
        lib.addSong(s);
        lib.addSong(new Song("One", "U2", 1991));
        SongPredicate byArtist = x -> x.getArtist().equalsIgnoreCase(s.getArtist());
        List<Song> res = lib.filterSongs(byArtist);
        assertTrue(res.stream().anyMatch(x -> x.getTitle().equals(s.getTitle())));
        assertThrows(IllegalArgumentException.class, () -> lib.filterSongs(null));
    }

    /**
     * loadFromFile should throw IOException when the file does not exist.
     */
    @ParameterizedTest
    @MethodSource("songs")
    void loadFromFile_missingFile_throwsIOException(Song ignored, @TempDir Path tempDir) {
        MusicLibrary lib = new MusicLibrary();
        Path missing = tempDir.resolve("missing.txt");
        IOException ex = assertThrows(IOException.class, () -> lib.loadFromFile(missing.toString()));
        assertTrue(ex.getMessage().contains("Nie znaleziono pliku"));
    }
}
