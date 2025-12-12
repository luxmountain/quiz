# Tài liệu chức năng: Bài kiểm tra đầu vào (Placement Test) & phân loại level

## 0) Tổng quan

Placement Test là bài kiểm tra ngắn giúp hệ thống **đề xuất level học** phù hợp cho người dùng (Beginner/Elementary/Intermediate/Advanced).

- **Nguồn câu hỏi**: Firebase Realtime Database node `placementTest`.
- **Lưu kết quả**: cục bộ bằng SharedPreferences để:
  - không bắt user làm lại ở lần mở app sau
  - tự động chọn level tương ứng trong Dashboard

Lưu ý: UI có thông tin “thời gian làm bài”, nhưng hiện trạng code **chưa có countdown timer chạy thật** trong quá trình làm bài.

---

## a) Luồng hoạt động + ràng buộc nghiệp vụ

### Luồng (activity diagram – Mermaid)

```mermaid
flowchart TD
    A[MainActivity onCreate] --> B[PlacementTestManager.hasCompletedTest()]
    B -- false --> C[Mở PlacementTestActivity]
    B -- true --> D[Không mở, vào Dashboard bình thường]

    C --> E[PlacementTestViewModel.loadPlacementTest()]
    E --> F{Tải Firebase OK?}
    F -- Không --> G[Hiện ErrorScreen]
    F -- Có --> H[Hiện InstructionsScreen]

    H --> I{User chọn?}
    I -- Start --> J[Bắt đầu làm bài]
    I -- Skip --> K[finish() quay về MainActivity]

    J --> L[Hiện câu hỏi + options]
    L --> M[User chọn 1 đáp án]
    M --> N[Enable nút Tiếp/Hoàn thành]
    N --> O{Còn câu tiếp?}
    O -- Có --> P[nextQuestion()]
    O -- Không --> Q[completeTest()]

    Q --> R[Tính correctCount + score%]
    R --> S[calculateLevel() theo passingScores]
    S --> T[Lưu result vào SharedPreferences]
    T --> U[Hiện TestResultScreen]
    U --> V[User bấm "Bắt đầu học" -> finish()]
```

### Ràng buộc / Business rules

- **Chỉ hỏi 1 lần khi chưa làm test**: `MainActivity` tự mở Placement Test nếu `hasCompletedTest() == false`.
- **Có thể bỏ qua test**: nút “Bỏ qua (Học từ cơ bản)” → quay về Dashboard.
- **Mỗi câu chỉ chọn 1 lần (assessment mode)**:
  - Khi đã chọn đáp án cho câu hiện tại → khoá không cho đổi (UI kiểm tra `!uiState.isAnswered`).
- **Nút Tiếp/Hoàn thành chỉ enable khi đã chọn đáp án**.
- **Tính level dựa trên số câu đúng**:
  - Ngưỡng lấy từ `placementTest.passingScores`.
  - Mapping levelId → tiếng Việt: beginner=Cơ bản, elementary=Sơ cấp, intermediate=Trung cấp, advanced=Nâng cao.
- **Score phần trăm**: tính bằng integer division: `(correctCount * 100) / totalQuestions`.

---

## b) Thiết kế UI/UX

### 1) Instructions screen

- Icon + tiêu đề + mô tả.
- Card “Hướng dẫn” (bullets) + info: tổng số câu, thời gian (phút).
- CTA:
  - “Bắt đầu làm bài”
  - “Bỏ qua (Học từ cơ bản)”

### 2) Test screen

- TopAppBar (chỉ hiển thị khi đang làm bài):
  - Tiêu đề “Bài kiểm tra đầu vào”
  - LinearProgressIndicator theo tiến độ câu hỏi
- Question navigator (LazyRow): nút tròn 1..N để nhảy câu.
- Card câu hỏi:
  - ưu tiên hiển thị `questionVi`
  - nếu `question` khác `questionVi` thì hiển thị thêm dòng tiếng Anh
- Option cards:
  - đổi border/background theo trạng thái selected
  - (hiện trạng) không hiển thị đúng/sai trong lúc làm bài
- Buttons:
  - “Trước” (nếu không phải câu 1)
  - “Tiếp”/“Hoàn thành” (enable khi đã chọn)

### 3) Result screen

- Hiển thị:
  - Điểm %
  - Tổng câu/đúng/sai
  - Level đề xuất (tiếng Việt)
- CTA: “Bắt đầu học” → quay về Dashboard.

---

## c) Giải pháp kỹ thuật (MVVM) + điểm mới + thách thức

### Kiến trúc

- ViewModel: `PlacementTestViewModel` (AndroidViewModel + StateFlow)
  - Load test từ Firebase bằng `FirebaseRepository.getPlacementTest()`.
  - Quản lý state: câu hiện tại, đáp án user, completion.
  - Tính kết quả và gọi `PlacementTestManager.saveTestResult()`.
- Repository:
  - `FirebaseRepository`: đọc node `placementTest`.
- Local storage:
  - `PlacementTestManager`: SharedPreferences `placement_test_prefs`.

### Tích hợp với Dashboard

- `MainViewModel` đọc `PlacementTestManager.getRecommendedLevel()`.
- Mapping levelId → tên level Firebase:
  - beginner→Beginner, elementary→Elementary, intermediate→Intermediate, advanced→Advanced.
- Tự chọn level mặc định phù hợp khi load levels.

### Điểm mới / novelty

- Onboarding “định hướng level” ngay lần đầu dùng app, giảm sai lệch khi user tự chọn level.
- Assessment mode: không hiển thị đáp án/giải thích trong lúc làm bài để tránh “học vẹt” trong bài test.

### Thách thức khi triển khai

- Đồng bộ luồng điều hướng lần đầu: `MainActivity` vừa render UI vừa có thể launch PlacementTestActivity.
- Cấu trúc dữ liệu Firebase phải đúng schema (passingScores, questions, correctAnswer index).
- Timer: đã có field `timeRemaining`/`duration` trong model nhưng chưa có countdown → nếu cần giới hạn thời gian thật phải bổ sung coroutine timer + UX xử lý.

---

## d) Hướng phát triển

- Countdown timer thật + auto-submit khi hết giờ.
- Cho phép làm lại test theo policy (ví dụ: 30 ngày/lần) + nút “Làm lại” trong Settings.
- Adaptive test (câu hỏi thay đổi theo câu trước) để phân loại nhanh hơn.
- Đồng bộ kết quả lên cloud khi có tài khoản đăng nhập.

---

## Phụ lục: File liên quan

- `app/src/main/java/com/uilover/project247/PlacementTestActivity/PlacementTestActivity.kt`
- `app/src/main/java/com/uilover/project247/PlacementTestActivity/Model/PlacementTestViewModel.kt`
- `app/src/main/java/com/uilover/project247/PlacementTestActivity/screens/PlacementTestScreen.kt`
- `app/src/main/java/com/uilover/project247/data/models/PlacementTest.kt`
- `app/src/main/java/com/uilover/project247/data/repository/FirebaseRepository.kt`
- `app/src/main/java/com/uilover/project247/data/repository/PlacementTestManager.kt`
- `app/src/main/java/com/uilover/project247/DashboardActivity/MainActivity.kt`
- `app/src/main/java/com/uilover/project247/DashboardActivity/Model/MainViewModel.kt`
