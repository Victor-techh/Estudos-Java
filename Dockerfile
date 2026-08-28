# Estágio 1: Build da aplicação utilizando Maven
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app

# Copia arquivos de dependência e código fonte
COPY pom.xml .
COPY src ./src

# Empacota a aplicação ignorando testes unitários
RUN mvn clean package -DskipTests

# Estágio 2: Execução com imagem leve JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia o JAR gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]