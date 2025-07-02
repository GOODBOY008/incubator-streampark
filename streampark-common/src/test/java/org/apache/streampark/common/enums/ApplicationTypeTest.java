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

package org.apache.streampark.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationTypeTest {

    @Test
    void testGetValue() {
        assertEquals(1, ApplicationType.STREAMPARK_FLINK.getType());
        assertEquals(2, ApplicationType.APACHE_FLINK.getType());
        assertEquals(3, ApplicationType.STREAMPARK_SPARK.getType());
        assertEquals(4, ApplicationType.APACHE_SPARK.getType());
        assertEquals(-1, ApplicationType.UNKNOWN.getType());
    }

    @Test
    void testOf() {
        assertEquals(ApplicationType.STREAMPARK_FLINK, ApplicationType.of(1));
        assertEquals(ApplicationType.APACHE_FLINK, ApplicationType.of(2));
        assertEquals(ApplicationType.STREAMPARK_SPARK, ApplicationType.of(3));
        assertEquals(ApplicationType.APACHE_SPARK, ApplicationType.of(4));
        assertEquals(ApplicationType.UNKNOWN, ApplicationType.of(99)); // Test unknown value
    }
}
