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
package be.atbash.config.configserver.config.converters;

import be.atbash.config.configserver.config.ConfigImpl;

import java.lang.reflect.Array;
import java.util.List;

public class ImplicitArrayConverter {
    private final ConfigImpl config;

    public ImplicitArrayConverter(ConfigImpl config) {
        this.config = config;
    }

    public Object convert(String value, Class<?> asType) {
        Class<?> elementType = asType.getComponentType();
        List<?> elements = config.convertList(value, elementType);
        Object arrayInst = Array.newInstance(elementType, elements.size());
        for (int i = 0; i < elements.size(); i++) {
            Array.set(arrayInst, i, elements.get(i));
        }
        return arrayInst;
    }
}
