Biblioteka muzyczna

Opis projektu
Aplikacja webowa do zarządzania utworami i playlistami. Umożliwia dodawanie, edycję, usuwanie, oznaczanie jako ulubione, wyszukiwanie oraz grupowanie utworów w playlisty. Działa jako aplikacja WAR wdrażana na serwerze Payara, korzysta z JPA (EclipseLink) do persistencji oraz udostępnia interfejs przez servlety i bazowy punkt REST.

Kluczowe możliwości
Dodawanie, edycja i usuwanie utworów (tytuł, wykonawca, rok wydania, status „ulubione”).
Tworzenie i usuwanie playlist, dodawanie/usuwanie utworów w playlistach.
Przegląd wszystkich utworów, filtr „ulubione”, szybkie wyszukiwanie po tytule/wykonawcy (bez rozróżniania wielkości liter).
Warstwa serwerowa generuje proste widoki HTML oraz udostępnia podstawowy endpoint REST typu „ping”.
Architektura i komponenty
Warstwa web (HTTP)
Servlety obsługują główną logikę oraz generują HTML. Sterowanie odbywa się przez parametry zapytań (action dla widoków i op dla operacji modyfikujących).
Konfiguracja REST (JAX‑RS) udostępnia przestrzeń /resources i przykładowy endpoint ping — przygotowane pod rozbudowę o API operujące na danych utworów/playlist.
Plik web.xml definiuje podstawowe ustawienia sesji i stronę powitalną.
Inicjalizacja aplikacji
Mechanizm startowy tworzy współdzieloną instancję wewnętrznej biblioteki w pamięci procesu i umieszcza ją w kontekście serwletów. Używana jest m.in. do logowania historii działań oraz prezentacji podstawowych statystyk.
Persistencja i model danych
Dostęp do bazy przez JPA (EclipseLink).
Encje odzwierciedlają informacje o utworach i playlistach (w tym relacje i indeksy wspomagające wyszukiwanie).
Mapery odwzorowują obiekty domenowe na encje JPA i odwrotnie.
Repozytoria (wzorzec Repository) realizują operacje CRUD, zapytania wybiórcze i transakcje (z użyciem EntityManager).
Serwer aplikacyjny
Aplikacja jest pakowana jako WAR i wdrażana na serwer Payara, który zapewnia środowisko uruchomieniowe Jakarta EE (servlety, JAX‑RS), zarządzanie sesją, logowanie serwerowe oraz konfigurację zasobów (datasource, kontekst aplikacji).
Interfejs użytkownika i punkty dostępu
Widoki i operacje (servlety)
Główny punkt wejścia: strona startowa (linki do listy utworów, ulubionych, playlist, podsumowania oraz historii).
Przegląd i operacje dostępne są przez adresy /<context-root>/music z parametrami:
Widoki (parametr action): np. all, favorites, playlists, summary, formularze potwierdzeń i edycji.
Operacje modyfikujące (parametr op, metoda POST): np. addSong, removeSong, createPlaylist, removePlaylist, addToPlaylist, removeFromPlaylist, toggleFavorite, updateSong.
Dodatkowy widok historii pod /<context-root>/history prezentuje ostatnie zdarzenia i statystyki (liczba utworów, liczba playlist).
Aplikacja wykorzystuje ciasteczka visitorId (do identyfikacji przeglądającego) oraz lastView (ostatnio otwarty widok).
REST (bazowy, do rozbudowy)
Przestrzeń REST pod /<context-root>/resources.
Przykładowy endpoint testowy: GET /resources/jakartaee11 zwraca komunikat „ping Jakarta EE”.
Model domenowy (podgląd logiki)
Utwór: tytuł, wykonawca, rok wydania, flaga „ulubione”; walidacja (m.in. rok w zakresie 1800..bieżący, brak pustych pól tekstowych).
Playlista: nazwa, zbiór utworów; operacje dodawania i usuwania utworów.
Historia aktywności: lista wpisów tekstowych dodawanych przez operacje w aplikacji (do wglądu w widoku historii).
Warstwa persistencji (JPA)
persistence.xml (Jakarta Persistence) definiuje jednostkę my_persistence_unit.
Generowanie schematu: tryb „create-or-extend-tables” — tabele są tworzone/rozszerzane automatycznie.
Logowanie: poziom FINE — w logach serwera widoczne są zapytania SQL.
Źródło danych: aplikacja korzysta z zasobu bazodanowego o JNDI jdbc/labDB skonfigurowanego na serwerze.
Transakcje: obecnie w trybie RESOURCE_LOCAL (transakcje otwierane w repozytoriach). Możliwa zmiana na JTA i użycie menedżera transakcji serwera.
Wymagania wstępne
Java 21 (kompilacja z poziomu Maven ustawiona na release 21).
Maven 3.8+.
Serwer Payara (np. Payara 6).
Baza danych i sterownik JDBC (do środowisk deweloperskich domyślnie H2; w środowisku serwerowym zalecana zewnętrzna baza, np. PostgreSQL/MySQL).
Uwaga: Payara 6 wspiera Jakarta EE 10. Jeżeli używasz biblioteki jakarta.jakartaee-api 11.x po stronie aplikacji (jako provided), upewnij się co do zgodności z serwerem. Najbezpieczniej dopasować wersję API do wersji wspieranej przez Payarę lub polegać na API dostarczanym przez serwer.

Konfiguracja bazy danych na Payara (datasource)
Utwórz pulę połączeń (Connection Pool) dla wybranego sterownika JDBC (H2/MySQL/Postgres).
Utwórz zasób JDBC (JDBC Resource) o nazwie JNDI jdbc/labDB i powiąż go z utworzoną pulą.
Przetestuj połączenie z poziomu konsoli administracyjnej Payara.
Warto włączyć walidację połączeń i ustawić właściwości puli (np. URL, użytkownik/hasło, AutoCommit, Validation Table/Query).

Budowanie i uruchamianie
Budowa artefaktu
mvn clean package
Wynik: plik WAR w target/Lab2-1.0-SNAPSHOT.war.

Wdrażanie na Payara
Konsola administracyjna: Applications → Deploy → wskaż plik WAR → ustaw Context Root (np. /lab2).
Lub z CLI:
asadmin deploy path\to\Lab2-1.0-SNAPSHOT.war
Dostęp do aplikacji (przykłady URL)
Strona główna: http://localhost:8080/<context-root>/
Widoki serwletów:
http://localhost:8080/<context-root>/music?action=all
http://localhost:8080/<context-root>/music?action=favorites
http://localhost:8080/<context-root>/music?action=playlists
http://localhost:8080/<context-root>/history
REST (ping): http://localhost:8080/<context-root>/resources/jakartaee11
Testowanie
Projekt zawiera konfigurację JUnit 5. Testy uruchomisz komendą:
mvn test
Dla diagnozy problemów persistencji podnieś poziom logowania EclipseLink (np. do FINEST) oraz sprawdź log Payary (server.log).
Struktura katalogów (skrót)
src/main/java — kod źródłowy aplikacji (warstwa web, model, persistencja, mapery, repozytoria).
src/main/resources/META-INF/persistence.xml — konfiguracja JPA.
src/main/webapp — statyczne zasoby i szablony HTML, WEB-INF/web.xml.
src/test/java — testy jednostkowe/integracyjne.
Najczęstsze problemy i wskazówki
Niezgodność wersji Jakarta EE między aplikacją a serwerem — dopasuj jakartaee-api do wersji wspieranej przez Payarę lub użyj API serwerowego (scope provided).
Konflikt providerów JPA — na Payara najlepiej korzystać z EclipseLink dostarczanego przez serwer (usuń własny provider z aplikacji lub oznacz jako provided).
Brak zasobu jdbc/labDB — utwórz i skonfiguruj datasource w Payara; bez tego JPA nie połączy się z bazą.
H2 w środowisku serwerowym — do celów produkcyjnych zalecana zewnętrzna baza (Postgres/MySQL) i odpowiednia konfiguracja puli.
Możliwe kierunki rozwoju
Rozbudowa REST API o pełne operacje CRUD na utworach i playlistach (JSON), a także wyszukiwanie i filtrowanie.
Przeniesienie transakcji na JTA i integracja z CDI/EJB dla spójniejszego zarządzania transakcjami.
Wdrożenie warstwy UI (np. SPA) konsumującej REST, zamiast generowania HTML po stronie serwera.
Autoryzacja i uwierzytelnianie (np. Jakarta Security), role i uprawnienia do operacji na zasobach.
