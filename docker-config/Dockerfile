FROM maven AS MAVEN_TOOL
COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21
COPY --from=MAVEN_TOOL /tmp/target/Study21-jar-with-dependencies.jar application.jar