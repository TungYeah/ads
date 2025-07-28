# Bước 1: Chọn base image (JDK image) cho Maven build
FROM eclipse-temurin:17-jdk-alpine AS builder

# Bước 2: Cài đặt Maven
RUN apk add --no-cache maven

# Bước 3: Đặt thư mục làm việc trong container
WORKDIR /app

# Bước 4: Copy pom.xml và tải các dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Bước 5: Copy mã nguồn vào container và build ứng dụng
COPY ./src /app/src
RUN mvn clean package -DskipTests

# Bước 6: Chạy ứng dụng với JRE
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy file jar đã build từ builder
COPY --from=builder /app/target/ads-0.0.1-SNAPSHOT.jar app.jar

# Bước 7: Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
