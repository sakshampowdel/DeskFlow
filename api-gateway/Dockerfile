
FROM eclipse-temurin:25-jre-jammy

WORKDIR /app

RUN useradd -ms /bin/bash deskflow
USER deskflow

COPY --chown=deskflow:deskflow target/*.jar app.jar

ENTRYPOINT ["java", \
            "-XX:+UseShenandoahGC", \
            "-XX:+UnlockExperimentalVMOptions", \
            "-XX:MaxRAMPercentage=75.0", \
            "-jar", "app.jar"]