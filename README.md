# HHKUNGFU CloudStream v0.5

Provider CloudStream cho HHKUNGFU.

## Cấu trúc

```text
.github/workflows/build.yml
HHKungfu/
  build.gradle.kts
  src/main/kotlin/com/volong/hhkungfu/HHKungfuProvider.kt
build.gradle.kts
settings.gradle.kts
gradle.properties
repo/
```

Có thể thêm provider khác bằng cách tạo thêm module, ví dụ `AnimeHay/` hoặc `PhimMoi/`, rồi thêm module đó vào `settings.gradle.kts`.

## Build

GitHub Actions dùng Java 11 và Gradle 7.4.2.

CloudStream Gradle plugin được ghim vào commit `7116299` thay vì `master-SNAPSHOT` để tránh phụ thuộc vào một bản SNAPSHOT thay đổi theo thời gian.

Lệnh build:

```bash
gradle HHKungfu:make --no-daemon
```

Artifact được tạo là file `.cs3`.

## Provider

Mã provider HHKungfu được giữ nguyên từ project gốc của người dùng.
