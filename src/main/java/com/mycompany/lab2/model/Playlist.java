/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.lab2.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;


/**
 * Represents a playlist that contains a collection of songs.
 *
 * Allows adding, removing, retrieving, and checking songs.
 * Includes validation and exception handling for model consistency.
 *
 * @author Mateusz Smuda
 * @version 3.0
 */
@ToString
public class Playlist {

    /** The name of the playlist. */
    @Getter
    private final String name;

    /** The list of songs in the playlist. */
    @Getter
    private final List<Song> songs = new ArrayList<>();


    /**
     * Constructs a new {@code Playlist} with the given name.
     *
     * @param name the name of the playlist
     * @throws IllegalArgumentException if the name is null or empty
     */
    public Playlist(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa playlisty nie moze byc pusta!");
        }
        this.name = name.trim();
    }

    /**
     * Adds a new song to the playlist.
     *
     * @param song the {@link Song} to be added
     * @throws IllegalArgumentException if song is null
     */
    public void addSong(Song song) {
        if (song == null) {
            throw new IllegalArgumentException("Nie mozna dodac pustej piosenki!");
        }
        songs.add(song);
    }

    /**
     * Removes a song from the playlist by title and artist.
     *
     * @param title  song title
     * @param artist song artist
     * @return {@code true} if removed, {@code false} otherwise
     * @throws IllegalArgumentException if title or artist are empty
     */
    public boolean removeSong(String title, String artist) {
        if (title == null || title.trim().isEmpty() ||
            artist == null || artist.trim().isEmpty()) {
            throw new IllegalArgumentException("Tytul i wykonawca nie moga byc puste!");
        }

        return songs.removeIf(s -> s.getTitle().equalsIgnoreCase(title.trim())
                && s.getArtist().equalsIgnoreCase(artist.trim()));
    }

    /**
     * Checks whether the playlist is empty.
     *
     * @return {@code true} if the playlist contains no songs
     */
    public boolean isEmpty() {
        return songs.isEmpty();
    }

    /**
     * Clears all songs from the playlist.
     */
    public void clear() {
        songs.clear();
    }

}




