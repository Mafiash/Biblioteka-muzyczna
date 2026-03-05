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
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "PLAYLIST",
        indexes = {
                @Index(name = "IDX_PLAYLIST_NAME", columnList = "NAME", unique = true)
        }
)
public class PlaylistEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false, unique = true, length = 200)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "PLAYLIST_SONG",
            joinColumns = @JoinColumn(name = "PLAYLIST_ID"),
            inverseJoinColumns = @JoinColumn(name = "SONG_ID"),
            uniqueConstraints = {
                    @UniqueConstraint(name = "UK_PLAYLIST_SONG", columnNames = {"PLAYLIST_ID", "SONG_ID"})
            },
            indexes = {
                    @Index(name = "IDX_PLAYLIST_SONG_PL", columnList = "PLAYLIST_ID"),
                    @Index(name = "IDX_PLAYLIST_SONG_SONG", columnList = "SONG_ID")
            }
    )
    private Set<SongEntity> songs = new HashSet<>();

    public PlaylistEntity() {}

    public PlaylistEntity(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Set<SongEntity> getSongs() { return songs; }

    public void setName(String name) { this.name = name; }

    public void addSong(SongEntity s) { this.songs.add(s); }
    public void removeSong(SongEntity s) { this.songs.remove(s); }
}
