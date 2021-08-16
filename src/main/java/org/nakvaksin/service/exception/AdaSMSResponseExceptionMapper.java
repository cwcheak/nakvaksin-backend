package org.nakvaksin.service.exception;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;

@Priority(1000)
public class AdaSMSResponseExceptionMapper implements ResponseExceptionMapper {
    private final Logger log = LoggerFactory.getLogger(AdaSMSResponseExceptionMapper.class);

    @Override
    public RuntimeException toThrowable(Response res) {
        int status = res.getStatus();
        String msg = getBody(res);
        log.error("AdaSMS API Error Response: [Status {}] {}", status, msg);

        RuntimeException re;
        switch (status) {
            case 401:
                re = new AuthenticationFailedException(msg);
                break;
            default:
                re = new WebApplicationException(status);
        }

        return re;
    }

    private String getBody(Response response) {
        ByteArrayInputStream is = (ByteArrayInputStream) response.getEntity();
        byte[] bytes = new byte[is.available()];
        is.read(bytes, 0, is.available());
        String body = new String(bytes);
        return body;
    }
}
