DROP TABLE IF EXISTS student_courses;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS groups;

DROP SEQUENCE IF EXISTS group_id_seq;
DROP SEQUENCE IF EXISTS student_id_seq;
DROP SEQUENCE IF EXISTS course_id_seq;

CREATE SEQUENCE group_id_seq START 100;
CREATE TABLE groups (
    group_id INT PRIMARY KEY DEFAULT nextval('group_id_seq'),
    group_name VARCHAR(255) NOT NULL
);

CREATE SEQUENCE student_id_seq START 1000;
CREATE TABLE students (
    student_id INT PRIMARY KEY DEFAULT nextval('student_id_seq'),
    group_id INT REFERENCES groups(group_id) ON DELETE CASCADE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL
);

CREATE SEQUENCE course_id_seq START 1;
CREATE TABLE courses (
    course_id INT PRIMARY KEY DEFAULT nextval('course_id_seq'),
    course_name VARCHAR(255) NOT NULL,
    course_description TEXT
);

CREATE TABLE student_courses (
    student_id INT REFERENCES students(student_id) ON DELETE CASCADE,
    course_id INT REFERENCES courses(course_id) ON DELETE CASCADE,
    PRIMARY KEY (student_id, course_id)
);
