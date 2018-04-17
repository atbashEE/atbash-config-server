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
package be.atbash.config.configserver.config;

import be.atbash.config.configserver.config.converters.*;
import be.atbash.util.exception.AtbashIllegalActionException;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

import javax.enterprise.inject.Typed;
import javax.enterprise.inject.Vetoed;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * @author <a href="mailto:rmannibucau@apache.org">Romain Manni-Bucau</a>
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
@Typed
@Vetoed
public class DefaultConfigBuilder implements ConfigBuilder {
    private ClassLoader forClassLoader;
    private final List<ConfigSource> sources = new ArrayList<>();
    private final Map<Type, MicroProfileTypedConverter<?>> registeredConverters = new HashMap<>();

    public DefaultConfigBuilder() {
        this.registerDefaultConverters();
    }

    @Override
    public ConfigBuilder addDefaultSources() {
        throw new AtbashIllegalActionException("TODO");

    }

    @Override
    public ConfigBuilder addDiscoveredSources() {
        throw new AtbashIllegalActionException("TODO");
    }

    @Override
    public ConfigBuilder forClassLoader(final ClassLoader loader) {
        this.forClassLoader = loader;
        return this;
    }

    @Override
    public ConfigBuilder withSources(final ConfigSource... sources) {
        this.sources.addAll(asList(sources));
        return this;
    }

    @Override
    public ConfigBuilder withConverters(Converter<?>... converters) {
        for (Converter<?> converter : converters) {
            Type typeOfConverter = getTypeOfConverter(converter.getClass());
            registerConverter(typeOfConverter, new MicroProfileTypedConverter<>(converter));
        }
        return this;
    }

    @Override
    public <T> ConfigBuilder withConverter(Class<T> type, int priority, Converter<T> converter) {
        MicroProfileTypedConverter<T> microProfileTypedConverter = new MicroProfileTypedConverter<>(converter, priority);
        return registerConverter(type, microProfileTypedConverter);
    }

    private <T> ConfigBuilder registerConverter(Type type, MicroProfileTypedConverter<T> microProfileTypedConverter) {
        MicroProfileTypedConverter<?> existing = registeredConverters.get(type);
        if (existing == null || microProfileTypedConverter.getPriority() > existing.getPriority()) {
            registeredConverters.put(type, microProfileTypedConverter);
        }
        return this;
    }

    @Override
    public ConfigBuilder addDiscoveredConverters() {
        throw new AtbashIllegalActionException("TODO");
    }

    @Override
    public Config build() {
        List<ConfigSource> configSources = new ArrayList<>();
        if (forClassLoader == null) {
            forClassLoader = Thread.currentThread().getContextClassLoader();
            if (forClassLoader == null) {
                // TODO
                //forClassLoader = DefaultConfigProvider.class.getClassLoader();
            }
        }

        configSources.addAll(sources);

        ConfigImpl config = new ConfigImpl();
        config.addConfigSources(configSources);

        for (Map.Entry<Type, MicroProfileTypedConverter<?>> entry : registeredConverters.entrySet()) {
            config.addConverter(entry.getKey(), entry.getValue());
        }

        return config;
    }

    private void registerDefaultConverters() {
        registeredConverters.put(String.class, new MicroProfileTypedConverter<>(StringConverter.INSTANCE));
        registeredConverters.put(Boolean.class, new MicroProfileTypedConverter<>(BooleanConverter.INSTANCE));
        registeredConverters.put(boolean.class, new MicroProfileTypedConverter<>(BooleanConverter.INSTANCE));
        registeredConverters.put(Double.class, new MicroProfileTypedConverter<>(DoubleConverter.INSTANCE));
        registeredConverters.put(double.class, new MicroProfileTypedConverter<>(DoubleConverter.INSTANCE));
        registeredConverters.put(Float.class, new MicroProfileTypedConverter<>(FloatConverter.INSTANCE));
        registeredConverters.put(float.class, new MicroProfileTypedConverter<>(FloatConverter.INSTANCE));
        registeredConverters.put(Integer.class, new MicroProfileTypedConverter<>(IntegerConverter.INSTANCE));
        registeredConverters.put(int.class, new MicroProfileTypedConverter<>(IntegerConverter.INSTANCE));
        registeredConverters.put(Long.class, new MicroProfileTypedConverter<>(LongConverter.INSTANCE));
        registeredConverters.put(long.class, new MicroProfileTypedConverter<>(LongConverter.INSTANCE));

        registeredConverters.put(URL.class, new MicroProfileTypedConverter<>(URLConverter.INSTANCE));
        registeredConverters.put(Class.class, new MicroProfileTypedConverter<>(ClassConverter.INSTANCE));
    }

    private Type getTypeOfConverter(Class clazz) {
        if (clazz.equals(Object.class)) {
            return null;
        }

        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericInterface;
                if (pt.getRawType().equals(Converter.class)) {
                    Type[] typeArguments = pt.getActualTypeArguments();
                    if (typeArguments.length != 1) {
                        throw new IllegalStateException("Converter " + clazz + " must be a ParameterisedType");
                    }
                    return typeArguments[0];
                }
            }
        }

        return getTypeOfConverter(clazz.getSuperclass());
    }
}
