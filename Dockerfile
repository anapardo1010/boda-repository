# Usar imagen base de Java 17
FROM eclipse-temurin:17-jdk-alpine AS build

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Dar permisos de ejecución
RUN chmod +x mvnw

# Descargar dependencias
RUN ./mvnw dependency:go-offline

# Copiar código fuente
COPY src ./src

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests

# Etapa final - imagen más pequeña
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Exponer puerto por defecto
EXPOSE 8080

# Opciones de JVM recomendadas para instancias con ~512MB
ENV JAVA_OPTS="-Xms384m -Xmx384m -XX:+UseSerialGC -XX:ActiveProcessorCount=1"

# Puerto por defecto (puede sobrescribirse con -e PORT=...)
ENV PORT=8080

# Ejecutar el JAR usando shell para expandir variables
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
