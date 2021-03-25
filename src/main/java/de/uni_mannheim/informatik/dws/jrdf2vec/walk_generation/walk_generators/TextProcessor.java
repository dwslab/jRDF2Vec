package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import java.util.function.UnaryOperator;

/**
 * Simple processor for datatpye property values.
 */
public class TextProcessor implements UnaryOperator<String> {

    private static final String[] stopwords = {"a", "the"};

    @Override
    public String apply(String s) {

        // remove language annotations
        s = s.replaceAll("\"@.{2}.*\\..*$", ""); // regex: "@.{2}.*\..*$
        s = s.replaceAll("\"@[A-Za-z]{2}$", "");

        // remove type annotations
        s = s.replaceAll("\"\\^\\^.*\\..*$", ""); // regex: "\^\^.*\..*$

        // remove everything that is not a number or a character of the alphabet
        s = s.replaceAll("[^A-Za-z0-9 ÄäÜüÖöß]", ""); // regex: [^A-Za-z0-9 ]

        // remove trailing and leading spaces
        s = s.trim();

        s = s.toLowerCase();

        return s;
    }


}
