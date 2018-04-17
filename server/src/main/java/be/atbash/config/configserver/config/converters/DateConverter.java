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

import org.eclipse.microprofile.config.spi.Converter;

import javax.annotation.Priority;
import javax.enterprise.inject.Vetoed;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
@Priority(1)
@Vetoed
public class DateConverter implements Converter<Date> {

    public static final DateConverter INSTANCE = new DateConverter();

    @Override
    public Date convert(String value) {
        if (value != null) {
            try {
                return SimpleDateFormat.getDateInstance().parse(value);
            } catch (ParseException pe) {
                throw new IllegalArgumentException(pe);
            }
        }
        return null;
    }
}
