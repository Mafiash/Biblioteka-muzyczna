package com.mycompany.lab2.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;

public class PlaylistRepository {
    private final EntityManagerFactory emf;

    public PlaylistRepository() {
        this.emf = JpaUtil.getEmf();
    }

    public PlaylistEntity save(PlaylistEntity entity) {
        if (entity == null) return null;
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (entity.getId() == null) {
                em.persist(entity);
            } else {
                entity = em.merge(entity);
            }
            em.getTransaction().commit();
            return entity;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    public PlaylistEntity update(PlaylistEntity entity) {
        return save(entity);
    }

    public List<PlaylistEntity> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select p from PlaylistEntity p order by p.name", PlaylistEntity.class).getResultList();
        } finally {
            em.close();
        }
    }

    public PlaylistEntity findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(PlaylistEntity.class, id);
        } finally {
            em.close();
        }
    }

    public PlaylistEntity findByName(String name) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select p from PlaylistEntity p where lower(p.name) = :n", PlaylistEntity.class)
                    .setParameter("n", name == null ? null : name.toLowerCase())
                    .setMaxResults(1)
                    .getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

    public List<SongEntity> listSongs(Long playlistId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "select s from PlaylistEntity p join p.songs s where p.id = :id order by s.title",
                    SongEntity.class)
                    .setParameter("id", playlistId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select count(p) from PlaylistEntity p", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    public boolean deleteByName(String name) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            PlaylistEntity p = em.createQuery("select p from PlaylistEntity p where lower(p.name)=:n", PlaylistEntity.class)
                    .setParameter("n", name == null ? null : name.toLowerCase())
                    .setMaxResults(1)
                    .getResultStream().findFirst().orElse(null);
            
            if (p != null) {
                em.remove(p);
                em.getTransaction().commit();
                return true;
            } else {
                em.getTransaction().rollback();
                return false;
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }
    
    public void delete(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            PlaylistEntity p = em.find(PlaylistEntity.class, id);
            if (p != null) {
                em.remove(p);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void addSongToPlaylist(Long playlistId, Long songId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            PlaylistEntity p = em.find(PlaylistEntity.class, playlistId);
            SongEntity s = em.find(SongEntity.class, songId);
            if (p != null && s != null) {
                p.getSongs().add(s);
                em.merge(p);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}