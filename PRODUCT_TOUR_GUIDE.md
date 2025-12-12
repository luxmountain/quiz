# Tài liệu chức năng: Product Tour (Spotlight Tour trong Dashboard)

## 0) Tổng quan

Product Tour hướng dẫn người dùng **lần đầu mở app** bằng “spotlight overlay”: làm tối toàn màn hình và khoét “lỗ” (hole) tại đúng UI element cần giới thiệu, kèm tooltip mô tả.

Trạng thái “đã xem tour” được lưu cục bộ bằng SharedPreferences để **không hiển thị lại** ở các lần mở app sau (trừ khi reset).

---

## a) Luồng hoạt động + ràng buộc nghiệp vụ

### Luồng (activity diagram – Mermaid)

```mermaid
flowchart TD
        A[MainActivity onCreate] --> B[Khởi tạo ProductTourManager]
        B --> C{hasCompletedTour()?}
        C -- false --> D[Render MainScreen(showInAppTour=true)]
        C -- true --> E[Render MainScreen(showInAppTour=false)]

        D --> F[InAppTourOverlay hiển thị (Welcome card)]
        F --> G[User bấm "Bắt đầu" hoặc tap để Next]
        G --> H[Highlight Level Selector]
        H --> I[Highlight Topic item đầu tiên]
        I --> J[Highlight Tab Tra từ]
        J --> K[Highlight Tab Ôn tập]
        K --> L[Highlight Tab Hội thoại]
        L --> M[Highlight Tab Thống kê]

        M --> N[Complete/Skip]
        N --> O[setTourCompleted()]
```

### Ràng buộc / Business rules

- Tour chỉ auto-show khi `!hasCompletedTour()`.
- User có thể **Skip** (nút X) ở mọi bước.
- Khi complete/skip → lưu `tour_completed = true`.
- Nếu thiếu “target rect” (UI chưa đo được bounds) → hệ thống fallback:
  - Step “welcome”: không cần target.
  - Step có target nhưng chưa có bounds: overlay vẫn tối; tooltip logic chỉ hiển thị khi có target hợp lệ.

---

## b) Thiết kế UI/UX

### Thành phần UI chính

- Overlay phủ toàn màn hình (zIndex cao).
- Nền tối (alpha ~0.75).
- “Hole” bo góc (corner radius ~12dp, padding ~8dp) để nhìn thấy element đang được hướng dẫn.
- Tooltip card:
  - Emoji + tiêu đề + mô tả.
  - Hiển thị tiến độ `${stepIndex+1}/${totalSteps}` + progress bar.
  - Nút “Tiếp theo” / “Hoàn thành!”.
  - Nút X để bỏ qua.

### Định vị tooltip

- Nếu element nằm nửa dưới màn hình → tooltip ưu tiên hiển thị phía **trên**.
- Nếu element nằm nửa trên màn hình → tooltip ưu tiên hiển thị phía **dưới**.

### Danh sách bước (đúng theo hiện trạng code)

- Welcome (không target)
- Level Selector (`level_selector`)
- Topic item đầu tiên (`topic_item`)
- Tab Tra từ (`tab_search`)
- Tab Học/Board (`tab_board`)
- Tab Hội thoại (`tab_chat`)
- Tab Thống kê (`tab_statistics`)

---

## c) Giải pháp kỹ thuật (Compose) + điểm mới + thách thức

### Kiến trúc & các module

- Local state manager: `ProductTourManager`
  - SharedPreferences: `product_tour_prefs`
  - Key: `tour_completed`
- Trigger:
  - `MainActivity` truyền `showInAppTour = !hasCompletedTour()` vào `MainScreen`.
  - Khi tour complete: gọi `setTourCompleted()`.
- Tracking vị trí UI element:
  - `MainScreen` dùng `Modifier.onGloballyPositioned { positionInRoot() }` để đo `Rect`.
  - `BottomNavigationBarStub` đo bounds từng tab và callback về `MainScreen`.
- Spotlight rendering:
  - `Canvas` vẽ path “full screen rect” trừ “rounded rect” của target (FillType.EvenOdd).

### Điểm mới / novelty

- Spotlight tour được viết thuần Compose (không phụ thuộc thư viện ngoài), dễ tuỳ biến theo UI.
- Cơ chế “auto-tracking” target dựa trên layout measurement (`onGloballyPositioned`).

### Thách thức khi triển khai

- Bounds phụ thuộc layout/scroll: với `LazyColumn`, item có thể thay đổi vị trí theo scroll → cần đảm bảo target đang nằm trong viewport.
- Timing đo layout: ở lần render đầu, có thể chưa có Rect → cần fallback UI (welcome/step tiếp theo).
- Z-index & gesture: overlay bắt event click để next/complete nhưng không làm hỏng UI nền.

---

## d) Hướng phát triển

- Thêm “scroll to target” (nếu target nằm ngoài viewport).
- Vẽ viền highlight quanh hole (hiện code chỉ khoét lỗ, chưa vẽ border riêng).
- Cho phép “xem lại tour” trong phần Settings.
- Tách cấu hình steps ra JSON/Firebase để chỉnh nội dung mà không cần release app.

---

## Phụ lục: File liên quan

- `app/src/main/java/com/uilover/project247/utils/ProductTourManager.kt`
- `app/src/main/java/com/uilover/project247/DashboardActivity/MainActivity.kt`
- `app/src/main/java/com/uilover/project247/DashboardActivity/screens/MainScreen.kt`
- `app/src/main/java/com/uilover/project247/DashboardActivity/components/BottomNavigationBarStub.kt`
- `app/src/main/java/com/uilover/project247/DashboardActivity/components/InAppTourOverlay.kt`
