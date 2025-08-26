CREATE TABLE tenant_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER'
);

-- Insert a default superadmin (password: superadmin, hashed with BCrypt)
INSERT INTO tenant_user (username, password, role)
VALUES ('admin', '$2a$12$k3KhNJusV99PpnkutfjnC.ZJ/ARxAShaFsyDuwky9lrmbNm1aELP2', 'ADMIN');