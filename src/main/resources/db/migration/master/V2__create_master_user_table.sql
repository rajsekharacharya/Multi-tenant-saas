CREATE TABLE master_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'SUPERADMIN'
);

-- Insert a default superadmin (password: superadmin, hashed with BCrypt)
INSERT INTO master_user (username, password, role)
VALUES ('superadmin', '$2a$12$yUudSTTDLmXEKb/g46yT/e6zr0rOFNcsApIQeYALcZQxHSwgWeGEy', 'SUPERADMIN');