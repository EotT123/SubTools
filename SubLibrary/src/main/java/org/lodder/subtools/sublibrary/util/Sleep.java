package org.lodder.subtools.sublibrary.util;

import static java.time.temporal.ChronoUnit.*;

import manifold.science.measures.Time;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Sleep {

    private Sleep() {
        // hide utility class constructor
    }

    public static void sleep(Time time) {
        try {
            Thread.sleep(time.get(SECONDS) * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
