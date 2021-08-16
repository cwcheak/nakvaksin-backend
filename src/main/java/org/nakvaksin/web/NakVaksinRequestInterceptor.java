package org.nakvaksin.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import java.io.IOException;

// NOT IN USE FOR NOW
public class NakVaksinRequestInterceptor implements ReaderInterceptor {
    private final Logger log = LoggerFactory.getLogger(NakVaksinRequestInterceptor.class);

    @Context
    private UriInfo uriInfo;

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        MultivaluedMap<String, String> headers = context.getHeaders();

        for (String key : headers.keySet()) {
            log.debug("{} : {}", key, headers.get(key).get(0));
        }

        log.debug("Path : {}", uriInfo.getPath());

        // String userAgent = headers.get("user-agent");
        return context.proceed();
    }
}
