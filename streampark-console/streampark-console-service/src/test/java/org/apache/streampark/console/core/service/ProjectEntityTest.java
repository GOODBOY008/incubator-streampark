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

package org.apache.streampark.console.core.service;

import org.apache.streampark.common.enums.ApplicationType;
import org.apache.streampark.console.core.entity.Project;
import org.apache.streampark.console.core.service.impl.ProjectServiceImpl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@ContextConfiguration(classes = {ProjectServiceImpl.class})
public class ProjectEntityTest {

    @Autowired
    private ProjectServiceImpl projectService;

    @Test
    void testPersistAndRetrieveApplicationType() {
        for (ApplicationType type : ApplicationType.values()) {
            Project project = new Project();
            project.setName("Test Project " + type.name());
            project.setType(type);
            projectService.save(project);

            Project loaded = projectService.getById(project.getId());
            assertNotNull(loaded);
            assertEquals(type, loaded.getType());
        }
    }

    @Test
    void testNullTypePersistence() {
        Project project = new Project();
        project.setName("Null Type Project");
        project.setType(null);
        projectService.save(project);

        Project loaded = projectService.getById(project.getId());
        assertNotNull(loaded);
        assertNull(loaded.getType());
    }

    @Test
    void testUnknownTypePersistence() {
        Project project = new Project();
        project.setName("Unknown Type Project");
        project.setType(ApplicationType.UNKNOWN);
        projectService.save(project);

        Project loaded = projectService.getById(project.getId());
        assertNotNull(loaded);
        assertEquals(ApplicationType.UNKNOWN, loaded.getType());
    }

    // If backward compatibility is needed, add a test for legacy integer values here.
}
