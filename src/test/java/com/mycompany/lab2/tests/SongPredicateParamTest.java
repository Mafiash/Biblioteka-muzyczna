package com.mycompany.lab2.tests;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import com.mycompany.lab2.model.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parameterized tests for SongPredicate functional interface via lambdas.
 */
class SongPredicateParamTest {

    /**
     * Provides songs, predicates, and expected outcomes for predicate evaluation.
     */
    static Stream<Arguments> predicateCases() {
        return Stream.of(
                Arguments.of(new Song("Hello", "Adele", 2015), (SongPredicate) s -> s.getArtist().equals("Adele") && s.getTitle().startsWith("He"), true),
                Arguments.of(new Song("Someone Like You", "Adele", 2011), (SongPredicate) s -> s.getArtist().equals("Adele") && s.getTitle().startsWith("He"), false)
        );
    }

    /**
     * Verifies the provided predicate evaluates the song to the expected boolean value.
     */
    @ParameterizedTest
    @MethodSource("predicateCases")
    void predicateEvaluatesSong(Song song, SongPredicate predicate, boolean expected) {
        assertEquals(expected, predicate.test(song));
    }
}
