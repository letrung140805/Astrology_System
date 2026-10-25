# AI-Based Vietnamese Astrology System

Một ứng dụng web AI về **chiêm tinh/tử vi Việt Nam**, được xây dựng bằng **Java Spring Boot, Thymeleaf, MySQL và Spring AI/OpenAI**. Hệ thống cho phép người dùng nhập thông tin cá nhân, ngày giờ sinh để tạo lá số, xem thông tin lá số và đặt câu hỏi với AI dựa trên dữ liệu đã được tính toán.

## Yêu cầu

- Java 21 hoặc cao hơn
- Maven 3.8.0 hoặc cao hơn
- MySQL 8.0 hoặc cao hơn
- OpenAI API Key
- IntelliJ IDEA (khuyến nghị, không bắt buộc)

## Cấu trúc Dự án

    astrology/src/main/java/com/example/astrology/
    ├── AstrologyApplication.java                # Spring Boot entry point
    ├── SecurityConfig.java                       # Spring Security configuration
    ├── controller/                              # MVC routes and request binding
    ├── service/                                 # Application orchestration
    ├── model/                                    # Page/view models
    ├── calculation/
    │   ├── calendar/                            # Calendar and birth input rules
    │   └── chart/                               # Domain chart calculators
    ├── entity/                                  # JPA entities
    ├── repository/                              # Spring Data repositories

    astrology/src/main/resources/
    ├── templates/                               # Thymeleaf views
    ├── static/                                  # CSS and JavaScript
    └── application.properties                   # Application configuration

    tuvi_AI.sql                                   # Database schema tuvi_ai

## Chạy Ứng dụng

### Clone Repository

    git clone <GITHUB_REPOSITORY_URL>
    cd Astrology_System

### Database

Ứng dụng chỉ kết nối tới database `tuvi_ai` được khai báo trong
`tuvi_AI.sql`; không dùng `astrology_ai` hoặc database khác. File SQL tạo duy
nhất schema này nếu chưa tồn tại, tạo các bảng còn thiếu, và có đoạn nâng cấp
có điều kiện cho dữ liệu hiện có. Sao lưu trước khi chạy file trên database
đang có dữ liệu. Không xóa database hoặc bảng để cài đặt phiên bản này.

File SQL cho phép `user_id` rỗng đối với hồ sơ/lá số khách và thêm hash token
vào chart để mở lại lá số. Các thay đổi tương thích được thực hiện trong cùng
`tuvi_AI.sql`; không cần chạy file migration riêng. JPA dùng
`spring.jpa.hibernate.ddl-auto=none` để không tự thay đổi schema.

### Cấu hình MySQL

Mở file:

    astrology/src/main/resources/application.properties

Cấu hình:

    spring.datasource.url=jdbc:mysql://localhost:3306/tuvi_ai?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8
    spring.datasource.username=root
    spring.datasource.password=YOUR_PASSWORD

Thay `YOUR_PASSWORD` bằng password MySQL trên máy của bạn.

### Cấu hình OpenAI API

Trong `application.properties`:

    spring.ai.openai.api-key=${OPENAI_API_KEY}
    spring.ai.openai.chat.model=<YOUR_MODEL>

Windows PowerShell:

    $env:OPENAI_API_KEY="YOUR_OPENAI_API_KEY"

Kiểm tra:

    echo $env:OPENAI_API_KEY


### Cài Dependencies

Chạy các lệnh Maven từ thư mục `astrology/`:

Windows:

    cd astrology
    .\mvnw.cmd clean install

Linux/macOS:

    cd astrology
    ./mvnw clean install

Hoặc nếu đã cài Maven:

    cd astrology
    mvn clean install

### Chạy Application

Windows:

    cd astrology
    .\mvnw.cmd spring-boot:run

Hoặc chạy:

    astrology/src/main/java/com/example/astrology/AstrologyApplication.java

bằng IntelliJ IDEA.

## Truy cập URL

Trang chủ:

    http://localhost:8080/

Các trang chức năng khác được điều hướng từ giao diện ứng dụng.

## Dependencies

- **spring-boot-starter-web**: Web framework và Spring MVC
- **spring-boot-starter-thymeleaf**: Thymeleaf template engine
- **spring-boot-starter-data-jpa**: JPA/Hibernate và database access
- **mysql-connector-j**: Kết nối MySQL
- **spring-boot-starter-security**: Authentication và Security
- **spring-boot-starter-validation**: Validation dữ liệu đầu vào
- **spring-ai-starter-model-openai**: Kết nối OpenAI thông qua Spring AI
- **spring-boot-devtools**: Hỗ trợ development/reload
- **spring-boot-starter-test**: Testing framework

## Cấu hình

File `application.properties` chứa các cấu hình chính:

    server.port=8080

    spring.datasource.url=jdbc:mysql://localhost:3306/tuvi_ai?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8
    spring.datasource.username=root
    spring.datasource.password=YOUR_PASSWORD

    spring.jpa.hibernate.ddl-auto=none
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true

    spring.thymeleaf.cache=false

    spring.ai.openai.api-key=${OPENAI_API_KEY}
    spring.ai.openai.chat.model=<YOUR_MODEL>

## Luồng hoạt động

    User
      ↓
    Birth Form (POST)
      ↓
    Astrology Engine
      ↓
    Birth Profile + Astrology Chart + Palaces + Stars
      ↓
    MySQL
      ↓
    Redirect to stored chart access URL
      ↓
    Load saved profile/chart from MySQL
      ↓
    Thymeleaf

Khi tạo chart với guest, ứng dụng tạo UUID ngẫu nhiên làm bearer access token,
chỉ lưu SHA-256 của token trong database và chỉ trả URL truy cập sau khi transaction
lưu hồ sơ cùng chart hoàn tất. Lịch sử khách lấy từ các token lưu trong phiên trình
duyệt hiện tại; ai có liên kết riêng tư đều có thể mở chart đó. Nếu request có
principal đã xác thực và tài khoản tồn tại trong `users`, chart được gắn với user
đó, và trang lịch sử truy vấn theo owner. Trang `/login` hiện chưa tự tạo phiên đăng nhập.
      ↓
    AI Orchestrator
      ↓
    Context Retrieval
      ↓
    OpenAI / LLM
      ↓
    AI Interpretation
      ↓
    Conversation History

## Mô hình dữ liệu JPA

Các Entity của luồng lá số ánh xạ theo `tuvi_AI.sql`: `AppUser` → `users`,
`BirthProfile` → `birth_profiles`, `BirthChart` → `astrology_charts`,
`AstrologyPalace` → `astrology_palaces`, `Star` → `stars` và
`ChartPalaceStar` → `chart_palace_stars`. Bảng sao danh mục được tạo khi cần;
liên kết sao-cung lưu tại `chart_palace_stars`.

### SQL xác minh chart đã lưu

Sau khi chạy `tuvi_AI.sql` và tạo chart, dùng truy vấn này để xem dữ liệu:

```sql
USE tuvi_ai;

SELECT bp.id AS birth_profile_id, ac.id AS chart_id, bp.full_name,
       bp.birth_date, bp.birth_time, ac.calculation_version, ac.generated_at
FROM birth_profiles bp
JOIN astrology_charts ac ON ac.birth_profile_id = bp.id
ORDER BY ac.generated_at DESC;

SELECT ac.id AS chart_id, COUNT(DISTINCT ap.id) AS palace_count,
       COUNT(cps.id) AS chart_star_count
FROM astrology_charts ac
JOIN astrology_palaces ap ON ap.chart_id = ac.id
LEFT JOIN chart_palace_stars cps ON cps.palace_id = ap.id
GROUP BY ap.chart_id;

SELECT ac.id AS chart_id, ap.palace_order, ap.palace_name,
       s.vietnamese_name, s.star_type, cps.brightness
FROM astrology_charts ac
JOIN astrology_palaces ap ON ap.chart_id = ac.id
LEFT JOIN chart_palace_stars cps ON cps.palace_id = ap.id
LEFT JOIN stars s ON s.id = cps.star_id
ORDER BY ac.generated_at DESC, ap.palace_order, cps.star_order;
```
