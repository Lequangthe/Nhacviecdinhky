# Nhắc việc định kỳ

Ứng dụng Android nhắc nhở các **công việc định kỳ**: thay dầu, đăng kiểm, bảo hiểm, thay bộ lọc, sao lưu... Thiết lập khoảng thời gian (mỗi X ngày, hàng tuần hoặc hàng tháng) và ứng dụng sẽ thông báo cho bạn trước khi đến hạn.

Jetpack Compose · Material 3 · Room · WorkManager · Glance (widget).

> 🇬🇧 Reminders for your recurring tasks. Set an interval (every X days, weekly or monthly) and the app reminds you before it's due.

---

## Cài đặt

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Có trên F-Droid" height="80">](https://f-droid.org/packages/com.quangthe.nhacviec/)

Hoặc tải về APK từ [phiên bản phát hành](https://codeberg.org/kapoue/MainTask/releases).

---

## Tính năng

- **Công việc định kỳ**: khoảng thời gian cố định, hàng tuần hoặc hàng tháng
- **Công việc một lần** (one-shot), có hoặc không có ngày mục tiêu
- **Lịch sử** hoàn thành theo từng công việc
- **Thông báo** có thể tùy chỉnh (trước 3 ngày hoặc 1 ngày, tùy chọn giờ) với các hành động "Xong" / "Hoãn"
- **Widget** có thể cuộn trên màn hình chính (với nút thêm nhanh)
- **Ô cài đặt nhanh** (Quick Settings Tile)
- **5 chủ đề** màu sắc + chế độ tối
- **Xuất / nhập** bản sao lưu (bao gồm cả lịch sử)
- **Đa ngôn ngữ**: tiếng Việt và tiếng Anh

Không quảng cáo, không trình theo dõi, không tài khoản. Mọi thứ luôn ở trên thiết bị của bạn.

---

## Công nghệ sử dụng

- **Ngôn ngữ**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Cơ sở dữ liệu**: Room (SQLite)
- **Thông báo**: WorkManager
- **Widget**: Glance

---

## Biên dịch từ mã nguồn

Yêu cầu: **JDK 17**, **Android SDK** (API 35, minSdk 26).

```bash
# APK debug
./gradlew assembleDebug

# APK release (chưa ký nếu không cấu hình keystore)
./gradlew assembleRelease
```

APK được tạo ra trong `app/build/outputs/apk/`.

---

## Bản quyền

[GPL-3.0-or-later](LICENSE) — phần mềm tự do.

Bản quyền © 2026 Kapoué.
