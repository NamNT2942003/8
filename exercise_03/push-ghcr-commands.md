# HƯỚNG DẪN CLI & LỆNH THỰC THI: BUILD VÀ PUSH IMAGE LÊN GHCR

- **Dịch vụ**: `payment-service`
- **Phiên bản (Tag)**: `1.0.0`
- **Registry**: GitHub Container Registry (`ghcr.io`)
- **Tài khoản GitHub**: `NamNT2942003` (Namespace GHCR: `namnt2942003`)
- **Package URL**: [https://github.com/NamNT2942003?tab=packages](https://github.com/NamNT2942003?tab=packages) hoặc [https://github.com/users/namnt2942003/packages/container/package/payment-service](https://github.com/users/namnt2942003/packages/container/package/payment-service)

---

## 1. CÁC BƯỚC THIẾT LẬP PERSONAL ACCESS TOKEN (PAT) TRÊN GITHUB

1. Truy cập: [https://github.com/settings/tokens](https://github.com/settings/tokens)
2. Chọn **Generate new token (classic)**.
3. Đặt tên Note: `GHCR_CI_PUSH_TOKEN`.
4. Tích chọn các phạm vi quyền (Scopes) tối thiểu cần thiết:
   -  `write:packages` (Ghi và push image lên GitHub Container Registry)
   -  `read:packages` (Đọc và tải image từ GHCR)
   -  `repo` (Toàn quyền quản trị repository liên kết)
5. Nhấn **Generate token** và lưu mã Token (định dạng: `ghp_xxxxxxxxxxxx`).

---

## 2. CHUỖI LỆNH TERMINAL / DOCKER CLI ĐÃ THỰC THI

```bash
# -------------------------------------------------------------
# Bước 1: Di chuyển vào thư mục dự án payment-service
# -------------------------------------------------------------
cd C:\Devop\16

# -------------------------------------------------------------
# Bước 2: Đăng nhập vào GHCR bằng Personal Access Token (PAT)
# Lưu ý: Sử dụng cờ --password-stdin để bảo mật mã token
# -------------------------------------------------------------
echo "ghp_YOUR_PERSONAL_ACCESS_TOKEN_HERE" | docker login ghcr.io -u namnt2942003 --password-stdin
# Kết quả mong đợi:
# Login Succeeded

# -------------------------------------------------------------
# Bước 3: Build Docker Image Multi-stage và gắn Tag GHCR chuẩn 1.0.0
# Lưu ý: Toàn bộ namespace (namnt2942003) và tên repository phải viết thường
# -------------------------------------------------------------
docker build -t ghcr.io/namnt2942003/payment-service:1.0.0 -t ghcr.io/namnt2942003/payment-service:latest .

# -------------------------------------------------------------
# Bước 4: Kiểm tra image và kích thước trong kho lưu trữ local
# -------------------------------------------------------------
docker images | grep payment-service
# Kết quả hiển thị:
# ghcr.io/namnt2942003/payment-service   1.0.0      a49fc261cd14   286MB

# -------------------------------------------------------------
# Bước 5: Đẩy (Push) image lên GitHub Container Registry
# -------------------------------------------------------------
docker push ghcr.io/namnt2942003/payment-service:1.0.0
docker push ghcr.io/namnt2942003/payment-service:latest

# -------------------------------------------------------------
# Bước 6: Kiểm tra package trên giao diện GitHub
# URL: https://github.com/NamNT2942003?tab=packages
# -------------------------------------------------------------
```

---

## 3. LOG MẪU TERMINAL KHI PUSH THÀNH CÔNG

```text
The push refers to repository [ghcr.io/namnt2942003/payment-service]
a1b2c3d4e5f6: Pushed
b2c3d4e5f6a1: Pushed
c3d4e5f6a1b2: Pushed
1.0.0: digest: sha256:6143ffe34861e0c414f9b9148ef45937af755064690da506d11ee93a24cc6ed6 size: 949
```

