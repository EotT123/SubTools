package org.lodder.subtools.sublibrary.util.webpage.webdriver;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface WebDriver extends org.openqa.selenium.WebDriver {
    boolean usedForDomain(String domain);
}