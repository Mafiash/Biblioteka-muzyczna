package com.mycompany.lab2.model.services;

import com.mycompany.lab2.model.DuplicateSongException;
import com.mycompany.lab2.model.MusicLibrary;
import com.mycompany.lab2.model.Playlist;
import com.mycompany.lab2.model.Song;
import com.mycompany.lab2.model.SongPredicate;
import com.mycompany.lab2.model.SongSummary;
import com.mycompany.lab2.persistence.PlaylistEntity;
import com.mycompany.lab2.persistence.PlaylistRepository;
import com.mycompany.lab2.persistence.SongEntity;
import com.mycompany.lab2.persistence.SongRepository;
import com.mycompany.lab2.persistence.mapper.PlaylistMapper;
import com.mycompany.lab2.persistence.mapper.SongMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import com.mycompany.lab2.resources.AppBootstrap;
import java.util.ArrayList;

/**
 * Main servlet that displays and manages songs and playlists using JPA Repositories.
 * Meets requirements for explicit UI information about data sources (Cookies vs DB).
 *
 * @author Mateusz Smuda
 * @version 1.1
 */
@WebServlet(name = "NewServlet", urlPatterns = {"/music"})
public class NewServlet extends HttpServlet {

    /** Shared application {@link MusicLibrary} instance (used only for history logs). */
    private MusicLibrary library;

    private SongRepository songRepo;
    private PlaylistRepository playlistRepo;

    /**
     * Initializes the servlet by obtaining the shared {@link MusicLibrary}
     * for logs and initializing JPA repositories.
     */
    @Override
    public void init() {
        Object lib = getServletContext().getAttribute(AppBootstrap.APP_LIBRARY_KEY);
        if (lib instanceof MusicLibrary) {
            this.library = (MusicLibrary) lib;
        } else {
            this.library = new MusicLibrary();
        }
        this.songRepo = new SongRepository();
        this.playlistRepo = new PlaylistRepository();
        try {
            if (songRepo.count() == 0) {
                seedDatabase();
            }
        } catch (Exception ignored) {}
    }

    private void seedDatabase() {
        SongEntity s1 = new SongEntity("Numb", "Linkin Park", 2003);
        s1.setFavorite(true);
        songRepo.save(s1);
        SongEntity s2 = new SongEntity("Believer", "Imagine Dragons", 2017);
        songRepo.save(s2);
        PlaylistEntity fav = new PlaylistEntity("Favorites");
        fav.getSongs().add(s1);
        playlistRepo.save(fav);
    }

    /**
     * Central request dispatcher for both GET and POST methods. Determines the
     * requested action or operation and renders the appropriate HTML view or
     * performs the requested update.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws IOException if an I/O error occurs while writing the response
     */
    private void process(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        if ("GET".equalsIgnoreCase(req.getMethod()) && req.getParameter("action") == null) {
            resp.sendRedirect("music?action=all");
            return;
        }

        try {
            if ("GET".equalsIgnoreCase(req.getMethod())) {
                String action = req.getParameter("action");
                if (action == null) action = "all";
                switch (action) {
                    case "favorites" -> { setLastViewCookie(resp, "favorites"); showFavorites(req, out); }
                    case "playlists" -> { setLastViewCookie(resp, "playlists"); showPlaylists(req, out); }
                    case "viewPlaylist" -> { setLastViewCookie(resp, "viewPlaylist"); showPlaylistView(req, out, req.getParameter("name")); }
                    case "summary" -> { setLastViewCookie(resp, "summary"); showSummary(req, out); }
                    case "editSong" -> { setLastViewCookie(resp, "editSong"); showEditSong(req, out, req.getParameter("title"), req.getParameter("artist")); }
                    case "confirmRemoveSong" -> { setLastViewCookie(resp, "confirmRemoveSong"); showConfirmRemoveSong(req, out, req.getParameter("title"), req.getParameter("artist")); }
                    case "confirmRemoveFromPlaylist" -> { setLastViewCookie(resp, "confirmRemoveFromPlaylist"); showConfirmRemoveFromPlaylist(req, out, req.getParameter("playlistName"), req.getParameter("title"), req.getParameter("artist")); }
                    case "confirmRemovePlaylist" -> { setLastViewCookie(resp, "confirmRemovePlaylist"); showConfirmRemovePlaylist(req, out, req.getParameter("playlistName")); }
                    case "all" -> { setLastViewCookie(resp, "all"); showAllSongs(req, out); }
                    default -> { setLastViewCookie(resp, "all"); showAllSongs(req, out); }
                }
            } else { 
                String op = req.getParameter("op");
                if (op == null || op.isBlank()) op = "addSong";
                switch (op) {
                    case "addSong" -> handleAddSong(req, out);
                    case "removeSong" -> handleRemoveSong(req, out);
                    case "createPlaylist" -> handleCreatePlaylist(req, out);
                    case "removePlaylist" -> handleRemovePlaylist(req, out);
                    case "addToPlaylist" -> handleAddToPlaylist(req, out);
                    case "removeFromPlaylist" -> handleRemoveFromPlaylist(req, out);
                    case "toggleFavorite" -> handleToggleFavorite(req, out);
                    case "updateSong" -> handleUpdateSong(req, out);
                    default -> {
                        out.println("<html><body>");
                        out.println("<h2>Nieznana operacja: " + escape(op) + "</h2>");
                        out.println(linkBack());
                        out.println("</body></html>");
                    }
                }
            }
        } catch (DuplicateSongException e) {
            out.println("<html><body>");
            out.println("<h2>Duplikat piosenki: " + escape(e.getMessage() == null ? "" : e.getMessage()) + "</h2>");
            out.println(linkBack());
            out.println("</body></html>");
        } catch (NumberFormatException e) {
            out.println("<html><body>");
            out.println("<h2>Błędny format liczby: " + escape(e.getMessage()) + "</h2>");
            out.println(linkBack());
            out.println("</body></html>");
        } catch (Exception e) {
            out.println("<html><body>");
            out.println("<h2>Błąd: " + escape(e.getMessage()) + "</h2>");
            out.println(linkBack());
            out.println("</body></html>");
            e.printStackTrace();
        }
    }

    /**
     * Handles HTTP GET requests. Ensures visitor cookie exists and dispatches to {@link #process(HttpServletRequest, HttpServletResponse)}.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws IOException if an I/O error occurs while writing the response
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ensureVisitorCookie(req, resp);
        process(req, resp);
    }

    /**
     * Handles HTTP POST requests. Ensures visitor cookie exists and dispatches to {@link #process(HttpServletRequest, HttpServletResponse)}.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws IOException if an I/O error occurs while writing the response
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ensureVisitorCookie(req, resp);
        process(req, resp);
    }
    
    /**
     * Ensures there is a {@code visitorId} cookie; creates a new one if absent.
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
     * Non-alphanumeric characters are stripped for safety.
     *
     * @param resp the HTTP response used to add the cookie
     * @param view the view identifier to store
     */
    private void setLastViewCookie(HttpServletResponse resp, String view) {
        if (view == null) return;
        String safe = view.replaceAll("[^A-Za-z0-9_-]", "");
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
     * Renders a confirmation page for removing a song from the library.
     *
     * @param req    the HTTP request
     * @param out    the response writer
     * @param title  song title to remove
     * @param artist song artist to remove
     */
    private void showConfirmRemoveSong(HttpServletRequest req, PrintWriter out, String title, String artist) {
        header(req, out, "Potwierdź usunięcie piosenki");
        if (title == null || artist == null || title.isBlank() || artist.isBlank()) {
            out.println("<p>Brak wymaganych parametrów.</p>");
            footer(out);
            return;
        }
        out.println("<p>Czy na pewno usunąć rekord z bazy danych: <strong>" + escape(title) + " — " + escape(artist) + "</strong>?</p>");
        out.println("<form method=\"post\" action=\"music\">" +
                "<input type=\"hidden\" name=\"op\" value=\"removeSong\">" +
                "<input type=\"hidden\" name=\"title\" value=\"" + escape(title) + "\">" +
                "<input type=\"hidden\" name=\"artist\" value=\"" + escape(artist) + "\">" +
                "<button type=\"submit\">Potwierdź usunięcie</button></form> " +
                "<a href=\"music?action=all\">Anuluj</a>");
        footer(out);
    }

    /**
     * Renders a confirmation page for removing a song from a specific playlist.
     *
     * @param req          the HTTP request
     * @param out          the response writer
     * @param playlistName name of the playlist
     * @param title        song title to remove
     * @param artist       song artist to remove
     */
    private void showConfirmRemoveFromPlaylist(HttpServletRequest req, PrintWriter out, String playlistName, String title, String artist) {
        header(req, out, "Potwierdź usunięcie z playlisty");
        if (playlistName == null || playlistName.isBlank() || title == null || artist == null) {
            out.println("<p>Brak wymaganych parametrów.</p>");
            footer(out);
            return;
        }
        out.println("<p>Czy usunąć powiązanie piosenki <strong>" + escape(title) + "</strong> z playlistą <strong>" + escape(playlistName) + "</strong>?</p>");
        out.println("<form method=\"post\" action=\"music\">" +
                "<input type=\"hidden\" name=\"op\" value=\"removeFromPlaylist\">" +
                "<input type=\"hidden\" name=\"playlistName\" value=\"" + escape(playlistName) + "\">" +
                "<input type=\"hidden\" name=\"title\" value=\"" + escape(title) + "\">" +
                "<input type=\"hidden\" name=\"artist\" value=\"" + escape(artist) + "\">" +
                "<button type=\"submit\">Potwierdź</button></form> " +
                "<a href=\"music?action=viewPlaylist&name=" + url(playlistName) + "\">Anuluj</a>");
        footer(out);
    }

    /**
     * Renders a confirmation page for removing the specified playlist.
     *
     * @param req          the HTTP request
     * @param out          the response writer
     * @param playlistName name of the playlist to remove
     */
    private void showConfirmRemovePlaylist(HttpServletRequest req, PrintWriter out, String playlistName) {
        header(req, out, "Potwierdź usunięcie playlisty");
        if (playlistName == null || playlistName.isBlank()) {
            out.println("<p>Brak wymaganych parametrów.</p>");
            footer(out);
            return;
        }
        out.println("<p>Czy usunąć playlistę <strong>" + escape(playlistName) + "</strong></p>");
        out.println("<form method=\"post\" action=\"music\">" +
                "<input type=\"hidden\" name=\"op\" value=\"removePlaylist\">" +
                "<input type=\"hidden\" name=\"playlistName\" value=\"" + escape(playlistName) + "\">" +
                "<button type=\"submit\">Potwierdź usunięcie</button></form> " +
                "<a href=\"music?action=playlists\">Anuluj</a>");
        footer(out);
    }

    /**
     * Renders the Favorites view listing all songs marked as favorite.
     *
     * @param req the HTTP request
     * @param out the response writer
     */
    private void showFavorites(HttpServletRequest req, PrintWriter out) {
        List<SongEntity> entities = songRepo.findFavorites();
        List<Song> favorites = entities.stream().map(SongMapper::fromEntity).collect(Collectors.toList());

        header(req, out, "Ulubione piosenki");
        if (favorites.isEmpty()) {
            out.println("<p>Brak ulubionych piosenek w bazie.</p>");
        } else {
            out.println("<ul>");
            for (Song s : favorites) {
                out.println("<li>" + renderSongLine(s) + buttonsForSongNoRemove(s) + "</li>");
            }
            out.println("</ul>");
        }
        out.println("</body></html>");
    }

    /**
     * Renders the main view with all songs and a form to add a new song.
     *
     * @param req the HTTP request
     * @param out the response writer
     */
    private void showAllSongs(HttpServletRequest req, PrintWriter out) {
        List<SongEntity> entities = songRepo.findAll();
        List<Song> songs = entities.stream().map(SongMapper::fromEntity).collect(Collectors.toList());

        header(req, out, "Biblioteka muzyczna");
        out.println("""
        <h3>Dodaj piosenkę</h3>
        <div style='background:#e9ecef; padding:10px; margin-bottom:10px; border-left: 4px solid #007bff;'>
            <strong>Instrukcja:</strong> Wypełnij poniższy formularz, aby utworzyć nowy rekord w tabeli <code>SONG</code>.
        </div>
        <form method="post" action="music">
            <input type="hidden" name="op" value="addSong">
            <label>Tytuł utworu: <input name="title" placeholder="np. Bohemian Rhapsody" required></label><br/>
            <label>Wykonawca: <input name="artist" placeholder="np. Queen" required></label><br/>
            <label>Rok wydania: <input type="number" name="year" placeholder="np. 1975" required></label><br/>
            <button type="submit" style='margin-top:5px;'>Zapisz w Bazie Danych</button>
        </form>
        <hr>
        """);

        if (songs.isEmpty()) {
            out.println("<p>Brak piosenek w bazie danych.</p>");
        } else {
            out.println("<ul>");
            for (Song s : songs) {
                out.println("<li>" +
                        renderSongLine(s) +
                        buttonsForSong(s) +
                        addToPlaylistInlineForm(s) +
                        "</li>");
            }
            out.println("</ul>");
        }
        footer(out);
    }

     /**
     * Renders the Playlists view with a list of existing playlists and a form
     * to create a new playlist.
     *
     * @param req the HTTP request
     * @param out the response writer
     */
    private void showPlaylists(HttpServletRequest req, PrintWriter out) {
        header(req, out, "Playlisty");
        List<PlaylistEntity> pls = playlistRepo.findAll();
        
        if (pls.isEmpty()) {
            out.println("<p>Brak playlist w bazie.</p>");
        } else {
            out.println("<ul>");
            for (PlaylistEntity p : pls) {
                out.println("<li><a href=\"music?action=viewPlaylist&name=" + url(p.getName()) + "\">" + escape(p.getName()) + "</a> (" + p.getSongs().size() + ")" + removePlaylistConfirmLink(p.getName()) + "</li>");
            }
            out.println("</ul>");
        }
        
        out.println("<h3>Utwórz playlistę</h3>");
        out.println("<p>Podaj unikalną nazwę, aby utworzyć nowy wpis w tabeli <code>PLAYLIST</code>.</p>");
        out.println("<form method=\"post\" action=\"music\">" +
                "<input type=\"hidden\" name=\"op\" value=\"createPlaylist\">" +
                "<label>Nazwa playlisty: <input name=\"playlistName\" required></label> " +
                "<button type=\"submit\">Utwórz</button>" +
                "</form>");

        footer(out);
    }

    /**
     * Renders a single playlist view showing all songs in the given playlist.
     *
     * @param req  the HTTP request
     * @param out  the response writer
     * @param name playlist name to display
     */
    private void showPlaylistView(HttpServletRequest req, PrintWriter out, String name) {
        header(req, out, "Playlista: " + escape(Objects.toString(name, "(brak)")));
        if (name == null || name.isBlank()) {
            out.println("<p>Nie podano nazwy playlisty.</p>");
            footer(out);
            return;
        }
        PlaylistEntity p = playlistRepo.findByName(name);
        if (p == null) {
            out.println("<p>Nie znaleziono playlisty: " + escape(name) + "</p>");
            footer(out);
            return;
        }
        if (p.getSongs().isEmpty()) {
            out.println("<p>Brak piosenek powiązanych z tą playlistą </p>");
        } else {
            out.println("<ul>");
            for (SongEntity se : p.getSongs()) {
                Song s = SongMapper.fromEntity(se);
                out.println("<li>" + renderSongLine(s) + buttonsForSongNoRemove(s) + removeFromPlaylistConfirmLink(name, s) + "</li>");
            }
            out.println("</ul>");
        }
        footer(out);
    }

    /**
     * Renders a summary view with counts and a few computed aggregations.
     *
     * @param req the HTTP request
     * @param out the response writer
     */
    private void showSummary(HttpServletRequest req, PrintWriter out) {
        header(req, out, "Podsumowanie piosenek");
        List<SongSummary> summaries = songRepo.findAll().stream()
                .map(SongMapper::fromEntity)
                .map(SongSummary::new)
                .collect(Collectors.toList());

        if (summaries.isEmpty()) {
            out.println("<p>Brak danych.</p>");
        } else {
            out.println("<ul>");
            for (SongSummary s : summaries) {
                out.println("<li>" + escape(s.toString()) + (s.favorite() ? " <strong>★</strong>" : "") + "</li>");
            }
            out.println("</ul>");
        }
        footer(out);
    }

    /**
     * Renders the edit form for a specific song identified by title and artist.
     *
     * @param req    the HTTP request
     * @param out    the response writer
     * @param title  current song title
     * @param artist current song artist
     */
    private void showEditSong(HttpServletRequest req, PrintWriter out, String title, String artist) {
        header(req, out, "Edytuj piosenkę");
        if (title == null || artist == null || title.isBlank() || artist.isBlank()) {
            out.println("<p>Brak wymaganych parametrów.</p>");
            footer(out);
            return;
        }
        SongEntity entity = findSongEntity(title, artist);
        
        if (entity == null) {
            out.println("<p>Nie znaleziono piosenki.</p>");
            footer(out);
            return;
        }
        
        out.println("""
            <h3>Edytuj piosenkę</h3>
            <p>Zmiana poniższych danych zaktualizuje tabelę <code>SONG</code></p>
            <form method=\"post\" action=\"music\">
                <input type=\"hidden\" name=\"op\" value=\"updateSong\"> 
        """);
        out.println("<input type=\"hidden\" name=\"oldTitle\" value=\"" + escape(entity.getTitle()) + "\">" );
        out.println("<input type=\"hidden\" name=\"oldArtist\" value=\"" + escape(entity.getArtist()) + "\">" );
        out.println("Tytuł: <input name=\"title\" value=\"" + escape(entity.getTitle()) + "\" required> ");
        out.println("Wykonawca: <input name=\"artist\" value=\"" + escape(entity.getArtist()) + "\" required> ");
        out.println("Rok: <input type=\"number\" name=\"year\" value=\"" + entity.getYear() + "\" required> ");
        out.println("<button type=\"submit\">Zapisz zmiany</button></form>");
        footer(out);
    }
    
    /**
     * Finds song entity matching the provided title and artist.
     *
     * @param title  song title to search
     * @param artist song artist to search
     */
    private SongEntity findSongEntity(String title, String artist) {
        return songRepo.searchByTitleOrArtist(title).stream()
                .filter(s -> s.getArtist().equalsIgnoreCase(artist))
                .findFirst().orElse(null);
    }

    /**
     * Finds exactly one song matching the provided title and artist.
     *
     * @param title  song title to search
     * @param artist song artist to search
     * @return the single matching {@link Song}
     * @throws IllegalArgumentException if no song or more than one song matches
     */
    private Song findSingleSongOrThrow(String title, String artist) {
        SongEntity se = findSongEntity(title, artist);
        if (se == null) {
            throw new IllegalArgumentException("Nie znaleziono piosenki.");
        }
        return SongMapper.fromEntity(se);
    }

    /**
     * Handles adding a new song to the library.
     *
     * @param req the HTTP request containing fields: {@code title}, {@code artist}, {@code year}
     * @param out the response writer for feedback HTML
     * @throws DuplicateSongException if a song with the same title and artist already exists
     * @throws NumberFormatException  if the provided {@code year} is not a valid number
     */
    private void handleAddSong(HttpServletRequest req, PrintWriter out) throws DuplicateSongException {
        String title = req.getParameter("title");
        String artist = req.getParameter("artist");
        String yearStr = req.getParameter("year");
        int year = Integer.parseInt(yearStr);
        
        if (songRepo.findByTitleArtistYear(title, artist, year) != null) {
            throw new DuplicateSongException();
        }

        Song newSong = new Song(title, artist, year);
        songRepo.save(SongMapper.toEntity(newSong));
        
        library.addLog(ts() + " Dodano piosenkę: " + newSong.getTitle());
        out.println("<html><body><h2>Pomyślnie dodano piosenkę!</h2>" + linkBack() + "</body></html>");
    }

    /**
     * Handles removing a song from the library identified by title and artist.
     *
     * @param req the HTTP request containing {@code title} and {@code artist}
     * @param out the response writer for feedback HTML
     * @throws IllegalArgumentException if the song does not exist
     */
    private void handleRemoveSong(HttpServletRequest req, PrintWriter out) {
        String title = req.getParameter("title");
        String artist = req.getParameter("artist");
        
        SongEntity entity = findSongEntity(title, artist);
        
        if (entity == null) {
            throw new IllegalArgumentException("Nie znaleziono piosenki.");
        }
        
        boolean removed = songRepo.delete(entity.getId());
        if (!removed) {
            throw new IllegalArgumentException("Nie udało się usunąć piosenki");
        }
        
        library.addLog(ts() + " Usunięto piosenkę: " + title);
        out.println("<html><body><h2>Usunięto piosenkę.</h2>" + linkBack() + "</body></html>");
    }

    /**
     * Handles creation of a new playlist.
     *
     * @param req the HTTP request containing {@code playlistName}
     * @param out the response writer for feedback HTML
     * @throws IllegalArgumentException if the playlist name is invalid
     */
    private void handleCreatePlaylist(HttpServletRequest req, PrintWriter out) {
        String name = req.getParameter("playlistName");
        if (playlistRepo.findByName(name) == null) {
            playlistRepo.save(new PlaylistEntity(name));
            library.addLog(ts() + " Utworzono playlistę: " + name);
        }
        out.println("<html><body><h2>Utworzono playlistę: " + escape(name) + "</h2>" + linkBack() + "</body></html>");
    }

    /**
     * Handles removal of a playlist by name.
     *
     * @param req the HTTP request containing {@code playlistName}
     * @param out the response writer for feedback HTML
     */
    private void handleRemovePlaylist(HttpServletRequest req, PrintWriter out) {
        String name = req.getParameter("playlistName");
        boolean removed = playlistRepo.deleteByName(name);
        if (removed) {
            library.addLog(ts() + " Usunięto playlistę: " + name);
        } else {
            library.addLog(ts() + " Próba usunięcia nieistniejącej playlisty: " + name);
        }
        out.println("<html><body><h2>" + (removed ? "Usunięto playlistę." : "Nie znaleziono playlisty.") + "</h2>" + linkBack() + "</body></html>");
    }

    /**
     * Handles adding an existing song to a playlist by name.
     *
     * @param req the HTTP request containing {@code playlistName}, {@code title}, {@code artist}
     * @param out the response writer for feedback HTML
     * @throws IllegalArgumentException if the referenced song does not exist
     */
    private void handleAddToPlaylist(HttpServletRequest req, PrintWriter out) {
        String playlistName = req.getParameter("playlistName");
        String title = req.getParameter("title");
        String artist = req.getParameter("artist");
        
        PlaylistEntity p = playlistRepo.findByName(playlistName);
        SongEntity s = findSongEntity(title, artist);
        
        if (p != null && s != null) {
            p.getSongs().add(s);
            playlistRepo.save(p);
            library.addLog(ts() + " DB: Powiązano '" + title + "' z playlistą '" + playlistName + "'");
            out.println("<html><body><h2>Dodano do playlisty w bazie.</h2>" + linkBack() + "</body></html>");
        } else {
             throw new IllegalArgumentException("Nie znaleziono playlisty lub piosenki w bazie.");
        }
    }

     /**
     * Handles removing a song from a playlist.
     *
     * @param req the HTTP request containing {@code playlistName}, {@code title}, {@code artist}
     * @param out the response writer for feedback HTML
     * @throws IllegalArgumentException if the playlist or song is not found
     */
    private void handleRemoveFromPlaylist(HttpServletRequest req, PrintWriter out) {
        String playlistName = req.getParameter("playlistName");
        String title = req.getParameter("title");
        String artist = req.getParameter("artist");
        
        PlaylistEntity p = playlistRepo.findByName(playlistName);
        SongEntity sToCheck = findSongEntity(title, artist);
        
        if (p == null) throw new IllegalArgumentException("Nie znaleziono playlisty.");
        if (sToCheck == null) throw new IllegalArgumentException("Nie znaleziono piosenki w bazie.");

        boolean removed = p.getSongs().removeIf(song -> song.getId().equals(sToCheck.getId()));
        
        if (removed) {
            playlistRepo.save(p);
            library.addLog(ts() + " Usunięto z playlisty '" + playlistName + "': " + title + " — " + artist);
            out.println("<html><body><h2>Usunięto z playlisty.</h2>" + linkBack() + "</body></html>");
        } else {
            throw new IllegalArgumentException("Piosenka nie była na playliście.");
        }
    }

    /**
     * Toggles the favorite flag for a song identified by title and artist.
     *
     * @param req the HTTP request containing {@code title} and {@code artist}
     * @param out the response writer for feedback HTML
     * @throws IllegalArgumentException if the song does not exist
     */
    private void handleToggleFavorite(HttpServletRequest req, PrintWriter out) {
        String title = req.getParameter("title");
        String artist = req.getParameter("artist");
        
        SongEntity s = findSongEntity(title, artist);
        if (s != null) {
            s.setFavorite(!s.isFavorite());
            songRepo.update(s);
            library.addLog(ts() + " DB: Zmieniono status ulubionej dla: " + title);
            out.println("<html><body><h2>Zmieniono status ulubionej w bazie.</h2>" + linkBack() + "</body></html>");
        } else {
             throw new IllegalArgumentException("Piosenka nie istnieje.");
        }
    }

    /**
     * Updates an existing song's metadata (title, artist, year).
     *
     * @param req the HTTP request containing fields: {@code oldTitle}, {@code oldArtist}, {@code title}, {@code artist}, {@code year}
     * @param out the response writer for feedback HTML
     * @throws NumberFormatException     if the provided {@code year} is not a valid number
     * @throws IllegalArgumentException  if validation fails or a duplicate would be created
     */
    private void handleUpdateSong(HttpServletRequest req, PrintWriter out) {
        String oldTitle = req.getParameter("oldTitle");
        String oldArtist = req.getParameter("oldArtist");
        String newTitle = req.getParameter("title");
        String newArtist = req.getParameter("artist");
        String yearStr = req.getParameter("year");

        SongEntity song = findSongEntity(oldTitle, oldArtist);
        if (song == null) {
            out.println("<html><body><h2>Nie znaleziono piosenki do edycji.</h2>" + linkBack() + "</body></html>");
            return;
        }

        try {
            boolean changingIdentity = !(song.getTitle().equalsIgnoreCase(newTitle) && song.getArtist().equalsIgnoreCase(newArtist));
            if (changingIdentity) {
                if (songRepo.findByTitleArtistYear(newTitle, newArtist, Integer.parseInt(yearStr)) != null) {
                    out.println("<html><body><h2>Piosenka o takim tytule i wykonawcy już istnieje w bazie.</h2>" + linkBack() + "</body></html>");
                    return;
                }
            }

            int newYear = Integer.parseInt(yearStr);
            String before = song.getTitle();
            
            song.setTitle(newTitle);
            song.setArtist(newArtist);
            song.setYear(newYear);
            
            songRepo.update(song);
            
            library.addLog(ts() + " DB Update: " + before + " -> " + newTitle);
            out.println("<html><body><h2>Zapisano zmiany w bazie danych.</h2>" + linkBack() + "</body></html>");
        } catch (NumberFormatException e) {
            out.println("<html><body><h2>Niepoprawny rok: " + escape(String.valueOf(yearStr)) + "</h2>" + linkBack() + "</body></html>");
        } catch (IllegalArgumentException e) {
            out.println("<html><body><h2>Błąd walidacji: " + escape(e.getMessage()) + "</h2>" + linkBack() + "</body></html>");
        }
    }


    /**
     * Produces a timestamp string for activity log entries.
     *
     * @return timestamp string in format {@code yyyy-MM-dd HH:mm:ss} wrapped in brackets
     */
    private String ts() {
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "[" + java.time.LocalDateTime.now().format(fmt) + "]";
    }

    /**
     * Writes HTML header with info about data sources.
     *
     * @param req   the HTTP request (used to display cookie info)
     * @param out   the response writer
     * @param title page title
     */
    private void header(HttpServletRequest req, PrintWriter out, String title) {
        out.println("<html><head><meta charset=\"UTF-8\"><title>" + escape(title) + "</title>");
        out.println("<style>");
        out.println(".cookie-box { border: 2px dashed #007bff; padding: 10px; background-color: #eef7ff; margin: 15px 0; }");
        out.println(".db-section { border-top: 3px solid #28a745; margin-top: 20px; padding-top: 10px; }");
        out.println(".task-info { background: #f8f9fa; border-bottom: 1px solid #dee2e6; padding: 10px; font-style: italic; color: #6c757d; }");
        out.println("</style></head><body>");
        out.println("<div class='task-info'>Temat zadania: Implementacja JPA aplikacji webowej.</div>");
        
        out.println("<h1>" + escape(title) + "</h1>");
        String visitorId = getCookieValue(req, "visitorId");
        String lastView = getCookieValue(req, "lastView");
        
        out.println("<div class='cookie-box'>");
        out.println("<strong>[DANE ODCZYTANE Z CIASTECZEK (Client-Side Cookie)]:</strong><br/>");
        out.println("Twój identyfikator sesji: <b>" + escape(String.valueOf(visitorId)) + "</b><br/>");
        out.println("Ostatnio odwiedzona strona: <b>" + escape(String.valueOf(lastView)) + "</b>");
        out.println("</div>");
        out.println("<nav>");
        out.println("<a href=\"music?action=all\">Wszystkie</a> | ");
        out.println("<a href=\"music?action=favorites\">Ulubione</a> | ");
        out.println("<a href=\"music?action=playlists\">Playlisty</a> | ");
        out.println("<a href=\"music?action=summary\">Podsumowanie</a> | ");
        out.println("<a href=\"history\">Historia Logów</a>");
        out.println("</nav>");
        
        out.println("<div class='db-section'>");
        out.println("<h4 style='color:#28a745; margin:0;'>[PONIŻSZE DANE POCHODZĄ Z BAZY DANYCH]:</h4>");
        out.println("<hr>");
    }

    /**
     * Writes a standard HTML footer with a back link.
     *
     * @param out the response writer
     */
    private void footer(PrintWriter out) {
        out.println("</div>");
        out.println("<hr><a href=\"index.html\">Powrót</a>");
        out.println("</body></html>");
    }

    /**
     * Generates a simple HTML link that navigates back to the welcome page.
     *
     * @return HTML snippet with a back link
     */
    private String linkBack() {
        return "<p><a href=\"index.html\">Powrót</a></p>";
    }

    /**
     * Renders a textual representation of a song with a star indicating favorite status.
     *
     * @param s the song to render
     * @return HTML-safe string representing the song
     */
    private String renderSongLine(Song s) {
        String star = s.isFavorite() ? "★" : "☆";
        return escape(s.getTitle() + " — " + s.getArtist() + " (" + s.getYear() + ") ") + star;
    }

    /**
     * Produces inline HTML forms with actions for a song: remove, toggle favorite, edit.
     *
     * @param s the song for which to render action buttons
     * @return HTML snippet with action buttons
     */
    private String buttonsForSong(Song s) {
        String title = escape(s.getTitle());
        String artist = escape(s.getArtist());
        return " <form style=\"display:inline\" method=\"get\" action=\"music\">" +
                "<input type=\"hidden\" name=\"action\" value=\"confirmRemoveSong\">" +
                "<input type=\"hidden\" name=\"title\" value=\"" + escape(title) + "\">" +
                "<input type=\"hidden\" name=\"artist\" value=\"" + escape(artist) + "\">" +
                "<button type=\"submit\">Usuń</button></form>" +
                " <form style=\"display:inline\" method=\"post\" action=\"music\">" +
                "<input type=\"hidden\" name=\"op\" value=\"toggleFavorite\">" +
                "<input type=\"hidden\" name=\"title\" value=\"" + title + "\">" +
                "<input type=\"hidden\" name=\"artist\" value=\"" + artist + "\">" +
                "<button type=\"submit\">Przełącz ulubioną</button></form>" +
                " <form style=\"display:inline\" method=\"get\" action=\"music\">" +
                "<input type=\"hidden\" name=\"action\" value=\"editSong\">" +
                "<input type=\"hidden\" name=\"title\" value=\"" + title + "\">" +
                "<input type=\"hidden\" name=\"artist\" value=\"" + artist + "\">" +
                "<button type=\"submit\">Edytuj</button></form>";
    }

    /**
     * Produces inline HTML forms with actions for a song without the remove action
     * (used in views where removal is not appropriate).
     *
     * @param s the song for which to render action buttons
     * @return HTML snippet with action buttons (without remove)
     */
    private String buttonsForSongNoRemove(Song s) {
        String title = escape(s.getTitle());
        String artist = escape(s.getArtist());
        return " <form style=\"display:inline\" method=\"post\" action=\"music\">" +
                "<input type=\"hidden\" name=\"op\" value=\"toggleFavorite\">" +
                "<input type=\"hidden\" name=\"title\" value=\"" + title + "\">" +
                "<input type=\"hidden\" name=\"artist\" value=\"" + artist + "\">" +
                "<button type=\"submit\">Przełącz ulubioną</button></form>" +
                " <form style=\"display:inline\" method=\"get\" action=\"music\">" +
                "<input type=\"hidden\" name=\"action\" value=\"editSong\">" +
                "<input type=\"hidden\" name=\"title\" value=\"" + title + "\">" +
                "<input type=\"hidden\" name=\"artist\" value=\"" + artist + "\">" +
                "<button type=\"submit\">Edytuj</button></form>";
    }

    /**
     * Produces an inline HTML form for adding the specified song to a playlist.
     *
     * @param s the song to be added to a playlist
     * @return HTML snippet with a dropdown menu to add the song to a playlist
     */
    private String addToPlaylistInlineForm(Song s) {
        String title = escape(s.getTitle());
        String artist = escape(s.getArtist());
        List<PlaylistEntity> pls = playlistRepo.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append(" <form style=\"display:inline\" method=\"post\" action=\"music\">")
          .append("<input type=\"hidden\" name=\"op\" value=\"addToPlaylist\">")
          .append("<input type=\"hidden\" name=\"title\" value=\"").append(title).append("\">")
          .append("<input type=\"hidden\" name=\"artist\" value=\"").append(artist).append("\">");

        if (pls == null || pls.isEmpty()) {
            sb.append("<span> Brak playlist. </span>")
              .append("<a href=\"music?action=playlists\">Utwórz playlistę</a>");
        } else {
            sb.append("<label>Do playlisty: <select name=\"playlistName\" required>");
            for (PlaylistEntity p : pls) {
                String name = escape(p.getName());
                sb.append("<option value=\"").append(name).append("\">")
                  .append(name).append("</option>");
            }
            sb.append("</select></label> ")
              .append("<button type=\"submit\">Dodaj</button>");
        }

        sb.append("</form>");
        return sb.toString();
    }

    /**
     * Produces a small inline form that links to a confirmation page for removing
     * the given song from the specified playlist.
     *
     * @param playlistName the playlist name
     * @param s            the song to be removed
     * @return HTML snippet that navigates to the confirmation page
     */
    private String removeFromPlaylistConfirmLink(String playlistName, Song s) {
        String title = escape(s.getTitle());
        String artist = escape(s.getArtist());
        return " <form style=\"display:inline\" method=\"get\" action=\"music\">" +
                "<input type=\"hidden\" name=\"action\" value=\"confirmRemoveFromPlaylist\">" +
                "<input type=\"hidden\" name=\"playlistName\" value=\"" + escape(playlistName) + "\">" +
                "<input type=\"hidden\" name=\"title\" value=\"" + title + "\">" +
                "<input type=\"hidden\" name=\"artist\" value=\"" + artist + "\">" +
                "<button type=\"submit\">Usuń z playlisty</button></form>";
    }

     /**
     * Produces an inline form that links to a confirmation page for removing a playlist.
     *
     * @param playlistName the playlist to be removed
     * @return HTML snippet that navigates to playlist removal confirmation
     */
    private String removePlaylistConfirmLink(String playlistName) {
        return " <form style=\"display:inline\" method=\"get\" action=\"music\">" +
                "<input type=\"hidden\" name=\"action\" value=\"confirmRemovePlaylist\">" +
                "<input type=\"hidden\" name=\"playlistName\" value=\"" + escape(playlistName) + "\">" +
                "<button type=\"submit\">Usuń</button></form>";
    }

    /**
     * Escapes basic HTML special characters in a string.
     *
     * @param s input string (may be {@code null})
     * @return escaped string safe for HTML; empty string for {@code null}
     */
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /**
     * Escapes basic HTML special characters in a string.
     *
     * @param s input string (may be {@code null})
     * @return escaped string safe for HTML; empty string for {@code null}
     */
    private String url(String s) {
        try {
            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }
}