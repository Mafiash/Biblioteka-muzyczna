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
import com.mycompany.lab2.model.Playlist;
import com.mycompany.lab2.model.Song;
import com.mycompany.lab2.persistence.PlaylistEntity;
import com.mycompany.lab2.persistence.SongEntity;

/**
 * Mapper for {@link Playlist} and {@link PlaylistEntity}.
 * Only for save/read scenarios in simple demos.
 */
public final class PlaylistMapper {
    private PlaylistMapper() {}

    public static PlaylistEntity toEntity(Playlist model) {
        if (model == null) return null;
        PlaylistEntity e = new PlaylistEntity(model.getName());
        if (model.getSongs() != null) {
            for (Song s : model.getSongs()) {
                SongEntity se = SongMapper.toEntity(s);
                if (se != null) e.getSongs().add(se);
            }
        }
        return e;
    }

    public static Playlist fromEntity(PlaylistEntity entity) {
        if (entity == null) return null;
        Playlist p = new Playlist(entity.getName());
        if (entity.getSongs() != null) {
            for (SongEntity se : entity.getSongs()) {
                Song s = SongMapper.fromEntity(se);
                if (s != null) p.addSong(s);
            }
        }
        return p;
    }
}

