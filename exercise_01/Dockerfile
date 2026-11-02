# Dockerfile kiểm chứng khả năng build container thông qua Docker-outside-of-Docker (DooD)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Khai báo thông tin metadata
LABEL maintainer="QuickBite DevOps Team"
LABEL description="QuickBite Service Verification Container for DooD Socket"

# Copy mã nguồn hoặc tài nguyên kiểm thử
COPY src/ /app/src/

# Lệnh kiểm thử mặc định khi container khởi chạy
CMD ["echo", "Docker container built successfully inside Self-hosted Runner via DooD socket!"]
