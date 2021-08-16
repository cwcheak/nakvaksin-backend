package org.nakvaksin.service;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.nakvaksin.service.exception.MySejahteraResponseExceptionMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "mysejahtera-api")
@RegisterProvider(value = MySejahteraResponseExceptionMapper.class, priority = 50)
public interface MySejahteraService {

    @POST
    @Path("/login")
    Response login(@QueryParam("username") String username, @QueryParam("password") String password);

    @GET
    @Path("/v1/mobileApp/vaccinationEmployeeInfo")
    @Consumes("application/json")
    String getUserProfile(@HeaderParam("x-auth-token") String token);

    @GET
    @Path("/v1/mobileApp/vaccination/processFlow")
    @Consumes("application/json")
    Response getVaccinationStatus(@HeaderParam("x-auth-token") String token);
}
