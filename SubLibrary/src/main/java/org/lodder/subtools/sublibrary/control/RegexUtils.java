package org.lodder.subtools.sublibrary.control;

import static java.util.Objects.*;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.control.Tags.Tag;

/**
 * <a href="http://rosettacode.org/wiki/Roman_numerals/Decode#Java_2">Source</a>
 */
@NullMarked
public class RegexUtils {

    public static final List<Character> DELIMITERS = List.of('.', ' ', '-');
    public static final String DELIMITER =
        "[" + Pattern.quote(DELIMITERS.stream().map(String::valueOf).collect(Collectors.joining(""))) + "]";

    private RegexUtils() {
        // Hide Utility Class Constructor
    }

    @NullMarked
    public interface RegexStart extends RegexTag {
        RegexTag startOfText();
    }

    @NullMarked
    public interface RegexTag extends RegexRegex {
        RegexRegex tag(Tag tag);
    }

    @NullMarked
    public interface RegexRegex {
        RegexNext regex(String regex);

        RegexNext regexOptional(String regex);

        <E extends Enum<E>> RegexNext regex(Class<E> enumClass, Function<? super E, String> toStringMapper);
    }

    @NullMarked
    public interface RegexNext extends RegexTag, RegexEnd {
    }

    @NullMarked
    public interface RegexEnd extends RegexBuilderBuild {
        RegexBuilderBuild endOfText();
    }

    @NullMarked
    public interface RegexBuilderBuild {
        String create();

        List<String> createWithDelimiter();
    }

    @NullMarked
    public static class Regex implements RegexStart, RegexTag, RegexRegex, RegexNext, RegexEnd, RegexBuilderBuild {
        private @Nullable Tag tag;
        private @Nullable String regex;
        private String result = "";
        private boolean finalized = false;

        private Regex() {

        }

        public static RegexStart builder() {
            return new Regex();
        }

        @Override
        public Regex startOfText() {
            return regex("^");
        }

        @Override
        public Regex endOfText() {
            return regex("$");
        }

        @Override
        public Regex tag(Tag tag) {
            if (StringUtils.isNotBlank(this.regex)) {
                next();
            }
            this.tag = tag;
            return this;
        }

        @Override
        public Regex regex(String regex) {
            if (StringUtils.isNotBlank(this.regex)) {
                next();
            }
            this.regex = regex;
            return this;
        }

        @Override
        public Regex regexOptional(String regex) {
            return regex("(" + regex + ")?");
        }

        @Override
        public <E extends Enum<E>> Regex regex(Class<E> enumClass, Function<? super E, String> toStringMapper) {
            return regex(enumClass.getEnumConstants().stream().map(toStringMapper).collect(Collectors.joining("|")));
        }

        private void next() {
            result += createCurrent();
            tag = null;
            regex = null;
        }

        private String createCurrent() {
            return tag != null ? "(?<${tag.value}>$regex)" : requireNonNull(regex);
        }

        @Override
        public String create() {
            if (!finalized) {
                next();
            }
            finalized = true;
            return result;
        }

        @Override
        public List<String> createWithDelimiter() {
            next();
            return List.of(
                DELIMITER + result + DELIMITER,
                DELIMITER + result + "$",
                "^" + result + DELIMITER,
                "^" + result + "$");
        }
    }
}
