/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.lab2.model.services;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import jakarta.servlet.http.Cookie;
import com.mycompany.lab2.model.MusicLibrary;
import com.mycompany.lab2.resources.AppBootstrap;


/**
 * Servlet responsible for presenting the activity history stored in the shared
 * {@link MusicLibrary}. It reads the library instance from the application
 * context, renders a simple HTML view, and shows snapshot statistics.
 *
 * This servlet also demonstrates basic cookie usage for a visitor identifier
 * and the last opened view name.
 *
 * @author Mateusz Smuda
 * @version 1.0
 */
@WebServlet(name = "HistoryServlet", urlPatterns = {"/history"})
public class HistoryServlet extends HttpServlet {

    /** Reference to the shared application {@link MusicLibrary}. */
    private MusicLibrary library;

    /**
     * Initializes the servlet by obtaining the shared {@link MusicLibrary}
     * instance from the {@code ServletContext}.
     *
     * @throws IllegalStateException if the library is not available in context
     */
    @Override
    public void init() {
        Object lib = getServletContext().getAttribute(AppBootstrap.APP_LIBRARY_KEY);
        if (lib instanceof MusicLibrary) {
            this.library = (MusicLibrary) lib;
        } else {
            throw new IllegalStateException("Brak współdzielonej biblioteki w kontekście aplikacji");
        }
    }

    /**
     * Central request handler used by both {@link #doGet(HttpServletRequest, HttpServletResponse)}
     * and {@link #doPost(HttpServletRequest, HttpServletResponse)}.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws IOException if an I/O error occurs while writing the response
     */
    private void process(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ensureVisitorCookie(req, resp);
        setLastViewCookie(resp, "history");
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        header(req, out, "Historia operacji");

        List<String> log = library.getActivityLog();
        if (log.isEmpty()) {
            out.println("<p>Brak wpisów w historii.</p>");
        } else {
            out.println("<ul>");
            for (String entry : log) {
                out.println("<li>" + escape(entry) + "</li>");
            }
            out.println("</ul>");
        }

        out.println("<h3>Statystyki</h3>");
        out.println("<p>Łącznie piosenek: " + library.getSongs().size() + ", Ilość playlist: " + library.getPlaylists().size() + "</p>");

        footer(out);
    }

    /**
     * Handles HTTP GET requests.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws IOException if an I/O error occurs while writing the response
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        process(req, resp);
    }

    /**
     * Handles HTTP POST requests.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws IOException if an I/O error occurs while writing the response
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        process(req, resp);
    }

    /**
     * Ensures there is a cookie {@code visitorId}; if absent a new one is created.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response used to add the cookie
     */
    private void ensureVisitorCookie(HttpServletRequest req, HttpServletResponse resp) {
        String vid = getCookieValue(req, "visitorId");
        if (vid == null || vid.isBlank()) {
            String newId = java.util.UUID.randomUUID().toString().replace("-", "");
            Cookie c = new Cookie("visitorId", newId);
            c.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
            c.setMaxAge(60 * 60 * 24 * 365);
            resp.addCookie(c);
        }
    }

    /**
     * Stores the last opened view name in the {@code lastView} cookie.
     *
     * @param resp the HTTP response used to add the cookie
     * @param view the view identifier to store
     */
    private void setLastViewCookie(HttpServletResponse resp, String view) {
        String safe = view == null ? "" : view.replaceAll("[^A-Za-z0-9_-]", "");
        Cookie c = new Cookie("lastView", safe);
        c.setPath("/");
        c.setMaxAge(60 * 60 * 24 * 30);
        resp.addCookie(c);
    }

    /**
     * Reads a cookie value by its name.
     *
     * @param req  the HTTP request
     * @param name cookie name
     * @return the cookie value or {@code null} if not present
     */
    private String getCookieValue(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    /**
     * Writes standard HTML header with page title and basic navigation.
     *
     * @param req   the HTTP request (used to show cookie values)
     * @param out   the response writer
     * @param title page title
     */
    private void header(HttpServletRequest req, PrintWriter out, String title) {
        out.println("<html><head><meta charset=\"UTF-8\"><title>" + escape(title) + "</title></head><body>");
        out.println("<h1>" + escape(title) + "</h1>");
        String visitorId = getCookieValue(req, "visitorId");
        String lastView = getCookieValue(req, "lastView");
        out.println("<div style=\"font-size:0.9em;color:#555\">Twój identyfikator: " + escape(String.valueOf(visitorId)) +
                " | Ostatnio odwiedzono: " + escape(String.valueOf(lastView)) + "</div>");
        out.println("<nav><a href=\"music?action=all\">Wszystkie</a> | <a href=\"music?action=favorites\">Ulubione</a> | <a href=\"music?action=playlists\">Playlisty</a> | <a href=\"music?action=summary\">Podsumowanie</a> | <a href=\"history\">Historia</a></nav><hr>");
    }

    /**
     * Writes standard HTML footer.
     *
     * @param out the response writer
     */
    private void footer(PrintWriter out) {
        out.println("<hr><a href=\"index.html\">Powrót</a>");
        out.println("</body></html>");
    }

    /**
     * Escapes basic HTML-special characters in a string.
     *
     * @param s input string
     * @return escaped string safe for HTML
     */
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
