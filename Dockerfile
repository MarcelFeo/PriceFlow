# etapa 1 - build da aplicação
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /app
COPY . .
RUN mvn clean package -Dmaven.test.skip=true

# etapa 2 - imagem final
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]