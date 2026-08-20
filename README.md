# HHKUNGFU CloudStream Repository v7

This repository contains the HHKUNGFU CloudStream extension.

## v7 fixes
- Adds CloudStream `mainPage` entries so the provider Home screen is populated.
- Keeps the URL normalization fixes from v6 for absolute, relative and protocol-relative URLs.
- Rejects invalid `javascript:`, `data:`, `mailto:` and `tel:` URLs before network access.
- Normalizes HTML entities and whitespace in URLs.
- GitHub Actions publishes `HHKungfu.cs3`, `plugins.json`, and `repo.json` to the `builds` branch.

CloudStream repository URL:

`https://raw.githubusercontent.com/Volongtg/CloudStream/builds/repo.json`

## Muvio / Nuvio

Đã thêm `muvio-hhkungfu/` — Stremio Addon dành cho Muvio/Nuvio Android. Xem README bên trong thư mục này để triển khai.

## v9 – StreamFree extractor fix

- Giữ nguyên `mainPage` và URL normalization của bản trước.
- Thêm `StreamFreeExtractor` và đăng ký extractor trong plugin.
- Khi HHKUNGFU trả iframe `streamfree.vip/embed/...`, CloudStream sẽ chuyển URL đó sang extractor riêng thay vì phụ thuộc extractor có sẵn.
- Chỉ trả kết quả `loadLinks()` khi extractor thực sự phát hiện được link video.

Lưu ý: việc build APK/plugin trong môi trường hiện tại không thực hiện được vì Gradle cần tải dependency từ Internet; mã nguồn đã được đóng gói trong ZIP.
