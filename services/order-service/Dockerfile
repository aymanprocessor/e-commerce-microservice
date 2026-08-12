FROM maven:3.9.11-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn package -DskipTests -B


FROM eclipse-temurin:21-jre-jammy

RUN addgroup --system spring && adduser --system --group spring

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT [ "java", "-jar", "app.jar" ]