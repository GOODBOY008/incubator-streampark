/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.streampark.console.core.enums;

import org.apache.streampark.common.enums.ApplicationType;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationTypeTest {

    @Test
    void testEnumValuesAndProperties() {
        assertEquals(-1, ApplicationType.UNKNOWN.getType());
        assertEquals("unknown", ApplicationType.UNKNOWN.getName());
        assertEquals(1, ApplicationType.STREAMPARK_FLINK.getType());
        assertEquals("StreamPark Flink", ApplicationType.STREAMPARK_FLINK.getName());
        assertEquals(2, ApplicationType.APACHE_FLINK.getType());
        assertEquals("Apache Flink", ApplicationType.APACHE_FLINK.getName());
        assertEquals(3, ApplicationType.STREAMPARK_SPARK.getType());
        assertEquals("StreamPark Spark", ApplicationType.STREAMPARK_SPARK.getName());
        assertEquals(4, ApplicationType.APACHE_SPARK.getType());
        assertEquals("Apache Spark", ApplicationType.APACHE_SPARK.getName());
    }

    @Test
    void testOfMethod() {
        assertEquals(ApplicationType.UNKNOWN, ApplicationType.of(-1));
        assertEquals(ApplicationType.STREAMPARK_FLINK, ApplicationType.of(1));
        assertEquals(ApplicationType.APACHE_FLINK, ApplicationType.of(2));
        assertEquals(ApplicationType.STREAMPARK_SPARK, ApplicationType.of(3));
        assertEquals(ApplicationType.APACHE_SPARK, ApplicationType.of(4));
        // Edge: unknown value
        assertEquals(ApplicationType.UNKNOWN, ApplicationType.of(999));
    }

    @Test
    void testSerializationDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Serialize
        String json = mapper.writeValueAsString(ApplicationType.APACHE_FLINK);
        assertTrue(json.contains("APACHE_FLINK") || json.contains("2"));
        // Deserialize by name
        ApplicationType typeByName = mapper.readValue("\"APACHE_FLINK\"", ApplicationType.class);
        assertEquals(ApplicationType.APACHE_FLINK, typeByName);
        // Deserialize by ordinal (should fail, so fallback to UNKNOWN)
        ApplicationType typeByInvalid = mapper.readValue("999", ApplicationType.class);
        assertEquals(ApplicationType.UNKNOWN, typeByInvalid);
    }

    @Test
    void testNullHandling() {
        // The of() method does not accept null, so we expect a NullPointerException
        assertThrows(NullPointerException.class, () -> ApplicationType.of((Integer) null));
    }
}
