# Digital Wallet API

Spring Boot tabanlı dijital cüzdan uygulaması. Müşteri ve çalışan rolleriyle cüzdan yönetimi, para transferi ve işlem
onay süreçlerini yönetir.

## 🚀 Özellikler

- **Kimlik Doğrulama**: JWT tabanlı authentication ve role-based authorization
- **Roller**: CUSTOMER (müşteri) ve EMPLOYEE (çalışan)
- **Cüzdan Yönetimi**: Çoklu para birimi desteği (TRY, USD, EUR)
- **Para İşlemleri**: Para yatırma, çekme ve transfer
- **Onay Süreci**: Çalışan onayı gerektiren işlemler
- **API Dokümantasyonu**: Swagger/OpenAPI entegrasyonu
- **Test Coverage**: JaCoCo ile yüksek test coverage

## 🛠️ Teknolojiler

- **Java 21** + **Spring Boot 3.5.5**
- **Spring Security** + **JWT**
- **H2 Database** (tüm ortamlarda)
- **Maven** build tool
- **Docker** containerization
- **JUnit 5** + **Mockito** testing
- **Swagger/OpenAPI** documentation
- **JaCoCo** test coverage

## ⚙️ Konfigürasyon Yönetimi

Uygulama profile-based konfigürasyon kullanır:

### 📁 Konfigürasyon Dosyaları

- `application.yaml` - Ana konfigürasyon
- `application-dev.yaml` - Development ortamı
- `application-test.yaml` - Test ortamı
- `application-prod.yaml` - Production ortamı

### 🔧 Profile Kullanımı

```bash
# Development (varsayılan)
./mvnw spring-boot:run

# Test profili ile çalıştırma
./mvnw spring-boot:run -Dspring-boot.run.profiles=test

# Production profili ile çalıştırma
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET=your-production-jwt-secret
export DB_URL=jdbc:h2:file:/app/data/digitalwallet_prod
export DB_USERNAME=sa
export DB_PASSWORD=password
./mvnw spring-boot:run
```

### 🔐 Environment Variables

**Development:**

- JWT secret yerleşik olarak tanımlı
- H2 in-memory database
- Swagger UI aktif

**Test:**

- Test için güvenli JWT secret
- H2 test database (ayrı in-memory)
- Swagger UI devre dışı
- Minimal logging

**Production:**

- `JWT_SECRET` (zorunlu)
- `DB_URL` - H2 file database path (opsiyonel, varsayılan: `/app/data/digitalwallet_prod`)
- `DB_USERNAME` - Database kullanıcı adı (opsiyonel, varsayılan: sa)
- `DB_PASSWORD` - Database şifresi (opsiyonel, varsayılan: password)
- `H2_CONSOLE_ENABLED` - H2 console aktif/pasif (opsiyonel, varsayılan: false)
- `SERVER_PORT` (opsiyonel, varsayılan: 8080)

## 🏃‍♂️ Hızlı Başlangıç

### Development Ortamı

```bash
# Uygulamayı çalıştır (dev profili)
./mvnw spring-boot:run

# Testleri çalıştır
./mvnw test

# Test coverage raporu oluştur
./mvnw jacoco:report
```

### Docker ile Çalıştırma

```bash
# Development
docker-compose up

# Production
docker-compose -f docker-compose.prod.yml up -d

# Test ortamı
docker-compose -f docker-compose.test.yml up
```

## 🌐 Erişim Noktaları

- **API Base URL**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` (sadece dev)
- **H2 Console**: `http://localhost:8080/h2-console` (sadece dev)
- **Health Check**: `http://localhost:8080/actuator/health`

## 👥 Test Kullanıcıları

| Role     | Username | Password |
|----------|----------|----------|
| CUSTOMER | customer | password |
| EMPLOYEE | employee | password |

## 🔐 API Kullanımı

### 1. Giriş Yapma

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"customer","password":"password"}'
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "role": "CUSTOMER",
  "username": "customer"
}
```

### 2. API Çağrıları

Tüm korumalı endpoint'lerde token kullanın:

```bash
Authorization: Bearer <token>
```

## 📡 Ana API Endpoint'leri

### Kimlik Doğrulama

- `POST /auth/login` - Kullanıcı girişi

### Cüzdan İşlemleri

- `POST /wallets` - Yeni cüzdan oluştur
- `GET /wallets` - Cüzdanları listele
- `GET /wallets/{id}/transactions` - Cüzdan işlem geçmişi

### Para İşlemleri

- `POST /transactions/deposit` - Para yatırma
- `POST /transactions/withdraw` - Para çekme
- `POST /transactions/{id}/decision` - İşlem onayı/reddi (EMPLOYEE)

## 📝 API Örnekleri

### Cüzdan Oluşturma

```bash
curl -X POST http://localhost:8080/wallets \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"currency":"TRY"}'
```

### Para Yatırma

```bash
curl -X POST http://localhost:8080/transactions/deposit \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"walletId":1,"amount":100.00,"description":"İlk yatırım"}'
```

### Para Çekme

```bash
curl -X POST http://localhost:8080/transactions/withdraw \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"walletId":1,"amount":50.00,"description":"Para çekme"}'
```

## 🔧 Yapılandırma

Proje ortam bazlı yapılandırma kullanır:

### Development (.env)

```bash
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:h2:mem:digitalwallet
H2_CONSOLE_ENABLED=true
SWAGGER_ENABLED=true
LOG_LEVEL=DEBUG
```

### Production (.env.prod)

```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:h2:file:/app/data/digitalwallet_prod
H2_CONSOLE_ENABLED=false
SWAGGER_ENABLED=false
LOG_LEVEL=INFO
```

### Test (.env.test)

```bash
SPRING_PROFILES_ACTIVE=test
DB_URL=jdbc:h2:mem:digitalwallet_test
H2_CONSOLE_ENABLED=true
SWAGGER_ENABLED=true
LOG_LEVEL=DEBUG
```

## 🧪 Test Coverage

Proje %92 test coverage'a sahiptir:

```bash
# Test çalıştır ve coverage raporu oluştur
./mvnw clean test jacoco:report

# Coverage raporu görüntüle
open target/site/jacoco/index.html
```

## 🐳 Docker Komutları

```bash
# Development ortamı
docker-compose up

# Production ortamı başlat
docker-compose -f docker-compose.prod.yml up -d

# Test ortamı başlat
docker-compose -f docker-compose.test.yml up

# Tüm container'ları durdur
docker-compose down

# Logları görüntüle
docker-compose logs -f app
```

## 📋 İş Kuralları

1. **Müşteriler** sadece kendi cüzdanlarını görebilir ve yönetebilir
2. **Çalışanlar** tüm cüzdanları görebilir ve yönetebilir
3. **Para çekme** işlemleri çalışan onayı gerektirir
4. **Para yatırma** işlemleri otomatik onaylanır
5. Cüzdan bakiyesi negatif olamaz
6. Her cüzdan tek bir para birimine sahiptir

## 📁 Proje Yapısı

```
src/main/java/com/furkan/digitalWallet/
├── controller/     # REST API controllers
├── service/        # Business logic
├── repository/     # Data access layer
├── entity/         # JPA entities
├── security/       # JWT & Security config
├── config/         # Application configuration
├── request/        # Request DTOs
├── exception/      # Custom exceptions
└── enums/          # Enums (Currency, Role, etc.)
```

## 🤝 Katkıda Bulunma

1. Fork the project
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
