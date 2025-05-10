package org.lodder.subtools.sublibrary.util.http;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status.Family;
import manifold.ext.props.rt.api.var;

public enum  HttpStatus {
    // 1xx Informational
    CONTINUE(100, "Continue: The client should continue with its request."),
    SWITCHING_PROTOCOLS(101, "Switching Protocols: The server is changing protocols as requested by the client."),
    PROCESSING(102, "Processing: The server has received and is processing the request, but no response is available yet."),
    EARLY_HINTS(103, "Early Hints: The server is sending preliminary headers before finalizing the request."),

    // 2xx Success
    SUCCESS(200, "Success: The request was successfully received, understood, and accepted."),
    CREATED(201, "Created: The request has been fulfilled and resulted in a new resource being created."),
    ACCEPTED(202, "Accepted: The request has been accepted for processing, but the processing has not been completed."),
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information: The server is a transforming proxy that received a 200 OK from its origin, but is returning a modified version of the origin's response."),
    NO_CONTENT(204, "No Content: The server has successfully fulfilled the request and there is no content to send."),
    RESET_CONTENT(205, "Reset Content: The server has fulfilled the request and the client should reset the document view."),
    PARTIAL_CONTENT(206, "Partial Content: The server is delivering only part of the resource due to a range header sent by the client."),
    MULTI_STATUS(207, "Multi-Status: The message body contains separate response codes for each sub-request."),
    ALREADY_REPORTED(208, "Already Reported: The members of a DAV binding have already been enumerated in a preceding part of the multistatus response."),
    IM_USED(226, "IM Used: The server has fulfilled a GET request for the resource, and the response is a representation of the result of one or more instance-manipulations applied to the current instance."),

    // 3xx Redirection
    MULTIPLE_CHOICES(300, "Multiple Choices: There are multiple options for the resource that the client may follow."),
    MOVED_PERMANENTLY(301, "Moved Permanently: The resource has been permanently moved to a new URI."),
    MOVED_TEMPORARILY(302, "Moved Temporarily: The resource resides temporarily under a different URI."),
    SEE_OTHER(303, "See Other: The response to the request can be found under another URI using the GET method."),
    NOT_MODIFIED(304, "Not Modified: The resource has not been modified since the last request."),
    USE_PROXY(305, "Use Proxy: The requested resource must be accessed through the proxy given by the Location field."),
    TEMPORARY_REDIRECT(307, "Temporary Redirect: The request should be repeated with another URI; however, future requests should still use the original URI."),
    PERMANENT_REDIRECT(308, "Permanent Redirect: The request and all future requests should be directed to the given URI."),

    // 4xx Client Error
    BAD_REQUEST(400, "Bad Request: The server cannot or will not process the request due to an apparent client error."),
    UNAUTHORIZED(401, "Unauthorized: Authentication is required and has failed or has not yet been provided."),
    PAYMENT_REQUIRED(402, "Payment Required: Reserved for future use; indicates that payment is required to access the resource."),
    FORBIDDEN(403, "Forbidden: The server understands the request but refuses to authorize it."),
    NOT_FOUND(404, "Not Found: The requested resource could not be found."),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed: The method specified in the request is not allowed for the resource."),
    NOT_ACCEPTABLE(406, "Not Acceptable: The resource is only capable of generating content not acceptable according to the Accept headers sent in the request."),
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required: The client must first authenticate itself with the proxy."),
    REQUEST_TIMEOUT(408, "Request Timeout: The server timed out waiting for the request."),
    CONFLICT(409, "Conflict: The request could not be completed due to a conflict with the current state of the resource."),
    GONE(410, "Gone: The resource requested is no longer available and will not be available again."),
    LENGTH_REQUIRED(411, "Length Required: The server refuses to accept the request without a defined Content-Length."),
    PRECONDITION_FAILED(412, "Precondition Failed: One or more conditions given in the request header fields evaluated to false."),
    REQUEST_TOO_LONG(413, "Request Entity Too Large: The request is larger than the server is willing or able to process."),
    REQUEST_URI_TOO_LONG(414, "Request-URI Too Long: The URI provided was too long for the server to process."),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type: The request entity has a media type which the server or resource does not support."),
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable: The client has asked for a portion of the file, but the server cannot supply that portion."),
    EXPECTATION_FAILED(417, "Expectation Failed: The server cannot meet the requirements of the Expect request-header field."),
    INSUFFICIENT_SPACE_ON_RESOURCE(419, "Insufficient Space on Resource: The server is unable to store the representation needed to complete the request."),
    METHOD_FAILURE(420, "Method Failure: The method could not be performed on the resource because the server is unable to process the contained instructions."),
    MISDIRECTED_REQUEST(421, "Misdirected Request: The request was directed at a server that is not able to produce a response."),
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity: The server understands the content type of the request entity, but was unable to process the contained instructions."),
    LOCKED(423, "Locked: The source or destination resource of a method is locked."),
    FAILED_DEPENDENCY(424, "Failed Dependency: The method could not be performed on the resource because the server is unable to process the contained instructions."),
    TOO_EARLY(425, "Too Early: The server is unwilling to risk processing a request that might be replayed."),
    UPGRADE_REQUIRED(426, "Upgrade Required: The client should switch to a different protocol as specified in the Upgrade header."),
    PRECONDITION_REQUIRED(428, "Precondition Required: The origin server requires the request to be conditional."),
    TOO_MANY_REQUESTS(429, "Too Many Requests: The user has sent too many requests in a given amount of time."),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large: The server is unwilling to process the request because its header fields are too large."),
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons: The requested resource is unavailable due to legal demands such as censorship or government-mandated blocks."),

    // 5xx Server Error
    SERVER_ERROR(500, "Server Error: The server encountered an unexpected condition that prevented it from fulfilling the request."),
    NOT_IMPLEMENTED(501, "Not Implemented: The server does not support the functionality required to fulfill the request."),
    BAD_GATEWAY(502, "Bad Gateway: The server received an invalid response from the upstream server."),
    SERVICE_UNAVAILABLE(503, "Service Unavailable: The server is currently unable to handle the request due to temporary overload or maintenance."),
    GATEWAY_TIMEOUT(504, "Gateway Timeout: The server did not receive a timely response from an upstream server."),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported: The server does not support the HTTP protocol version used in the request."),
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates: The server has an internal configuration error and is unable to select a representation."),
    INSUFFICIENT_STORAGE(507, "Insufficient Storage: The server is unable to store the representation needed to complete the request."),
    LOOP_DETECTED(508, "Loop Detected: The server detected an infinite loop while processing the request."),
    NOT_EXTENDED(510, "Not Extended: Further extensions to the request are required for the server to fulfill it."),
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required: The client needs to authenticate to gain network access.");

    @var int code;
    @var String description;
    @var Family family;

    HttpStatus(int code, String description) {
        this.code = code;
        this.description = description;
        this.family = Response.Status.Family.familyOf(code);
    }

    public static HttpStatus fromStatusCode(int statusCode) {
       return HttpStatus.values().stream().filter(s -> s.code == statusCode).findFirst().orElse(null);
    }

}
