/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.lab2.model;

/**
 * A simple functional interface used to test whether a {@link Song}
 * matches a condition. Designed for use with lambda expressions.
 * 
 * @author Mateusz Smuda
 * @version 1.0
 */
@FunctionalInterface
public interface SongPredicate {
    /**
     * Evaluates this predicate on the given song.
     *
     * @param song the song to test
     * @return {@code true} if the song matches the condition; {@code false} otherwise
     */
    boolean test(Song song);
}
