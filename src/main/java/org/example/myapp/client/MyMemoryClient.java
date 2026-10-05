package org.example.myapp.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/get")
@RegisterRestClient(configKey = "mymemory-api")
public interface MyMemoryClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    MyMemoryResponse translate(
            @QueryParam("q") String text,
            @QueryParam("langpair") String langPair
    );

    @JsonIgnoreProperties(ignoreUnknown = true)
    class MyMemoryResponse {
        public ResponseData responseData;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ResponseData {
            public String translatedText;
        }
    }
}