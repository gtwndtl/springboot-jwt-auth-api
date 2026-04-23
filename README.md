# Spring Boot Demo - REST API + JWT Auth

โปรเจกต์นี้ทำขึ้นมาเพื่อศึกษา Spring Boot ตั้งแต่เริ่มต้น ลองสร้าง REST API ที่มีระบบ login/register พร้อม JWT authentication และ CRUD user ทั่วไป

ใช้ Java 26 + Spring Boot 4.0.5 + MySQL

## ศึกษาอะไรไปบ้าง

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

### Interceptor (ป้องกัน Route)
- สร้าง `AuthInterceptor` implement `HandlerInterceptor` ดัก request ก่อนเข้า controller
- เช็ค header `Authorization: Bearer <token>` ถ้าไม่มีหรือ token ไม่ valid ก็ return 401
- ถ้า token ผ่าน ดึง email ออกมาแล้ว set ลง `request.setAttribute("email", email)` ให้ controller เอาไปใช้ต่อ
- config ใน `WebConfig` กำหนดว่า route ไหนต้อง authen (`/**`) route ไหนไม่ต้อง (`/login`, `/register`)

### Password
- ใช้ BCrypt hash password ก่อนเก็บลง DB ไม่เก็บ plain text
- สร้าง `PasswordEncoder` bean ไว้ใน `PasswordConfig` แล้ว inject ไปใช้ใน controller

### Validation
- ใช้ Jakarta Validation เช่น `@NotBlank`, `@Email` ติดไว้ที่ field ใน DTO
- ใส่ `@Valid` หน้า `@RequestBody` ให้ Spring validate อัตโนมัติ ถ้าไม่ผ่านจะ throw `MethodArgumentNotValidException`

### Exception Handling
- สร้าง `GlobalExceptionHandler` ด้วย `@RestControllerAdvice` จัดการ error ที่เดียว
- จับ validation error, HTTP status error, duplicate data error แล้ว return JSON format ที่อ่านง่าย

## โครงสร้างโปรเจกต์

```
src/main/java/com/gtwndtl/demo/
├── DemoApplication.java
├── config/
│   ├── PasswordConfig.java          # BCrypt bean
│   └── WebConfig.java               # Interceptor config
├── controllers/
│   ├── AuthController.java          # register, login, profile
│   ├── UserController.java          # CRUD users
│   └── GlobalExceptionHandler.java  # จัดการ error
├── dtos/
│   ├── LoginRequestDto.java
│   ├── LoginResponseDto.java
│   ├── RegisterRequestDto.java
│   ├── UserDto.java
│   └── UserResponseDto.java
├── interceptor/
│   └── AuthInterceptor.java         # เช็ค JWT token
├── models/
│   └── UserModel.java               # Entity map กับ DB
├── repositories/
│   └── UserRepository.java          # Data access
└── utils/
    └── JwtUtil.java                 # สร้าง/เช็ค JWT
```

## API Endpoints

**ไม่ต้อง Token:**

| Method | Endpoint    | ทำอะไร                  | Body                              |
|--------|-------------|------------------------|-----------------------------------|
| POST   | `/register` | สมัครสมาชิก              | `{ username, email, password }`   |
| POST   | `/login`    | login แล้วได้ JWT กลับมา | `{ email, password }`             |

**ต้องแนบ Token:**

| Method | Endpoint        | ทำอะไร              | Body / Params                              |
|--------|-----------------|---------------------|--------------------------------------------|
| GET    | `/profile`      | ดูโปรไฟล์ตัวเอง       | -                                          |
| GET    | `/users`        | ดู user ทั้งหมด       | -                                          |
| GET    | `/users/{id}`   | ดู user ตาม id       | -                                          |
| POST   | `/users/search` | หา user ด้วย email   | `{ email }`                                |
| POST   | `/users`        | สร้าง user ใหม่       | `{ username, firstname, lastname, email, password }` |
| PATCH  | `/users/{id}`   | แก้ข้อมูล user        | `{ username?, firstname?, lastname? }`     |
| DELETE | `/users/{id}`   | ลบ user              | -                                          |

## วิธีรัน

ต้องมี Java 26+, MySQL, Maven

1. สร้าง database:
```sql
CREATE DATABASE mydb;
```

2. แก้ `application.properties` ใส่ password MySQL ของตัวเอง

3. รัน:
```bash
./mvnw spring-boot:run
```

## ลองเล่น

register:
```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","password":"1234"}'
```

login แล้วเอา token ไปใช้:
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"1234"}'
```

เรียก API ที่ต้อง authen:
```bash
curl http://localhost:8080/profile \
  -H "Authorization: Bearer <token_ที่ได้จาก_login>"
```

## Dependencies ที่ใช้

- spring-boot-starter-web — สร้าง REST API
- spring-boot-starter-data-jpa — เชื่อม DB ผ่าน JPA
- mysql-connector-j — driver สำหรับ MySQL
- spring-boot-starter-validation — validate request body
- spring-security-crypto — BCrypt สำหรับ hash password
- jjwt (api + impl + jackson) v0.12.3 — สร้างและเช็ค JWT token
