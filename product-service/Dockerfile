# Stage 1: Build the JAR files
FROM openjdk:17-oracle as builder

WORKDIR /app

# Copy the source code
COPY . .

# Build the JAR files
RUN ./mvnw clean package -DskipTests

# Stage 2: Build the final image
FROM eclipse-temurin:17-jre-focal

WORKDIR /app

# Copy the JAR files from the builder stage
COPY --from=builder /app/target/*.jar ./product-service.jar

# Set the command to run the application
CMD ["java", "-jar", "product-service.jar"]