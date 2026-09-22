-- ============================================================
--  HOSTEL ROOM ALLOCATION AND MANAGEMENT SYSTEM
--  MySQL schema + seed data
--  Run with:  mysql -u root -p < database/schema.sql
-- ============================================================

DROP DATABASE IF EXISTS hostel_management;
CREATE DATABASE hostel_management;
USE hostel_management;

-- ------------------------------------------------------------
-- 1. admins
-- ------------------------------------------------------------
CREATE TABLE admins (
    admin_id  INT AUTO_INCREMENT PRIMARY KEY,
    username  VARCHAR(30) NOT NULL UNIQUE,
    password  VARCHAR(30) NOT NULL
);

-- ------------------------------------------------------------
-- 2. students
--    register_number is UNIQUE -> the database itself refuses
--    duplicates, even if the Java check is somehow bypassed.
-- ------------------------------------------------------------
CREATE TABLE students (
    student_id      INT AUTO_INCREMENT PRIMARY KEY,
    register_number VARCHAR(20)  NOT NULL UNIQUE,
    name            VARCHAR(50)  NOT NULL,
    department      VARCHAR(30)  NOT NULL,
    year            INT          NOT NULL,
    gender          VARCHAR(10)  NOT NULL,
    phone           VARCHAR(10)  NOT NULL,
    email           VARCHAR(60)  NOT NULL,
    address         VARCHAR(150)
);

-- ------------------------------------------------------------
-- 3. hostel_blocks
-- ------------------------------------------------------------
CREATE TABLE hostel_blocks (
    block_id   INT AUTO_INCREMENT PRIMARY KEY,
    block_name VARCHAR(30) NOT NULL UNIQUE,
    gender     VARCHAR(10) NOT NULL,
    floors     INT         NOT NULL
);

-- ------------------------------------------------------------
-- 4. rooms
--    A room number only has to be unique INSIDE its block,
--    so the UNIQUE constraint covers both columns together.
-- ------------------------------------------------------------
CREATE TABLE rooms (
    room_id       INT AUTO_INCREMENT PRIMARY KEY,
    room_number   VARCHAR(10) NOT NULL,
    block_id      INT         NOT NULL,
    floor         INT         NOT NULL,
    capacity      INT         NOT NULL,
    occupied_beds INT         NOT NULL DEFAULT 0,
    status        VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT uq_room_in_block UNIQUE (block_id, room_number),
    CONSTRAINT fk_room_block FOREIGN KEY (block_id)
        REFERENCES hostel_blocks(block_id)
);

-- ------------------------------------------------------------
-- 5. allocations
--    status: ACTIVE | CHECKED_OUT | TRANSFERRED
--    Old rows are never deleted, so this table doubles as the
--    transfer / stay history of every student.
-- ------------------------------------------------------------
CREATE TABLE allocations (
    allocation_id   INT AUTO_INCREMENT PRIMARY KEY,
    student_id      INT  NOT NULL,
    room_id         INT  NOT NULL,
    allocation_date DATE NOT NULL,
    checkout_date   DATE NULL,
    status          VARCHAR(15) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_alloc_student FOREIGN KEY (student_id)
        REFERENCES students(student_id),
    CONSTRAINT fk_alloc_room FOREIGN KEY (room_id)
        REFERENCES rooms(room_id)
);

-- ------------------------------------------------------------
-- 6. complaints
--    status: PENDING | IN_PROGRESS | RESOLVED
-- ------------------------------------------------------------
CREATE TABLE complaints (
    complaint_id  INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT          NOT NULL,
    category      VARCHAR(20)  NOT NULL,
    description   VARCHAR(255) NOT NULL,
    status        VARCHAR(15)  NOT NULL DEFAULT 'PENDING',
    created_date  DATE         NOT NULL,
    resolved_date DATE         NULL,
    CONSTRAINT fk_complaint_student FOREIGN KEY (student_id)
        REFERENCES students(student_id)
);

-- ------------------------------------------------------------
-- 7. hostel_fees
--    status: PENDING | PAID
-- ------------------------------------------------------------
CREATE TABLE hostel_fees (
    fee_id       INT AUTO_INCREMENT PRIMARY KEY,
    student_id   INT           NOT NULL,
    amount       DECIMAL(10,2) NOT NULL,
    payment_date DATE          NULL,
    status       VARCHAR(10)   NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_fee_student FOREIGN KEY (student_id)
        REFERENCES students(student_id)
);

-- ------------------------------------------------------------
-- 8. waiting_list
--    status: WAITING | ALLOCATED | REMOVED
--    priority: 1 = highest
-- ------------------------------------------------------------
CREATE TABLE waiting_list (
    waiting_id   INT AUTO_INCREMENT PRIMARY KEY,
    student_id   INT  NOT NULL,
    request_date DATE NOT NULL,
    priority     INT  NOT NULL DEFAULT 5,
    status       VARCHAR(15) NOT NULL DEFAULT 'WAITING',
    CONSTRAINT fk_wait_student FOREIGN KEY (student_id)
        REFERENCES students(student_id)
);

-- ============================================================
--  SEED DATA (so the project has something to show in a demo)
-- ============================================================

INSERT INTO admins (username, password) VALUES
    ('admin',  'admin123'),
    ('warden', 'warden123');

INSERT INTO hostel_blocks (block_name, gender, floors) VALUES
    ('Block A', 'MALE',   3),
    ('Block B', 'MALE',   2),
    ('Block C', 'FEMALE', 3);

INSERT INTO rooms (room_number, block_id, floor, capacity, occupied_beds, status) VALUES
    ('A-101', 1, 1, 4, 0, 'AVAILABLE'),
    ('A-102', 1, 1, 4, 0, 'AVAILABLE'),
    ('A-201', 1, 2, 3, 0, 'AVAILABLE'),
    ('B-101', 2, 1, 2, 0, 'AVAILABLE'),
    ('B-102', 2, 1, 2, 0, 'MAINTENANCE'),
    ('C-101', 3, 1, 4, 0, 'AVAILABLE'),
    ('C-201', 3, 2, 3, 0, 'AVAILABLE');

INSERT INTO students
    (register_number, name, department, year, gender, phone, email, address) VALUES
    ('26CSE001', 'Rohith',  'CSE', 2, 'MALE',   '9876543210', 'rohith@gmail.com',  'Salem'),
    ('26CSE002', 'Kumar',   'CSE', 3, 'MALE',   '9876543211', 'kumar@gmail.com',   'Erode'),
    ('26ECE010', 'Priya',   'ECE', 1, 'FEMALE', '9876543212', 'priya@gmail.com',   'Chennai'),
    ('26MEC005', 'Arun',    'MECH',4, 'MALE',   '9876543213', 'arun@gmail.com',    'Madurai'),
    ('26CSE004', 'Divya',   'CSE', 2, 'FEMALE', '9876543214', 'divya@gmail.com',   'Trichy');

INSERT INTO hostel_fees (student_id, amount, payment_date, status) VALUES
    (1, 45000.00, NULL, 'PENDING'),
    (2, 45000.00, '2026-07-15', 'PAID'),
    (3, 48000.00, NULL, 'PENDING');

SELECT 'Database hostel_management created successfully.' AS message;
