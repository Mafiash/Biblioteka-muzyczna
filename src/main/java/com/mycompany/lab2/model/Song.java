/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.lab2.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a single song with its title, artist, and release year.
 * 
 * Includes validation using standard Java exceptions.
 * 
 * @author Mateusz Smuda
 * @version 2.1
 */
public final class Song {

    /** The title of the song. */
    @Getter
    private String title;

    /** The artist performing the song. */
    @Getter
    private String artist;

    /** The year the song was released. */
    @Getter
    private int year;

    /** Favourite status of the song. */
    @Getter @Setter
    private boolean favorite;

    /**
     * Constructs a new {@code Song} object with validation.
     *
     * @param title  the title of the song (cannot be null or empty)
     * @param artist the artist of the song (cannot be null or empty)
     * @param year   the release year (must be between 1800 and current year)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public Song(String title, String artist, int year) {
        setTitle(title);
        setArtist(artist);
        setYear(year);
        this.favorite = false;
    }

    /**
     * Updates the song title with validation.
     * @param title new title (not null/empty)
     */
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Tytul nie moze byc pusty!");
        }
        this.title = title.trim();
    }

    /**
     * Updates the song artist with validation.
     * @param artist new artist (not null/empty)
     */
    public void setArtist(String artist) {
        if (artist == null || artist.trim().isEmpty()) {
            throw new IllegalArgumentException("Wykonawca nie moze byc pusty!");
        }
        this.artist = artist.trim();
    }

    /**
     * Updates the release year with validation.
     * @param year year in range 1800..current
     */
    public void setYear(int year) {
        if (year < 1800 || year > java.time.Year.now().getValue()) {
            throw new IllegalArgumentException("Niepoprawny rok wydania od 1800 do biezacego roku: " + year);
        }
        this.year = year;
    }

    /**
     * Returns a string representation of the song in the format:
     * "Title - Artist Year".
     *
     * @return a formatted string describing the song
     */
    @Override
    public String toString() {
        return title + " - " + artist + " " + year;
    }
}















