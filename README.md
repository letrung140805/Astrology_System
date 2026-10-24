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
`WebController`. `BirthInputNormalizationService` kiểm tra/chuẩn hóa ngày dương
Gregorian, giữ riêng đầu vào lịch âm và cờ tháng nhuận, giải quyết offset IANA
cho ngày giờ địa phương dương lịch và xác định địa chi giờ theo các khoảng giờ
được công bố. Khoảng giờ trên trang an sao nêu Tý 23:00–01:00; trang đó cũng có
một ví dụ gọi 10:00 là giờ Tý, trái với chính bảng khoảng giờ. Engine dùng bảng
khoảng giờ, không dùng ví dụ mâu thuẫn, và ghi nhận nguồn này cần xác minh
thêm bằng lá số đối chiếu.

Lựa chọn đổi ngày Can Chi tại giờ Tý phải được truyền rõ bằng
`ZiHourDayBoundary`; mặc định `UNSPECIFIED` để thời điểm 23:00 được đánh dấu
chưa xác định, không tự dịch ngày sinh gốc. DST gap bị từ chối; DST overlap
cần UTC offset tường minh. Đối với ngày âm, không áp dụng kiểm tra DST trước
khi đổi được ngày sang Gregorian.

Chưa có phép chuyển lịch âm/dương, Can Chi năm/tháng/ngày/thiên can giờ hoặc
bảng nạp âm chạy được. Các port `VietnameseLunarCalendarProvider`,
`CanChiCalculator` và `NapAmCalculator` chỉ mô tả hợp đồng, chưa đăng ký
implementation. Không hiển thị kết quả giả; trang kết quả tiếp tục báo rõ
các quy tắc còn thiếu.

Thư viện `nhatanh2996/LunarCalendar4J` có MIT license, nhưng tại revision
`1b5ef2b169276b2e39918bd292a645318e7efe7c` `convertSolar2LunarDate` trả `null`,
artifact không có trên Maven Central; vì vậy không tích hợp. Ứng dụng Android
`tinhtinh1908/Amlich` có Apache-2.0 nhưng chưa xác minh được module chuyển lịch
thuần Java độc lập và các fixture, nên cũng chưa dùng. Ngày rà soát nguồn:
2026-10-24. Khi chọn implementation, provider phải ghi tên, phiên bản, URL,
phạm vi năm, chính sách múi giờ và bộ fixture đã đối chiếu.

Ngày nhập từ biểu mẫu được hiểu là ngày dương lịch theo ISO; Java kiểm tra tính
hợp lệ của ngày Gregorian. Giờ là giờ địa phương dạng `LocalTime`; hệ thống
giữ nguyên giá trị và không đổi múi giờ, tính giờ mặt trời hoặc suy giờ chi.
Nếu giờ sinh bị bỏ trống, dữ liệu được chấp nhận nhưng đánh dấu chưa thể lập
phần lá số cần giờ sinh. Địa điểm hiện chỉ là văn bản, chưa được tra thành tọa
độ hay múi giờ; nếu địa điểm để trống, hệ thống báo riêng dữ liệu còn thiếu.

Trước khi triển khai các phép tính còn thiếu, cần chọn nguồn/phiên bản lịch âm;
mốc chuyển năm/tháng/ngày Can Chi; bảng nạp âm đủ 60 tổ hợp và fixtures độc
lập; quy tắc an Mệnh/Thân/Cục, 12 cung, sao và độ sáng. Unit tests của
`BirthInputNormalizationServiceTests` kiểm tra ranh giờ, DST và policy đổi
ngày; chúng không được xem là xác nhận Can Chi hay lịch âm.

### Cung chức, Mệnh/Thân và Ngũ hành Cục

Trang [Lập lá số Tử Vi của Tra Cứu Tử Vi](https://tracuutuvi.com/lap-la-so-tu-vi)
nêu tên 12 cung chức và mô tả cung Thân cùng vị trí với một số cung chức.
Nghiên cứu tiếp bài [An sao tử vi](https://tracuutuvi.com/an-sao-tu-vi.html)
(các mục 2.2–3.4, 4.1–4.3; truy cập 2026-10-24) tìm thấy cách an Mệnh theo
tháng/giờ âm lịch và bảng Cục theo Can năm với địa chi cung Mệnh. Đây là mô tả
của chính website mục tiêu, phù hợp làm profile tham chiếu để đối chiếu với
website, nhưng chưa phải xác minh độc lập về độ đúng của một trường phái Tử Vi.

Nguồn nói: lấy Dần làm tháng 1, đếm thuận đến tháng sinh để định cung giờ Tý,
rồi đếm nghịch đến giờ sinh để an Mệnh. Nguồn có ví dụ tháng 5, giờ Mão, nhưng
không đưa kết quả cuối cùng thành dữ liệu máy đọc được. Bảng Cục được lập từ
Can năm sinh và địa chi cung Mệnh. Cách an 12 cung sau đó trong cùng bài lại
nói “theo chiều thuận hoặc nghịch tùy từng người, theo chiều nào cũng được”;
điều này không đủ xác định một phép xếp 12 cung duy nhất nhất quán với yêu
cầu của engine. Vì vậy phép an Mệnh/Cục có thể được đặc tả theo profile này,
nhưng chưa được tích hợp vào kết quả production; chiều xếp 12 cung vẫn
`PENDING_CONFIRMATION`.

Nguồn được kiểm tra không công bố đủ thuật toán cung Thân, cách suy Can Chi
cung Mệnh, Chủ Mệnh hay Chủ Thân. Các phần này vẫn `PENDING_CONFIRMATION`.

`EarthlyBranch` và `PalaceFunction` là hai kiểu miền riêng; `PalaceLayout`
chỉ chấp nhận ánh xạ một-một đủ 12 cung với 12 địa chi. Nó kiểm tra tính toàn
vẹn cấu trúc, không quyết định thứ tự hay chiều an. `HourBranch` tham chiếu
`EarthlyBranch` để tránh coi nhãn giờ là tên cung chức.

Fixture đối chiếu chưa có (`PENDING_FIXTURE`). Mẫu rỗng ở
`astrology/src/test/resources/calculation/reference-chart-fixture-template.json`
chỉ là khung nhập liệu; không chứa kết quả mong đợi giả. Chỉ bổ sung fixture
chạy được khi có biểu đồ tham chiếu độc lập, đầu vào/quy ước đầy đủ và ngày
ghi nhận.

### Mô hình vị trí sao

`StarChart` là dữ liệu có cấu trúc theo `EarthlyBranch`, cho phép nhiều sao
cùng một địa chi. Việc thêm lại cùng một sao tại địa chi cũ là idempotent;
thêm sao đó tại địa chi khác bị từ chối thay vì ghi đè. `StarPlacementService`
chỉ chấp nhận vị trí khi bằng chứng nhóm quy tắc có trạng thái `VERIFIED`;
nguồn phải có URL HTTPS, phiên bản/ấn bản và mục tham chiếu.

`StarRuleSet.pendingInitial()` hiện giữ an toàn trạng thái pending cho mọi
nhóm, cho đến khi profile quy tắc được chọn và test đối chiếu được cung cấp.
Bài [An sao tử vi](https://tracuutuvi.com/an-sao-tu-vi.html) mô tả các quy tắc
cụ thể sau:

| Nhóm | Quy tắc được nguồn mục tiêu công bố | Tình trạng |
| --- | --- | --- |
| 14 chính tinh | Tử Vi lấy ngày âm và số Cục; các sao còn lại an tương đối theo vị trí Tử Vi/Thiên Phủ và thứ tự nêu trong mục 3.1 | Có mô tả, chưa đối chiếu độc lập |
| Lộc Tồn | Bảng 10 Thiên Can → địa chi Lộc Tồn; vòng Bác Sĩ 12 sao đi thuận với Dương Nam/Âm Nữ, nghịch với Âm Nam/Dương Nữ | Có bảng và chiều, cần fixture |
| Tràng Sinh | Bảng Cục → vị trí Trường Sinh; 12 sao tiếp theo đi thuận/nghịch theo Âm Dương năm và giới tính | Có bảng và chiều, cần fixture |
| Tứ Hóa | Bảng đủ 10 Thiên Can × 4 Hóa → sao tương ứng, áp vào vị trí sao đó | Có bảng, cần fixture |
| Sao theo tháng/giờ | Mục 4.3 và các trang [Văn Xương](https://tracuutuvi.com/sao-van-xuong.html), [Văn Khúc](https://tracuutuvi.com/sao-van-khuc.html), [Tả Phù](https://tracuutuvi.com/sao-ta-phu.html), [Hữu Bật](https://tracuutuvi.com/sao-huu-bat.html) mô tả các điểm khởi đầu và chiều đếm | Có mô tả một số sao, cần kiểm tra toàn bảng và fixture |
| Độ sáng sao | Chưa xác lập bảng đầy đủ, cùng cách diễn giải đồng nhất cho toàn bộ sao/địa chi | `PENDING_CONFIRMATION` |

Đây là căn cứ đủ để xây một ruleset được gắn nhãn rõ `TRACUUTUVI_PUBLIC_ARTICLE`,
không phải bằng chứng rằng ruleset đúng cho mọi trường phái hay tái tạo chính
xác thuật toán nội bộ của website. Không được đổi `RuleStatus` sang
`VERIFIED` trước khi có fixture chart độc lập hoặc đối chiếu thủ công với
nhiều lá số website, ghi đủ input, quy ước giờ Tý và kết quả có cấu trúc.
Nguồn [Hồ Ngọc Đức](https://xemamlich.uhm.vn/calrules.html) là tài liệu lịch
âm Việt Nam, không phải tài liệu an sao Tử Vi.

Một dự án Java công khai
[minhthytran/tuvi-dauso-tanbien](https://github.com/minhthytran/tuvi-dauso-tanbien)
tự mô tả là dựa theo sách *Tử Vi Đẩu Số Tân Biên* của Thái Thứ Lang nhưng
không cung cấp edition/page citations và không có LICENSE ở repository gốc
đã kiểm tra. Nó không được dùng làm dependency hoặc nguồn để sao chép code;
có thể xem như đầu mối tìm tài liệu bản in, chưa phải bằng chứng độc lập đã
xác minh.

### Đối chiếu với lá số tham chiếu

Mẫu fixture có cấu trúc nằm tại
`astrology/src/test/resources/calculation/reference-chart-fixture-template.json`.
Mỗi fixture cần giữ ngày theo loại lịch, cờ tháng nhuận, giờ/phút, giới tính,
nơi sinh, múi giờ/offset, quy ước giờ Tý, URL nguồn, ngày chụp, cách ghi nhận,
quy ước website đã chọn và các section dữ liệu đọc được. Ảnh chỉ là bằng chứng
nguồn; expected values phải được ghi thủ công thành `CanChiValue`,
`EarthlyBranch`, `PalaceFunction` và mã sao ổn định dạng chuỗi, không so sánh
ảnh. Mã sao dạng chuỗi cho phép fixture giữ cả sao chưa có calculator hoặc
chưa nằm trong enum của engine.

`ReferenceChartComparator` so sánh chính xác các trường đã khai báo, báo từng
field path, expected/actual và nhóm sai khác. `ReferenceChartRegressionRunner`
từ chối fixture `PENDING_REFERENCE`. `ComparisonSummary` tính tỷ lệ trên số
trường đã so sánh và trả tỷ lệ không xác định nếu mẫu số bằng 0. Calculator
hiện chưa tạo snapshot thực tế và chưa có fixture do người dùng cung cấp, nên
chưa có regression comparison nào chạy trên lá số tham chiếu thật.

| Nhóm trường | Nhóm sai khác khi mismatch |
| --- | --- |
| Can Chi năm/tháng/ngày/giờ | `CAN_CHI` |
| Bản mệnh | `NATAL_ELEMENT` |
| Cục, Chủ Mệnh/Chủ Thân, độ sáng | `RULE_DATA` |
| Cung Mệnh, cung Thân, 12 cung chức | `PALACE_PLACEMENT` |
| Tọa độ các sao | `STAR_PLACEMENT` |
| Lịch, quy ước ngày Tý, dữ liệu tham chiếu | Chưa tự kết luận nguyên nhân; cần so input/normalization và nguồn theo fixture |

Tình trạng tập đối chiếu hiện tại: **0 fixture tham chiếu thực**, **0 trường
đã so sánh**, tỷ lệ khớp **chưa xác định** (mẫu số bằng 0). Các test trong
`ReferenceChartComparatorTests` dùng dữ liệu tổng hợp để kiểm tra comparator,
không phải lá số thật và không được tính vào tỷ lệ.

Phần **Astrology Engine** chịu trách nhiệm tính toán dữ liệu lá số. AI được sử dụng để diễn giải và trả lời dựa trên dữ liệu lá số và ngữ cảnh liên quan.
