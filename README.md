# Astrology_System
from pathlib import Path

readme = """# AI-Based Vietnamese Astrology System

Một ứng dụng web AI về **chiêm tinh/tử vi Việt Nam**, cho phép người dùng nhập thông tin cá nhân và thông tin ngày giờ sinh để tạo lá số, xem diễn giải và đặt câu hỏi với AI dựa trên dữ liệu lá số.

## Yêu cầu

- Java 21 hoặc cao hơn
- Maven 3.8.0 hoặc cao hơn
- MySQL 8.0 hoặc cao hơn
- OpenAI API Key
- IntelliJ IDEA (khuyến nghị, không bắt buộc)

## Cấu trúc Dự án

```text
src/
├── main/
│   ├── java/com/example/astrologydemo/
│   │   ├── AstrologydemoApplication.java    # Spring Boot Application
│   │   ├── controller/                      # Web Controllers
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

