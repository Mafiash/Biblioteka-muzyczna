/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.lab2.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@Table(
        name = "SONG",
        uniqueConstraints = @UniqueConstraint(name = "UK_SONG_TITLE_ARTIST_YEAR", columnNames = {"TITLE", "ARTIST", "RELEASE_YEAR"}),
        indexes = {
                @Index(name = "IDX_SONG_TITLE", columnList = "TITLE"),
                @Index(name = "IDX_SONG_ARTIST", columnList = "ARTIST"),
                @Index(name = "IDX_SONG_YEAR", columnList = "RELEASE_YEAR")
        }
)
public class SongEntity implements Serializable { 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "ARTIST", nullable = false, length = 200)
    private String artist;

    @Column(name = "RELEASE_YEAR", nullable = false)
    private int year;

    @Column(name = "FAVORITE", nullable = false)
    private boolean favorite = false;

    public SongEntity() {}

    public SongEntity(String title, String artist, int year) {
        this.title = title;
        this.artist = artist;
        this.year = year;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public int getYear() { return year; }
    public boolean isFavorite() { return favorite; }

    public void setTitle(String title) { this.title = title; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setYear(int year) { this.year = year; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
}