INSERT INTO groups (group_name)
SELECT CONCAT(
         chr(65 + floor(random() * 26)::int),
         chr(65 + floor(random() * 26)::int),
         '-',
         lpad(floor(random() * 100)::text, 2, '0')
       )
FROM generate_series(1,10);

INSERT INTO courses (course_name, course_description) VALUES
  ('Mathematics', 'Course covering fundamental mathematical concepts.'),
  ('Biology', 'Introduction to the study of living organisms.'),
  ('Chemistry', 'Basics of chemical reactions and properties.'),
  ('Physics', 'Fundamentals of matter, energy, and forces.'),
  ('History', 'Overview of historical events and trends.'),
  ('Geography', 'Study of the Earth and its features.'),
  ('Literature', 'Exploration of literary works and genres.'),
  ('Computer Science', 'Introduction to computer programming and algorithms.'),
  ('Economics', 'Principles of economic theory and practice.'),
  ('Art', 'Appreciation and study of visual arts.');

INSERT INTO students (group_id, first_name, last_name)
SELECT 
  (SELECT group_id FROM groups ORDER BY random() LIMIT 1),
  f.firstname,
  l.lastname
FROM 
  (SELECT unnest(array[
      'Alice','Bob','Charlie','David','Eva','Frank','Grace','Hannah','Ian','Jane',
      'Kevin','Laura','Michael','Nina','Oliver','Pam','Quentin','Rachel','Steve','Tina'
  ]) AS firstname) AS f
CROSS JOIN 
  (SELECT unnest(array[
      'Anderson','Brown','Clark','Davis','Evans','Franklin','Garcia','Harris','Ivanov','Johnson',
      'King','Lewis','Martinez','Nelson','Olsen','Perez','Quinn','Roberts','Smith','Turner'
  ]) AS lastname) AS l
ORDER BY random()
LIMIT 200;

INSERT INTO students_courses (student_id, course_id)
SELECT s.student_id, c.course_id
FROM students s
CROSS JOIN LATERAL (
    SELECT course_id
    FROM courses
    ORDER BY random() + (s.student_id * 0)
    LIMIT (floor(random() * 3) + 1)::int
) c;
