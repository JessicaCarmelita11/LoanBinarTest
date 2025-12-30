package com.example.ProjectBinar.config;

import com.example.ProjectBinar.entity.Plafond;
import com.example.ProjectBinar.entity.Role;
import com.example.ProjectBinar.entity.User;
import com.example.ProjectBinar.entity.Branch;
import com.example.ProjectBinar.repository.PlafondRepository;
import com.example.ProjectBinar.repository.RoleRepository;
import com.example.ProjectBinar.repository.UserRepository;
import com.example.ProjectBinar.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Data Initializer - Menginisialisasi data awal saat aplikasi dijalankan.
 * 
 * Mendukung:
 * - Database baru (create all roles dan users)
 * - Database existing (create missing roles dan users dengan password ter-hash)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

        private final RoleRepository roleRepository;
        private final UserRepository userRepository;
        private final BranchRepository branchRepository;
        private final PlafondRepository plafondRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        public void run(String... args) throws Exception {
                log.info("=== Initializing Data ===");

                // Selalu pastikan roles yang diperlukan ada
                initializeRoles();

                // Selalu pastikan sample users ada dengan password ter-hash
                initializeUsers();

                // Inisialisasi Branch jika belum ada
                if (branchRepository.count() == 0) {
                        initializeBranch();
                } else {
                        log.info("Branch already exists, skipping branch initialization.");
                }

                // Inisialisasi Plafond jika belum ada
                if (plafondRepository.count() == 0) {
                        initializePlafondData();
                } else {
                        log.info("Plafond data already exists, skipping plafond initialization.");
                }

                log.info("=== Data Initialization Complete ===");
        }

        /**
         * Inisialisasi roles - buat role jika belum ada.
         */
        private void initializeRoles() {
                createRoleIfNotExists("CUSTOMER");
                createRoleIfNotExists("MARKETING");
                createRoleIfNotExists("BRANCH_MANAGER");
                createRoleIfNotExists("BACK_OFFICE");
        }

        /**
         * Buat role jika belum ada.
         */
        private Role createRoleIfNotExists(String roleName) {
                Optional<Role> existingRole = roleRepository.findByName(roleName);
                if (existingRole.isPresent()) {
                        log.info("Role {} already exists", roleName);
                        return existingRole.get();
                }

                Role role = Role.builder().name(roleName).build();
                role = roleRepository.save(role);
                log.info("Created role: {}", roleName);
                return role;
        }

        /**
         * Inisialisasi users dengan password ter-hash.
         * Update password jika user sudah ada tapi passwordnya belum ter-hash.
         */
        private void initializeUsers() {
                String encodedPassword = passwordEncoder.encode("password123");

                // Get or create roles
                Role customerRole = getOrCreateRole("CUSTOMER");
                Role marketingRole = getOrCreateRole("MARKETING");
                Role branchManagerRole = getOrCreateRole("BRANCH_MANAGER");
                Role backOfficeRole = getOrCreateRole("BACK_OFFICE");

                // Create users if not exists
                createOrUpdateUser("customer", "customer@example.com", encodedPassword, Set.of(customerRole));
                createOrUpdateUser("marketing", "marketing@example.com", encodedPassword, Set.of(marketingRole));
                createOrUpdateUser("branchmanager", "branchmanager@example.com", encodedPassword,
                                Set.of(branchManagerRole));
                createOrUpdateUser("backoffice", "backoffice@example.com", encodedPassword, Set.of(backOfficeRole));

                // Super admin with all roles
                Set<Role> allRoles = new HashSet<>();
                allRoles.add(customerRole);
                allRoles.add(marketingRole);
                allRoles.add(branchManagerRole);
                allRoles.add(backOfficeRole);
                createOrUpdateUser("admin", "admin@example.com", encodedPassword, allRoles);
        }

        /**
         * Get role by name or create if not exists.
         */
        private Role getOrCreateRole(String roleName) {
                return roleRepository.findByName(roleName)
                                .orElseGet(() -> {
                                        Role role = Role.builder().name(roleName).build();
                                        return roleRepository.save(role);
                                });
        }

        /**
         * Create user if not exists, or update password if exists.
         */
        private void createOrUpdateUser(String username, String email, String encodedPassword, Set<Role> roles) {
                Optional<User> existingUser = userRepository.findByUsername(username);

                if (existingUser.isPresent()) {
                        User user = existingUser.get();
                        // Update password jika belum ter-hash (tidak dimulai dengan $2a$)
                        if (user.getPassword() == null || !user.getPassword().startsWith("$2a$")) {
                                user.setPassword(encodedPassword);
                                user.setRoles(roles);
                                userRepository.save(user);
                                log.info("Updated user {} with hashed password and roles", username);
                        } else {
                                log.info("User {} already exists with hashed password", username);
                        }
                } else {
                        User user = User.builder()
                                        .username(username)
                                        .email(email)
                                        .password(encodedPassword)
                                        .isActive(true)
                                        .roles(roles)
                                        .build();
                        userRepository.save(user);
                        log.info("Created user: {} with roles: {}", username,
                                        roles.stream().map(Role::getName).toList());
                }
        }

        /**
         * Inisialisasi branch.
         */
        private void initializeBranch() {
                Branch branch = Branch.builder()
                                .name("Branch Jakarta Pusat")
                                .address("Jalan Sudirman No. 1")
                                .city("Jakarta")
                                .build();
                branchRepository.save(branch);
                log.info("Created branch: {}", branch.getName());

                Branch branch2 = Branch.builder()
                                .name("Branch Surabaya")
                                .address("Jalan Basuki Rahmat No. 10")
                                .city("Surabaya")
                                .build();
                branchRepository.save(branch2);
                log.info("Created branch: {}", branch2.getName());
        }

        /**
         * Inisialisasi data plafond untuk pengajuan kredit.
         */
        private void initializePlafondData() {
                Plafond bronze = Plafond.builder()
                                .name("Bronze")
                                .description("Pinjaman level bronze untuk pemula dengan limit rendah")
                                .maxAmount(new BigDecimal("10000000"))
                                .interestRate(new BigDecimal("15.00"))
                                .tenorMonth(12)
                                .isActive(true)
                                .build();
                plafondRepository.save(bronze);
                log.info("Created plafond: {} - Max: Rp {}", bronze.getName(), bronze.getMaxAmount());

                Plafond silver = Plafond.builder()
                                .name("Silver")
                                .description("Pinjaman level silver dengan limit menengah")
                                .maxAmount(new BigDecimal("25000000"))
                                .interestRate(new BigDecimal("13.50"))
                                .tenorMonth(18)
                                .isActive(true)
                                .build();
                plafondRepository.save(silver);
                log.info("Created plafond: {} - Max: Rp {}", silver.getName(), silver.getMaxAmount());

                Plafond gold = Plafond.builder()
                                .name("Gold")
                                .description("Pinjaman level gold dengan limit tinggi dan bunga kompetitif")
                                .maxAmount(new BigDecimal("50000000"))
                                .interestRate(new BigDecimal("12.00"))
                                .tenorMonth(24)
                                .isActive(true)
                                .build();
                plafondRepository.save(gold);
                log.info("Created plafond: {} - Max: Rp {}", gold.getName(), gold.getMaxAmount());

                Plafond platinum = Plafond.builder()
                                .name("Platinum")
                                .description("Pinjaman level platinum premium dengan limit maksimal")
                                .maxAmount(new BigDecimal("100000000"))
                                .interestRate(new BigDecimal("10.50"))
                                .tenorMonth(36)
                                .isActive(true)
                                .build();
                plafondRepository.save(platinum);
                log.info("Created plafond: {} - Max: Rp {}", platinum.getName(), platinum.getMaxAmount());

                Plafond diamond = Plafond.builder()
                                .name("Diamond")
                                .description("Pinjaman level diamond VIP - sementara tidak tersedia")
                                .maxAmount(new BigDecimal("200000000"))
                                .interestRate(new BigDecimal("9.00"))
                                .tenorMonth(48)
                                .isActive(false)
                                .build();
                plafondRepository.save(diamond);
                log.info("Created plafond: {} - Max: Rp {} (inactive)", diamond.getName(), diamond.getMaxAmount());
        }
}
