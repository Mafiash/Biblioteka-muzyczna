/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.lab2.persistence.mapper;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.io.Serializable;
import com.mycompany.lab2.model.Song;
import com.mycompany.lab2.persistence.SongEntity;

/**
 * Simple mapper between domain model {@link Song} and JPA {@link SongEntity}.
 * Save/read only; no persistence logic here.
 */
public final class SongMapper {
    private SongMapper() {}

    public static SongEntity toEntity(Song model) {
        if (model == null) return null;
        SongEntity e = new SongEntity(model.getTitle(), model.getArtist(), model.getYear());
        e.setFavorite(model.isFavorite());
        return e;
        }

    public static Song fromEntity(SongEntity entity) {
        if (entity == null) return null;
        Song s = new Song(entity.getTitle(), entity.getArtist(), entity.getYear());
        s.setFavorite(entity.isFavorite());
        return s;
    }
}

