# Database Scripts - Hệ Thống Quản Lý Tìm Trọ

## Mô tả
Thư mục này chứa các script SQL để tạo và quản lý database cho hệ thống quản lý tìm trọ.

## Cấu trúc thư mục
```
Database/Scripts/
├── QuanLyTimTro_20250126_v1.sql    # Script chính tạo database
└── README.md                       # File hướng dẫn này
```

## Yêu cầu hệ thống
- SQL Server 2019 trở lên
- Quyền tạo database và schema
- Quyền tạo roles và cấp quyền

## Cách sử dụng

### 1. Tạo database từ đầu
```sql
-- Chạy script chính
sqlcmd -S server_name -i QuanLyTimTro_20250126_v1.sql
```

### 2. Tạo database thông qua SQL Server Management Studio
1. Mở SQL Server Management Studio
2. Kết nối đến SQL Server instance
3. Mở file `QuanLyTimTro_20250126_v1.sql`
4. Thực thi script (F5)

### 3. Tạo database thông qua Azure Data Studio
1. Mở Azure Data Studio
2. Kết nối đến SQL Server
3. Mở file script
4. Chạy script

## Cấu trúc Database

### Bảng chính
- **NguoiDung**: Thông tin người dùng (admin, chủ trọ, người thuê)
- **PhongTro**: Thông tin phòng trọ
- **Booking**: Thông tin đặt phòng
- **ThanhToan**: Thông tin thanh toán
- **HoaDon**: Hóa đơn
- **ThongBao**: Thông báo hệ thống
- **HoTro**: Hỗ trợ người dùng

### Bảng phụ trợ
- **VaiTro**: Vai trò người dùng
- **LoaiPhong**: Loại phòng trọ
- **TienIch**: Tiện ích phòng
- **ThongTinChuTro**: Thông tin chi tiết chủ trọ
- **ThongTinNguoiThue**: Thông tin chi tiết người thuê

### Bảng thanh toán
- **ThongTinVNPay**: Thông tin giao dịch VNPay
- **ThongTinChuyenKhoan**: Thông tin chuyển khoản
- **HoanTien**: Thông tin hoàn tiền
- **Phi**: Phí giao dịch

## Roles và Permissions

### app_admin
- Toàn quyền trên tất cả bảng
- Quyền thực thi stored procedures

### app_manager
- Quyền quản lý người dùng, phòng trọ, booking
- Quyền xem thống kê doanh thu

### app_landlord
- Quyền quản lý phòng trọ của mình
- Quyền xem booking và thanh toán liên quan

### app_tenant
- Quyền xem phòng trọ
- Quyền tạo booking và thanh toán
- Quyền xem thông báo

## Stored Procedures

### sp_TaoSoHopDong
Tạo số hợp đồng tự động theo format HD000001

### sp_TaoSoHoaDon
Tạo số hóa đơn tự động theo format INV202501000001

### sp_CapNhatDiemDanhGiaPhong
Cập nhật điểm đánh giá trung bình của phòng trọ

### sp_ThongKeDoanhThu
Thống kê doanh thu theo người dùng và khoảng thời gian

## Views

### vw_ThongKePhongTro
View tổng hợp thống kê phòng trọ

### vw_ThongKeDoanhThu
View tổng hợp thống kê doanh thu

### vw_BookingChiTiet
View chi tiết booking với thông tin đầy đủ

## Indexes
Database được tối ưu với hơn 20 indexes để đảm bảo hiệu suất truy vấn tốt.

## Triggers
6 triggers tự động cập nhật trường UpdatedAt khi có thay đổi dữ liệu.

## Dữ liệu mẫu
Script bao gồm dữ liệu mẫu:
- 3 vai trò: admin, landlord, tenant
- 5 loại phòng trọ
- 20 tiện ích phòng
- 1 tài khoản admin mặc định

## Lưu ý bảo mật
- Mật khẩu admin mặc định: cần thay đổi sau khi triển khai
- Các thông tin nhạy cảm được mã hóa
- Phân quyền chi tiết theo vai trò

## Troubleshooting

### Lỗi quyền truy cập
```sql
-- Cấp quyền cho user
GRANT CONNECT SQL TO [username];
GRANT CREATE DATABASE TO [username];
```

### Lỗi tạo role
```sql
-- Kiểm tra quyền tạo role
SELECT HAS_PERMS_BY_NAME(NULL, NULL, 'CREATE ROLE');
```

### Lỗi trigger
```sql
-- Kiểm tra quyền tạo trigger
SELECT HAS_PERMS_BY_NAME('dbo.PhongTro', 'OBJECT', 'CREATE TRIGGER');
```

## Liên hệ hỗ trợ
Nếu gặp vấn đề khi chạy script, vui lòng liên hệ team phát triển.
