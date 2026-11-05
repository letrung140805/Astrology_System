# AI-Based Vietnamese Astrology System (Hệ Thống Tử Vi AI)

Ứng dụng web luận giải **Tử Vi Đẩu Số Việt Nam ứng dụng Trí Tuệ Nhân Tạo (AI)**, được phát triển trên nền tảng **Java Spring Boot, Spring Security, Spring AI, Thymeleaf và MySQL 8.0+**. 

Hệ thống cho phép người dùng an sao, lập lá số Tử Vi chuẩn xác theo thuật toán lịch âm dương Việt Nam cổ truyền (múi giờ GMT+7), đồng thời kết hợp mô hình ngôn ngữ lớn (**Google Gemini** qua chuẩn OpenAI API) và kiến trúc **RAG (Retrieval-Augmented Generation)** để đàm đạo, phân tích và luận giải vận mệnh chuyên sâu theo thời gian thực.

---

## Công nghệ sử dụng

| Lớp (Layer) | Công nghệ / Thư viện |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 4.1.1 (Spring MVC, Spring Data JPA, Spring Security, Spring AI, Validation) |
| **Database** | MySQL 8.0+ |
| **Frontend** | Thymeleaf, Bootstrap 5, Modern CSS, JavaScript (ES6) |
| **AI / LLM** | Google Gemini (`gemini-3.5-flash-lite`) qua Spring AI OpenAI Client, RAG Architecture |
| **Libraries** | Lombok, Marked.js (Markdown parser), html2pdf.bundle.min.js (Xuất PDF) |
| **Build Tool** | Apache Maven / Maven Wrapper (`mvnw`) |

---

## Yêu cầu hệ thống

Trước khi bắt đầu, đảm bảo máy tính của bạn đã cài đặt các công cụ sau:

- **Java Development Kit (JDK)**: Phiên bản 21 trở lên.
- **Git**: Dùng để tải mã nguồn dự án.
- **MySQL Server**: Phiên bản 8.0 hoặc cao hơn.
- **Google Gemini API Key**: Lấy API Key miễn phí tại [Google AI Studio](https://aistudio.google.com/)
- **IDE (Khuyến nghị)**: IntelliJ IDEA, VS Code (kèm Extension Java & Spring) hoặc Eclipse.

---

## Hướng dẫn tải dự án từ GitHub

### Cách 1: Sử dụng lệnh Git Clone

Mở **Terminal** (macOS/Linux) hoặc **Command Prompt / PowerShell** (Windows) và thực hiện các lệnh:

```bash
# Clone mã nguồn dự án về máy
git clone https://github.com/letrung140805/Astrology_System.git

# Di chuyển vào thư mục dự án
cd Astrology_System
```

### Cách 2: Tải trực tiếp file ZIP

1. Truy cập vào kho mã nguồn: [https://github.com/letrung140805/Astrology_System](https://github.com/letrung140805/Astrology_System)
2. Bấm vào nút xanh **Code** -> chọn **Download ZIP**.
3. Giải nén file `.zip` vừa tải về vào thư mục mong muốn trên máy của bạn.
4. Mở cửa sổ dòng lệnh tại thư mục vừa giải nén.

---

## Hướng dẫn cài đặt & Khởi chạy

### Bước 1: Khởi tạo Cơ sở dữ liệu (MySQL)

Dự án sử dụng cơ sở dữ liệu có tên `tuvi_ai`. Toàn bộ cấu trúc bảng và thủ tục tương thích đều nằm trong file `tuvi_AI.sql` ở thư mục gốc của dự án.

1. Khởi động dịch vụ MySQL trên máy tính của bạn (cổng mặc định `3306`).
2. Mở cửa sổ dòng lệnh (Terminal/PowerShell) tại thư mục gốc của dự án và chạy lệnh sau để import cơ sở dữ liệu:
   ```bash
   mysql -u root -p < tuvi_AI.sql
   ```
   *(Nhập mật khẩu tài khoản `root` MySQL khi được hỏi).*

   > **Mẹo:** Bạn cũng có thể mở file `tuvi_AI.sql` bằng các công cụ trực quan như **MySQL Workbench**, **DBeaver**, **Navicat** hoặc **phpMyAdmin** rồi bấm **Execute / Run** toàn bộ nội dung file.

---

### Bước 2: Cấu hình thông tin kết nối

Mở file cấu hình tại đường dẫn:
```
astrology/src/main/resources/application.properties
```
*(Bạn cũng có thể tham khảo file mẫu `astrology/src/main/resources/application-example.properties`)*.

#### 1. Cấu hình MySQL
Chỉnh sửa `spring.datasource.username` và `spring.datasource.password` cho đúng với tài khoản MySQL của bạn:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tuvi_ai?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

#### 2. Cấu hình Gemini AI API Key
Ứng dụng kết nối tới Google Gemini thông qua giao diện tương thích OpenAI của Google AI Studio:

```properties
spring.ai.openai.base-url=https://generativelanguage.googleapis.com/v1beta/openai/
spring.ai.openai.api-key=${GEMINI_API_KEY}
spring.ai.openai.chat.options.model=${GEMINI_MODEL:gemini-3.5-flash-lite}
spring.ai.openai.chat.model=${GEMINI_MODEL:gemini-3.5-flash-lite}
```

Bạn có thể thiết lập khóa API theo một trong hai cách:

- **Cách 1: Thiết lập qua Biến môi trường (Khuyến nghị để bảo mật API key)**
  - Trên **Windows PowerShell**:
    ```powershell
    $env:GEMINI_API_KEY="AIzaSyYourGeminiApiKeyHere..."
    ```
  - Trên **Windows CMD**:
    ```cmd
    set GEMINI_API_KEY=AIzaSyYourGeminiApiKeyHere...
    ```
  - Trên **Linux / macOS**:
    ```bash
    export GEMINI_API_KEY="AIzaSyYourGeminiApiKeyHere..."
    ```

- **Cách 2: Ghi trực tiếp vào `application.properties`**:
  ```properties
  spring.ai.openai.api-key=AIzaSyYourGeminiApiKeyHere...
  ```

---

### Bước 3: Biên dịch và Chạy ứng dụng

Di chuyển vào thư mục con `astrology` (nơi chứa file `pom.xml` và Maven Wrapper):

```bash
cd astrology
```

#### Trên Windows (PowerShell / Command Prompt):
```cmd
.\mvnw.cmd clean spring-boot:run
```

#### Trên Linux / macOS:
```bash
chmod +x mvnw
./mvnw clean spring-boot:run
```

#### Hoặc sử dụng Maven đã cài sẵn trên máy:
```bash
mvn clean spring-boot:run
```

#### Chạy từ IDE (IntelliJ IDEA / VS Code / Eclipse):
1. Mở thư mục dự án trong IDE.
2. Đợi IDE tải xong các dependency của Maven.
3. Tìm đến file: `astrology/src/main/java/com/example/astrology/AstrologyApplication.java`.
4. Nhấn chuột phải và chọn **Run 'AstrologyApplication'**.

Khi console hiển thị:
```
Started AstrologyApplication in ... seconds (process running for ...)
```
Hệ thống đã sẵn sàng phục vụ!

---

### Bước 4: Mở trình duyệt và sử dụng

Mở trình duyệt web bất kỳ và truy cập vào địa chỉ:
```
http://localhost:8080/
```
---

## Cấu trúc thư mục dự án

```text
Astrology_System/
├── tuvi_AI.sql                                # Script khởi tạo Database schema & migration
├── README.md                                  # Hướng dẫn dự án chi tiết
└── astrology/                                 # Module Spring Boot chính
    ├── mvnw, mvnw.cmd                         # Maven Wrapper (chạy không cần cài Maven)
    ├── pom.xml                                # Quản lý dependency & plugins của dự án
    └── src/
        ├── main/
        │   ├── java/com/example/astrology/
        │   │   ├── AstrologyApplication.java  # Điểm khởi chạy (Main Application Entry Point)
        │   │   ├── config/                    # Cấu hình Security, PasswordEncoder, GlobalAdvice
        │   │   │   ├── SecurityConfig.java
        │   │   │   ├── PasswordEncoderConfig.java
        │   │   │   └── GlobalUserAdvice.java
        │   │   ├── controller/                # Bộ điều hướng Web MVC & REST endpoints
        │   │   │   ├── WebController.java     # Trang chủ, xem lá số, lịch sử, tài khoản
        │   │   │   ├── AuthController.java    # Đăng nhập, đăng ký tài khoản
        │   │   │   └── AstrologyChatController.java # Chat AI, SSE Streaming, đa hội thoại
        │   │   ├── service/                   # Nghiệp vụ ứng dụng
        │   │   │   ├── BirthChartApplicationService.java
        │   │   │   ├── BirthChartPersistenceService.java
        │   │   │   ├── AstrologyChatService.java
        │   │   │   ├── AstrologyRagService.java # Tự động seed & truy vấn tài liệu tri thức RAG
        │   │   │   ├── OpenAiAstrologyAnswerGenerator.java # Tương tác LLM qua Spring AI
        │   │   │   ├── UserService.java
        │   │   │   └── CustomUserDetailsService.java
        │   │   ├── calculation/               # Bộ máy toán học an sao & tính toán Tử Vi
        │   │   │   ├── CanLuongCalculator.java
        │   │   │   ├── TuViDetailService.java
        │   │   │   ├── FiveElement.java
        │   │   │   ├── calendar/              # Chuyển đổi Dương - Âm, Can Chi, Nạp Âm
        │   │   │   │   ├── HoNgocDucLunarCalendar.java
        │   │   │   │   ├── BirthInputNormalizer.java
        │   │   │   │   └── NayinElementCalculator.java
        │   │   │   └── chart/                 # An 12 cung, vòng Tràng Sinh, 14 chính tinh & phụ tinh
        │   │   │       ├── FiveElementsBureauCalculator.java
        │   │   │       ├── StarPlacementService.java
        │   │   │       └── TracuutuviMajorStarsCalculator.java
        │   │   ├── entity/                    # JPA Entities ánh xạ 100% với tuvi_AI.sql
        │   │   │   ├── AppUser.java
        │   │   │   ├── BirthProfile.java
        │   │   │   ├── BirthChart.java
        │   │   │   ├── AstrologyPalace.java
        │   │   │   ├── Star.java
        │   │   │   ├── ChartPalaceStar.java
        │   │   │   ├── Conversation.java
        │   │   │   ├── ChatMessage.java
        │   │   │   ├── AstrologyKnowledge.java
        │   │   │   └── KnowledgeChunk.java
        │   │   ├── repository/                # Spring Data JPA Repositories
        │   │   └── model/                     # View Models & DTO dữ liệu trang
        │   └── resources/
        │       ├── application.properties     # Cấu hình chính của ứng dụng
        │       ├── application-example.properties # File cấu hình mẫu tham khảo
        │       ├── static/                    # Tệp tĩnh: CSS, JavaScript, hình ảnh
        │       │   ├── css/site.css
        │       │   ├── js/site.js, marked.min.js, html2pdf.bundle.min.js
        │       │   └── images/
        │       └── templates/                 # Thymeleaf HTML Views
        │           ├── home.html              # Trang chủ: Form nhập ngày giờ sinh
        │           ├── result.html            # Đồ hình 12 Cung & Chi tiết lá số
        │           ├── chat.html              # Giao diện Đàm đạo với AI Tử Vi (Streaming)
        │           ├── history.html           # Lịch sử các lá số đã tạo
        │           ├── login.html             # Đăng nhập & Đăng ký tài khoản
        │           ├── account.html           # Quản lý tài khoản cá nhân & đổi mật khẩu
        │           ├── about.html             # Giới thiệu hệ thống
        │           └── fragments/layout.html  # Header, Footer, Thanh điều hướng dùng chung
        └── test/                              # Bộ kiểm thử đơn vị & kiểm thử tích hợp
```

---
