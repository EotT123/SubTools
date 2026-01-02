package org.lodder.subtools.sublibrary;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record Credentials(String username, String password) {
}
