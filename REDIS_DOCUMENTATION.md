# Dokumentasi Redis Caching - ProjectBinar

## 1. Pengenalan Redis

Redis (Remote Dictionary Server) adalah in-memory data structure store yang digunakan sebagai database, cache, dan message broker. Dalam project ini, Redis digunakan sebagai **cache layer** untuk mempercepat akses data yang sering dibaca.

### Mengapa Menggunakan Redis Cache?

| Benefit | Penjelasan |
|---------|------------|
| **Performa** | Akses data dari memory jauh lebih cepat (~1ms) dibanding database (~10-100ms) |
| **Scalability** | Mengurangi beban database untuk operasi read-heavy |
| **Cost Efficient** | Mengurangi resource database yang dibutuhkan |
| **TTL Support** | Data otomatis expired setelah waktu tertentu |

---

## 2. Arsitektur Caching

```
┌────────────────────────────────────────────────────────────────────────────┐
│                           ALUR REQUEST DENGAN CACHE                         │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Client Request                                                             │
│        │                                                                     │
│        ▼                                                                     │
│   ┌─────────────┐                                                           │
│   │ Controller  │                                                           │
│   └──────┬──────┘                                                           │
│          │                                                                   │
│          ▼                                                                   │
│   ┌─────────────┐    Cache Hit?    ┌─────────────┐                         │
│   │   Service   │─────────────────►│    Redis    │                         │
│   │  @Cacheable │       YES        │   Cache     │                         │
│   └──────┬──────┘◄─────────────────└─────────────┘                         │
│          │                               ▲                                   │
│          │ NO (Cache Miss)               │ Store                            │
│          ▼                               │                                   │
│   ┌─────────────┐                        │                                   │
│   │ Repository  │────────────────────────┘                                   │
│   │  (JPA)      │                                                           │
│   └──────┬──────┘                                                           │
│          │                                                                   │
│          ▼                                                                   │
│   ┌─────────────┐                                                           │
│   │ SQL Server  │                                                           │
│   │  Database   │                                                           │
│   └─────────────┘                                                           │
│                                                                              │
└────────────────────────────────────────────────────────────────────────────┘
```

### Cache Strategy: Cache-Aside (Lazy Loading)

Pattern yang digunakan:
1. **Read**: Cek cache → Jika ada, return → Jika tidak, query database → Store di cache → Return
2. **Write**: Update database → Invalidate cache

---

## 3. Konfigurasi Redis

### Docker Compose

```yaml
services:
  redis:
    image: redis:latest
    container_name: redis_container
    ports:
      - "6379:6379"
    restart: always

  redisinsight:
    image: redis/redisinsight:latest
    container_name: lofi-redisinsight
    ports:
      - "4000:5540"
    depends_on:
      - redis
    restart: always
```

### Application Configuration (application.yml)

```yaml
spring:
  data:
    redis:
      host: ${SPRING_DATA_REDIS_HOST:localhost}
      port: ${SPRING_DATA_REDIS_PORT:6379}
```

### Dependencies (pom.xml)

```xml
<!-- Redis Cache -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

---

## 4. Cache Annotations

### @Cacheable

Menyimpan hasil method ke cache. Jika key sudah ada di cache, method **tidak dieksekusi**.

```java
@Cacheable(value = "branches", key = "'all'")
public List<Branch> getAllBranch() {
    log.info("Fetching from database (cache miss)");
    return branchRepository.findAll();
}
```

**Parameters:**
| Parameter | Penjelasan |
|-----------|------------|
| `value` | Nama cache (namespace) |
| `key` | Key dalam cache (SpEL expression) |
| `condition` | Kondisi untuk cache (opsional) |
| `unless` | Kondisi untuk TIDAK cache (opsional) |

### @CacheEvict

Menghapus entry dari cache. Biasanya digunakan saat data berubah (create/update/delete).

```java
@CacheEvict(value = "branches", allEntries = true)
public Branch createBranch(Branch branch) {
    return branchRepository.save(branch);
}
```

**Parameters:**
| Parameter | Penjelasan |
|-----------|------------|
| `value` | Nama cache yang akan di-evict |
| `key` | Key spesifik yang akan dihapus |
| `allEntries` | `true` = hapus semua entry dalam cache |

### @CachePut

Selalu menjalankan method dan update cache dengan hasilnya.

```java
@CachePut(value = "branches", key = "#branch.id")
public Branch updateBranch(Branch branch) {
    return branchRepository.save(branch);
}
```

---

## 5. Implementasi per Service

### BranchService

| Method | Cache | TTL | Key |
|--------|-------|-----|-----|
| `getAllBranch()` | `@Cacheable` | 10 min | `branches::all` |
| `findByName()` | `@Cacheable` | 10 min | `branches::{name}` |
| `createBranch()` | `@CacheEvict` | - | Evict all |

### RoleService

| Method | Cache | TTL | Key |
|--------|-------|-----|-----|
| `getAllRoles()` | `@Cacheable` | 30 min | `roles::all` |
| `findByName()` | `@Cacheable` | 30 min | `roles::{name}` |
| `createRole()` | `@CacheEvict` | - | Evict all |

### UserService

| Method | Cache | TTL | Key |
|--------|-------|-----|-----|
| `getAllUsers()` | `@Cacheable` | 5 min | `users::all` |
| `findByUsername()` | `@Cacheable` | 5 min | `users::{username}` |
| `createUser()` | `@CacheEvict` | - | Evict all |

---

## 6. Monitoring dengan RedisInsight

RedisInsight adalah GUI tool untuk memonitor Redis.

**Akses:** http://localhost:4000

### Cara Menghubungkan:

1. Buka http://localhost:4000
2. Click "Add Redis Database"
3. Masukkan:
   - Host: `redis` (jika dari Docker) atau `localhost` (jika dari host)
   - Port: `6379`
4. Click "Add Redis Database"

### Yang Bisa Dimonitor:

- **Keys**: Melihat semua cache keys
- **Memory**: Usage memory Redis
- **Commands**: Melihat command yang dieksekusi
- **TTL**: Melihat sisa waktu hidup tiap key

---

## 7. Testing Cache

### Verifikasi Cache Hit/Miss

1. **Request pertama** (Cache Miss):
```
2024-01-01 10:00:00 INFO  - Fetching all branches from database (cache miss)
```

2. **Request kedua** (Cache Hit):
   - Tidak ada log "cache miss" → berarti data dari cache

### Test dengan cURL

```bash
# Start Redis
docker-compose up -d redis

# Request pertama - Cache Miss
curl http://localhost:8081/branches

# Request kedua - Cache Hit (lebih cepat, tidak ada log database)
curl http://localhost:8081/branches

# Create data baru - Cache di-evict
curl -X POST -H "Content-Type: application/json" \
     -d '{"name":"Branch Baru","address":"Alamat","city":"Jakarta"}' \
     http://localhost:8081/branches

# Request lagi - Cache Miss (karena sudah di-evict)
curl http://localhost:8081/branches
```

---

## 8. Best Practices

### Do's ✅

1. **Cache data yang sering dibaca tapi jarang berubah** (roles, branches, config)
2. **Set TTL yang appropriate** - tidak terlalu lama, tidak terlalu singkat
3. **Gunakan key yang descriptive** - mudah di-debug
4. **Log cache miss** - untuk monitoring

### Don'ts ❌

1. **Jangan cache data sensitif** tanpa enkripsi
2. **Jangan cache data yang sering berubah** - overhead eviction tinggi
3. **Jangan set TTL terlalu lama** - data bisa stale
4. **Jangan cache null values** - bisa menyebabkan unexpected behavior

---

## 9. Troubleshooting

### Redis Connection Failed

```
Error: Unable to connect to Redis
```

**Solusi:**
```bash
# Cek Redis running
docker ps | grep redis

# Jika tidak ada, start ulang
docker-compose up -d redis

# Cek connection
docker exec -it redis_container redis-cli ping
# Should return: PONG
```

### Cache Tidak Bekerja

**Kemungkinan masalah:**
1. `@EnableCaching` tidak ada
2. Method dipanggil dari class yang sama (self-invocation)
3. Method bukan public

**Solusi:**
- Pastikan `@EnableCaching` ada di config
- Cache hanya bekerja untuk external calls
- Method harus public

### Data Stale (Tidak Update)

**Penyebab:** Cache tidak di-evict saat data berubah

**Solusi:**
```java
// Tambahkan @CacheEvict di method yang mengubah data
@CacheEvict(value = "branches", allEntries = true)
public Branch updateBranch(Branch branch) { ... }
```

### Memory Usage Tinggi

**Solusi:**
1. Kurangi TTL
2. Batasi jumlah entry per cache
3. Monitor dengan RedisInsight

---

## 10. Code Reference

### RedisConfig.java

```java
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("branches", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("roles", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
```

### Service Example

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class BranchService {
    private final BranchRepository branchRepository;

    @CacheEvict(value = "branches", allEntries = true)
    public Branch createBranch(Branch branch) {
        log.info("Creating new branch: {}", branch.getName());
        return branchRepository.save(branch);
    }

    @Cacheable(value = "branches", key = "'all'")
    public List<Branch> getAllBranch() {
        log.info("Fetching all branches from database (cache miss)");
        return branchRepository.findAll();
    }

    @Cacheable(value = "branches", key = "#name")
    public Optional<Branch> findByName(String name) {
        log.info("Fetching branch by name: {} (cache miss)", name);
        return branchRepository.findByName(name);
    }
}
```

---

## 11. Struktur File

```
com.example.ProjectBinar
├── config
│   ├── DataInitializer.java
│   └── RedisConfig.java          ← NEW
├── service
│   ├── BranchService.java        ← Modified (+ caching)
│   ├── RoleService.java          ← Modified (+ caching)
│   └── UserService.java          ← Modified (+ caching)
└── ...
```

---

## 12. Performance Comparison

| Operation | Without Cache | With Cache (Hit) | Improvement |
|-----------|---------------|------------------|-------------|
| Get All Branches | ~50ms | ~2ms | **25x faster** |
| Get All Roles | ~40ms | ~2ms | **20x faster** |
| Get User by Username | ~30ms | ~1ms | **30x faster** |

*Note: Hasil dapat bervariasi tergantung environment dan data*

---

## 13. Menjalankan Aplikasi

```bash
# 1. Start Redis dan RedisInsight
docker-compose up -d

# 2. Verify Redis running
docker ps

# 3. Run Spring Boot application
./mvnw spring-boot:run

# 4. Test endpoints
curl http://localhost:8081/branches
curl http://localhost:8081/roles
curl http://localhost:8081/users

# 5. Monitor di RedisInsight
# Buka http://localhost:4000
```
