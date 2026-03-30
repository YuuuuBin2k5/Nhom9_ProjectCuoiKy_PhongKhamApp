# UC_25_UpdateQueueStatus: Cập nhật trạng thái hàng đợi

## 1. Thông tin chung
- **Mã Use Case**: UC_UC_20
- **Tên Use Case**: Cập nhật trạng thái hàng đợi
- **Tác nhân**: Doctor
- **Tiền điều kiện**: Bác sĩ (trực tiếp) hoặc hệ thống (gián tiếp) đang thay đổi vòng lặp trạng thái khám của một hồ sơ.

## 2. Luồng sự kiện chính
1. Bác sĩ (hoặc hệ thống trigger từ UC_UC_19) gửi tín hiệu cần cập nhật vị trí Queue của một bệnh nhân đang IN-PROGRESS hoặc WAITING.
2. Chuyển đổi các trạng thái đích hợp lệ:
   - **WAITING**: Bệnh nhân tạm thời đi vệ sinh/ra ngoài, đẩy lùi báo hiệu tạm ngừng nhưng không mất hoàn toàn thứ tự.
   - **COMPLETED**: Hoàn tất khám, kick bệnh nhân khỏi danh sách để rảnh slot.
   - **SKIPPED/NO-SHOW**: Bệnh nhân vắng mặt, lướt qua.
3. Hệ thống xác nhận và ghi nhận state mới xuống Database.
4. Hệ thống gọi method tính toán lại Estimated Waiting Time (Thời gian chờ ước tính) cho toàn cục những người còn lại trong Queue.
5. Socket push event tới tất cả client (Patient) tương ứng ở sảnh chờ.

## 3. Các trường hợp ngoại lệ
- **Lỗi mạng (Disconnect)**: Sự cố mạng làm Client không bắt được WebSocket. App sẽ tự Fallback xuống Long-polling HTTP.

## 4. Hậu điều kiện
- Quản trị tốt vòng đời dữ liệu hàng đợi.
