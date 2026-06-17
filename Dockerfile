# 多阶段构建 前端打包进 Spring Boot 单 jar 部署
FROM node:22-alpine AS front
WORKDIR /build/front
COPY front/package.json front/package-lock.json ./
RUN npm ci
COPY front/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-21-alpine AS backend
WORKDIR /build
COPY pom.xml ./
COPY server/pom.xml server/
RUN mvn -q -pl server -am dependency:go-offline -B
COPY server/ server/
RUN rm -rf server/src/main/resources/static && mkdir -p server/src/main/resources/static
COPY --from=front /build/front/dist/ server/src/main/resources/static/
RUN mvn -q -pl server -am package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
USER app
COPY --from=backend /build/server/target/server-*.jar /app/app.jar
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
