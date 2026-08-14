# HHKUNGFU CloudStream

Provider CloudStream cho HHKUNGFU.

## Cấu trúc

Mỗi website là một module/provider riêng. Ví dụ:

- `HHKungfu/`
- `AnimeHay/`
- `PhimMoi/`

`.github/workflows/build.yml` nằm ở thư mục gốc và dùng để build các provider.

## Build

Project này dùng cấu trúc Gradle của CloudStream plugin template cũ, với JDK 11.

GitHub Actions sẽ chạy:

```bash
gradle make --no-daemon
```

và upload các file `.cs3` làm artifact.

## Lưu ý

Provider chỉ đọc dữ liệu và URL video được website cung cấp công khai. Không vượt DRM, đăng nhập hoặc cơ chế bảo vệ truy cập.
