# Usa una imagen base de OpenJDK
FROM maven:3.8.8-eclipse-temurin-17 AS build

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR generado en el contenedor
COPY target/email-service-0.0.1-SNAPSHOT.jar email-service.jar

# Expone el puerto en el que se ejecutará el servicio
EXPOSE 8086

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "email-service.jar"]
