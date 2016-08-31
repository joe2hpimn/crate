/*
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */

package io.crate.operation.collect.files;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import io.crate.types.DataTypes;
import org.elasticsearch.node.internal.InternalSettingsPreparer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SummitsIterable implements Supplier<Iterable<?>> {

    private final Set<SummitsContext> summits;
    private static final Splitter TAB_SPLITTER = Splitter.on("\t");

    public SummitsIterable() {
        try (InputStream input = InternalSettingsPreparer.class.getResourceAsStream("/config/names.txt")) {
            summits = new HashSet<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, Charsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    List<String> parts = TAB_SPLITTER.splitToList(line);
                    summits.add(new SummitsContext(
                        parts.get(0),
                        safeParseStr2ToInt(parts.get(1)),
                        safeParseStr2ToInt(parts.get(2)),
                        parseCoordinates(parts.get(3)),
                        parts.get(4),
                        parts.get(5),
                        parts.get(6),
                        parts.get(7),
                        safeParseStr2ToInt(parts.get(8)))
                    );
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot populate the sys.summits table", e);
        }
    }

    private Integer safeParseStr2ToInt(String value) {
        return value.isEmpty() ? null : Integer.parseInt(value);
    }

    private Double[] parseCoordinates(String value) {
        return value.isEmpty() ? null : DataTypes.GEO_POINT.value(value);
    }

    @Override
    public Iterable<?> get() {
        return summits;
    }
}
