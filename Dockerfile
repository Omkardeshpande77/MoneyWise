FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY target/moneywise-0.0.1-SNAPSHOT.jar moneywise.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "moneywise.jar"]