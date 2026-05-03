package org.lodder.subtools.sublibrary.util.webpage;

import java.util.List;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class CloudFlare {

    public static final List<String> strings =
        List.of("DDoS protection by CloudFlare", "Checking your browser before accessing",
            "This process is automatic. Your browser will redirect to your requested content shortly.", "Please Wait",
            "Checking if the site connection is secure", "Why am I seeing this page?",
            "Just a moment...", "Please complete the security check", "we need to verify that you're not a robot");

    public static boolean isProtected(String text) {
        return strings.stream().anyMatch(text::contains);
    }
}
