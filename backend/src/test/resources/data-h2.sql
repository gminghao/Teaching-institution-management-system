-- Test data: admin user (password is BCrypt of 'admin123')
INSERT INTO admin_user (username, password, real_name, status) VALUES
('admin', '$2a$10$CBr8M3z3oPFt2kD2D1BDG.WY5eJ.0PJcdJ6fLsAPrLv4K/0./QWmW', 'Administrator', 1);

-- Test data: course category
INSERT INTO course_category (id, name, sort_order, status) VALUES
(1, 'programming', 1, 1),
(2, 'design', 2, 1);
