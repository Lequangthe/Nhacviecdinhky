### Cho phép đặt ngày lần cuối làm cho task định kỳ
- **File thay đổi:** `app/src/main/java/com/quangthe/nhacviec/ui/TaskFormScreen.kt`
- **Chi tiết:** Thêm ô chọn "Lần cuối làm" (DatePicker) cho task định kỳ (DAYS, WEEKLY, MONTHLY). Cho phép user đặt ngày thực tế đã làm lần cuối, không cứng là thời điểm hiện tại. Khi tạo mới hoặc sửa task, có thể chọn ngày bất kỳ trong quá khứ.

### ViewModel hỗ trợ lastDoneAt tuỳ chỉnh
- **File thay đổi:** `app/src/main/java/com/quangthe/nhacviec/viewmodel/TaskViewModel.kt`
- **Chi tiết:** `addTask()` thêm tham số `lastDoneAtMillis` cho phép truyền ngày lần cuối làm. `markDone()` và `markDoneWithUndo()` thêm tham số `doneAtMillis` cho phép backdate khi đánh dấu đã làm.

### Repository hỗ trợ doneAtMillis
- **File thay đổi:** `app/src/main/java/com/quangthe/nhacviec/data/TaskRepository.kt`
- **Chi tiết:** `markDone()` thêm tham số `doneAtMillis` để cập nhật `lastDoneAt` với timestamp tuỳ chỉnh.

### TaskActionScreen: chọn ngày khi "Đã làm"
- **File thay đổi:** `app/src/main/java/com/quangthe/nhacviec/ui/TaskActionScreen.kt`
- **Chi tiết:** Thêm dialog chọn "Hôm nay" hoặc "Chọn ngày khác" khi bấm nút Đã làm. Cho phép backdate markDone.

### String resources mới
- **File thay đổi:** `app/src/main/res/values/strings.xml`, `app/src/main/res/values-vi/strings.xml`
- **Chi tiết:** Thêm `form_last_done_label`, `action_done_title`, `action_done_today`, `action_done_other_date` cho cả tiếng Anh và tiếng Việt.
