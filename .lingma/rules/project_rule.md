**Add rules to help the model understand your coding preferences, including preferred frameworks, coding styles, and other conventions.**
**Note: This file only applies to the current project, with each file limited to 10,000 characters. If you do not need to commit this file to a remote Git repository, please add it to .gitignore.**

## Revised Migration Plan: Mybatis-Plus Enum Usage Refactoring

**Goal:** To leverage MyBatis-Plus's automatic enum conversion by using `@EnumValue` annotation for better maintainability and consistency.

**Phase 1: Analysis and Identification**

1.  **Identify Enum Structures (streampark-common):**
    *   Examine each enum in `streampark-common/src/main/java/org/apache/streampark/common/enums/` to understand its internal structure (fields, constructors, methods).
    *   Determine if each enum has a field that naturally represents its persistent value (e.g., an `int` or `String` code).


2.  **Identify Enum Structures (streampark-console):**
    *   Examine each enum in `streampark-console/streampark-console-service/src/main/java/org/apache/streampark/console/core/enums/` to understand its internal structure.
    *   Determine if each enum has a field that naturally represents its persistent value.

 
3.  **Identify Classes Using Enums for Persistence:**
    *   These are the classes that will benefit from the automatic enum conversion. The primary candidates are MyBatis-Plus entity classes (annotated with `@TableName`) and any mapper interfaces that directly use these enums in their method signatures or XML mappings.
  

**Phase 2: Refactoring Implementation**

1.  **Apply `@EnumValue` Annotation to Enums:**
    *   All enums identified in Phase 1 (both `streampark-common` and `streampark-console`) have been reviewed and the `@EnumValue` annotation has been applied to their respective persistent value fields where necessary.

2.  **Refactor Entity Classes to Use Enum Types Directly:**
    *   Entity classes (e.g., `FlinkApplication.java`, `SparkApplication.java`, `FlinkCluster.java`, `AlertConfig.java`) have been refactored.
    *   Fields that previously stored integer or string representations of enum values (e.g., `private Integer jobType;`) have been updated to directly use the corresponding enum type (e.g., `private FlinkJobType jobType;`).
    *   Getter and setter methods for these fields have been updated to reflect the change in type, removing any manual conversion logic (e.g., `FlinkJobType.of(jobType)` or `enumInstance.getValue()`).
    *   Any other code within the entity classes that accessed these fields has been updated to use the enum type directly.

**Phase 3: Verification and Testing**

1.  **Unit Tests:**
    *   For each refactored enum, ensure there are unit tests that cover:
        *   Correct mapping of enum values to their persistent representation (the annotated field).
        *   Correct retrieval of enum instances from their persistent representation.
        *   Edge cases (e.g., unknown values, nulls if applicable).
    *   If existing tests don't cover this, create new dedicated unit tests.
    *   **Example Test Structure:**
        ```java
        // Example for ApplicationType
        import org.junit.jupiter.api.Test;
        import static org.junit.jupiter.api.Assertions.assertEquals;
        import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue; // Import if needed for test setup

        public class ApplicationTypeTest {
            @Test
            void testEnumValueMapping() {
                // Assuming ApplicationType.STREAMPARK_FLINK has @EnumValue on 'type'
                assertEquals(1, ApplicationType.STREAMPARK_FLINK.getType());
                // You might need to simulate MyBatis-Plus behavior or use a test utility
                // to verify the actual persistence/retrieval if not directly exposed.
                // For now, verifying the internal value is sufficient for unit testing the enum itself.
            }
        }
        ```
    *   Identify existing test files by searching for `src/test/java/**/*.java` in both `streampark-common` and `streampark-console` modules.

2.  **Integration Tests:**
    *   Run existing integration tests that involve database persistence of entities containing these enums.
    *   If no such tests exist, consider creating simple integration tests to verify end-to-end persistence.

3.  **Build and Lint:**
    *   Run a full build of the project using Maven: `mvn clean install`
    *   Run any project-specific linting or code style checks to maintain code quality. (e.g., `mvn spotless:check` or similar if configured).

### 6. 枚举值序列化为前端返回值（全局配置推荐）

结合本项目实际，已采用 Jackson 作为主序列化方案（详见 pom.xml 依赖与 ObjectMapper 使用）。建议采用全局配置的方式，将枚举值序列化为前端所需格式，避免每个枚举单独加注解或重写 toString，统一风格、便于维护。

#### 6.1 Jackson 全局配置（Spring Boot 项目，推荐）

E.g
```java
@Bean
public Jackson2ObjectMapperBuilderCustomizer customizer() {
    return builder -> builder.featuresToEnable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
}
```
- 配置后，所有枚举类型在序列化为 JSON 时，自动调用其 `toString()` 方法。
- 如需自定义序列化值，可在枚举类中重写 `toString()`。
- 如需特殊枚举自定义序列化，也可在该枚举类字段加 `@JsonValue` 注解。

#### 6.2 建议
- 推荐全局统一采用 Jackson 配置，保证前后端数据一致性。
- Fastjson、Gson 等方案在本项目未见直接使用，无需额外配置。

**Phase 4: Final Verification**

1.  **Build Project:**
    *   Execute a complete Maven build to ensure all modules compile correctly: `mvn clean install`
    *   Verify that no compilation errors occur during the build process.

2.  **Run Unit Tests:**
    *   Execute all unit tests to ensure no functionality has been broken: `mvn test`
    *   Pay special attention to any tests related to the refactored enum classes and entity classes.

3.  **Run Integration Tests:**
    *   Execute integration tests, especially those involving database operations with the refactored entities: `mvn verify`
    *   Ensure that all database persistence and retrieval operations work correctly with the updated enum mappings.

4.  **Run E2E Tests:**
    *   If available, execute end-to-end tests to verify the complete workflow: `mvn test -Dtest=*E2E*`
    *   This ensures that the changes work correctly in a real application scenario.

技术参考：[MyBatis-Plus 官方文档-自动映射枚举](https://baomidou.com/guides/auto-convert-enum/)
