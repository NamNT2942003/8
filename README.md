# BÁO CÁO THỰC HÀNH TỔNG HỢP (SESSION 08)
## Chủ đề: CI/CD Pipeline với Docker, DooD, Multi-stage Build & GitHub Container Registry (GHCR)

- **Học viên:** NamNT (NamNTHE176286)
- **Repository:** [NamNT2942003/8](https://github.com/NamNT2942003/8)
- **Hệ sinh thái:** QuickBite Microservices Platform & Cloud Native Automation

---

## I. MỤC TIÊU BÀI THỰC HÀNH TỔNG HỢP

Sau khi hoàn thành bài thực hành Session 08, học viên làm chủ các kỹ năng thực chiến:
1. **Docker-outside-of-Docker (DooD)**: Hiểu sâu kiến trúc Client - Server của Docker, phân tích log lỗi daemon và cấu hình bind mount socket `/var/run/docker.sock` cho Self-hosted Runner.
2. **Dockerfile Multi-stage Build**: Nắm vững cú pháp `COPY --from=...`, tối ưu dung lượng image từ >500MB xuống 256MB (giảm 50%) và loại bỏ 100% mã nguồn thô khỏi môi trường Production.
3. **GitHub Container Registry (GHCR)**: Thiết lập Personal Access Token (PAT) với phạm vi `write:packages`, tuân thủ quy tắc namespace chữ thường và đẩy image phiên bản `1.0.0` lên GHCR.
4. **Bảo mật Zero-Trust trong CI/CD**: Khai thác biến môi trường mặc định `secrets.GITHUB_TOKEN` kèm khối `permissions: packages: read/write` để tự động hóa pull/push mà không để lộ PAT cá nhân.
5. **Chẩn đoán hiệu năng & Cache**: Phân tích sự cô lập của Container OverlayFS dẫn tới sự cố mất cache Gradle (`~/.gradle`) trong Multi-stage build và đề xuất giải pháp Docker BuildKit Cache Mount.
6. **Xây dựng Pipeline End-to-End từ con số 0**: Tự thiết kế toàn diện luồng CI/CD cho service mới `delivery-service` tích hợp build, push và verify container runtime.

---

## II. DANH MỤC NGHIỆM THU CHI TIẾT (BÀI 1 - BÀI 6)

| Bài tập | Nội dung chuyên môn | Thư mục & Tệp bàn giao | Mục tiêu kỹ thuật cốt lõi | Trạng thái nghiệm thu |
| :--- | :--- | :--- | :--- | :---: |
| **Bài 1** | Sửa lỗi chia sẻ Docker Socket (DooD) | [`exercise_01/`](exercise_01/)<br>`quickbite-runner` | Bind mount `/var/run/docker.sock:/var/run/docker.sock`, sửa lỗi `Cannot connect to daemon` | **ĐẠT (Passed)** |
| **Bài 2** | Hoàn thiện Dockerfile Multi-stage Build | [`exercise_02/`](exercise_02/)<br>`cart-service` | Điền `FROM eclipse-temurin:17-jre-alpine` và `COPY --from=builder /app/build/libs/*.jar app.jar`, giảm 50% size | **ĐẠT (Passed)** |
| **Bài 3** | Build & Push Image lên GHCR bằng PAT | [`exercise_03/`](exercise_03/)<br>`payment-service` | Tạo PAT `write:packages`, tag `ghcr.io/namnt2942003/payment-service:1.0.0` (lowercase), push GHCR | **ĐẠT (Passed)** |
| **Bài 4** | CI/CD tự động Pull Image từ GHCR | [`exercise_04/`](exercise_04/)<br>`payment-service` | Dùng `secrets.GITHUB_TOKEN`, khối `permissions: packages: read`, chạy verify dưới 20s | **ĐẠT (Passed)** |
| **Bài 5** | Phân tích sự cố mất Cache Multi-stage | [`exercise_05/`](exercise_05/)<br>`order-service` | Báo cáo RCA gửi CTO: Phân tích Filesystem Isolation, OverlayFS vs Host Storage | **ĐẠT (Passed)** |
| **Bài 6** | Pipeline CI/CD hoàn chỉnh (Multi-stage + GHCR) | [`exercise_06/`](exercise_06/)<br>`delivery-service` | Dựng Pipeline E2E từ A-Z: Build Multi-stage, Push GHCR, khởi chạy detached & verify HTTP health | **ĐẠT (Passed)** |

---

## III. TÓM TẮT GIẢI PHÁP KỸ THUẬT CÁC BÀI TẬP

### 1. Bài 1: Sửa lỗi cấu hình chia sẻ Docker Socket (DooD)
- **Vấn đề**: Runner chạy trong container văng lỗi `Cannot connect to the Docker daemon at unix:///var/run/docker.sock`.
- **Nguyên nhân**: Docker Client trong runner không tìm thấy socket IPC vì chưa được ánh xạ từ máy host.
- **Giải pháp**: Cấu hình file [`exercise_01/docker-compose.yml`](exercise_01/docker-compose.yml) mount:
  ```yaml
  volumes:
    - /var/run/docker.sock:/var/run/docker.sock
    - ./runner-work:/_work
  ```

---

### 2. Bài 2: Hoàn thiện Dockerfile Multi-stage Build (`cart-service`)
- **Vấn đề**: Điền khuyết vị trí `[___]` trong Dockerfile tối ưu hóa kích thước image.
- **Giải pháp**: File [`exercise_02/Dockerfile`](exercise_02/Dockerfile):
  - Vị trí 1: `FROM eclipse-temurin:17-jre-alpine` (Chỉ dùng JRE cho Runtime).
  - Vị trí 2: `COPY --from=builder /app/build/libs/*.jar app.jar` (Chuyển giao tệp `.jar` từ Stage 1).
- **Đo lường thực tế**:
  - `single-stage`: **511 MB** (Lộ mã nguồn thô).
  - `multi-stage`: **256 MB** (**Giảm 50% dung lượng**, bảo mật 100%).

---

### 3. Bài 3: Thiết lập quy trình Build và Push Image lên GHCR (`payment-service`)
- **Vấn đề**: Lỗi xác thực `denied: requested access to the resource is denied` khi push image.
- **Giải pháp**: 
  1. Tạo PAT (classic) với quyền `write:packages`, `read:packages`.
  2. Xác thực CLI: `echo "$PAT" | docker login ghcr.io -u namnt2942003 --password-stdin`.
  3. Tag định dạng chuẩn lowercase: `ghcr.io/namnt2942003/payment-service:1.0.0`.
  4. File hướng dẫn CLI: [`exercise_03/push-ghcr-commands.md`](exercise_03/push-ghcr-commands.md).

---

### 4. Bài 4: Tích hợp CI/CD tự động Pull Image từ GHCR
- **Vấn đề**: Pipeline kéo image từ GHCR bị lỗi `unauthenticated`.
- **Giải pháp**: File [`.github/workflows/ci.yml`](exercise_04/.github/workflows/ci.yml):
  - Khai báo quyền bảo mật:
    ```yaml
    permissions:
      contents: read
      packages: read
    ```
  - Tự động đăng nhập bằng `secrets.GITHUB_TOKEN` và `${{ github.actor }}`.
  - Xử lý tên repo owner chữ hoa thành chữ thường: `echo "${{ github.repository_owner }}" | tr '[:upper:]' '[:lower:]'`.
  - Thời gian chạy verify cực nhanh: **14 giây** (thay vì 2-3 phút biên dịch lại).

---

### 5. Bài 5: Phân tích sự cố mất Cache trong Multi-stage CI/CD (`order-service`)
- **Vấn đề**: CTO thắc mắc tại sao pipeline mất tới 4-5 phút tải lại thư viện dù runner có sẵn `~/.gradle`.
- **Phân tích RCA ([`exercise_05/Bao_Cao_Phan_Tich_Cache_MultiStage.md`](exercise_05/Bao_Cao_Phan_Tich_Cache_MultiStage.md))**:
  - Tiến trình build chạy trong một **Container Builder tạm thời** có hệ thống tệp ảo độc lập (OverlayFS).
  - Thư mục `/root/.gradle` bên trong container trống rỗng, không thể truy cập vào `~/.gradle` của máy host.
  - Cấu hình DooD (`/var/run/docker.sock`) chỉ là kênh điều khiển (Control Plane), không tự động mount dữ liệu (Data Plane).
  - Đề xuất giải pháp: Sử dụng **Docker BuildKit Cache Mount** (`RUN --mount=type=cache,target=/root/.gradle ./gradlew bootJar`).

---

### 6. Bài 6: Xây dựng luồng CI/CD hoàn chỉnh (Multi-stage + GHCR) (`delivery-service`)
- **Dịch vụ mới**: `delivery-service` chạy trên cổng 8085.
- **Dockerfile Multi-stage**: [`exercise_06/Dockerfile`](exercise_06/Dockerfile)
- **Workflow End-to-End**: [`exercise_06/.github/workflows/main.yml`](exercise_06/.github/workflows/main.yml)
  - Tuần tự trong 1 job: `Checkout` $\rightarrow$ `Login GHCR` $\rightarrow$ `Build Image` $\rightarrow$ `Push Image` $\rightarrow$ `Run Detached Container` $\rightarrow$ `Verify Health Check`.
  - Quyền hạn: `permissions: packages: write`.
  - Image được phát hành thành công: `ghcr.io/namnt2942003/delivery-service:1.0.0`.

---

## IV. HƯỚNG DẪN TRUY CẬP & KIỂM TRA

1. **Toàn bộ mã nguồn & Báo cáo**: [https://github.com/NamNT2942003/8](https://github.com/NamNT2942003/8)
2. **GitHub Container Registry (Packages)**: [https://github.com/NamNT2942003?tab=packages](https://github.com/NamNT2942003?tab=packages)
3. **Lịch sử chạy CI/CD (GitHub Actions)**: [https://github.com/NamNT2942003/8/actions](https://github.com/NamNT2942003/8/actions)
