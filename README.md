# Digital Wallet API

Digital Wallet API, kullanıcıların çoklu para birimi cüzdanları oluşturabileceği, para transferi yapabileceği ve işlem
geçmişini takip edebileceği bir Spring Boot uygulamasıdır.

## Teknoloji Stack

- **Java**: 17
- **Framework**: Spring Boot 3.5.5
- **Security**: Spring Security + JWT
- **Database**: H2 (In-Memory)
- **ORM**: Spring Data JPA
- **Build Tool**: Maven
- **Documentation**: OpenAPI 3 (Swagger)
- **Testing**: JUnit 5, Mockito
- **Code Coverage**: JaCoCo
- **Containerization**: Docker

## Özellikler

### Temel Özellikler

- Kullanıcı kimlik doğrulama ve yetkilendirme (JWT)
- Çoklu para birimi desteği (TRY, USD, EUR)
- Cüzdan yönetimi
- Para transferi işlemleri
- İşlem geçmişi takibi
- Role-based access control (CUSTOMER, EMPLOYEE)

### Güvenlik

- BCrypt şifreleme
- JWT token tabanlı kimlik doğrulama
- Method-level security
- CORS yapılandırması

### API Dokümantasyonu

- OpenAPI 3.0 spesifikasyonu
- Swagger UI entegrasyonu
- Otomatik API dokümantasyonu

## Proje Yapısı

```
src/main/java/com/furkan/digitalWallet/
├── config/
│   ├── DataInitializer.java      # Test verileri
│   └── SecurityConfig.java       # Güvenlik yapılandırması
├── controller/
│   ├── AuthController.java       # Kimlik doğrulama
│   ├── WalletController.java     # Cüzdan işlemleri
│   └── TransactionController.java # İşlem yönetimi
├── entity/
│   ├── Customer.java             # Müşteri entity
│   ├── Wallet.java               # Cüzdan entity
│   └── Transaction.java          # İşlem entity
├── enums/
│   ├── Role.java                 # Kullanıcı rolleri
│   ├── Currency.java             # Para birimleri
│   ├── TransactionType.java      # İşlem tipleri
│   ├── TransactionStatus.java    # İşlem durumları
│   └── OppositePartyType.java    # Karşı taraf tipleri
├── repository/                   # Veri erişim katmanı
├── service/                      # İş mantığı katmanı
├── security/                     # Güvenlik bileşenleri
├── request/                      # Request DTO'lar
├── response/                     # Response DTO'lar
└── exception/                    # Hata yönetimi
```

## Veritabanı Şeması

### Customer (Müşteri)

- id: Benzersiz kimlik
- name: Ad
- surname: Soyad
- tckn: TC Kimlik No (11 karakter, benzersiz)
- username: Kullanıcı adı (benzersiz)
- password: Şifre (BCrypt hashli)
- role: Kullanıcı rolü (CUSTOMER/EMPLOYEE)

### Wallet (Cüzdan)

- id: Benzersiz kimlik
- customer_id: Müşteri referansı
- walletName: Cüzdan adı
- currency: Para birimi (TRY/USD/EUR)
- activeForShopping: Alışveriş için aktif
- activeForWithdraw: Para çekme için aktif
- balance: Toplam bakiye
- usableBalance: Kullanılabilir bakiye
- createdAt: Oluşturulma tarihi

### Transaction (İşlem)

- id: Benzersiz kimlik
- wallet_id: Cüzdan referansı
- amount: İşlem tutarı
- type: İşlem tipi
- oppositePartyType: Karşı taraf tipi
- oppositeParty: Karşı taraf bilgisi
- status: İşlem durumu
- createdAt: Oluşturulma tarihi
- updatedAt: Güncellenme tarihi

## API Endpoints

### Kimlik Doğrulama

```
POST /auth/login
```

### Cüzdan İşlemleri

```
GET    /wallets              # Kullanıcının cüzdanlarını listele
POST   /wallets              # Yeni cüzdan oluştur
GET    /wallets/{id}         # Cüzdan detayı
PUT    /wallets/{id}         # Cüzdan güncelle
DELETE /wallets/{id}         # Cüzdan sil
POST   /wallets/{id}/deposit # Para yatır
POST   /wallets/{id}/withdraw # Para çek
POST   /wallets/transfer     # Para transferi
```

### İşlem Geçmişi

```
GET /transactions           # İşlem geçmişi
GET /transactions/{id}      # İşlem detayı
```

## Çalıştırma

### Gereksinimler

- Java 17+
- Maven 3.6+
- Docker (opsiyonel)

### Yerel Ortamda Çalıştırma

1. Projeyi klonlayın:

```bash
git clone <repository-url>
cd digital-wallet
```

2. Bağımlılıkları yükleyin:

```bash
mvn clean install
```

3. Uygulamayı başlatın:

```bash
mvn spring-boot:run
```

### Docker ile Çalıştırma

#### Test Ortamı (Port 8080)

```bash
docker-compose up app-test
```

#### Production Ortamı (Port 8081)

```bash
docker-compose up app-prod
```

## Test Kullanıcıları

Uygulama başladığında aşağıdaki test kullanıcıları otomatik olarak oluşturulur:

### Employee

- **Username**: employee
- **Password**: employee123
- **Role**: EMPLOYEE

### Customers

- **Username**: customer1, **Password**: customer123
- **Username**: customer2, **Password**: password123
- **Username**: customer3, **Password**: test123
- **Username**: customer4, **Password**: demo123

Her müşteri için farklı para birimlerinde cüzdanlar otomatik oluşturulur.

## Konfigürasyon

### Environment Değişkenleri

| Değişken           | Varsayılan     | Açıklama               |
|--------------------|----------------|------------------------|
| DB_PASSWORD        | -              | Veritabanı şifresi     |
| JWT_SECRET         | default-secret | JWT şifreleme anahtarı |
| SERVER_PORT        | 8080           | Sunucu portu           |
| SWAGGER_ENABLED    | true           | Swagger UI aktif/pasif |
| LOG_LEVEL          | INFO           | Uygulama log seviyesi  |
| DB_NAME            | testdb         | Veritabanı adı         |
| HIBERNATE_DDL_AUTO | create-drop    | Hibernate DDL modu     |

### Profiller

- **default**: Geliştirme ortamı
- **test**: Test ortamı (H2 console açık)
- **prod**: Production ortamı (Swagger kapalı, güvenli ayarlar)

## Testing

### Unit Testleri Çalıştırma

```bash
mvn test
```

### Code Coverage Raporu

```bash
mvn test jacoco:report
```

Coverage raporu `target/site/jacoco/index.html` dosyasında görüntülenebilir.

### Test Coverage Hedefi

- Minimum %50 instruction coverage
- Configuration ve main class'lar coverage'dan hariç tutulmuştur

## API Dokümantasyonu

Uygulama çalıştıktan sonra Swagger UI'a aşağıdaki adresten erişebilirsiniz:

- **Test Ortamı**: http://localhost:8080/swagger-ui.html
- **Production Ortamı**: Swagger kapalı

### H2 Database Console

Test ortamında H2 veritabanı console'una erişim:

- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: jdbc:h2:mem:testdb
- **Username**: sa
- **Password**: (boş)

## Güvenlik

### JWT Token Kullanımı

1. `/auth/login` endpoint'ine kullanıcı bilgileri gönderilir
2. Başarılı girişte JWT token döner
3. Diğer API çağrılarında Authorization header'ında token gönderilir:

```
Authorization: Bearer <jwt-token>
```

### Şifre Güvenliği

- Tüm şifreler BCrypt algoritması ile hashlenmiştir
- Minimum güvenlik gereksinimleri karşılanmaktadır

## Geliştirme

### Yeni Özellik Ekleme

1. Entity/DTO oluşturun
2. Repository interface'i tanımlayın
3. Service katmanında iş mantığını uygulayın
4. Controller'da endpoint'leri oluşturun
5. Unit ve integration testlerini yazın

### Code Quality

- JaCoCo ile code coverage takibi
- Maven Surefire ile unit testler
- Maven Failsafe ile integration testler
- Lombok ile boilerplate kod azaltma

## Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## Katkı Sağlama

1. Fork yapın
2. Feature branch oluşturun
3. Değişikliklerinizi commit edin
4. Branch'inizi push edin
5. Pull Request oluşturun

## İletişim

Proje ile ilgili sorularınız için issue açabilirsiniz.
