package com.mycompany.lab2.resources;

import com.mycompany.lab2.model.MusicLibrary;
import com.mycompany.lab2.model.Song;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Application bootstrap that creates a single MusicLibrary instance for the whole app lifecycle.
 * No static fields are used; the instance lives in ServletContext under key APP_LIBRARY_KEY.
 *
 * @author Mateusz Smuda
 * @version 1.0
 */
@WebListener
public class AppBootstrap implements ServletContextListener {

      /** ServletContext attribute key under which the shared library is stored. */
    public static final String APP_LIBRARY_KEY = "app.library";

    /**
     * Creates and stores a shared {@link MusicLibrary} instance in the
     * application {@link ServletContext} when the app starts.
     *
     * @param sce the servlet context event
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        MusicLibrary library = new MusicLibrary();
        try {
            Song s1 = new Song("Numb", "Linkin Park", 2003);
            s1.setFavorite(true);
            Song s2 = new Song("Believer", "Imagine Dragons", 2017);
            Song s3 = new Song("Skyfall", "Adele", 2012);
            s3.setFavorite(true);
            library.addSong(s1);
            library.addSong(s2);
            library.addSong(s3);
            library.addPlaylist("Favorites");
        } catch (Exception ignored) {}
        ctx.setAttribute(APP_LIBRARY_KEY, library);
    }

    /**
     * Removes the shared {@link MusicLibrary} reference from the context on shutdown.
     *
     * @param sce the servlet context event
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        ctx.removeAttribute(APP_LIBRARY_KEY);
    }
}
