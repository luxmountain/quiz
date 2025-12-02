# GitHub Actions CI/CD Setup

## PR Build Check

File đã được tạo: `.github/workflows/pr-build-check.yml`

### Chức năng:
- ✅ Tự động build khi có Pull Request vào nhánh `main`
- ✅ Chạy unit tests
- ✅ Upload build reports nếu build lỗi
- ✅ Comment vào PR nếu build thất bại

### Cách hoạt động:
1. Khi tạo PR vào `main`, workflow sẽ tự động chạy
2. Build project với Gradle
3. Chạy tests
4. Nếu thất bại → upload logs + comment vào PR
5. Nếu thành công → hiển thị green check ✅

## Bật Branch Protection

Để **bắt buộc** build pass trước khi merge, làm theo các bước sau:

### Bước 1: Vào Repository Settings
1. Truy cập: `https://github.com/luxmountain/quiz/settings`
2. Chọn **Branches** ở menu bên trái

### Bước 2: Thêm Branch Protection Rule
1. Click **Add branch protection rule**
2. Branch name pattern: `main`
3. Bật các options sau:
   - ✅ **Require a pull request before merging**
   - ✅ **Require status checks to pass before merging**
     - Search và chọn: `Build & Test`
   - ✅ **Require branches to be up to date before merging**
   - (Optional) **Require conversation resolution before merging**
4. Click **Create** hoặc **Save changes**

### Kết quả:
- ❌ Không thể merge PR nếu build failed
- ✅ Chỉ merge được khi build passed
- 🔒 Bảo vệ nhánh `main` khỏi code lỗi

## Testing Workflow

Để test workflow này:
1. Tạo một branch mới
2. Commit và push code
3. Tạo Pull Request vào `main`
4. Workflow sẽ tự động chạy
5. Xem kết quả ở tab "Checks" trong PR

## Troubleshooting

Nếu build lỗi do `google-services.json`:
- Đảm bảo file `google-services.json` đã được commit
- Hoặc sử dụng GitHub Secrets để lưu Firebase config

Nếu cần thêm secrets:
1. Settings → Secrets and variables → Actions
2. New repository secret
3. Thêm các secrets cần thiết
