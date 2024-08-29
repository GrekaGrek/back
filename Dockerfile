FROM eclipse-temurin:21-jdk-alpine as builder

WORKDIR /back

COPY gradle /workspace/gradle
COPY gradle.properties /workspace/
COPY settings.gradle.kts /workspace/
COPY build.gradle.kts /workspace/

RUN ./gradlew dependencies --no-daemon

COPY src /back/src

RUN ./gradlew build --no-daemon --stacktrace

FROM eclipse-temurin:21-jre-alpine

WORKDIR /back

COPY --from=builder /back/build/libs/*.jar back-0.0.1-SNAPSHOT.jar

RUN java -Djarmode=layertools -jar back-0.0.1-SNAPSHOT.jar extract

COPY --from=builder /back/build/libs/application/dependencies/ ./
COPY --from=builder /back/build/libs/application/spring-boot-loader/ ./
COPY --from=builder /back/build/libs/application/snapshot-dependencies/ ./
COPY --from=builder /back/build/libs/application/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]