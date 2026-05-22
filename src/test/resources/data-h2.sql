INSERT INTO employee (name, department, email, annual_leave_total, annual_leave_used)
VALUES ('张三', '研发中心', 'zhangsan@example.com', 15, 5);

INSERT INTO employee (name, department, email, annual_leave_total, annual_leave_used)
VALUES ('李四', '人力资源部', 'lisi@example.com', 12, 2);

INSERT INTO leave_record (emp_id, start_date, end_date, status)
VALUES (1, '2026-06-03', '2026-06-05', 'PENDING');
