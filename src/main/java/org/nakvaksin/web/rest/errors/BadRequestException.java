package org.nakvaksin.web.rest.errors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

public class BadRequestException extends WebApplicationException {
    private static final long serialVersionUID = 1L;

    public BadRequestException(String message) {
        super(Response.status(BAD_REQUEST).entity(message).build());
    }
}
