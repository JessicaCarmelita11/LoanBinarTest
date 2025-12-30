# Dokumentasi Project Spring Boot - User & Role Management

## 1. Penjelasan Anotasi JPA

### Entity Annotations

| Anotasi | Fungsi |
|---------|--------|
| `@Entity` | Menandai class sebagai JPA entity yang akan di-mapping ke tabel database |
| `@Table(name = "...")` | Menentukan nama tabel di database |
| `@Id` | Menandai field sebagai primary key |
| `@GeneratedValue(strategy = GenerationType.IDENTITY)` | Mengatur auto-increment menggunakan IDENTITY (SQL Server) |
| `@Column` | Mengatur properti kolom (unique, nullable, length, dll) |

### Relationship Annotations

| Anotasi | Fungsi |
|---------|--------|
| `@ManyToMany` | Mendefinisikan relasi many-to-many antara dua entity |
| `@JoinTable` | Menentukan tabel penghubung untuk relasi many-to-many |
| `@JoinColumn` | Menentukan kolom foreign key dalam join table |

### Lombok Annotations

| Anotasi | Fungsi |
|---------|--------|
| `@Data` | Menggenerate getter, setter, toString, equals, hashCode |
| `@Builder` | Menggenerate Builder pattern |
| `@NoArgsConstructor` | Menggenerate constructor tanpa parameter |
| `@AllArgsConstructor` | Menggenerate constructor dengan semua parameter |
| `@RequiredArgsConstructor` | Menggenerate constructor untuk field final |

---

## 2. Mengapa Terbentuk 3 Tabel?

Relasi **Many-to-Many** memerlukan tabel penghubung (join table) untuk menyimpan relasi antar entity.

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│   users     │      │  user_roles  │      │   roles     │
├─────────────┤      ├──────────────┤      ├─────────────┤
│ id (PK)     │◄────►│ user_id (FK) │      │ id (PK)     │
│ username    │      │ role_id (FK) │◄────►│ name        │
│ email       │      └──────────────┘      └─────────────┘
│ password    │
│ is_active   │
└─────────────┘
```

### Penjelasan:
1. **users** - Menyimpan data user
2. **roles** - Menyimpan data role
3. **user_roles** - Tabel penghubung yang menyimpan relasi user-role
   - `user_id`: Foreign key ke tabel users
   - `role_id`: Foreign key ke tabel roles

Contoh data:
```
users: {id: 1, username: "admin"}
roles: {id: 1, name: "ADMIN"}, {id: 2, name: "USER"}
user_roles: {user_id: 1, role_id: 1}, {user_id: 1, role_id: 2}
```

---

## 3. Alur ORM dari Request sampai Database

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           ALUR REQUEST                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  1. HTTP Request                                                         │
│     POST /users                                                          │
│     {"username": "john", "email": "john@example.com"}                   │
│                           │                                              │
│                           ▼                                              │
│  2. Controller Layer (UserController)                                    │
│     - @RestController menerima request                                   │
│     - @RequestBody deserialize JSON → User object                        │
│     - Memanggil UserService                                              │
│                           │                                              │
│                           ▼                                              │
│  3. Service Layer (UserService)                                          │
│     - Business logic (validasi, transformasi)                            │
│     - Memanggil UserRepository                                           │
│                           │                                              │
│                           ▼                                              │
│  4. Repository Layer (UserRepository)                                    │
│     - JpaRepository.save(user)                                           │
│     - Spring Data JPA generate SQL                                       │
│                           │                                              │
│                           ▼                                              │
│  5. Hibernate (ORM)                                                      │
│     - Convert Java object → SQL query                                    │
│     - INSERT INTO users (username, email, ...) VALUES (?, ?, ...)       │
│     - Jika ada roles: INSERT INTO user_roles (user_id, role_id) ...     │
│                           │                                              │
│                           ▼                                              │
│  6. Database (SQL Server)                                                │
│     - Eksekusi SQL query                                                 │
│     - Data tersimpan di tabel users dan user_roles                       │
│                           │                                              │
│                           ▼                                              │
│  7. Response                                                             │
│     - Hibernate return entity dengan ID                                  │
│     - Controller serialize → JSON                                        │
│     - HTTP Response 201 Created                                          │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Endpoint API

| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| GET | `/roles` | Mendapatkan semua role |
| POST | `/roles` | Membuat role baru |
| GET | `/users` | Mendapatkan semua user beserta role |
| POST | `/users` | Membuat user baru |

### Contoh Request & Response

**GET /users**
```json
[
  {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "password": "admin123",
    "isActive": true,
    "roles": [
      {"id": 1, "name": "ADMIN"},
      {"id": 2, "name": "USER"}
    ]
  }
]
```

**POST /roles**
```json
// Request
{"name": "MANAGER"}

// Response (201 Created)
{"id": 3, "name": "MANAGER"}
```

---

## 5. Struktur Package

```
com.example.ProjectBinar
├── ProjectBinarApplication.java    # Main class
├── config
│   └── DataInitializer.java        # Inisialisasi data awal
├── controller
│   ├── RoleController.java         # REST endpoint untuk Role
│   └── UserController.java         # REST endpoint untuk User
├── entity
│   ├── Role.java                   # Entity Role
│   └── User.java                   # Entity User
├── repository
│   ├── RoleRepository.java         # JPA Repository untuk Role
│   └── UserRepository.java         # JPA Repository untuk User
└── service
    ├── RoleService.java            # Business logic untuk Role
    └── UserService.java            # Business logic untuk User
```

---

## 6. Cara Menjalankan

```bash
# Di direktori project
./mvnw spring-boot:run
```

Aplikasi akan berjalan di `http://localhost:8080`

### Test dengan cURL

```bash
# Get all roles
curl http://localhost:8080/roles

# Get all users
curl http://localhost:8080/users

# Create new role
curl -X POST -H "Content-Type: application/json" \
     -d '{"name":"MANAGER"}' http://localhost:8080/roles

# Create new user
curl -X POST -H "Content-Type: application/json" \
     -d '{"username":"john","email":"john@example.com","password":"pass123","isActive":true}' \
     http://localhost:8080/users
```
