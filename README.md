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

## Mô hình dữ liệu JPA

Các Entity JPA nằm trong `astrology/src/main/java/com/example/astrology/entity/`
và ánh xạ vào các bảng đã có trong `tuvi_AI.sql`: `AppUser` → `users`,
`BirthProfile` → `birth_profiles`, `BirthChart` → `astrology_charts`,
`AstrologyPalace` → `astrology_palaces`, `Star` → `stars`,
`ChartPalaceStar` → `chart_palace_stars`, `AstrologyKnowledge` →
`knowledge_documents`, `KnowledgeChunk` → `knowledge_chunks`,
`Conversation` → `conversations` và `ChatMessage` → `messages`.

Repository cho hồ sơ, lá số, hội thoại và tin nhắn có truy vấn giới hạn theo
user ID; khi triển khai service, luôn dùng các truy vấn này thay vì tải dữ liệu
theo ID đơn lẻ. Migration bổ sung khóa ngoại composite bảo đảm lá số thuộc cùng
user với hồ sơ sinh được đặt tại
`database/migrations/V1__enforce_chart_profile_ownership.sql`. Migration cần
được rà soát, sao lưu và chạy thủ công sau khi truy vấn kiểm tra dữ liệu không
trả về hàng nào. Không chạy migration lặp lại; migration không xóa bảng hoặc
dữ liệu. Liên kết chart tùy chọn của hội thoại tiếp tục giữ hành vi
`ON DELETE SET NULL` hiện có, nên service cũng phải xác minh chart thuộc user
sở hữu hội thoại.

Các trường Can Chi, lịch âm và ngũ hành trong `BirthChart` là kết quả tính toán
được lưu cùng phiên bản thuật toán để giữ nguyên lịch sử lá số; thông tin đầu
vào chuẩn vẫn nằm trong `BirthProfile`. Cột `raw_calculation_data` cũ được giữ
nguyên trong database nhưng không ánh xạ vào Entity để tránh ghi lặp dữ liệu
kết quả. Chỉ xem xét loại bỏ cột đó trong một migration riêng sau khi xác nhận
không còn dữ liệu cần bảo toàn.

Không lưu API key trong Entity hoặc database. Nội dung `embedding_reference`
chỉ là định danh tham chiếu tới vector store bên ngoài, không phải embedding
hay bí mật xác thực.

## Astrology Calculation Engine

`AstrologyCalculationService` nằm trong package `calculation` và được gọi từ
`WebController`; service hiện chỉ kiểm tra dữ liệu bắt buộc và báo rõ các quy
tắc chưa có căn cứ. Chưa có phép tính Can Chi, lịch âm, Mệnh/Cục, 12 cung hoặc
vị trí sao. Vì vậy trang kết quả không hiển thị các lá số mẫu như kết quả tính
thật.

Ngày nhập từ biểu mẫu được hiểu là ngày dương lịch theo ISO; Java kiểm tra tính
hợp lệ của ngày Gregorian. Giờ là giờ địa phương dạng `LocalTime`; hệ thống
giữ nguyên giá trị và không đổi múi giờ, tính giờ mặt trời hoặc suy giờ chi.
Nếu giờ sinh bị bỏ trống, dữ liệu được chấp nhận nhưng đánh dấu chưa thể lập
phần lá số cần giờ sinh. Địa điểm hiện chỉ là văn bản, chưa được tra thành tọa
độ hay múi giờ; nếu địa điểm để trống, hệ thống báo riêng dữ liệu còn thiếu.

Trước khi triển khai phép tính, cần chọn và ghi nguồn/phiên bản cho lịch âm và
tháng nhuận; mốc chuyển năm Can Chi; thuật toán Can Chi; quy tắc an Mệnh, an
Thân và Cục; thứ tự/chiều an 12 cung; bảng an sao, độ sáng và tứ hóa; cách xử
lý lịch sử múi giờ, tọa độ và giờ sinh không rõ. Unit tests của
`AstrologyCalculationServiceTests` chỉ xác nhận kiểm tra đầu vào và việc không
đưa ra kết quả khi các quy tắc này còn thiếu.

Phần **Astrology Engine** chịu trách nhiệm tính toán dữ liệu lá số. AI được sử dụng để diễn giải và trả lời dựa trên dữ liệu lá số và ngữ cảnh liên quan.
