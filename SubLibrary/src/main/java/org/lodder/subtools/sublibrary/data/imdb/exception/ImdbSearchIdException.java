package org.lodder.subtools.sublibrary.data.imdb.exception;

import java.io.Serial;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class ImdbSearchIdException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public ImdbSearchIdException(String s, String url, Exception e) {
        super("$s: $url", e);
    }
}
