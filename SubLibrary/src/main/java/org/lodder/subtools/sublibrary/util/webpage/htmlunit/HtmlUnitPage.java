package org.lodder.subtools.sublibrary.util.webpage.htmlunit;

import org.htmlunit.html.HtmlPage;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.webpage.PageCondition.PageConditionIntf;
import org.lodder.subtools.sublibrary.util.webpage.PageIntf;

@NullMarked
public record HtmlUnitPage(HtmlPage page) implements PageIntf {

    @Override
    public boolean contains(PageConditionIntf pageConditionIntf) {
        return pageConditionIntf.contains(page);
    }

    @Override
    public String getPageSource() {
        return page.asXml();
    }
}
