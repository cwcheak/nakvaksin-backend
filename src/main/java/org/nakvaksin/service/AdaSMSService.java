package org.nakvaksin.service;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.nakvaksin.domain.AdaSMSResponse;
import org.nakvaksin.service.exception.AdaSMSResponseExceptionMapper;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@RegisterRestClient(configKey = "adasms-api")
@RegisterProvider(value = AdaSMSResponseExceptionMapper.class, priority = 50)
public interface AdaSMSService {

    @POST
    @Path("/v1/send")
    AdaSMSResponse sendSMS(
        @QueryParam("_token") String token,
        @QueryParam("phone") String phone,
        @QueryParam("message") String message);
}
