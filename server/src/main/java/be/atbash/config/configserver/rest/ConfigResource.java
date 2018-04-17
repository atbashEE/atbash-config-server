/*
 * Copyright 2018 Rudy De Busscher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.atbash.config.configserver.rest;

import be.atbash.config.configserver.config.ConfigReader;
import be.atbash.ee.security.octopus.jwt.JWTEncoding;
import be.atbash.ee.security.octopus.jwt.encoder.JWTEncoder;
import be.atbash.ee.security.octopus.jwt.parameter.JWTParameters;
import be.atbash.ee.security.octopus.jwt.parameter.JWTParametersBuilder;
import org.eclipse.microprofile.config.Config;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/config")
@RequestScoped // Required for KumuluzEE since @ApplicationScoped doesn't work in jersey/hk2.
public class ConfigResource {

    @Inject
    private ConfigReader configReader;

    @Inject
    private JWTEncoder jwtEncoder;

    @GET
    @Path("/{application}")
    @Produces("text/plain")
    public Response getApplicationConfigValues(@PathParam("application") String application, @QueryParam("stage") String stage) {
        Config config = configReader.getConfig(application, stage);
        if (config == null) {
            return Response.noContent().build();
        }
        Map<String, String> configValues = new HashMap<>();

        config.getPropertyNames().forEach(key -> configValues.put(key, config.getValue(key, String.class)));

        JWTParameters parameters = JWTParametersBuilder.newBuilderFor(JWTEncoding.NONE)
                .build();

        String data = jwtEncoder.encode(configValues, parameters);
        return Response.ok(data).build();
    }
}