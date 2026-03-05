/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.lab2.model;

/**
 * Exception thrown when attempting to add a duplicate song to the music library.
 *
 * This exception is used in the {@link MusicLibrary} class to prevent adding
 * multiple songs with the same title and artist.
 *
 * @author Mateusz Smuda
 * @version 1.0
 */
public class DuplicateSongException extends Exception {
    /**
     * Constructs a new {@code DuplicateSongException} with no detail message.
     */
    public DuplicateSongException() {
        super();
    }
}
