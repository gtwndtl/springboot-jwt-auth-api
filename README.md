# Spring Boot REST API with JWT Authentication

โปรเจกต์นี้ทำขึ้นมาเพื่อศึกษา Spring Boot ตั้งแต่เริ่มต้น ลองสร้าง REST API ที่มีระบบ login/register พร้อม JWT authentication และ CRUD user ทั่วไป

Built with Java 26 + Spring Boot 4.0.5 + MySQL

## What I Learned

### REST API
- สร้าง API ด้วย `@RestController` ใช้ `@GetMapping`, `@PostMapping`, `@PatchMapping`, `@DeleteMapping`
- รับ data จาก request body ด้วย `@RequestBody` และจาก URL path ด้วย `@PathVariable`
- ใช้ DTO แยก request/response ออกจาก entity ไม่ส่ง model ตรง ๆ ไปที่ client

### Database (JPA + MySQL)
- เชื่อมต่อ MySQL ผ่าน Spring Data JPA
- สร้าง entity ด้วย `@Entity`, `@Table`, `@Column` แล้ว map กับตาราง user ใน DB
- ใช้ `JpaRepository` เป็น repository ไม่ต้องเขียน SQL เอง เรียก `.save()`, `.findAll()`, `.findById()`, `.deleteById()` ได้เลย
- เขียน custom query แบบง่าย ๆ ด้วย method name convention เช่น `findByEmail(String email)` Spring จะ generate query ให้อัตโนมัติ
- ตั้ง `ddl-auto=update` ให้ Hibernate sync schema ให้เอง ไม่ต้อง migrate มือ

### JWT Authentication
- ใช้ library `jjwt` (v0.12.3) สร้าง JWT token ตอน login
- sign ด้วย HmacSHA256 ตั้ง expiration 24 ชั่วโมง
- สร้าง `JwtUtil` class สำหรับ generate token, extract email จาก token, เช็ค valid/expired
- เก็บ secret key กับ expiration ไว้ใน `application.properties` อ่านค่าด้วย `@Value`

### Interceptor (Route Protection)
- สร้าง `AuthInterceptor` implement `HandlerInterceptor` ดัก request ก่อนเข้า controller
- เช็ค header `Authorization: Bearer <token>` ถ้าไม่มีหรือ token ไม่ valid ก็ return 401
- ถ้า token ผ่าน ดึง email ออกมาแล้ว set ลง `request.setAttribute("email", email)` ให้ controller เอาไปใช้ต่อ
- config ใน `WebConfig` กำหนดว่า route ไหนต้อง authen (`/**`) route ไหนไม่ต้อง (`/login`, `/register`)

### Password Hashing
- ใช้ BCrypt hash password ก่อนเก็บลง DB ไม่เก็บ plain text
- สร้าง `PasswordEncoder` bean ไว้ใน `PasswordConfig` แล้ว inject ไปใช้ใน controller

### Validation
- ใช้ Jakarta Validation เช่น `@NotBlank`, `@Email` ติดไว้ที่ field ใน DTO
- ใส่ `@Valid` หน้า `@RequestBody` ให้ Spring validate อัตโนมัติ ถ้าไม่ผ่านจะ throw `MethodArgumentNotValidException`

### Global Exception Handling
- สร้าง `GlobalExceptionHandler` ด้วย `@RestControllerAdvice` จัดการ error ที่เดียว
- จับ validation error, HTTP status error, duplicate data error แล้ว return JSON format ที่อ่านง่าย

## Project Structure

```
src/main/java/com/gtwndtl/demo/
├── DemoApplication.java
├── config/
│   ├── PasswordConfig.java          # BCrypt bean
│   └── WebConfig.java               # Interceptor config
├── controllers/
│   ├── AuthController.java          # register, login, profile
│   ├── UserController.java          # CRUD users
│   └── GlobalExceptionHandler.java  # error handling
├── dtos/
│   ├── LoginRequestDto.java
│   ├── LoginResponseDto.java
│   ├── RegisterRequestDto.java
│   ├── UserDto.java
│   └── UserResponseDto.java
├── interceptor/
│   └── AuthInterceptor.java         # JWT token verification
├── models/
│   └── UserModel.java               # JPA entity
├── repositories/
│   └── UserRepository.java          # data access layer
└── utils/
    └── JwtUtil.java                 # JWT utility
```

## API Endpoints

### Public (no token required)

| Method | Endpoint    | Description      | Body                              |
|--------|-------------|------------------|-----------------------------------|
| POST   | `/register` | Register new user | `{ username, email, password }`   |
| POST   | `/login`    | Login & get JWT  | `{ email, password }`             |

### Protected (Bearer token required)

| Method | Endpoint        | Description       | Body / Params                              |
|--------|-----------------|--------------------|--------------------------------------------|
| GET    | `/profile`      | Get own profile    | -                                          |
| GET    | `/users`        | Get all users      | -                                          |
| GET    | `/users/{id}`   | Get user by ID     | -                                          |
| POST   | `/users/search` | Search by email    | `{ email }`                                |
| POST   | `/users`        | Create new user    | `{ username, firstname, lastname, email, password }` |
| PATCH  | `/users/{id}`   | Update user        | `{ username?, firstname?, lastname? }`     |
| DELETE | `/users/{id}`   | Delete user        | -                                          |

## Getting Started

### Prerequisites
- Java 26+
- MySQL
- Maven

### Setup

1. Create database:
```sql
CREATE DATABASE mydb;
```

2. Update `application.properties` with your MySQL password.

3. Run:
```bash
./mvnw spring-boot:run
```

### Usage Examples

Register:
```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","password":"1234"}'
```

Login:
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"1234"}'
```

Call protected API:
```bash
curl http://localhost:8080/profile \
  -H "Authorization: Bearer <token_from_login>"
```

## Dependencies

- `spring-boot-starter-web` — REST API
- `spring-boot-starter-data-jpa` — JPA + Hibernate
- `mysql-connector-j` — MySQL driver
- `spring-boot-starter-validation` — request validation
- `spring-security-crypto` — BCrypt password hashing
- `jjwt` (api + impl + jackson) v0.12.3 — JWT token
