-- =====================================================
-- Module: Quản Lý Tìm Trọ
-- Author: AI Assistant
-- Date: 2025-01-26
-- Version: 1.0
-- Description: Script tạo database cho hệ thống quản lý tìm trọ
-- Dependencies: SQL Server 2019+
-- =====================================================

-- Tạo database
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'Project_KLTN')
BEGIN
    CREATE DATABASE Project_KLTN;
END
GO

USE Project_KLTN;
GO

-- =====================================================
-- TẠO BẢNG NGƯỜI DÙNG VÀ PHÂN QUYỀN
-- =====================================================

-- Bảng vai trò
CREATE TABLE VaiTro (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    Ten NVARCHAR(100) NOT NULL UNIQUE,
    MoTa NVARCHAR(500),
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    UpdatedAt DATETIME2 DEFAULT GETDATE()
);

-- Bảng người dùng
CREATE TABLE NguoiDung (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    HoTen NVARCHAR(100) NOT NULL,
    Email NVARCHAR(255) NOT NULL UNIQUE,
    MatKhau NVARCHAR(255) NOT NULL,
    SoDienThoai NVARCHAR(15) NOT NULL UNIQUE,
    VaiTroId INT NOT NULL,
    Avatar NVARCHAR(500),
    DiaChi NVARCHAR(500),
    ThanhPho NVARCHAR(100),
    QuanHuyen NVARCHAR(100),
    PhuongXa NVARCHAR(100),
    Duong NVARCHAR(200),
    ToaDoLat FLOAT,
    ToaDoLng FLOAT,
    IsActive BIT DEFAULT 1,
    IsVerified BIT DEFAULT 0,
    VerificationToken NVARCHAR(255),
    ResetPasswordToken NVARCHAR(255),
    ResetPasswordExpires DATETIME2,
    LastLogin DATETIME2,
    LoginCount INT DEFAULT 0,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    UpdatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (VaiTroId) REFERENCES VaiTro(Id)
);

-- Bảng thông tin chủ trọ
CREATE TABLE ThongTinChuTro (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    NguoiDungId INT NOT NULL,
    MaSoKinhDoanh NVARCHAR(50),
    MaSoThue NVARCHAR(50),
    TenNganHang NVARCHAR(100),
    SoTaiKhoan NVARCHAR(50),
    TenChuTaiKhoan NVARCHAR(100),
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    UpdatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (NguoiDungId) REFERENCES NguoiDung(Id) ON DELETE CASCADE
);

-- Bảng thông tin người thuê
CREATE TABLE ThongTinNguoiThue (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    NguoiDungId INT NOT NULL,
    SoCCCD NVARCHAR(20),
    NgaySinh DATE,
    NgheNghiep NVARCHAR(100),
    TenNguoiLienHe NVARCHAR(100),
    SoDienThoaiLienHe NVARCHAR(15),
    MoiQuanHe NVARCHAR(50),
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    UpdatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (NguoiDungId) REFERENCES NguoiDung(Id) ON DELETE CASCADE
);

-- =====================================================
-- TẠO BẢNG PHÒNG TRỌ VÀ BÀI ĐĂNG
-- =====================================================

-- Bảng loại phòng
CREATE TABLE LoaiPhong (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    Ten NVARCHAR(50) NOT NULL UNIQUE,
    MoTa NVARCHAR(200),
    CreatedAt DATETIME2 DEFAULT GETDATE()
);

-- Bảng tiện ích
CREATE TABLE TienIch (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    Ten NVARCHAR(100) NOT NULL UNIQUE,
    Icon NVARCHAR(100),
    CreatedAt DATETIME2 DEFAULT GETDATE()
);

-- Bảng phòng trọ
CREATE TABLE PhongTro (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    TieuDe NVARCHAR(200) NOT NULL,
    MoTa NVARCHAR(2000),
    ChuTroId INT NOT NULL,
    DiaChi NVARCHAR(500),
    ThanhPho NVARCHAR(100),
    QuanHuyen NVARCHAR(100),
    PhuongXa NVARCHAR(100),
    Duong NVARCHAR(200),
    ToaDoLat FLOAT,
    ToaDoLng FLOAT,
    LoaiPhongId INT NOT NULL,
    DienTich FLOAT NOT NULL,
    GiaThueThang DECIMAL(15,2) NOT NULL,
    TienCoc DECIMAL(15,2) NOT NULL,
    PhiTienIch DECIMAL(15,2) DEFAULT 0,
    TrangThai NVARCHAR(50) DEFAULT 'active',
    IsAvailable BIT DEFAULT 1,
    AvailableFrom DATE,
    MinimumStay INT DEFAULT 1,
    SoLuongNguoiO INT DEFAULT 1,
    LuotXem INT DEFAULT 0,
    DiemTrungBinh FLOAT DEFAULT 0,
    SoLuongDanhGia INT DEFAULT 0,
    SoDienThoaiLienHe NVARCHAR(15),
    EmailLienHe NVARCHAR(255),
    PreferredContact NVARCHAR(20) DEFAULT 'both',
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    UpdatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ChuTroId) REFERENCES NguoiDung(Id),
    FOREIGN KEY (LoaiPhongId) REFERENCES LoaiPhong(Id)
);

-- Bảng hình ảnh phòng
CREATE TABLE HinhAnhPhong (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    PhongTroId INT NOT NULL,
    UrlHinhAnh NVARCHAR(500) NOT NULL,
    Caption NVARCHAR(200),
    IsPrimary BIT DEFAULT 0,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (PhongTroId) REFERENCES PhongTro(Id) ON DELETE CASCADE
);

-- Bảng tiện ích phòng (many-to-many)
CREATE TABLE PhongTro_TienIch (
    PhongTroId INT NOT NULL,
    TienIchId INT NOT NULL,
    PRIMARY KEY (PhongTroId, TienIchId),
    FOREIGN KEY (PhongTroId) REFERENCES PhongTro(Id) ON DELETE CASCADE,
    FOREIGN KEY (TienIchId) REFERENCES TienIch(Id) ON DELETE CASCADE
);

-- Bảng quy định phòng
CREATE TABLE QuyDinhPhong (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    PhongTroId INT NOT NULL,
    NoiDung NVARCHAR(500) NOT NULL,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (PhongTroId) REFERENCES PhongTro(Id) ON DELETE CASCADE
);

-- Bảng địa điểm gần phòng
CREATE TABLE DiaDiemGanPhong (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    PhongTroId INT NOT NULL,
    Ten NVARCHAR(100) NOT NULL,
    Loai NVARCHAR(50) NOT NULL,
    KhoangCach FLOAT,
    MoTa NVARCHAR(200),
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (PhongTroId) REFERENCES PhongTro(Id) ON DELETE CASCADE
);

-- =====================================================
-- TẠO BẢNG BOOKING VÀ HỢP ĐỒNG
-- =====================================================

-- Bảng booking
CREATE TABLE Booking (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    PhongTroId INT NOT NULL,
    NguoiThueId INT NOT NULL,
    ChuTroId INT NOT NULL,
    NgayNhanPhong DATE NOT NULL,
    NgayTraPhong DATE NOT NULL,
    ThoiGianThue INT NOT NULL, -- số tháng
    SoNguoiO INT NOT NULL,
    GiaThueThang DECIMAL(15,2) NOT NULL,
    TienCoc DECIMAL(15,2) NOT NULL,
    PhiTienIch DECIMAL(15,2) DEFAULT 0,
    TongTien DECIMAL(15,2) NOT NULL,
    TrangThai NVARCHAR(50) DEFAULT 'pending',
    SoHopDong NVARCHAR(20) UNIQUE,
    NgayKyHopDong DATETIME2,
    FileHopDong NVARCHAR(500),
    GhiChuNguoiThue NVARCHAR(1000),
    GhiChuChuTro NVARCHAR(1000),
    GhiChuAdmin NVARCHAR(1000),
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    UpdatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (PhongTroId) REFERENCES PhongTro(Id),
    FOREIGN KEY (NguoiThueId) REFERENCES NguoiDung(Id),
    FOREIGN KEY (ChuTroId) REFERENCES NguoiDung(Id)
);

-- Bảng điều khoản hợp đồng
CREATE TABLE DieuKhoanHopDong (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    BookingId INT NOT NULL,
    DieuKhoan NVARCHAR(100) NOT NULL,
    MoTa NVARCHAR(1000),
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (BookingId) REFERENCES Booking(Id) ON DELETE CASCADE
);

-- Bảng tài liệu booking
CREATE TABLE TaiLieuBooking (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    BookingId INT NOT NULL,
    LoaiTaiLieu NVARCHAR(50) NOT NULL,
    TenFile NVARCHAR(200),
    UrlFile NVARCHAR(500),
    NgayUpload DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (BookingId) REFERENCES Booking(Id) ON DELETE CASCADE
);

-- Bảng hủy booking
CREATE TABLE HuyBooking (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    BookingId INT NOT NULL,
    NguoiHuy NVARCHAR(20) NOT NULL, -- 'tenant', 'landlord', 'admin'
    NgayHuy DATETIME2 NOT NULL,
    LyDo NVARCHAR(1000),
    SoTienHoan DECIMAL(15,2) DEFAULT 0,
    TrangThaiHoanTien NVARCHAR(50) DEFAULT 'pending',
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (BookingId) REFERENCES Booking(Id) ON DELETE CASCADE
);

-- Bảng đánh giá
CREATE TABLE DanhGia (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    BookingId INT NOT NULL,
    NguoiDanhGiaId INT NOT NULL,
    Diem FLOAT NOT NULL CHECK (Diem >= 1 AND Diem <= 5),
    NoiDung NVARCHAR(1000),
    Loai NVARCHAR(20) NOT NULL, -- 'tenant', 'landlord'
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (BookingId) REFERENCES Booking(Id) ON DELETE CASCADE,
    FOREIGN KEY (NguoiDanhGiaId) REFERENCES NguoiDung(Id)
);

-- =====================================================
-- TẠO BẢNG THANH TOÁN VÀ VNPAY
-- =====================================================

-- Bảng thanh toán
CREATE TABLE ThanhToan (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    BookingId INT NOT NULL,
    NguoiThanhToanId INT NOT NULL,
    NguoiNhanId INT NOT NULL,
    Loai NVARCHAR(50) NOT NULL, -- 'deposit', 'monthly_rent', 'utilities', 'penalty', 'refund'
    SoTien DECIMAL(15,2) NOT NULL,
    LoaiTienTe NVARCHAR(10) DEFAULT 'VND',
    TrangThai NVARCHAR(50) DEFAULT 'pending',
    PhuongThucThanhToan NVARCHAR(50) NOT NULL, -- 'vnpay', 'bank_transfer', 'cash', 'other'
    MaGiaoDich NVARCHAR(100) UNIQUE,
    MaGiaoDichBenNgoai NVARCHAR(100),
    MoTa NVARCHAR(500),
    GhiChu NVARCHAR(1000),
    NgayKhoiTao DATETIME2 DEFAULT GETDATE(),
    NgayXuLy DATETIME2,
    NgayHoanThanh DATETIME2,
    NgayThatBai DATETIME2,
    LyDoThatBai NVARCHAR(500),
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    UpdatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (BookingId) REFERENCES Booking(Id),
    FOREIGN KEY (NguoiThanhToanId) REFERENCES NguoiDung(Id),
    FOREIGN KEY (NguoiNhanId) REFERENCES NguoiDung(Id)
);

-- Bảng thông tin VNPay
CREATE TABLE ThongTinVNPay (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    ThanhToanId INT NOT NULL,
    TxnRef NVARCHAR(100),
    OrderInfo NVARCHAR(500),
    OrderType NVARCHAR(50),
    Amount DECIMAL(15,2),
    Locale NVARCHAR(10) DEFAULT 'vn',
    CurrCode NVARCHAR(10) DEFAULT 'VND',
    ReturnUrl NVARCHAR(500),
    IpAddr NVARCHAR(50),
    CreateDate NVARCHAR(20),
    ExpireDate NVARCHAR(20),
    VnpTxnRef NVARCHAR(100),
    VnpAmount DECIMAL(15,2),
    VnpOrderInfo NVARCHAR(500),
    VnpResponseCode NVARCHAR(10),
    VnpTransactionNo NVARCHAR(100),
    VnpTransactionStatus NVARCHAR(50),
    VnpSecureHash NVARCHAR(500),
    VnpSecureHashType NVARCHAR(20),
    VnpBankCode NVARCHAR(20),
    VnpCardType NVARCHAR(50),
    VnpPayDate NVARCHAR(20),
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ThanhToanId) REFERENCES ThanhToan(Id) ON DELETE CASCADE
);

-- Bảng thông tin chuyển khoản
CREATE TABLE ThongTinChuyenKhoan (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    ThanhToanId INT NOT NULL,
    TenNganHang NVARCHAR(100),
    SoTaiKhoan NVARCHAR(50),
    TenChuTaiKhoan NVARCHAR(100),
    GhiChuChuyenKhoan NVARCHAR(500),
    NgayChuyenKhoan DATE,
    HinhAnhBienLai NVARCHAR(500),
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ThanhToanId) REFERENCES ThanhToan(Id) ON DELETE CASCADE
);

-- Bảng hoàn tiền
CREATE TABLE HoanTien (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    ThanhToanId INT NOT NULL,
    SoTienHoan DECIMAL(15,2) NOT NULL,
    LyDo NVARCHAR(500),
    NgayXuLy DATETIME2,
    MaGiaoDichHoanTien NVARCHAR(100),
    TrangThai NVARCHAR(50) DEFAULT 'pending',
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ThanhToanId) REFERENCES ThanhToan(Id) ON DELETE CASCADE
);

-- Bảng phí
CREATE TABLE Phi (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    ThanhToanId INT NOT NULL,
    PhiNenTang DECIMAL(15,2) DEFAULT 0,
    PhiXuLy DECIMAL(15,2) DEFAULT 0,
    TongPhi DECIMAL(15,2) DEFAULT 0,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ThanhToanId) REFERENCES ThanhToan(Id) ON DELETE CASCADE
);

-- =====================================================
-- TẠO BẢNG HÓA ĐƠN
-- =====================================================

-- Bảng hóa đơn
CREATE TABLE HoaDon (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    SoHoaDon NVARCHAR(20) UNIQUE NOT NULL,
    BookingId INT NOT NULL,
    NguoiThueId INT NOT NULL,
    ChuTroId INT NOT NULL,
    PhongTroId INT NOT NULL,
    Loai NVARCHAR(50) NOT NULL, -- 'deposit', 'monthly_rent', 'utilities', 'penalty', 'refund', 'other'
    Thang NVARCHAR(10),
    Nam INT,
    NgayBatDau DATE,
    NgayKetThuc DATE,
    TongTienTruocThue DECIMAL(15,2) NOT NULL,
    TyLeThue DECIMAL(5,2) DEFAULT 0,
    SoTienThue DECIMAL(15,2) DEFAULT 0,
    GiamGia DECIMAL(15,2) DEFAULT 0,
    TongTien DECIMAL(15,2) NOT NULL,
    TrangThai NVARCHAR(50) DEFAULT 'draft',
    NgayDenHan DATE NOT NULL,
    NgayThanhToan DATE,
    PhuongThucThanhToan NVARCHAR(50),
    MaThamChieuThanhToan NVARCHAR(100),
    GhiChu NVARCHAR(1000),
    NgayPhatHanh DATETIME2 DEFAULT GETDATE(),
    NguoiPhatHanhId INT,
    UrlFilePdf NVARCHAR(500),
    TenFilePdf NVARCHAR(200),
    NgayTaoPdf DATETIME2,
    EmailDaGui BIT DEFAULT 0,
    NgayGuiEmail DATETIME2,
    EmailNguoiNhan NVARCHAR(255),
    TrangThaiGiaoEmail NVARCHAR(50),
    TyLePhatTre DECIMAL(5,2) DEFAULT 0.05,
    SoTienPhatTre DECIMAL(15,2) DEFAULT 0,
    NgayApDungPhatTre DATETIME2,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    UpdatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (BookingId) REFERENCES Booking(Id),
    FOREIGN KEY (NguoiThueId) REFERENCES NguoiDung(Id),
    FOREIGN KEY (ChuTroId) REFERENCES NguoiDung(Id),
    FOREIGN KEY (PhongTroId) REFERENCES PhongTro(Id),
    FOREIGN KEY (NguoiPhatHanhId) REFERENCES NguoiDung(Id)
);

-- Bảng chi tiết hóa đơn
CREATE TABLE ChiTietHoaDon (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    HoaDonId INT NOT NULL,
    MoTa NVARCHAR(500) NOT NULL,
    SoLuong INT DEFAULT 1,
    DonGia DECIMAL(15,2) NOT NULL,
    ThanhTien DECIMAL(15,2) NOT NULL,
    TyLeThue DECIMAL(5,2) DEFAULT 0,
    SoTienThue DECIMAL(15,2) DEFAULT 0,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (HoaDonId) REFERENCES HoaDon(Id) ON DELETE CASCADE
);

-- Bảng nhắc nhở hóa đơn
CREATE TABLE NhacNhoHoaDon (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    HoaDonId INT NOT NULL,
    NgayGui DATETIME2 NOT NULL,
    Loai NVARCHAR(20) NOT NULL, -- 'first', 'second', 'final'
    PhuongThuc NVARCHAR(20) NOT NULL, -- 'email', 'sms', 'push'
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (HoaDonId) REFERENCES HoaDon(Id) ON DELETE CASCADE
);

-- =====================================================
-- TẠO BẢNG THÔNG BÁO VÀ HỖ TRỢ
-- =====================================================

-- Bảng thông báo
CREATE TABLE ThongBao (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    NguoiNhanId INT NOT NULL,
    Loai NVARCHAR(50) NOT NULL,
    TieuDe NVARCHAR(200) NOT NULL,
    NoiDung NVARCHAR(1000) NOT NULL,
    DuLieu NVARCHAR(2000), -- JSON data
    DaDoc BIT DEFAULT 0,
    NgayDoc DATETIME2,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    UpdatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (NguoiNhanId) REFERENCES NguoiDung(Id) ON DELETE CASCADE
);

-- Bảng hỗ trợ
CREATE TABLE HoTro (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    NguoiDungId INT NOT NULL,
    Loai NVARCHAR(50) NOT NULL, -- 'technical', 'billing', 'general', 'complaint'
    TieuDe NVARCHAR(200) NOT NULL,
    MoTa NVARCHAR(2000) NOT NULL,
    TrangThai NVARCHAR(50) DEFAULT 'open', -- 'open', 'in_progress', 'resolved', 'closed'
    MdoUuTien NVARCHAR(20) DEFAULT 'medium', -- 'low', 'medium', 'high', 'urgent'
    NguoiXuLyId INT,
    GhiChu NVARCHAR(1000),
    NgayGiaiQuyet DATETIME2,
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    UpdatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (NguoiDungId) REFERENCES NguoiDung(Id),
    FOREIGN KEY (NguoiXuLyId) REFERENCES NguoiDung(Id)
);

-- Bảng phản hồi hỗ trợ
CREATE TABLE PhanHoiHoTro (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    HoTroId INT NOT NULL,
    NguoiGuiId INT NOT NULL,
    NoiDung NVARCHAR(2000) NOT NULL,
    IsInternal BIT DEFAULT 0, -- true nếu là phản hồi nội bộ
    CreatedAt DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (HoTroId) REFERENCES HoTro(Id) ON DELETE CASCADE,
    FOREIGN KEY (NguoiGuiId) REFERENCES NguoiDung(Id)
);

-- =====================================================
-- TẠO INDEXES ĐỂ TỐI ƯU HIỆU SUẤT
-- =====================================================

-- Indexes cho bảng NguoiDung
CREATE INDEX IX_NguoiDung_Email ON NguoiDung(Email);
CREATE INDEX IX_NguoiDung_SoDienThoai ON NguoiDung(SoDienThoai);
CREATE INDEX IX_NguoiDung_VaiTroId ON NguoiDung(VaiTroId);
CREATE INDEX IX_NguoiDung_IsActive ON NguoiDung(IsActive);
CREATE INDEX IX_NguoiDung_ThanhPho ON NguoiDung(ThanhPho);

-- Indexes cho bảng PhongTro
CREATE INDEX IX_PhongTro_ChuTroId ON PhongTro(ChuTroId);
CREATE INDEX IX_PhongTro_TrangThai ON PhongTro(TrangThai);
CREATE INDEX IX_PhongTro_ThanhPho ON PhongTro(ThanhPho);
CREATE INDEX IX_PhongTro_QuanHuyen ON PhongTro(QuanHuyen);
CREATE INDEX IX_PhongTro_GiaThueThang ON PhongTro(GiaThueThang);
CREATE INDEX IX_PhongTro_LoaiPhongId ON PhongTro(LoaiPhongId);
CREATE INDEX IX_PhongTro_IsAvailable ON PhongTro(IsAvailable);
CREATE INDEX IX_PhongTro_DiemTrungBinh ON PhongTro(DiemTrungBinh DESC);
CREATE INDEX IX_PhongTro_CreatedAt ON PhongTro(CreatedAt DESC);

-- Indexes cho bảng Booking
CREATE INDEX IX_Booking_PhongTroId ON Booking(PhongTroId);
CREATE INDEX IX_Booking_NguoiThueId ON Booking(NguoiThueId);
CREATE INDEX IX_Booking_ChuTroId ON Booking(ChuTroId);
CREATE INDEX IX_Booking_TrangThai ON Booking(TrangThai);
CREATE INDEX IX_Booking_NgayNhanPhong ON Booking(NgayNhanPhong);
CREATE INDEX IX_Booking_NgayTraPhong ON Booking(NgayTraPhong);
CREATE INDEX IX_Booking_CreatedAt ON Booking(CreatedAt DESC);

-- Indexes cho bảng ThanhToan
CREATE INDEX IX_ThanhToan_BookingId ON ThanhToan(BookingId);
CREATE INDEX IX_ThanhToan_NguoiThanhToanId ON ThanhToan(NguoiThanhToanId);
CREATE INDEX IX_ThanhToan_NguoiNhanId ON ThanhToan(NguoiNhanId);
CREATE INDEX IX_ThanhToan_TrangThai ON ThanhToan(TrangThai);
CREATE INDEX IX_ThanhToan_Loai ON ThanhToan(Loai);
CREATE INDEX IX_ThanhToan_PhuongThucThanhToan ON ThanhToan(PhuongThucThanhToan);
CREATE INDEX IX_ThanhToan_MaGiaoDich ON ThanhToan(MaGiaoDich);
CREATE INDEX IX_ThanhToan_CreatedAt ON ThanhToan(CreatedAt DESC);

-- Indexes cho bảng HoaDon
CREATE INDEX IX_HoaDon_BookingId ON HoaDon(BookingId);
CREATE INDEX IX_HoaDon_NguoiThueId ON HoaDon(NguoiThueId);
CREATE INDEX IX_HoaDon_ChuTroId ON HoaDon(ChuTroId);
CREATE INDEX IX_HoaDon_PhongTroId ON HoaDon(PhongTroId);
CREATE INDEX IX_HoaDon_TrangThai ON HoaDon(TrangThai);
CREATE INDEX IX_HoaDon_NgayDenHan ON HoaDon(NgayDenHan);
CREATE INDEX IX_HoaDon_SoHoaDon ON HoaDon(SoHoaDon);
CREATE INDEX IX_HoaDon_CreatedAt ON HoaDon(CreatedAt DESC);

-- Indexes cho bảng ThongBao
CREATE INDEX IX_ThongBao_NguoiNhanId ON ThongBao(NguoiNhanId);
CREATE INDEX IX_ThongBao_Loai ON ThongBao(Loai);
CREATE INDEX IX_ThongBao_DaDoc ON ThongBao(DaDoc);
CREATE INDEX IX_ThongBao_CreatedAt ON ThongBao(CreatedAt DESC);

-- =====================================================
-- TẠO STORED PROCEDURES
-- =====================================================

-- Stored procedure tạo số hợp đồng tự động
CREATE PROCEDURE sp_TaoSoHopDong
AS
BEGIN
    DECLARE @SoHopDong NVARCHAR(20);
    DECLARE @SoThuTu INT;
    
    SELECT @SoThuTu = ISNULL(MAX(CAST(SUBSTRING(SoHopDong, 3, LEN(SoHopDong) - 2) AS INT)), 0) + 1
    FROM Booking
    WHERE SoHopDong IS NOT NULL;
    
    SET @SoHopDong = 'HD' + RIGHT('000000' + CAST(@SoThuTu AS NVARCHAR(10)), 6);
    
    SELECT @SoHopDong AS SoHopDong;
END
GO

-- Stored procedure tạo số hóa đơn tự động
CREATE PROCEDURE sp_TaoSoHoaDon
    @Thang INT,
    @Nam INT
AS
BEGIN
    DECLARE @SoHoaDon NVARCHAR(20);
    DECLARE @SoThuTu INT;
    DECLARE @Prefix NVARCHAR(10);
    
    SET @Prefix = 'INV' + CAST(@Nam AS NVARCHAR(4)) + RIGHT('00' + CAST(@Thang AS NVARCHAR(2)), 2);
    
    SELECT @SoThuTu = ISNULL(MAX(CAST(SUBSTRING(SoHoaDon, LEN(@Prefix) + 1, LEN(SoHoaDon) - LEN(@Prefix)) AS INT)), 0) + 1
    FROM HoaDon
    WHERE SoHoaDon LIKE @Prefix + '%';
    
    SET @SoHoaDon = @Prefix + RIGHT('000000' + CAST(@SoThuTu AS NVARCHAR(10)), 6);
    
    SELECT @SoHoaDon AS SoHoaDon;
END
GO

-- Stored procedure cập nhật điểm đánh giá phòng
CREATE PROCEDURE sp_CapNhatDiemDanhGiaPhong
    @PhongTroId INT
AS
BEGIN
    DECLARE @DiemTrungBinh FLOAT;
    DECLARE @SoLuongDanhGia INT;
    
    SELECT @DiemTrungBinh = AVG(CAST(Diem AS FLOAT)),
           @SoLuongDanhGia = COUNT(*)
    FROM DanhGia d
    INNER JOIN Booking b ON d.BookingId = b.Id
    WHERE b.PhongTroId = @PhongTroId;
    
    UPDATE PhongTro
    SET DiemTrungBinh = ISNULL(@DiemTrungBinh, 0),
        SoLuongDanhGia = ISNULL(@SoLuongDanhGia, 0)
    WHERE Id = @PhongTroId;
END
GO

-- Stored procedure thống kê doanh thu
CREATE PROCEDURE sp_ThongKeDoanhThu
    @NguoiDungId INT = NULL,
    @TuNgay DATE = NULL,
    @DenNgay DATE = NULL
AS
BEGIN
    SELECT 
        COUNT(*) AS TongGiaoDich,
        SUM(SoTien) AS TongTien,
        AVG(SoTien) AS TrungBinhTien,
        COUNT(CASE WHEN TrangThai = 'completed' THEN 1 END) AS GiaoDichThanhCong,
        SUM(CASE WHEN TrangThai = 'completed' THEN SoTien ELSE 0 END) AS TienThanhCong
    FROM ThanhToan
    WHERE (@NguoiDungId IS NULL OR NguoiThanhToanId = @NguoiDungId OR NguoiNhanId = @NguoiDungId)
    AND (@TuNgay IS NULL OR CAST(CreatedAt AS DATE) >= @TuNgay)
    AND (@DenNgay IS NULL OR CAST(CreatedAt AS DATE) <= @DenNgay);
END
GO

-- =====================================================
-- TẠO ROLES VÀ PERMISSIONS
-- =====================================================

-- Tạo roles
CREATE ROLE app_admin;
CREATE ROLE app_manager;
CREATE ROLE app_landlord;
CREATE ROLE app_tenant;

-- Cấp quyền cho app_admin
GRANT SELECT, INSERT, UPDATE, DELETE ON SCHEMA::dbo TO app_admin;
GRANT EXECUTE ON SCHEMA::dbo TO app_admin;

-- Cấp quyền cho app_manager
GRANT SELECT, INSERT, UPDATE ON NguoiDung TO app_manager;
GRANT SELECT, INSERT, UPDATE, DELETE ON PhongTro TO app_manager;
GRANT SELECT, INSERT, UPDATE ON Booking TO app_manager;
GRANT SELECT, INSERT, UPDATE ON ThanhToan TO app_manager;
GRANT SELECT, INSERT, UPDATE ON HoaDon TO app_manager;
GRANT SELECT, INSERT, UPDATE ON ThongBao TO app_manager;
GRANT SELECT, INSERT, UPDATE ON HoTro TO app_manager;
GRANT EXECUTE ON sp_ThongKeDoanhThu TO app_manager;

-- Cấp quyền cho app_landlord
GRANT SELECT, INSERT, UPDATE ON PhongTro TO app_landlord;
GRANT SELECT, INSERT, UPDATE ON Booking TO app_landlord;
GRANT SELECT ON ThanhToan TO app_landlord;
GRANT SELECT ON HoaDon TO app_landlord;
GRANT SELECT ON ThongBao TO app_landlord;
GRANT SELECT, INSERT ON HoTro TO app_landlord;

-- Cấp quyền cho app_tenant
GRANT SELECT ON PhongTro TO app_tenant;
GRANT SELECT, INSERT ON Booking TO app_tenant;
GRANT SELECT, INSERT ON ThanhToan TO app_tenant;
GRANT SELECT ON HoaDon TO app_tenant;
GRANT SELECT ON ThongBao TO app_tenant;
GRANT SELECT, INSERT ON HoTro TO app_tenant;

-- =====================================================
-- INSERT DỮ LIỆU MẪU
-- =====================================================

-- Insert vai trò
INSERT INTO VaiTro (Ten, MoTa) VALUES
('admin', 'Quản trị viên hệ thống'),
('landlord', 'Chủ trọ'),
('tenant', 'Người thuê trọ');

-- Insert loại phòng
INSERT INTO LoaiPhong (Ten, MoTa) VALUES
('studio', 'Phòng trọ studio'),
('1bedroom', 'Phòng trọ 1 phòng ngủ'),
('2bedroom', 'Phòng trọ 2 phòng ngủ'),
('3bedroom', 'Phòng trọ 3 phòng ngủ'),
('shared', 'Phòng trọ chung');

-- Insert tiện ích
INSERT INTO TienIch (Ten, Icon) VALUES
('wifi', 'wifi'),
('air_conditioner', 'ac_unit'),
('refrigerator', 'kitchen'),
('washing_machine', 'local_laundry_service'),
('television', 'tv'),
('bed', 'bed'),
('wardrobe', 'checkroom'),
('desk', 'desk'),
('chair', 'chair'),
('fan', 'air'),
('hot_water', 'hot_tub'),
('kitchen', 'kitchen'),
('bathroom', 'bathroom'),
('balcony', 'balcony'),
('parking', 'local_parking'),
('elevator', 'elevator'),
('security', 'security'),
('gym', 'fitness_center'),
('swimming_pool', 'pool'),
('garden', 'yard');

-- Insert admin user mặc định
INSERT INTO NguoiDung (HoTen, Email, MatKhau, SoDienThoai, VaiTroId, IsActive, IsVerified) VALUES
('Administrator', 'admin@quanlytimtro.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4J/8Kz8KzK', '0123456789', 1, 1, 1);

-- =====================================================
-- TẠO TRIGGERS
-- =====================================================

-- Trigger cập nhật UpdatedAt
CREATE TRIGGER tr_NguoiDung_UpdatedAt
ON NguoiDung
AFTER UPDATE
AS
BEGIN
    UPDATE NguoiDung
    SET UpdatedAt = GETDATE()
    FROM NguoiDung n
    INNER JOIN inserted i ON n.Id = i.Id;
END
GO

CREATE TRIGGER tr_PhongTro_UpdatedAt
ON PhongTro
AFTER UPDATE
AS
BEGIN
    UPDATE PhongTro
    SET UpdatedAt = GETDATE()
    FROM PhongTro p
    INNER JOIN inserted i ON p.Id = i.Id;
END
GO

CREATE TRIGGER tr_Booking_UpdatedAt
ON Booking
AFTER UPDATE
AS
BEGIN
    UPDATE Booking
    SET UpdatedAt = GETDATE()
    FROM Booking b
    INNER JOIN inserted i ON b.Id = i.Id;
END
GO

CREATE TRIGGER tr_ThanhToan_UpdatedAt
ON ThanhToan
AFTER UPDATE
AS
BEGIN
    UPDATE ThanhToan
    SET UpdatedAt = GETDATE()
    FROM ThanhToan t
    INNER JOIN inserted i ON t.Id = i.Id;
END
GO

CREATE TRIGGER tr_HoaDon_UpdatedAt
ON HoaDon
AFTER UPDATE
AS
BEGIN
    UPDATE HoaDon
    SET UpdatedAt = GETDATE()
    FROM HoaDon h
    INNER JOIN inserted i ON h.Id = i.Id;
END
GO

CREATE TRIGGER tr_ThongBao_UpdatedAt
ON ThongBao
AFTER UPDATE
AS
BEGIN
    UPDATE ThongBao
    SET UpdatedAt = GETDATE()
    FROM ThongBao t
    INNER JOIN inserted i ON t.Id = i.Id;
END
GO

-- =====================================================
-- TẠO VIEWS CHO BÁO CÁO
-- =====================================================

-- View thống kê phòng trọ
CREATE VIEW vw_ThongKePhongTro AS
SELECT 
    p.Id,
    p.TieuDe,
    p.ThanhPho,
    p.QuanHuyen,
    p.GiaThueThang,
    p.DiemTrungBinh,
    p.SoLuongDanhGia,
    p.LuotXem,
    p.TrangThai,
    n.HoTen AS TenChuTro,
    n.SoDienThoai AS SoDienThoaiChuTro,
    lp.Ten AS LoaiPhong,
    COUNT(b.Id) AS SoLuongBooking,
    COUNT(CASE WHEN b.TrangThai = 'active' THEN 1 END) AS SoLuongBookingDangHoatDong
FROM PhongTro p
LEFT JOIN NguoiDung n ON p.ChuTroId = n.Id
LEFT JOIN LoaiPhong lp ON p.LoaiPhongId = lp.Id
LEFT JOIN Booking b ON p.Id = b.PhongTroId
GROUP BY p.Id, p.TieuDe, p.ThanhPho, p.QuanHuyen, p.GiaThueThang, 
         p.DiemTrungBinh, p.SoLuongDanhGia, p.LuotXem, p.TrangThai,
         n.HoTen, n.SoDienThoai, lp.Ten;
GO

-- View thống kê doanh thu
CREATE VIEW vw_ThongKeDoanhThu AS
SELECT 
    t.Id,
    t.SoTien,
    t.TrangThai,
    t.Loai,
    t.PhuongThucThanhToan,
    t.CreatedAt,
    n1.HoTen AS TenNguoiThanhToan,
    n2.HoTen AS TenNguoiNhan,
    b.SoHopDong,
    p.TieuDe AS TenPhong
FROM ThanhToan t
LEFT JOIN NguoiDung n1 ON t.NguoiThanhToanId = n1.Id
LEFT JOIN NguoiDung n2 ON t.NguoiNhanId = n2.Id
LEFT JOIN Booking b ON t.BookingId = b.Id
LEFT JOIN PhongTro p ON b.PhongTroId = p.Id;
GO

-- View danh sách booking chi tiết
CREATE VIEW vw_BookingChiTiet AS
SELECT 
    b.Id,
    b.SoHopDong,
    b.NgayNhanPhong,
    b.NgayTraPhong,
    b.ThoiGianThue,
    b.SoNguoiO,
    b.TongTien,
    b.TrangThai,
    p.TieuDe AS TenPhong,
    p.DiaChi AS DiaChiPhong,
    n1.HoTen AS TenNguoiThue,
    n1.SoDienThoai AS SoDienThoaiNguoiThue,
    n1.Email AS EmailNguoiThue,
    n2.HoTen AS TenChuTro,
    n2.SoDienThoai AS SoDienThoaiChuTro,
    n2.Email AS EmailChuTro,
    b.CreatedAt
FROM Booking b
LEFT JOIN PhongTro p ON b.PhongTroId = p.Id
LEFT JOIN NguoiDung n1 ON b.NguoiThueId = n1.Id
LEFT JOIN NguoiDung n2 ON b.ChuTroId = n2.Id;
GO

-- =====================================================
-- HOÀN THÀNH SCRIPT
-- =====================================================

PRINT 'Script tạo database Project_KLTN đã hoàn thành thành công!';
PRINT 'Database: Project_KLTN';
PRINT 'Số bảng: 25+ bảng chính';
PRINT 'Số stored procedures: 4';
PRINT 'Số views: 3';
PRINT 'Số triggers: 6';
PRINT 'Số indexes: 20+';
PRINT 'Roles: app_admin, app_manager, app_landlord, app_tenant';
PRINT 'Dữ liệu mẫu: Vai trò, loại phòng, tiện ích, admin user';
