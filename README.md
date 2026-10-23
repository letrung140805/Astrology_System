# AI-Based Vietnamese Astrology System

Một ứng dụng web AI về **chiêm tinh/tử vi Việt Nam**, được xây dựng bằng **Java Spring Boot, Thymeleaf, MySQL và Spring AI/OpenAI**. Hệ thống cho phép người dùng nhập thông tin cá nhân, ngày giờ sinh để tạo lá số, xem thông tin lá số và đặt câu hỏi với AI dựa trên dữ liệu đã được tính toán.

## Yêu cầu

- Java 21 hoặc cao hơn
- Maven 3.8.0 hoặc cao hơn
- MySQL 8.0 hoặc cao hơn
- OpenAI API Key
- IntelliJ IDEA (khuyến nghị, không bắt buộc)

## Cấu trúc Dự án

    src/
    ├── main/
    │   ├── java/com/example/astrologydemo/
    │   │   ├── AstrologydemoApplication.java    # Spring Boot Application
    │   │   ├── controller/                      # Controllers
    │   │   ├── service/                         # Business Logic
    │   │   ├── repository/                      # JPA Repositories
    │   │   ├── entity/                          # Database Entities
    │   │   ├── dto/                             # Data Transfer Objects
    │   │   └── security/                        # Security Configuration
    │   │
    │   └── resources/
    │       ├── application.properties           # Application Configuration
    │       ├── templates/                       # Thymeleaf Pages
    │       └── static/                          # CSS / JavaScript / Images
    │
    ├── database/
    │   └── tuvi_ai.sql                          # MySQL Database
    │
    ├── pom.xml                                  # Maven Configuration
    └── README.md

## Chạy Ứng dụng

### Clone Repository

    git clone <GITHUB_REPOSITORY_URL>
    cd astrologydemo

### Tạo Database

Import file:

    database/tuvi_ai.sql

Hoặc sử dụng MySQL:

    mysql -u root -p < database/tuvi_ai.sql

Database sử dụng:

    tuvi_ai

### Cấu hình MySQL

Mở file:

    src/main/resources/application.properties

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

Nếu project có Maven Wrapper:

Windows:

    .\mvnw.cmd clean install

Linux/macOS:

    ./mvnw clean install

Hoặc nếu đã cài Maven:

    mvn clean install

### Chạy Application

Windows:

    .\mvnw.cmd spring-boot:run

Hoặc chạy:

    AstrologyApplication.java

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

Phần **Astrology Engine** chịu trách nhiệm tính toán dữ liệu lá số. AI được sử dụng để diễn giải và trả lời dựa trên dữ liệu lá số và ngữ cảnh liên quan.
