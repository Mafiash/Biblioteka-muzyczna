package com.mycompany.lab2.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;

public class SongRepository {
    private final EntityManagerFactory emf;

    public SongRepository() {
        this.emf = JpaUtil.getEmf();
    }

    public SongEntity save(SongEntity entity) {
        if (entity == null) return null;
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(entity);
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

    public SongEntity update(SongEntity entity) {
        if (entity == null) return null;
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            SongEntity merged = em.merge(entity);
            em.getTransaction().commit();
            return merged;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    public boolean delete(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            SongEntity managed = em.find(SongEntity.class, id);
            if (managed != null) {
                em.remove(managed);
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

    public List<SongEntity> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select s from SongEntity s", SongEntity.class).getResultList();
        } finally {
            em.close();
        }
    }

    public SongEntity findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(SongEntity.class, id);
        } finally {
            em.close();
        }
    }

    public SongEntity findByTitleArtistYear(String title, String artist, int year) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "select s from SongEntity s where lower(s.title)=:t and lower(s.artist)=:a and s.year=:y",
                    SongEntity.class)
                    .setParameter("t", title == null ? null : title.toLowerCase())
                    .setParameter("a", artist == null ? null : artist.toLowerCase())
                    .setParameter("y", year)
                    .setMaxResults(1)
                    .getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

    public List<SongEntity> findFavorites() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select s from SongEntity s where s.favorite = true order by s.title", SongEntity.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<SongEntity> searchByTitleOrArtist(String text) {
        EntityManager em = emf.createEntityManager();
        try {
            String q = text == null ? "" : text.toLowerCase();
            return em.createQuery(
                    "select s from SongEntity s where lower(s.title) like :q or lower(s.artist) like :q order by s.title",
                    SongEntity.class)
                    .setParameter("q", "%" + q + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select count(s) from SongEntity s", Long.class).getSingleResult();
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