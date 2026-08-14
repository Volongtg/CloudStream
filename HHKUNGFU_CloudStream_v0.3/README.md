# HHKUNGFU CloudStream v0.3

Provider CloudStream cho https://hhkungfu.ee

## Build

Yêu cầu JDK 11. Có thể dùng Android Studio/Gradle hoặc GitHub Actions.

Lệnh build:

```bash
gradle HHKungfu:make
```

GitHub Actions trong `.github/workflows/build.yml` tự build plugin và upload artifact `HHKungfu.cs3`.

## Lưu ý

Provider chỉ đọc dữ liệu và URL video được website cung cấp công khai. Không vượt DRM, đăng nhập hoặc cơ chế bảo vệ truy cập.
