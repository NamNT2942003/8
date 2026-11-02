# BÁO CÁO KỸ THUẬT: THIẾT LẬP QUY TRÌNH BUILD VÀ PUSH IMAGE LÊN GHCR

- **Dự án**: QuickBite Microservices Platform
- **Service**: `payment-service`
- **Phiên bản release**: `1.0.0`
- **Container Registry**: GitHub Container Registry (`ghcr.io`)
- **Tác giả**: NamNT (NamNTHE176286)
- **GitHub Profile**: [NamNT2942003](https://github.com/NamNT2942003)
- **Package URL**: [https://github.com/users/namnt2942003/packages/container/package/payment-service](https://github.com/users/namnt2942003/packages/container/package/payment-service)

---

## 1. PHÂN TÍCH NGUYÊN NHÂN LỖI XÁC THỰC `denied: requested access to the resource is denied`

### 1.1. Hiện tượng lỗi
Khi lập trình viên thực hiện câu lệnh:
```bash
docker push payment-service:1.0.0
# hoặc
docker push ghcr.io/namnt2942003/payment-service:1.0.0
```
Terminal lập tức phản hồi mã lỗi `403 Forbidden` / `401 Unauthorized`:
```text
denied: requested access to the resource is denied
```

### 1.2. Phân tích nguyên nhân gốc rễ (Root Cause)
1. **GitHub đã chấm dứt xác thực bằng mật khẩu tài khoản**:
   - Kể từ ngày 13/8/2021, GitHub chính thức khai tử việc sử dụng mật khẩu cá nhân (Account Password) để xác thực qua Git CLI và Docker CLI. Mọi tương tác với GHCR đều bắt buộc phải thông qua **Personal Access Token (PAT)**.
2. **Thiếu phạm vi quyền (Scope Permissions) của Token**:
   - Khi tạo Token, nếu lập trình viên không tích chọn quyền `write:packages`, tài khoản sẽ không có quyền tạo hoặc đẩy các layer container lên namespace cá nhân trên GHCR.
3. **Quy tắc đặt Tag của Docker & GHCR (Namespace Conventions)**:
   - Nếu chỉ gõ `docker push payment-service:1.0.0`, Docker daemon mặc định hiểu rằng bạn đang muốn push lên Docker Hub công cộng (`docker.io/library/payment-service`).
   - Để đẩy lên GHCR, bắt buộc tên image phải có tiền tố registry `ghcr.io/` kèm theo **namespace của người dùng/tổ chức viết thường hoàn toàn**:
     $$\texttt{ghcr.io/<github\_username\_lowercase>/<service\_name>:<tag>}$$
   - *Lưu ý*: Docker Registry API bắt buộc tất cả ký tự trong tên repository phải là chữ thường (`lowercase`). Nếu sử dụng chữ hoa (`NamNT2942003`), Docker CLI sẽ chặn ngay tại máy: `invalid reference format: repository name must be lowercase`.

---

## 2. QUY TRÌNH THIẾT LẬP PERSONAL ACCESS TOKEN (PAT) CHUẨN BẢO MẬT

1. Đăng nhập vào GitHub cá nhân > Chọn Avatar góc trên bên phải > **Settings**.
2. Chọn **Developer Settings** ở menu bên trái > **Personal access tokens** > **Tokens (classic)**.
3. Nhấp vào **Generate new token** > **Generate new token (classic)**.
4. Đặt tên Token: `GHCR_DOCKER_PUSH`.
5. Tích chọn các Scope bắt buộc:
   -  `write:packages`: Cho phép push image lên GitHub Container Registry.
   -  `read:packages`: Cho phép pull image từ GHCR.
   -  `repo`: Cần thiết nếu image liên kết với mã nguồn trong kho lưu trữ riêng tư.
6. Nhấn **Generate token** và sao chép chuỗi mã Token bí mật (bắt đầu bằng `ghp_`).

---

## 3. CÁC BƯỚC THỰC THI DÒNG LỆNH (CLI EXECUTION)

### Bước 1: Xác thực Docker CLI với GHCR bằng PAT
Thay vì nhập trực tiếp token qua bàn phím (dễ lộ trong lịch sử shell), sử dụng cơ chế truyền qua STDIN:

```bash
echo "ghp_YOUR_PAT_TOKEN_HERE" | docker login ghcr.io -u namnt2942003 --password-stdin
```
*Kết quả xuất ra terminal:*
```text
Login Succeeded
```

### Bước 2: Build Multi-stage Image và gắn Tag GHCR
Sử dụng Dockerfile Multi-stage tinh gọn (JDK 21 cho Stage Build, JRE 21 Alpine cho Stage Runtime) để đóng gói `payment-service`:

```bash
docker build -t ghcr.io/namnt2942003/payment-service:1.0.0 -t ghcr.io/namnt2942003/payment-service:latest .
```

### Bước 3: Kiểm tra Image đã được tạo
```bash
docker images | findstr payment-service
```
*Kết quả kiểm tra:*
```text
ghcr.io/namnt2942003/payment-service   1.0.0           a49fc261cd14   286MB
ghcr.io/namnt2942003/payment-service   latest          a49fc261cd14   286MB
```

### Bước 4: Đẩy Image lên GitHub Container Registry
```bash
docker push ghcr.io/namnt2942003/payment-service:1.0.0
docker push ghcr.io/namnt2942003/payment-service:latest
```

*Terminal log xuất ra khi push thành công:*
```text
The push refers to repository [ghcr.io/namnt2942003/payment-service]
7c82dae11a3c: Pushed
fa930b51c8b3: Pushed
b912f2c9431e: Pushed
1.0.0: digest: sha256:6143ffe34861e0c414f9b9148ef45937af755064690da506d11ee93a24cc6ed6 size: 949
```

---

## 4. BẰNG CHỨNG XÁC NHẬN TRÊN GITHUB PACKAGES

Sau khi push thành công, package sẽ hiển thị trên trang cá nhân của bạn:
- **Đường dẫn Package**: `https://github.com/users/namnt2942003/packages/container/package/payment-service`
- **Phiên bản tag**: `1.0.0` và `latest`
- **Quyền truy cập (Visibility)**: Có thể vào **Package settings** và chuyển từ `Private` sang `Public` để chia sẻ công khai cho bất kỳ ai có thể `docker pull ghcr.io/namnt2942003/payment-service:1.0.0`.

---

## 5. HƯỚNG DẪN NỘP BÀI LÊN PORTAL

Nộp file văn bản [`push-ghcr-commands.md`](push-ghcr-commands.md) hoặc gửi link Package trực tiếp:
- **URL nộp bài**: `https://github.com/users/namnt2942003/packages/container/package/payment-service`
- Kèm ảnh chụp màn hình terminal lúc chạy lệnh `docker login` và `docker push` thành công.
