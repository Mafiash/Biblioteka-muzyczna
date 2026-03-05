package com.mycompany.lab2.tests;

import org.junit.jupiter.api.Test;
import com.mycompany.lab2.model.*;

import static org.junit.jupiter.api.Assertions.*;

class SongPredicateTest {

    /**
     * Checks a non-trivial predicate returns true for a matching song and false for a non-matching one.
     */
    @Test
    void predicateEvaluatesSongCorrectly() {
        Song song = new Song("Hello", "Adele", 2015);
        SongPredicate p = s -> s.getArtist().equals("Adele") && s.getTitle().startsWith("He");
        assertTrue(p.test(song));
        Song other = new Song("Someone Like You", "Adele", 2011);
        assertFalse(p.test(other));
    }
    
    /**
     * Sanity check that trivial always-true/always-false predicates behave as expected.
     */
    @Test
    void alwaysTrue_and_alwaysFalse_predicates() {
        Song s = new Song("Hello", "Adele", 2015);
        SongPredicate alwaysTrue = x -> true;
        SongPredicate alwaysFalse = x -> false;
        assertTrue(alwaysTrue.test(s));
        assertFalse(alwaysFalse.test(s));
    }
}
