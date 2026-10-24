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
    ├── controller/                              # MVC routes and request binding
    ├── service/                                 # Application orchestration
    ├── model/                                    # Page/view models
    ├── calculation/
    │   ├── calendar/                            # Calendar and birth input rules
    │   └── chart/                               # Domain chart calculators
    ├── entity/                                  # JPA entities
    ├── repository/                              # Spring Data repositories
    └── security/                                # Spring Security configuration

    astrology/src/main/resources/
    ├── templates/                               # Thymeleaf views
    ├── static/                                  # CSS / JavaScript / images
    └── application.properties                   # Application configuration

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

`WebController` ở package `controller` chỉ xử lý request/view; nó gọi
`BirthChartApplicationService` trong `service` để điều phối việc tính toán và
đóng gói `BirthChartPageData`/`ChartPalaceDisplay` trong `model`.
`AstrologyCalculationService` nằm trong package `calculation` để chuẩn hóa
đầu vào và báo các khoảng trống nghiệp vụ. `BirthInputNormalizationService`
kiểm tra/chuẩn hóa ngày dương
Gregorian, giữ riêng đầu vào lịch âm và cờ tháng nhuận, giải quyết offset IANA
cho ngày giờ địa phương dương lịch và xác định địa chi giờ theo các khoảng giờ
được công bố. Khoảng giờ trên trang an sao nêu Tý 23:00–01:00; trang đó cũng có
một ví dụ gọi 10:00 là giờ Tý, trái với chính bảng khoảng giờ. Engine dùng bảng
khoảng giờ, không dùng ví dụ mâu thuẫn, và ghi nhận nguồn này cần xác minh
thêm bằng lá số đối chiếu.

Profile giao diện hiện dùng giờ dân dụng Việt Nam UTC+7, không hiệu chỉnh giờ
mặt trời, và chọn ngày Can Chi đổi lúc 00:00. Do đó 23:00–00:59 vẫn là giờ Tý
nhưng không tự chuyển ngày chu kỳ; chính sách được ghi tại
`AstrologyCalculationService`. API chuẩn hóa tổng quát vẫn cho phép các caller
khác chọn `UNSPECIFIED` hoặc `CHANGE_AT_23`. DST gap bị từ chối; DST overlap
cần UTC offset tường minh nếu caller cung cấp múi giờ IANA.

`HoNgocDucLunarCalendarProvider` chuyển ngày dương sang âm theo thuật toán
thiên văn được mô tả tại Hồ Ngọc Đức, dùng múi giờ lịch UTC+7 và giới hạn năm
dương 1800-2199. `SexagenaryYearCalculator` suy Can Chi năm từ năm âm, neo chu
kỳ 60 năm tại Giáp Tý 1984. Đây là phép tính theo quy tắc đã công bố, chưa được
đối chiếu độc lập bằng tập fixture lịch đủ rộng. Can Chi tháng/ngày/giờ chưa
được triển khai. Ngũ hành Nạp Âm năm được tra từ bảng `NAYIN_ELEMENTS` trong
revision CEStu nêu dưới đây; bảng này chỉ được tích hợp ở mức ngũ hành, không
xuất tên đầy đủ từng nạp âm.

Thư viện `nhatanh2996/LunarCalendar4J` có MIT license, nhưng tại revision
`1b5ef2b169276b2e39918bd292a645318e7efe7c` `convertSolar2LunarDate` trả `null`,
artifact không có trên Maven Central; vì vậy không tích hợp. Ứng dụng Android
`tinhtinh1908/Amlich` có Apache-2.0 nhưng không dùng làm dependency Java thuần.
Ngày rà soát các lựa chọn: 2026-10-24. Provider hiện ghi tên thuật toán,
nguồn, phạm vi năm và múi giờ; dữ liệu thực tế vẫn cần đối chiếu với fixtures.

Ngày nhập từ biểu mẫu là ngày dương Gregorian; lịch âm được đổi tự động bằng
provider nêu trên. Giờ là giờ địa phương dạng `LocalTime`; hệ thống giữ nguyên
giá trị và không đổi múi giờ, tính giờ mặt trời hoặc hiệu chỉnh theo tọa độ.
Giờ sinh có thể bỏ trống, nhưng khi đó chart chỉ hiện khung 12 cung chưa an.
Địa điểm vẫn là văn bản, chưa được tra thành tọa độ hoặc múi giờ sinh. Vì vậy
ngày âm theo GMT+7 có thể khác ngày địa phương của người sinh ngoài Việt Nam.

`BirthInputNormalizationServiceTests` kiểm tra ranh giờ, DST và policy đổi
ngày; chúng không xác nhận độ chính xác của chuyển lịch hay Can Chi.
`AstrologyCalculationService` giữ danh sách các quy tắc cần xử lý; application
service loại các mục đã được tính được khỏi thông báo. Khi thiếu giờ sinh,
giao diện vẫn hiển thị lịch âm, Can Chi năm và ngũ hành Nạp Âm nếu tính được;
các vị trí 12 cung/sao phụ thuộc giờ được đánh dấu chưa an.

### Cung chức, Mệnh/Thân và Ngũ hành Cục

Profile đang triển khai là
`tracuutuvi-public-rules-2026-10-24+quanshu-supplement-1+cestu-79ebf331`.
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

Các bảng Thiên Khôi/Thiên Việt, Thiên Mã và ngũ hành Nạp Âm được chuyển thành dữ liệu Java từ
[`HAHVAC/CEStu-vi-ai`, revision `79ebf331e95e8f2005a0c8ef62b82a2a845fc893`](https://github.com/HAHVAC/CEStu-vi-ai/blob/79ebf331e95e8f2005a0c8ef62b82a2a845fc893/lib/ziwei/constants.ts)
(`TIANKUI_TABLE`, `TIANMA_TABLE`, `NAYIN_ELEMENTS`). Repository công bố giấy phép MIT. Đây là
đối chiếu dữ liệu triển khai, không phải xác nhận độc lập về trường phái hay
độ chính xác của toàn bộ lá số; không đưa mã nguồn Next.js/TypeScript hoặc
thư viện an sao của dự án đó vào đây.

`TracuutuviPositionsCalculator` dùng Dần làm mốc tháng 1; tính tháng nhuận như
tháng kế tiếp theo quy tắc bổ sung; đếm ngược giờ từ vị trí tháng để an Mệnh,
đếm thuận để an Thân. Bố cục cung dùng danh sách cung chức của bài Tra Cứu,
nhưng đếm nghịch theo bổ sung 《紫微斗數全書》. `FiveElementsBureauCalculator`
tra bảng Can-năm × nhóm địa chi cung Mệnh được công bố trong bài Tra Cứu.
Các phép tính này nhận ngày âm và Can/Chi năm do application service suy ra từ
ngày dương; địa chi giờ lấy từ giờ địa phương nhập. Ngũ hành Nạp Âm năm được
hiển thị từ bảng CEStu; tên đầy đủ Nạp Âm chưa được tra.

`EarthlyBranch` và `PalaceFunction` là kiểu miền riêng, còn `PalaceLayout`
kiểm tra ánh xạ một-một đủ 12 cung/12 địa chi. `HourBranch` giữ phân loại giờ
riêng. Input tính chart đòi hỏi giới tính theo quy ước nam/nữ của nguồn; không
tự suy từ tên người dùng.

Fixture đối chiếu thực hiện có cho một lá số tuvi.vn, với phạm vi và kết quả
được ghi trong mục đối chiếu bên dưới. Mẫu rỗng ở
`astrology/src/test/resources/calculation/reference-chart-fixture-template.json`
chỉ là khung nhập liệu, không chứa kết quả mong đợi giả. Chưa có bộ fixture
độc lập đủ rộng để xác nhận mọi tổ hợp hoặc toàn bộ quy tắc.

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
| Sao theo Can/Chi năm | Bảng Thiên Khôi/Thiên Việt theo Can năm và Thiên Mã theo Chi năm từ CEStu-vi-ai, revision nêu trên |
| Sao tháng/giờ | Văn Xương, Văn Khúc, Tả Phù, Hữu Bật |
| Tứ Hóa | Bảng Can năm → Hóa Lộc/Quyền/Khoa/Kỵ theo bảng Nam phái của trang [Tứ Hóa](https://tracuutuvi.com/tu-hoa.html) |
| Tràng Sinh | Điểm Trường Sinh theo Cục; 12 sao theo chiều được nêu trong bài Tra Cứu |
| Độ sáng, Chủ Mệnh/Chủ Thân | Chưa tính; trạng thái `PENDING_CONFIRMATION` |

`TracuutuviChartCalculator` trả dữ liệu có cấu trúc cho các nhóm trên và giữ
phiên bản ruleset. Kết quả hiện **không phải lá số hoàn chỉnh**: chưa tính các
nhóm phụ tinh khác được liệt kê ở mục 4.1–4.3, độ sáng, Chủ Mệnh/Chủ Thân,
bản mệnh Nạp Âm và Can Chi tháng/ngày/giờ. Ứng dụng chỉ hiển thị các nhóm sao
đang được calculator an; các giới hạn còn lại được nêu rõ bên cạnh kết quả.

MIT cross-check candidates checked on 2026-10-24 include
[ziweiknows/ziwei-chart](https://github.com/ziweiknows/ziwei-chart) (GPL-3.0,
not integrated), [baronguyen001/ziwei-cli](https://github.com/baronguyen001/ziwei-cli)
(MIT wrapper around `iztro`) and [ying-rushi/cdestiny](https://github.com/ying-rushi/cdestiny)
(MIT). They are implementation comparisons for general Zi Wei Dou Shu, not
independent validation of the selected Vietnamese website profile. No code
from these repositories is included in the engine.

For the Vietnamese lunar calendar, [Hồ Ngọc Đức's calendar rules](https://xemamlich.uhm.vn/calrules.html)
describe the new-moon/major-solar-term method and UTC+7. The integrated
provider implements the published conversion formulas; results are not yet
independently cross-checked across the supported range.
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
trường đã so sánh và trả tỷ lệ không xác định nếu mẫu số bằng 0.

Fixture thực đầu tiên được ghi tại
`astrology/src/test/java/com/example/astrology/calculation/chart/TuviVnReferenceFixtures.java`.
Fixture đã bỏ tên và query parameters; dữ liệu được chép thủ công từ lá số do
người dùng cung cấp trên tuvi.vn. Trang không công bố thuật toán, nên fixture
này chỉ là bằng chứng đối chiếu đầu ra, không phải nguồn để suy ra công thức.
Test tạo snapshot từ các phần engine hiện hỗ trợ: chuẩn hóa đầu vào, Cục,
cung Mệnh/Thân, 12 cung và 14 chính tinh. Trường hợp này khớp **35/35 trường**
được so sánh; phép đổi ngày cho ra ngày âm 10/7/2005 (không nhuận), năm Ất Dậu.

Tỷ lệ trên chỉ áp dụng cho một lá số và các trường được bao phủ. Fixture chưa
xác minh Can Chi tháng/ngày/giờ, bản mệnh Nạp Âm, Chủ Mệnh/Chủ Thân, độ sáng,
các sao phụ ngoài 14 chính tinh (bao gồm các sao mới đối chiếu từ CEStu) hoặc
các sao lưu theo năm xem. Không sửa công thức chỉ để khớp một mẫu đơn lẻ.

| Nhóm trường | Nhóm sai khác khi mismatch |
| --- | --- |
| Can Chi năm/tháng/ngày/giờ | `CAN_CHI` |
| Bản mệnh | `NATAL_ELEMENT` |
| Cục, Chủ Mệnh/Chủ Thân, độ sáng | `RULE_DATA` |
| Cung Mệnh, cung Thân, 12 cung chức | `PALACE_PLACEMENT` |
| Tọa độ các sao | `STAR_PLACEMENT` |
| Lịch, quy ước ngày Tý, dữ liệu tham chiếu | Chưa tự kết luận nguyên nhân; cần so input/normalization và nguồn theo fixture |

Tình trạng tập đối chiếu hiện tại: **1 fixture tham chiếu thực**, **35 trường
được so sánh**, tỷ lệ khớp **100% trên fixture này**. Đây chưa phải đánh giá
độ chính xác tổng thể. Các test trong `ReferenceChartComparatorTests` vẫn dùng
dữ liệu tổng hợp để kiểm tra comparator và không được tính vào tỷ lệ.

Phần **Astrology Engine** chịu trách nhiệm tính toán dữ liệu lá số. AI được sử dụng để diễn giải và trả lời dựa trên dữ liệu lá số và ngữ cảnh liên quan.
