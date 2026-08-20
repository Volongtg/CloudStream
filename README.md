# HHKUNGFU CloudStream

Bản repository đã sửa lỗi `no protocol` khi CloudStream truyền URL tương đối vào provider.

## Link repository cho CloudStream

```text
https://raw.githubusercontent.com/Volongtg/CloudStream/builds/repo.json
```

## Cách cập nhật

1. Upload/push source này lên branch `main` của `Volongtg/CloudStream`.
2. Vào **Actions** và chờ workflow **Build and publish HHKungfu** hoàn tất.
3. Workflow tự build `HHKungfu.cs3`, tạo `plugins.json`, `repo.json` và cập nhật branch `builds`.
4. Trong CloudStream, giữ nguyên URL repository ở trên; không cần đổi link sau mỗi lần cập nhật.

## Lưu ý

- Repository GitHub phải để **Public** để CloudStream truy cập raw files.
- Không đưa GitHub Personal Access Token vào URL.
- Sau bản cập nhật đầu tiên, nếu CloudStream vẫn chạy plugin cũ, gỡ HHKUNGFU cũ rồi cài lại một lần để xóa cache.
