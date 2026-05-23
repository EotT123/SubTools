package org.lodder.subtools.sublibrary.util.webpage;

import org.htmlunit.html.HtmlPage;
import org.jspecify.annotations.NullMarked;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

@SuppressWarnings("unused")
@NullMarked
public class PageCondition {

    public static PageConditionIntf id(String id) {
        return new PageConditionId(id);
    }

    public static PageConditionIntf className(String className) {
        return new PageConditionClassName(className);
    }

    public static PageConditionIntf css(String cssSelector) {
        return new PageConditionCss(cssSelector);
    }

    public static PageConditionIntf tagName(String tagName) {
        return new PageConditionTagName(tagName);
    }

    public static PageConditionIntf xpath(String xpath) {
        return new PageConditionXpath(xpath);
    }

    @NullMarked
    public record PageConditionId(String id) implements PageConditionIntf {

        @Override
        public boolean contains(HtmlPage page) {
            return page.getElementById(id) != null;
        }

        @Override
        public By getSelector() {
            return By.id(id);
        }
    }

    @NullMarked
    public record PageConditionClassName(String className) implements PageConditionIntf {

        @Override
        public boolean contains(HtmlPage page) {
            return page.querySelector("." + className) != null;
        }

        @Override
        public By getSelector() {
            return By.className(className);
        }
    }

    @NullMarked
    public record PageConditionCss(String cssSelector) implements PageConditionIntf {

        @Override
        public boolean contains(HtmlPage page) {
            return page.querySelector(cssSelector) != null;
        }

        @Override
        public By getSelector() {
            return By.cssSelector(cssSelector);
        }
    }

    @NullMarked
    public record PageConditionTagName(String tagName) implements PageConditionIntf {

        @Override
        public boolean contains(HtmlPage page) {
            return page.querySelector(tagName) != null;
        }

        @Override
        public By getSelector() {
            return By.tagName(tagName);
        }
    }

    @NullMarked
    public record PageConditionXpath(String xpath) implements PageConditionIntf {

        @Override
        public boolean contains(HtmlPage page) {
            return !page.getByXPath(xpath).isEmpty();
        }

        @Override
        public By getSelector() {
            return By.xpath(xpath);
        }
    }

    @NullMarked
    public sealed interface PageConditionIntf
        permits PageConditionXpath, PageConditionTagName, PageConditionCss, PageConditionClassName,
        PageConditionId {
        boolean contains(HtmlPage page);

        default boolean doesNotContain(HtmlPage page) {
            return !contains(page);
        }

        default boolean contains(WebDriver driver) {
            try {
                driver.findElement(getSelector());
                return true;
            } catch (NoSuchElementException e) {
                return false;
            }
        }

        default boolean doesNotContain(WebDriver driver) {
            return !contains(driver);
        }

        By getSelector();
    }

}
