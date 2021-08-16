package org.nakvaksin.web.rest.errors;

import org.nakvaksin.service.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UserNotFoundExceptionMapper implements ExceptionMapper<UserNotFoundException> {
    @Override
    public Response toResponse(UserNotFoundException exception) {
        exception.printStackTrace();
        return Response.status(Response.Status.NOT_FOUND).entity(exception.getMessage()).build();
    }
}
