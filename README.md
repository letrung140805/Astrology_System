# AI-Based Vietnamese Astrology System (Hệ Thống Tử Vi AI)

Ứng dụng web luận giải **Tử Vi Đẩu Số Việt Nam ứng dụng Trí Tuệ Nhân Tạo (AI)**, được phát triển trên nền tảng **Java Spring Boot, Spring Security, Spring AI, Thymeleaf và MySQL 8.0+**. 

Hệ thống cho phép người dùng an sao, lập lá số Tử Vi chuẩn xác theo thuật toán lịch âm dương Việt Nam cổ truyền (múi giờ GMT+7), đồng thời kết hợp mô hình ngôn ngữ lớn (**Google Gemini** qua chuẩn OpenAI API) và kiến trúc **RAG (Retrieval-Augmented Generation)** để đàm đạo, phân tích và luận giải vận mệnh chuyên sâu theo thời gian thực. Ngoài ra, hệ thống còn cung cấp module đối chiếu **Tương hợp Tình Duyên** và **Tương hợp Bạn Bè & Cộng Sự** với bộ quy tắc thẩm định 5 yếu tố chuẩn xác.

---

## Công nghệ sử dụng

| Lớp (Layer) | Công nghệ / Thư viện |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 4.1.1 (Spring MVC, Spring Data JPA, Spring Security, Spring AI, Validation) |
| **Database** | MySQL 8.0+ |
| **Frontend** | Thymeleaf, Modern CSS, JavaScript (ES6) |
| **AI / LLM** | Google Gemini (`gemini-3.5-flash-lite`) qua Spring AI OpenAI Client, RAG Architecture |
| **Rule Engine** | Rule Engine tùy biến đọc cấu hình JSON thẩm định tương hợp 5 yếu tố |
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
├── tuvi_AI.sql                                # Script khởi tạo Database schema, bảng và procedures
├── README.md                                  # Hướng dẫn dự án chi tiết & tài liệu kiến trúc
└── astrology/                                 # Module Spring Boot chính
    ├── mvnw, mvnw.cmd                         # Maven Wrapper (chạy độc lập không cần cài Maven)
    ├── pom.xml                                # Quản lý dependencies & plugins của dự án
    └── src/
        ├── main/
        │   ├── java/com/example/astrology/
        │   │   ├── AstrologyApplication.java  # Điểm khởi chạy chính của ứng dụng Spring Boot
        │   │   ├── config/                    # Cấu hình hệ thống (Security, PasswordEncoder, Advice, JSON)
        │   │   │   ├── SecurityConfig.java
        │   │   │   ├── PasswordEncoderConfig.java
        │   │   │   ├── GlobalUserAdvice.java              # Cung cấp UserSummaryDto cho View, caching request
        │   │   │   ├── GlobalExceptionHandler.java        # Xử lý ngoại lệ tập trung toàn cục (@ControllerAdvice)
        │   │   │   └── CalculationJsonConfiguration.java
        │   │   ├── controller/                # Tầng điều hướng Web MVC & REST endpoints
        │   │   │   ├── WebController.java             # Trang chủ, xem kết quả lá số, lịch sử, giới thiệu
        │   │   │   ├── AccountController.java         # Quản lý tài khoản, cập nhật hồ sơ, đổi mật khẩu
        │   │   │   ├── AuthController.java            # Đăng nhập, đăng ký tài khoản
        │   │   │   ├── AstrologyChatController.java   # Đàm đạo AI, SSE Streaming, quản lý đa hội thoại
        │   │   │   ├── CoupleController.java          # Tương hợp tình duyên (sử dụng CoupleCalculationForm DTO)
        │   │   │   └── FriendController.java          # Tương hợp bạn bè & cộng sự (sử dụng FriendCalculationForm DTO)
        │   │   ├── service/                   # Tầng nghiệp vụ ứng dụng & Tích hợp AI / RAG
        │   │   │   ├── BirthChartApplicationService.java   # Điều phối thuật toán an sao & tính toán lá số Tử Vi
        │   │   │   ├── BirthChartDisplayService.java       # Chuẩn bị & định dạng dữ liệu tinh bàn hiển thị ra View
        │   │   │   ├── BirthChartPersistenceService.java   # Lưu trữ & tải lá số cá nhân từ DB (tối ưu truy vấn N+1)
        │   │   │   ├── AstrologyChatService.java           # Điều phối hội thoại đàm đạo
        │   │   │   ├── AstrologyChatPersistenceService.java# Quản lý & lưu phiên chat AI
        │   │   │   ├── AstrologyRagService.java            # Tự động nạp & truy vấn tri thức RAG
        │   │   │   ├── AstrologyAnswerGenerator.java       # Interface giao tiếp AI LLM
        │   │   │   ├── OpenAiAstrologyAnswerGenerator.java # Triển khai AI Streaming qua Gemini / Spring AI
        │   │   │   ├── CoupleCompatibilityService.java     # Đánh giá & tính điểm tương hợp tình duyên
        │   │   │   ├── AbstractCompatibilityRuleEvaluator.java # Lớp trừu tượng chia sẻ quy tắc và thuật toán tương hợp
        │   │   │   ├── CoupleRuleEvaluator.java            # Bộ thẩm định 24 quy tắc tình duyên 5 yếu tố
        │   │   │   ├── CoupleAiInterpretationService.java  # Luận giải tình duyên chuyên sâu từ AI
        │   │   │   ├── CoupleChartPersistenceService.java  # Lưu trữ hồ sơ tương hợp tình duyên
        │   │   │   ├── FriendCompatibilityService.java     # Đánh giá & tính điểm tương hợp bạn bè & cộng sự
        │   │   │   ├── FriendRuleEvaluator.java            # Bộ thẩm định 34 quy tắc bạn bè 5 yếu tố
        │   │   │   ├── FriendAiInterpretationService.java  # Luận giải bạn bè & cộng sự từ AI
        │   │   │   ├── FriendChartPersistenceService.java  # Lưu trữ hồ sơ tương hợp bạn bè
        │   │   │   ├── UserService.java                    # Quản lý tài khoản, mật khẩu, hồ sơ người dùng
        │   │   │   └── CustomUserDetailsService.java       # Xác thực người dùng Spring Security
        │   │   ├── calculation/               # Bộ máy toán học an sao & lịch pháp Tử Vi
        │   │   │   ├── AstrologyCalculationService.java
        │   │   │   ├── AstrologyCalculationInput.java
        │   │   │   ├── CanLuongCalculator.java
        │   │   │   ├── TuViDetailService.java
        │   │   │   ├── FiveElement.java
        │   │   │   ├── CalculationAssessment.java, CalculationGap.java
        │   │   │   ├── calendar/              # Chuyển đổi Dương - Âm, Can Chi, Nạp Âm (Lịch Hồ Ngọc Đức)
        │   │   │   │   ├── VietnameseLunarCalendarProvider.java
        │   │   │   │   ├── HoNgocDucLunarCalendarProvider.java
        │   │   │   │   ├── BirthInputNormalizationService.java
        │   │   │   │   ├── NayinElementCalculator.java
        │   │   │   │   └── SexagenaryYearCalculator.java
        │   │   │   └── chart/                 # An 12 cung, vòng Tràng Sinh, chính tinh, phụ tinh, tứ hóa
        │   │   │       ├── TracuutuviChartCalculator.java
        │   │   │       ├── FiveElementsBureauCalculator.java
        │   │   │       ├── StarPlacementService.java
        │   │   │       ├── TracuutuviMajorStarsCalculator.java
        │   │   │       ├── TracuutuviMonthHourStarsCalculator.java
        │   │   │       ├── TracuutuviYearStarsCalculator.java
        │   │   │       ├── TracuutuviPositionsCalculator.java
        │   │   │       ├── TracuutuviChangShengCalculator.java
        │   │   │       └── TracuutuviFourTransformationsCalculator.java
        │   │   ├── entity/                    # Các thực thể JPA ánh xạ với cơ sở dữ liệu MySQL
        │   │   │   ├── AppUser.java                   # Tài khoản người dùng
        │   │   │   ├── BirthProfile.java              # Thông tin sinh cá nhân
        │   │   │   ├── BirthChart.java                # Hồ sơ lá số Tử Vi
        │   │   │   ├── AstrologyPalace.java           # 12 cung trong lá số
        │   │   │   ├── Star.java                      # Danh mục các sao
        │   │   │   ├── ChartPalaceStar.java           # Sao tọa thủ / chiếu tại cung
        │   │   │   ├── Conversation.java              # Cuộc hội thoại đàm đạo
        │   │   │   ├── ChatMessage.java               # Tin nhắn đàm đạo
        │   │   │   ├── CoupleChart.java               # Hồ sơ lá số tương hợp tình duyên
        │   │   │   ├── FriendChart.java               # Hồ sơ lá số tương hợp bạn bè & cộng sự
        │   │   │   ├── AstrologyKnowledge.java        # Tài liệu tri thức Tử Vi RAG
        │   │   │   └── KnowledgeChunk.java            # Phân mảnh tri thức RAG
        │   │   ├── repository/                # Tầng truy xuất dữ liệu Spring Data JPA
        │   │   │   ├── AppUserRepository.java
        │   │   │   ├── BirthProfileRepository.java
        │   │   │   ├── BirthChartRepository.java
        │   │   │   ├── AstrologyPalaceRepository.java
        │   │   │   ├── StarRepository.java
        │   │   │   ├── ChartPalaceStarRepository.java
        │   │   │   ├── ConversationRepository.java
        │   │   │   ├── ChatMessageRepository.java
        │   │   │   ├── CoupleChartRepository.java
        │   │   │   ├── FriendChartRepository.java
        │   │   │   ├── AstrologyKnowledgeRepository.java
        │   │   │   └── KnowledgeChunkRepository.java
        │   │   └── model/                     # Data Transfer Objects (DTO) & View Models
        │   │       ├── BirthChartPageData.java
        │   │       ├── ChartHistoryItem.java
        │   │       ├── ChartPalaceDisplay.java, ChartStarDisplay.java
        │   │       ├── ConversationHistoryItem.java
        │   │       ├── CoupleCalculationForm.java         # Form DTO đóng gói tham số tương hợp đôi lứa
        │   │       ├── CoupleCompatibilityResult.java, CouplePersonData.java, CoupleHistoryItem.java
        │   │       ├── FriendCalculationForm.java         # Form DTO đóng gói tham số tương hợp bạn bè
        │   │       ├── FriendCompatibilityResult.java, FriendHistoryItem.java
        │   │       ├── UserSummaryDto.java                # DTO thông tin người dùng cho View (ẩn passwordHash)
        │   │       └── TriggeredRule.java
        │   └── resources/
        │       ├── application.properties             # Cấu hình chính của ứng dụng
        │       ├── application-example.properties     # File cấu hình mẫu tham khảo
        │       ├── rules/                             # Bộ quy tắc thẩm định tương hợp JSON
        │       │   ├── couple_compatibility_rules.json# 24 quy tắc tương hợp tình duyên (5 yếu tố)
        │       │   └── friend_compatibility_rules.json# 34 quy tắc tương hợp bạn bè & cộng sự (5 yếu tố)
        │       ├── static/                            # Tài nguyên tĩnh phía Client
        │       │   ├── css/site.css                   # Stylesheet giao diện hiện đại (Light & Dark Mode)
        │       │   ├── js/
        │       │   │   ├── site.js                    # Script điều hướng, đồng bộ ngày sinh, SSE streaming
        │       │   │   ├── marked.min.js              # Thư viện Markdown parser
        │       │   │   └── html2pdf.bundle.min.js     # Thư viện xuất tinh bàn lá số ra PDF
        │       │   └── images/                        # Logo, avatar AI, hình ảnh bài viết
        │       └── templates/                         # Giao diện Thymeleaf HTML
        │           ├── home.html                      # Trang chủ: Nhập thông tin & khởi tạo lá số
        │           ├── result.html                    # Hiển thị tinh bàn 12 cung & luận giải cá nhân
        │           ├── chat.html                      # Giao diện đàm đạo Tử Vi với AI (hỗ trợ SSE)
        │           ├── couple.html                    # Đối chiếu & luận giải tương hợp tình duyên
        │           ├── friend.html                    # Đối chiếu & luận giải tương hợp bạn bè & cộng sự
        │           ├── history.html                   # Lịch sử 3 phân hệ: Cá nhân, Tình duyên, Bạn bè
        │           ├── account.html                   # Quản lý tài khoản, đổi tên & mật khẩu
        │           ├── login.html                     # Đăng nhập & Đăng ký tài khoản
        │           ├── about.html                     # Giới thiệu dự án, tôn chỉ & đội ngũ
        │           └── fragments/                     # Các đoạn giao diện tái sử dụng
        │               ├── layout.html                # Header, Footer, Menu responsive
        │               └── chart.html                 # Khung tinh bàn 12 cung lá số Tử Vi
        └── test/                                      # Bộ kiểm thử tự động (Unit & Integration Tests)
            └── java/com/example/astrology/            # 31 Test Classes kiểm thử toán học & nghiệp vụ
```

---
