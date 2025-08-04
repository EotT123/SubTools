package org.lodder.subtools.sublibrary.util.http;

import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.CacheStrategy;
import org.lodder.subtools.sublibrary.LogLevel;

public interface ApiExceptionIntf {
    @val HttpStatus errorCode;
    @val CacheStrategy cacheStrategy;
    @val LogLevel logLevel;
}
