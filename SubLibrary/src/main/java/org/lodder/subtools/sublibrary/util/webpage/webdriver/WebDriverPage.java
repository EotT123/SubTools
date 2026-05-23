package org.lodder.subtools.sublibrary.util.webpage.webdriver;

import static java.util.Objects.*;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.webpage.PageCondition.PageConditionIntf;
import org.lodder.subtools.sublibrary.util.webpage.PageIntf;
import org.openqa.selenium.WebDriver;

@NullMarked
public record WebDriverPage(WebDriver driver) implements PageIntf {

    @Override
    public boolean contains(PageConditionIntf pageConditionIntf) {
        return pageConditionIntf.contains(driver);
    }

    @Override
    public String getPageSource() {
        return requireNonNull(driver.getPageSource());
    }
}
