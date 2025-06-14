package extensions.org.slf4j.Logger;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.lodder.subtools.sublibrary.LogLevel;
import org.slf4j.Logger;

@Extension
public class LoggerExt {

    public static void log(@This Logger logger, LogLevel logLevel, String message, Object arg){
        switch (logLevel) {
            case TRACE -> {
                if (logger.isTraceEnabled()) logger.trace(message, arg);
            }
            case DEBUG -> {
                if (logger.isDebugEnabled()) logger.debug(message, arg);
            }
            case INFO -> {
                if (logger.isInfoEnabled()) logger.info(message, arg);
            }
            case WARN -> {
                if (logger.isWarnEnabled()) logger.warn(message, arg);
            }
            case ERROR -> {
                if (logger.isErrorEnabled()) logger.error(message, arg);
            }
        }
    }

    public static void log(@This Logger logger, LogLevel logLevel,String message, Object arg1, Object arg2){
        switch (logLevel) {
            case TRACE -> {
                if (logger.isTraceEnabled()) logger.trace(message, arg1,arg2);
            }
            case DEBUG -> {
                if (logger.isDebugEnabled()) logger.debug(message, arg1,arg2);
            }
            case INFO -> {
                if (logger.isInfoEnabled()) logger.info(message, arg1,arg2);
            }
            case WARN -> {
                if (logger.isWarnEnabled()) logger.warn(message, arg1,arg2);
            }
            case ERROR -> {
                if (logger.isErrorEnabled()) logger.error(message, arg1,arg2);
            }
        }
    }

    public static void log(@This Logger logger, LogLevel logLevel,String message, Object... arguments){
        switch (logLevel) {
            case TRACE -> {
                if (logger.isTraceEnabled()) logger.trace(message, arguments);
            }
            case DEBUG -> {
                if (logger.isDebugEnabled()) logger.debug(message, arguments);
            }
            case INFO -> {
                if (logger.isInfoEnabled()) logger.info(message, arguments);
            }
            case WARN -> {
                if (logger.isWarnEnabled()) logger.warn(message, arguments);
            }
            case ERROR -> {
                if (logger.isErrorEnabled()) logger.error(message, arguments);
            }
        }
    }

    public static void log(@This Logger logger,LogLevel logLevel, String message, Throwable t){
        switch (logLevel) {
            case TRACE -> {
                if (logger.isTraceEnabled()) logger.trace(message, t);
            }
            case DEBUG -> {
                if (logger.isDebugEnabled()) logger.debug(message,  t);
            }
            case INFO -> {
                if (logger.isInfoEnabled()) logger.info(message,  t);
            }
            case WARN -> {
                if (logger.isWarnEnabled()) logger.warn(message,  t);
            }
            case ERROR -> {
                if (logger.isErrorEnabled()) logger.error(message,  t);
            }
        }
    }
}
