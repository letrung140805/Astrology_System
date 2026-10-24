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

`BirthInputNormalizationServiceTests` chỉ kiểm tra ranh giờ, DST và policy
đổi ngày; chúng không xác nhận Can Chi hay lịch âm. Để tránh biến quy tắc
chưa kiểm chứng thành kết quả production, `AstrologyCalculationService` vẫn
báo các gap của luồng ngày dương lịch tới khi có provider lịch/Can Chi/Nạp Âm
và fixture đối chiếu.

### Cung chức, Mệnh/Thân và Ngũ hành Cục

Profile đang triển khai là `tracuutuvi-public-rules-2026-10-24+quanshu-supplement-1`.
Nguồn chính là bài [An sao tử vi của Tra Cứu Tử Vi](https://tracuutuvi.com/an-sao-tu-vi.html)
(các mục 2.2–3.4, 4.1; truy cập 2026-10-24). Nguồn này nêu cách an Mệnh theo
tháng/giờ âm, bảng Cục theo Can năm và địa chi cung Mệnh, phép an Tử Vi theo
ngày âm/Cục, các sao chính tinh, Lộc Tồn, Tứ Hóa, Tràng Sinh và một số phụ tinh.
Đây là tài liệu của website mục tiêu, không phải xác nhận độc lập thuật toán
nội bộ của website hay chuẩn duy nhất của mọi trường phái.

Hai chỗ còn thiếu được bổ sung riêng từ bản điện tử
[《紫微斗數全書》 quyển 2 trên Wikisource](https://zh.wikisource.org/wiki/%E7%B4%AB%E5%BE%AE%E6%96%97%E6%95%B8%E5%85%A8%E6%9B%B8/%E5%8D%B7%E4%BA%8C)
(các mục 安身命例, 安十二宮例, 安南北斗諸星訣; truy cập 2026-10-24):
công thức cung Thân/tháng nhuận và chiều an 12 cung nghịch. Đây là nguồn bổ
sung cho các quy tắc mà bài Tra Cứu không xác định; không được gán các quy tắc
này cho Tra Cứu Tử Vi. Bản Wikisource là bản phiên âm điện tử, không nêu số
trang/ấn bản in đã đối chiếu.

`TracuutuviPositionsCalculator` dùng Dần làm mốc tháng 1; tính tháng nhuận như
tháng kế tiếp theo quy tắc bổ sung; đếm ngược giờ từ vị trí tháng để an Mệnh,
đếm thuận để an Thân. Bố cục cung dùng danh sách cung chức của bài Tra Cứu,
nhưng đếm nghịch theo bổ sung 《紫微斗數全書》. `FiveElementsBureauCalculator`
tra bảng Can-năm × nhóm địa chi cung Mệnh được công bố trong bài Tra Cứu.
Các phép tính này nhận trực tiếp ngày âm, Can/Chi năm và giờ chi; chúng chưa
được nối với chuyển lịch dương hoặc tính Can Chi năm.

`EarthlyBranch` và `PalaceFunction` là kiểu miền riêng, còn `PalaceLayout`
kiểm tra ánh xạ một-một đủ 12 cung/12 địa chi. `HourBranch` giữ phân loại giờ
riêng. Input tính chart đòi hỏi giới tính theo quy ước nam/nữ của nguồn; không
tự suy từ tên người dùng.

Fixture đối chiếu chưa có (`PENDING_FIXTURE`). Mẫu rỗng ở
`astrology/src/test/resources/calculation/reference-chart-fixture-template.json`
chỉ là khung nhập liệu; không chứa kết quả mong đợi giả. Chỉ bổ sung fixture
chạy được khi có biểu đồ tham chiếu độc lập, đầu vào/quy ước đầy đủ và ngày
ghi nhận.

### Mô hình vị trí sao

`StarChart` là dữ liệu có cấu trúc theo `EarthlyBranch`, cho phép nhiều sao
cùng một địa chi; xung đột vị trí của một sao bị từ chối. `StarPlacementService`
phân biệt quy tắc đã xác minh với `DOCUMENTED_UNVERIFIED`; profile dưới đây
chạy theo nguồn đã ghi nhưng không được tính là đã đối chiếu đúng với website.

| Nhóm đã triển khai | Phép tính có trong profile |
| --- | --- |
| 14 chính tinh | An Tử Vi theo ngày âm/Cục; an Thiên Phủ theo đối xứng quanh Dần; hai nhóm chính tinh theo thứ tự và chiều từ bài Tra Cứu cùng 《紫微斗數全書》 |
| Lộc Tồn/vòng Bác Sĩ | Tra Can năm → Lộc Tồn; an đủ 12 sao theo chiều Âm Dương năm × giới tính |
| Vòng Thái Tuế | An theo Chi năm; Thiên Không cùng địa chi với Thiếu Dương |
| Sao tháng/giờ | Văn Xương, Văn Khúc, Tả Phù, Hữu Bật |
| Tứ Hóa | Bảng Can năm → Hóa Lộc/Quyền/Khoa/Kỵ theo bảng Nam phái của trang [Tứ Hóa](https://tracuutuvi.com/tu-hoa.html) |
| Tràng Sinh | Điểm Trường Sinh theo Cục; 12 sao theo chiều được nêu trong bài Tra Cứu |
| Độ sáng, Chủ Mệnh/Chủ Thân | Chưa tính; trạng thái `PENDING_CONFIRMATION` |

`TracuutuviChartCalculator` trả dữ liệu có cấu trúc cho các nhóm trên và giữ
phiên bản ruleset. Kết quả hiện **không phải lá số hoàn chỉnh**: chưa tính các
nhóm phụ tinh khác được liệt kê ở mục 4.1–4.3, độ sáng, Chủ Mệnh/Chủ Thân,
bản mệnh Nạp Âm, Can Chi và chuyển lịch. `AstrologyCalculationService` và
`result.html` chưa dùng calculator này để xuất kết quả; giao diện tiếp tục báo
gap thay vì hiển thị dữ liệu nửa vời như lá số hoàn chỉnh.

MIT cross-check candidates checked on 2026-10-24 include
[ziweiknows/ziwei-chart](https://github.com/ziweiknows/ziwei-chart) (GPL-3.0,
not integrated), [baronguyen001/ziwei-cli](https://github.com/baronguyen001/ziwei-cli)
(MIT wrapper around `iztro`) and [ying-rushi/cdestiny](https://github.com/ying-rushi/cdestiny)
(MIT). They are implementation comparisons for general Zi Wei Dou Shu, not
independent validation of the selected Vietnamese website profile. No code
from these repositories is included in the engine.

For the Vietnamese lunar calendar, [Hồ Ngọc Đức's calendar rules](https://xemamlich.uhm.vn/calrules.html)
describe the new-moon/major-solar-term method and UTC+7, but explicitly note
their simplified examples are less accurate than the author's online program.
The Apache-2.0 [tinhtinh1908/Amlich](https://github.com/tinhtinh1908/Amlich)
repository inspected is an Android app, not a verified plain-Java calendar
library with independent fixtures; it is not integrated. The MIT
[LunarCalendar4J](https://github.com/nhatanh2996/LunarCalendar4J) implementation
was also rejected after the inspected revision returned `null` from its solar
to lunar conversion method.

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
