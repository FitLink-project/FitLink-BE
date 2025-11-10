FROM eclipse-temurin:17-jdk
COPY ./build/libs/fitlink-0.0.1-SNAPSHOT.jar fitlink.jar
ENTRYPOINT ["java", "-jar", "fitlink.jar"]