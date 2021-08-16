package org.nakvaksin.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/api/v1")
@RequestScoped
public class AdaSMSCallbackResource {
    private final Logger log = LoggerFactory.getLogger(AdaSMSCallbackResource.class);

    @GET
    @Path("/adasms-callback")
    public Response callback(@Context UriInfo info) {
        log.debug("AdaSMS callback...");

        MultivaluedMap<String, String> params = info.getQueryParameters();

        for (String key : params.keySet()) {
            log.debug("{} : {}", key, params.getFirst(key));
        }

        return Response.ok().build();
    }
}
