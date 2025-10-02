# Ứng dụng Quản Lý Tìm Trọ - Android

Ứng dụng Android để quản lý và tìm kiếm phòng trọ, kết nối với API Express backend.

## Tính năng chính

### 🔐 Xác thực người dùng
- Đăng ký tài khoản (Người thuê trọ / Chủ trọ)
- Đăng nhập / Đăng xuất
- Quản lý thông tin cá nhân
- Đổi mật khẩu
- Quên mật khẩu

### 🏠 Quản lý phòng trọ
- Xem danh sách phòng trọ
- Tìm kiếm và lọc phòng trọ
- Chi tiết phòng trọ (hình ảnh, mô tả, tiện ích)
- Yêu thích phòng trọ
- Quản lý phòng trọ (cho chủ trọ)

### 📅 Quản lý đặt phòng
- Đặt phòng trọ
- Xem lịch sử đặt phòng
- Quản lý trạng thái đặt phòng
- Thanh toán

### 👤 Phân quyền người dùng
- **Admin**: Quản lý toàn bộ hệ thống
- **Chủ trọ**: Quản lý phòng trọ, xem đặt phòng
- **Người thuê**: Tìm phòng, đặt phòng, xem lịch sử

## Công nghệ sử dụng

### Backend
- Node.js + Express.js
- MongoDB + Mongoose
- JWT Authentication
- Swagger API Documentation

### Android
- **Language**: Java
- **Architecture**: MVVM Pattern
- **Networking**: Retrofit2 + OkHttp
- **Image Loading**: Glide
- **UI**: Material Design với ConstraintLayout
- **Database**: Room (local caching)
- **Navigation**: Android Navigation Component

## Cấu trúc dự án

```
appquanlytimtro/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/appquanlytimtro/
│   │   │   ├── adapters/          # RecyclerView Adapters
│   │   │   ├── auth/              # Authentication Activities
│   │   │   ├── bookings/          # Booking Management
│   │   │   ├── models/            # Data Models
│   │   │   ├── network/           # API Services
│   │   │   ├── profile/           # User Profile
│   │   │   ├── rooms/             # Room Management
│   │   │   └── MainActivity.java  # Main Dashboard
│   │   ├── res/
│   │   │   ├── layout/            # XML Layouts
│   │   │   ├── menu/              # Menu Resources
│   │   │   └── drawable/          # Icons & Images
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
└── README.md
```

## Cài đặt và chạy

### Yêu cầu hệ thống
- Android Studio Arctic Fox trở lên
- Android SDK 24+ (Android 7.0)
- Java 11+

### Cài đặt
1. Clone repository
2. Mở project trong Android Studio
3. Sync Gradle files
4. Cấu hình API endpoint trong `build.gradle.kts`:
   ```kotlin
   buildConfigField "String", "BASE_URL", "\"http://YOUR_IP:5000/api/\""
   ```

### Chạy ứng dụng
1. Đảm bảo backend server đang chạy trên port 5000
2. Kết nối thiết bị Android hoặc khởi động emulator
3. Build và chạy ứng dụng

## API Endpoints

### Authentication
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/register` - Đăng ký
- `GET /api/auth/me` - Thông tin user hiện tại
- `POST /api/auth/logout` - Đăng xuất

### Rooms
- `GET /api/rooms` - Danh sách phòng trọ
- `GET /api/rooms/{id}` - Chi tiết phòng trọ
- `POST /api/rooms` - Tạo phòng trọ (Chủ trọ)
- `PUT /api/rooms/{id}` - Cập nhật phòng trọ
- `POST /api/rooms/{id}/like` - Yêu thích phòng trọ

### Bookings
- `GET /api/bookings` - Danh sách đặt phòng
- `POST /api/bookings` - Tạo đặt phòng
- `PUT /api/bookings/{id}` - Cập nhật đặt phòng

### Users
- `GET /api/users/{id}` - Thông tin người dùng
- `PUT /api/users/{id}` - Cập nhật thông tin

## Tính năng đã implement

✅ **Hoàn thành:**
- [x] Authentication (Login/Register)
- [x] User Profile Management
- [x] Room Listing & Details
- [x] Booking Management
- [x] Role-based Navigation
- [x] Material Design UI
- [x] API Integration
- [x] Image Loading
- [x] Pull-to-refresh
- [x] Error Handling

🔄 **Đang phát triển:**
- [ ] Room Creation/Editing (for landlords)
- [ ] Payment Integration
- [ ] Push Notifications
- [ ] Offline Support
- [ ] Advanced Search & Filters
- [ ] Map Integration
- [ ] Chat System

## Cấu hình mạng

Để kết nối với backend từ emulator Android:
- Sử dụng IP `10.0.2.2:5000` thay vì `localhost:5000`
- Đảm bảo backend cho phép CORS từ mobile app
- Cấu hình `usesCleartextTraffic="true"` trong AndroidManifest.xml

## Demo Account

Để test ứng dụng, bạn có thể sử dụng:
- **Admin**: admin@example.com / 123456
- **Landlord**: landlord@example.com / 123456  
- **Tenant**: tenant@example.com / 123456

## Đóng góp

1. Fork repository
2. Tạo feature branch
3. Commit changes
4. Push to branch
5. Tạo Pull Request

## License

MIT License - xem file LICENSE để biết thêm chi tiết.

## Liên hệ

Nếu có câu hỏi hoặc góp ý, vui lòng tạo issue trên GitHub repository.
