# =========================================================================
# Multi-stage Dockerfile cho QuickBite delivery-service
# =========================================================================

# Stage 1: Build mã nguồn bằng JDK 17 Alpine
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Sao chép mã nguồn và công cụ đóng gói
COPY . .

# Cấp quyền thực thi và biên dịch ứng dụng Spring Boot
RUN chmod +x ./gradlew && ./gradlew bootJar

# Stage 2: Runtime với JRE 17 Alpine siêu nhẹ (tối ưu hóa dung lượng & bảo mật)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Chỉ copy file .jar đã đóng gói từ Stage 1, loại bỏ toàn bộ mã nguồn thô
COPY --from=builder /app/build/libs/*.jar app.jar

# Khai báo cổng dịch vụ
EXPOSE 8085

# Khởi chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]

