FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jdk
WORKDIR /app

# 👇 COPIAR EL JAR
COPY --from=build /app/target/*.jar app.jar

# 👇 COPIAR EL WALLET (ESTO ES LO QUE TE FALTA)
COPY wallet /app/wallet

# 👇 CONFIGURAR ORACLE
ENV TNS_ADMIN=/app/wallet

EXPOSE 8081

CMD ["java", "-jar", "app.jar"]