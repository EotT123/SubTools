package org.lodder.subtools.sublibrary.util.http;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.CacheStrategy;
import org.lodder.subtools.sublibrary.LogLevel;

@NullMarked
public interface ApiExceptionIntf {
    @val HttpStatus errorCode;
    @val CacheStrategy cacheStrategy;
    @val LogLevel logLevel;
}
