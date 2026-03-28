# UC14: Use Case Bệnh nhân theo dõi lộ trình điều trị (Follow Treatment Path)

## 1. Thông tin chung
- **Mã Use Case**: UC14
- **Tên Use Case**: Theo dõi lộ trình điều trị
- **Tác nhân**: Bệnh nhân
- **Tiền điều kiện**: 
  - Bệnh nhân đã hoàn thành check-in (UC13) và được bác sĩ khám sơ bộ.
  - Bác sĩ đã khởi tạo một phác đồ điều trị (Treatment Plan) cho bệnh nhân.

## 2. Luồng sự kiện chính
1. Bác sĩ thực hiện khám và chẩn đoán, sau đó tạo lộ trình điều trị gồm nhiều bước (ví dụ: Chụp X-Quang -> Nhổ răng -> Trám răng).
2. Bệnh nhân mở ứng dụng di động và truy cập mục **Lộ trình điều trị**.
3. Hệ thống gửi yêu cầu (`GET /api/treatment-plans/my`) để lấy danh sách phác đồ của bệnh nhân.
4. Bệnh nhân **Xem danh sách các bước điều trị** trong lộ trình hiện tại:
   - Hệ thống hiển thị: Tên dịch vụ, Thứ tự thực hiện, Trạng thái từng bước.
5. Bệnh nhân có thể **Xem chi tiết từng bước** để biết thêm thông tin:
   - Mô tả dịch vụ, Tên phòng khám tương ứng, Thời gian bắt đầu dự kiến hoặc kết quả chẩn đoán (nếu có).
6. Bệnh nhân **Theo dõi trạng thái** và di chuyển đến phòng khám được chỉ định cho bước tiếp theo (đang ở trạng thái `PENDING` hoặc `IN_PROGRESS`).
7. Khi bác sĩ tại phòng đó hoàn thành dịch vụ, họ cập nhật trạng thái bước điều trị (`PATCH /api/treatment-plans/steps/{id}/complete`).
8. Hệ thống tự động cập nhật tiến độ lộ trình và thông báo cho bệnh nhân bước tiếp theo cần thực hiện.
9. Quy trình lặp lại cho đến khi tất cả các bước trong lộ trình đều ở trạng thái `COMPLETED`.

## 3. Các trường hợp ngoại lệ
- **401 Unauthorized**: Token không hợp lệ hoặc hết hạn.
- **404 Not Found**: Không tìm thấy phác đồ điều trị nào cho bệnh nhân.
- **500 Internal Server Error**: Lỗi kết nối cơ sở dữ liệu.

## 4. Hậu điều kiện
- Bệnh nhân nắm bắt được toàn bộ quy trình điều trị của mình.
- Hồ sơ bệnh án điện tử được cập nhật đầy đủ thông tin sau mỗi bước hoàn thành.
