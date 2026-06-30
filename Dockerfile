FROM eclipse-temurin:25-jdk-ubi10-minimal

WORKDIR /app

COPY . .

RUN microdnf install -y findutils && \
    ./gradlew clean build -x test

EXPOSE 8080

CMD ["java", "-jar", "./build/libs/cm-service-0.0.1-SNAPSHOT.jar"]