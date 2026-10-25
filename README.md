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

    database/migrations/                         # Manual SQL migrations
    tuvi_AI.sql                                   # Base MySQL schema

## Chạy Ứng dụng

### Clone Repository

    git clone <GITHUB_REPOSITORY_URL>
    cd Astrology_System

### Tạo Database

Import file:

    tuvi_AI.sql

Hoặc sử dụng MySQL:

    mysql -u root -p < tuvi_AI.sql

Database sử dụng:

    tuvi_ai

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

    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true

    spring.thymeleaf.cache=false

    spring.ai.openai.api-key=${OPENAI_API_KEY}
    spring.ai.openai.chat.model=<YOUR_MODEL>

## Luồng hoạt động

    User
      ↓
    Birth Information
      ↓
    Astrology Engine
      ↓
    Astrology Chart
      ↓
    MySQL
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

Các Entity JPA nằm trong `astrology/src/main/java/com/example/astrology/entity/`
và ánh xạ vào các bảng đã có trong `tuvi_AI.sql`: `AppUser` → `users`,
`BirthProfile` → `birth_profiles`, `BirthChart` → `astrology_charts`,
`AstrologyPalace` → `astrology_palaces`, `Star` → `stars`,
`ChartPalaceStar` → `chart_palace_stars`, `AstrologyKnowledge` →
`knowledge_documents`, `KnowledgeChunk` → `knowledge_chunks`,
`Conversation` → `conversations` và `ChatMessage` → `messages`.

