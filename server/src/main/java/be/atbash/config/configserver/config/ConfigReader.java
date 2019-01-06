/*
 * Copyright 2018-2019 Rudy De Busscher
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
package be.atbash.config.configserver.config;

import be.atbash.config.source.AtbashConfigSource;
import be.atbash.config.source.ConfigType;
import be.atbash.util.StringUtils;
import be.atbash.util.resource.ResourceUtil;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
@ApplicationScoped
public class ConfigReader {

    @Inject
    private ResourceUtil resourceUtil;

    //@ConfigProperty(name = "applications")  TODO This is not working
    private List<String> applications;

    //@ConfigProperty(name = "rootDirectory") TODO This is not working
    private String rootDirectory;

    @PostConstruct
    public void init() {
        applications = Arrays.asList(ConfigProvider.getConfig().getValue("applications", String[].class));
        rootDirectory = ConfigProvider.getConfig().getValue("rootDirectory", String.class);
    }

    public Config getConfig(String application, String stage) {
        if (!applications.contains(application)) {
            return null;
        }
        DefaultConfigBuilder builder = new DefaultConfigBuilder();

        List<ConfigSource> sources = getConfigSources(application, stage);
        builder.withSources(sources.toArray(new ConfigSource[0]));
        return builder.build();
    }

    public List<ConfigSource> getConfigSources(String application, String stage) {

        List<ConfigSource> result = new ArrayList<>();

        for (ConfigType configType : ConfigType.values()) {

            String configLocation = rootDirectory + "/" + application + "/" + application + configType.getSuffix();
            if (resourceUtil.resourceExists(configLocation)) {
                result.add(new AtbashConfigSource(configType, configLocation, 150));
            }
        }

        if (StringUtils.hasText(stage)) {
            for (ConfigType configType : ConfigType.values()) {

                String configLocation = rootDirectory + "/" + application + "/" + application + "-" + stage + configType.getSuffix();
                if (resourceUtil.resourceExists(configLocation)) {
                    result.add(new AtbashConfigSource(configType, configLocation, 200));
                }
            }

        }

        return result;
    }

}
