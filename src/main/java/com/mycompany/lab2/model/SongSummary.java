/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.lab2.model;

/**
 * A simple record used to show summaary of music library
 *
 * 
 * @author Mateusz Smuda
 * @version 1.0
 */
public record SongSummary(String title, String artist, boolean favorite) {

    public SongSummary(Song song) {
        this(song.getTitle(), song.getArtist(), song.isFavorite());
    }

    /** Returns a short formatted text version. */
    @Override
    public String toString() {
        return title + " — " + artist;
    }
}

