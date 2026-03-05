/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.lab2.model;

import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents a music library that stores a collection of songs and playlists.
 *
 * Allows adding, removing, searching, saving, and loading songs.
 * Supports multiple playlists (including favorites or custom ones).
 *
 * @author Mateusz Smuda
 * @version 3.1
 */
@ToString
public class MusicLibrary {

    /** All songs in the library */
    @Getter
    private final List<Song> songs = new ArrayList<>();

    /** All user playlists */
    @Getter
    private final List<Playlist> playlists = new ArrayList<>();
    
    /** Activity history log */
    private final List<String> activityLog = java.util.Collections.synchronizedList(new ArrayList<>());

    /** 
     * Constructs an empty {@code MusicLibrary}.
     */
    public MusicLibrary() {}
    
    /** Adds an entry to the activity log. */
    public void addLog(String entry) {
        if (entry == null) return;
        activityLog.add(entry);
    }

    /** Returns a snapshot copy of the activity log for safe iteration. */
    public java.util.List<String> getActivityLog() {
        synchronized (activityLog) {
            return new java.util.ArrayList<>(activityLog);
        }
    }

    /**
     * Adds a new song to the library.
     *
     * @param song the {@link Song} to be added
     * @throws IllegalArgumentException if the song is {@code null}
     * @throws DuplicateSongException if the same song (title and artist) already exists
     */
    public void addSong(Song song) throws DuplicateSongException {
        if (song == null) {
            throw new IllegalArgumentException("Nie mozna dodac pustej piosenki!");
        }

        boolean duplicate = songs.stream()
                .anyMatch(s -> s.getTitle().equalsIgnoreCase(song.getTitle())
                        && s.getArtist().equalsIgnoreCase(song.getArtist()));
        if (duplicate) {
            throw new DuplicateSongException();
        }
        songs.add(song);
    }

    /**
    * Removes a song from the library (and from all playlists) based on title and artist.
    *
    * @param title  the title of the song to remove
    * @param artist the artist of the song to remove
    * @return {@code true} if the song was removed, otherwise {@code false}
    * @throws IllegalArgumentException if title or artist are empty
    */

    public boolean removeSong(String title, String artist) {
        if (title == null || title.trim().isEmpty() ||
            artist == null || artist.trim().isEmpty()) {
            throw new IllegalArgumentException("Tytul i wykonawca nie moga byc puste!");
        }

        boolean removed = songs.removeIf(s ->
                s.getTitle().equalsIgnoreCase(title.trim()) &&
                s.getArtist().equalsIgnoreCase(artist.trim()));
        if (removed) {
            playlists.forEach(p -> p.removeSong(title, artist));
        }
        return removed;
    }

    /**
    * Adds a new playlist with the given name.
    *
    * @param name the name of the playlist to create
    * @throws IllegalArgumentException if the name is empty or already exists
    */

    public void addPlaylist(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa playlisty nie moze byc pusta!");
        }

        boolean exists = playlists.stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name.trim()));
        if (exists) {
            throw new IllegalArgumentException("Playlista o tej nazwie juz istnieje!");
        }
        playlists.add(new Playlist(name.trim()));
    }

    /**
    * Removes a playlist from the library by its name.
    *
    * @param name the name of the playlist to remove
    * @return {@code true} if the playlist was removed, otherwise {@code false}
    */

    public boolean removePlaylist(String name) {
        return playlists.removeIf(p -> p.getName().equalsIgnoreCase(name.trim()));
    }

    /**
    * Returns a playlist by its name.
    *
    * @param name the name of the playlist to search for
    * @return the playlist if found, otherwise {@code null}
    */

    public Playlist getPlaylist(String name) {
        return playlists.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElse(null);
    }

    /**
    * Adds a song to a specified playlist.
    *
    * @param playlistName the name of the playlist
    * @param song         the song to add
    * @throws IllegalArgumentException if the playlist does not exist
    */

    public void addSongToPlaylist(String playlistName, Song song) {
        Playlist p = getPlaylist(playlistName);
        if (p == null) {
            throw new IllegalArgumentException("Nie znaleziono playlisty o nazwie: " + playlistName);
        }
        p.addSong(song);
    }

    /**
    * Removes a song from a specified playlist.
    *
    * @param playlistName the name of the playlist
    * @param song         the song to remove
    * @throws IllegalArgumentException if the playlist does not exist
    */

    public void removeSongFromPlaylist(String playlistName, Song song) {
        Playlist p = getPlaylist(playlistName);
        if (p == null) {
            throw new IllegalArgumentException("Nie znaleziono playlisty o nazwie: " + playlistName);
        }
        p.removeSong(song.getTitle(), song.getArtist());
    }

    /**
    * Saves all songs and playlists to a text file.
    *
    * @param filename the name of the file used for saving
    * @throws IOException              if the file cannot be written
    * @throws IllegalArgumentException if filename is empty
    */

    public void saveToFile(String filename) throws IOException {
        if (filename == null || filename.trim().isEmpty())
            throw new IllegalArgumentException("Nazwa pliku nie moze byc pusta!");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("[PIOSENKI]");
            writer.newLine();
            for (Song s : songs) {
                writer.write(s.getTitle() + ";" + s.getArtist() + ";" + s.getYear() + ";" + s.isFavorite());
                writer.newLine();
            }

            writer.write("[PLAYLISTY]");
            writer.newLine();
            for (Playlist p : playlists) {
                writer.write("#" + p.getName());
                writer.newLine();
                for (Song s : p.getSongs()) {
                    writer.write(s.getTitle() + ";" + s.getArtist());
                    writer.newLine();
                }
            }
        }
    }

    /**
    * Loads all songs and playlists from a text file.
    *
    * @param filename the file to read data from
    * @throws IOException              if the file cannot be read or format is invalid
    * @throws IllegalArgumentException if filename is empty
    */

    public void loadFromFile(String filename) throws IOException {
        if (filename == null || filename.trim().isEmpty())
            throw new IllegalArgumentException("Nazwa pliku nie moze byc pusta!");

        songs.clear();
        playlists.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean readingSongs = false;
            boolean readingPlaylists = false;
            Playlist current = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.equalsIgnoreCase("[PIOSENKI]")) {
                    readingSongs = true;
                    readingPlaylists = false;
                    continue;
                } else if (line.equalsIgnoreCase("[PLAYLISTY]")) {
                    readingSongs = false;
                    readingPlaylists = true;
                    continue;
                }

                if (readingSongs) {
                    String[] parts = line.split(";");
                    if (parts.length >= 4) {
                        try {
                            String title = parts[0].trim();
                            String artist = parts[1].trim();
                            int year = Integer.parseInt(parts[2].trim());
                            boolean fav = Boolean.parseBoolean(parts[3].trim());
                            Song song = new Song(title, artist, year);
                            song.setFavorite(fav);
                            songs.add(song);
                        } catch (NumberFormatException ignored) {}
                    }
                } else if (readingPlaylists) {
                    if (line.startsWith("#")) {
                        current = new Playlist(line.substring(1).trim());
                        playlists.add(current);
                    } else if (current != null) {
                        String[] parts = line.split(";");
                        if (parts.length >= 2) {
                            String title = parts[0].trim();
                            String artist = parts[1].trim();
                            Song found = findSong(title, artist);
                            if (found != null) {
                                current.addSong(found);
                            } else {
                                JOptionPane.showMessageDialog(null,
                                        "Piosenka nie zostala znaleziona w bibliotece:\n" + title + " - " + artist,
                                        "Brakujaca piosenka", JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new IOException("Nie znaleziono pliku: " + filename);
        } catch (IOException e) {
            throw new IOException("Blad wczytywania danych z pliku: " + filename);
        }
    }

    /**
     * Returns a list of songs that match the provided predicate.
     * This demonstrates the use of a custom functional interface with lambdas.
     *
     * @param predicate the condition to test songs against
     * @return a new list containing only songs that satisfy the predicate
     * @throws IllegalArgumentException if predicate is null
     */
    public List<Song> filterSongs(SongPredicate predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException("Predicate cannot be null");
        }
        return songs.stream()
                .filter(predicate::test)
                .collect(Collectors.toList());
    }
    
    /**
     * Finds a song in the library by its title and artist.
     */
    private Song findSong(String title, String artist) {
        return songs.stream()
                .filter(s -> s.getTitle().equalsIgnoreCase(title)
                        && s.getArtist().equalsIgnoreCase(artist))
                .findFirst()
                .orElse(null);
    }
}

