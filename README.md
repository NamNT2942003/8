# Báo cáo Bài 2: Hoàn thiện luồng Workflow CI và lưu trữ Artifact

## 1. Bối cảnh & Mục tiêu
- **Dự án**: QuickBite Microservices Platform.
- **Service**: `user-service`.
- **Mục tiêu**: Điền khuyết chính xác các thành phần còn thiếu trong file Workflow CI (`.github/workflows/ci.yml`), đảm bảo runner tải mã nguồn, build thành công file `.jar` và lưu trữ làm Artifact có thể tải về.

---

## 2. Giải thích chi tiết các vị trí điền khuyết `[___]`

### 1️⃣ Vị trí 1: Môi trường chạy Job (`runs-on`)
* **Đoạn cấu hình gốc**: `runs-on: [___]`
* **Giá trị điền**: `ubuntu-latest`
* **Giải thích**: 
  - `ubuntu-latest` là hệ điều hành runner Linux tiêu chuẩn, phổ biến và ổn định nhất được GitHub Actions cung cấp miễn phí cho các repository public.
  - Hầu hết các service backend (đặc biệt là Java/Spring Boot) trong môi trường production và CI/CD thực tế đều được build và chạy trên nền Linux.

### 2️⃣ Vị trí 2: Action tải mã nguồn về Runner (`uses`)
* **Đoạn cấu hình gốc**:
  ```yaml
  - name: Checkout code
    uses: [___]
  ```
* **Giá trị điền**: `actions/checkout@v4`
* **Giải thích**:
  - `actions/checkout` là action chính thức của GitHub dùng để clone repository vào thư mục làm việc của runner (`$GITHUB_WORKSPACE`), giúp các bước sau (build, test) có thể truy cập được mã nguồn.
  - Phiên bản khuyến nghị sử dụng hiện tại là `@v4` (hỗ trợ Node.js 20 runtime, bảo mật hơn và tải nhanh hơn các phiên bản cũ v2/v3).

### 3️⃣ Vị trí 3: Đường dẫn file Artifact cần lưu trữ (`path`)
* **Đoạn cấu hình gốc**:
  ```yaml
  - name: Upload Artifact
    uses: actions/upload-artifact@v4
    with:
      name: user-service-artifact
      path: [___]
      retention-days: 3
  ```
* **Giá trị điền**: `build/libs/*.jar` (hoặc `build/libs/user-service-0.0.1.jar`)
* **Giải thích**:
  - Sau khi chạy lệnh `./gradlew bootJar`, cấu trúc thư mục đầu ra mặc định của Gradle đối với ứng dụng Spring Boot là `build/libs/`.
  - File đóng gói sẽ nằm tại `build/libs/<tên-dự-án>-<phiên-bản>.jar` (cụ thể là `build/libs/user-service-0.0.1.jar`).
  - Sử dụng pattern `build/libs/*.jar` là best practice trong CI/CD vì nó tự động bắt đúng file `.jar` kể cả khi phiên bản dự án thay đổi trong file cấu hình build.

---

## 3. File Workflow hoàn chỉnh (`.github/workflows/ci.yml`)

```yaml
name: User Service CI
on:
  push:
    branches:
      - main

jobs:
  build_job:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup Java JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build Jar
        run: |
          chmod +x ./gradlew
          ./gradlew bootJar

      - name: Upload Artifact
        uses: actions/upload-artifact@v4
        with:
          name: user-service-artifact
          path: build/libs/*.jar
          retention-days: 3
```

---

## 4. Hướng dẫn Push lên GitHub & Kiểm tra Artifact

1. **Tạo repository trên GitHub cá nhân**:
   - Truy cập: https://github.com/new
   - Tên Repository: `8` (hoặc `quickbite-user-service`)
   - Chọn **Public** -> **Create repository**.
2. **Push mã nguồn**:
   ```bash
   cd C:\Devop\8
   git remote add origin https://github.com/NamNT2942003/8.git
   git push -u origin main
   ```
3. **Kiểm tra kết quả**:
   - Truy cập tab **Actions** tại: `https://github.com/NamNT2942003/8/actions`
   - Bấm vào lần chạy workflow **User Service CI** mới nhất.
   - Kéo xuống phần **Artifacts** ở cuối trang kết quả, bạn sẽ thấy artifact **`user-service-artifact`** (khoảng vài KB đến vài MB) kèm thời gian lưu trữ 3 ngày.
4. Copy link run thành công kèm link repository để nộp bài.
