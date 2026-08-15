# HHKUNGFU CloudStream v1.1

Bản này sửa lỗi build hiện tại:
`Could not find method apk() for arguments [com.lagradost:cloudstream3:pre-release]`.

Root build tạo rõ configuration `apk` trước khi thêm CloudStream stub.


Build fix: removed the unused NiceHttp dependency from the plugin module. The latest build log showed Gradle failing solely because com.github.Blatzar:NiceHttp:0.3.5 could not be resolved. The provider source does not reference NiceHttp directly.
