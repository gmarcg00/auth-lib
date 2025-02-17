-- Datos de permisos.
INSERT INTO permissions(name) VALUES ('CREATE_USERS');
INSERT INTO permissions(name) VALUES ('READ_ACCOUNT');
INSERT INTO permissions(name) VALUES ('WRITE_ACCOUNT');

-- Datos de roles.
INSERT INTO roles (name) VALUES ('ADMIN');
INSERT INTO roles (name) VALUES ('USER');


-- Datos de relación entre roles y permisos.
INSERT INTO roles_permissions (role_id, permission_id) VALUES (1, 1);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (1, 2);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (1, 3);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 2);

-- Datos de status.
INSERT INTO users_status (name) VALUES ('VERIFICATION_PENDING');
INSERT INTO users_status (name) VALUES ('ACTIVE');
INSERT INTO users_status (name) VALUES ('INACTIVE');

-- Datos de usuarios.
INSERT INTO users (email, password, creation_date, last_password_change, external_user, status_id) VALUES ('fake.user@tenthman.com', '$2a$12$y2LCCFGB/2l8FYX1ktrqHOsaYh6V7okXhMX4/ZG0B0RFwYgniRPJK', '2021-12-01 14:30:15', '2021-12-01 14:30:15', false, 2);
INSERT INTO users (email, password, creation_date, last_password_change, external_user, status_id, verification_code) VALUES ('pending.user@tenthman.com', '$2a$12$y2LCCFGB/2l8FYX1ktrqHOsaYh6V7okXhMX4/ZG0B0RFwYgniRPJK', '2021-12-01 14:30:15', '2021-12-01 14:30:15', false, 1, '0000-1111');
INSERT INTO users (email, password, creation_date, last_password_change, external_user, status_id, verification_code) VALUES ('pending.user.no.password@tenthman.com', null, '2021-12-01 14:30:15', '2021-12-01 14:30:15', false, 1, '0000-1111');
INSERT INTO users (email, password, creation_date, last_password_change, external_user, status_id) VALUES ('inactive.user.no.passwordr@tenthman.com', null, '2021-12-01 14:30:15', '2021-12-01 14:30:15', false, 3);

-- Datos de códigos de sesión.
INSERT INTO exchange_session_codes (code,expiration_date,user_id) VALUES ('0000-1111', '2021-12-01 14:30:15', 1);
INSERT INTO exchange_session_codes (code,expiration_date,user_id) VALUES ('0000-1112', '2040-12-01 14:30:15', 1);

-- Datos de relación entre usuarios y roles.
INSERT INTO users_roles (user_id, role_id) VALUES (1, 1)