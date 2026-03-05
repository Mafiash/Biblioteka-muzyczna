package com.mycompany.lab2.tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.mycompany.lab2.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MusicLibraryTest {

    /**
     * Ensures a new MusicLibrary is created empty with zero songs.
     */
    @Test
    void constructor_createsEmptyLibrary() {
        MusicLibrary lib = new MusicLibrary();
        assertNotNull(lib);
        List<Song> allSongs = lib.filterSongs(song -> true);
        assertEquals(0, allSongs.size());
    }

    /**
     * Adds a song successfully; duplicate and null inputs throw exceptions.
     */
    @Test
    void addSong_valid_and_duplicateHandling() throws Exception {
        MusicLibrary lib = new MusicLibrary();
        Song s1 = new Song("Numb", "Linkin Park", 2003);
        lib.addSong(s1);
        List<Song> all = lib.filterSongs(song -> true);
        assertEquals(1, all.size());
        assertSame(s1, all.get(0));
        Song duplicate = new Song("Numb", "Linkin Park", 2003);
        assertThrows(DuplicateSongException.class, () -> lib.addSong(duplicate));
        assertThrows(IllegalArgumentException.class, () -> lib.addSong(null));
    }

    /**
     * Removes existing song (true), then again (false); verifies invalid params throw IAE.
     */
    @Test
    void removeSong_valid_and_invalidParams() throws Exception {
        MusicLibrary lib = new MusicLibrary();
        lib.addSong(new Song("Fix You", "Coldplay", 2005));
        assertTrue(lib.removeSong("Fix You", "Coldplay"));
        assertFalse(lib.removeSong("Fix You", "Coldplay"));
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> lib.removeSong(null, "A")),
                () -> assertThrows(IllegalArgumentException.class, () -> lib.removeSong(" ", "A")),
                () -> assertThrows(IllegalArgumentException.class, () -> lib.removeSong("T", null)),
                () -> assertThrows(IllegalArgumentException.class, () -> lib.removeSong("T", " "))
        );
    }

    /**
     * Manages playlists: create/get/invalid names and assign/remove songs to/from playlists.
     */
    @Test
    void playlist_management_and_song_assignment() throws Exception {
        MusicLibrary lib = new MusicLibrary();
        lib.addPlaylist("Gym");
        assertNotNull(lib.getPlaylist("Gym"));
        assertNull(lib.getPlaylist("Missing"));
        assertThrows(IllegalArgumentException.class, () -> lib.addPlaylist(null));
        assertThrows(IllegalArgumentException.class, () -> lib.addPlaylist(" "));
        assertThrows(IllegalArgumentException.class, () -> lib.addPlaylist("gym"));
        Song s = new Song("Alive", "Sia", 2015);
        lib.addSong(s);
        lib.addSongToPlaylist("Gym", s);
        assertEquals(1, lib.getPlaylist("Gym").getSongs().size());

        assertThrows(IllegalArgumentException.class, () -> lib.addSongToPlaylist("Nope", s));

        lib.removeSongFromPlaylist("Gym", s);
        assertEquals(0, lib.getPlaylist("Gym").getSongs().size());
        assertThrows(IllegalArgumentException.class, () -> lib.removeSongFromPlaylist("Nope", s));
    }

    /**
     * Filters songs using predicate and verifies null predicate throws IAE.
     */
    @Test
    void filterSongs_behaviour_and_nullPredicate() throws Exception {
        MusicLibrary lib = new MusicLibrary();
        lib.addSong(new Song("One", "U2", 1991));
        lib.addSong(new Song("One More Time", "Daft Punk", 2000));
        List<Song> ones = lib.filterSongs(s -> s.getTitle().toLowerCase().startsWith("one"));
        assertEquals(2, ones.size());
        assertThrows(IllegalArgumentException.class, () -> lib.filterSongs(null));
    }

    /**
     * Removing a playlist returns true when it existed, then false on subsequent attempts.
     */
    @Test
    void removePlaylist_trueAndFalse() {
        MusicLibrary lib = new MusicLibrary();
        lib.addPlaylist("X");
        assertTrue(lib.removePlaylist("x"));
        assertFalse(lib.removePlaylist("x"));
    }

    /**
     * Saves a library to file and loads it back, verifying songs and playlists are preserved.
     */
    @Test
    void saveAndLoad_roundTrip(@TempDir Path tempDir) throws Exception {
        MusicLibrary lib = new MusicLibrary();
        Song s1 = new Song("Yellow", "Coldplay", 2000);
        s1.setFavorite(true);
        Song s2 = new Song("Clocks", "Coldplay", 2002);
        lib.addSong(s1);
        lib.addSong(s2);
        lib.addPlaylist("Coldplay");
        lib.addSongToPlaylist("Coldplay", s1);
        Path file = tempDir.resolve("library.txt");

        lib.saveToFile(file.toString());
        assertTrue(Files.exists(file));

        MusicLibrary loaded = new MusicLibrary();
        loaded.loadFromFile(file.toString());

        List<Song> loadedAll = loaded.filterSongs(song -> true);
        assertEquals(2, loadedAll.size());
        assertEquals(1, loaded.getPlaylist("Coldplay").getSongs().size());

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> lib.saveToFile(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> lib.saveToFile(" ")),
                () -> assertThrows(IllegalArgumentException.class, () -> loaded.loadFromFile(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> loaded.loadFromFile(" "))
        );

        IOException ex = assertThrows(IOException.class, () -> loaded.loadFromFile(file.resolveSibling("missing.txt").toString()));
        assertTrue(ex.getMessage().contains("Nie znaleziono pliku"));
    }
    
    /**
     * getPlaylist should find playlists regardless of case.
     */
    @Test
    void getPlaylist_caseInsensitive_found() {
        MusicLibrary lib = new MusicLibrary();
        lib.addPlaylist("Chill");
        assertNotNull(lib.getPlaylist("chill"));
    }

    /**
     * Removing a song should also remove it from all playlists it belongs to.
     */
    @Test
    void removeSong_cascadesAcrossMultiplePlaylists() throws Exception {
        MusicLibrary lib = new MusicLibrary();
        Song s = new Song("Believer", "Imagine Dragons", 2017);
        lib.addSong(s);
        lib.addPlaylist("Workout");
        lib.addPlaylist("Drive");
        lib.addSongToPlaylist("Workout", s);
        lib.addSongToPlaylist("Drive", s);
        assertEquals(1, lib.getPlaylist("Workout").getSongs().size());
        assertEquals(1, lib.getPlaylist("Drive").getSongs().size());
        assertTrue(lib.removeSong("Believer", "Imagine Dragons"));
        assertEquals(0, lib.getPlaylist("Workout").getSongs().size());
        assertEquals(0, lib.getPlaylist("Drive").getSongs().size());
    }

    /**
     * filterSongs should return an empty list when no songs match the predicate.
     */
    @Test
    void filterSongs_returnsEmptyWhenNoMatch() throws Exception {
        MusicLibrary lib = new MusicLibrary();
        lib.addSong(new Song("Yellow", "Coldplay", 2000));
        assertTrue(lib.filterSongs(s -> s.getArtist().equals("Adele")).isEmpty());
    }

    /**
     * getSongs() and getPlaylists() should be non-null and empty on a new library.
     */
    @Test
    void getters_emptyOnNewLibrary() {
        MusicLibrary lib = new MusicLibrary();
        assertNotNull(lib.getSongs());
        assertNotNull(lib.getPlaylists());
        assertTrue(lib.getSongs().isEmpty());
        assertTrue(lib.getPlaylists().isEmpty());
    }

    /**
     * getSongs() and getPlaylists() should reflect changes after adding items.
     */
    @Test
    void getters_reflectAddedItems() throws Exception {
        MusicLibrary lib = new MusicLibrary();

        Song s = new Song("Smooth", "Santana", 1999);
        lib.addSong(s);
        assertEquals(1, lib.getSongs().size());
        assertSame(s, lib.getSongs().get(0));

        lib.addPlaylist("Latin Hits");
        assertEquals(1, lib.getPlaylists().size());
        assertTrue(lib.getPlaylists().stream().anyMatch(p -> p.getName().equals("Latin Hits")));
    }
}
