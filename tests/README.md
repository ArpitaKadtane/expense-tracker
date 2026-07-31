# Test Suite

The automated test suite is located under Maven's standard test source directory:

text
src/test/java/


The tests are executed using:

powershell
.\mvnw.cmd clean test


The `src/test/java` structure is used because it is the standard Maven project layout and allows Maven and Spring Boot to automatically discover and execute the tests.
