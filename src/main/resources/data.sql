INSERT INTO employee (name, department, email, annual_leave_total, annual_leave_used)
VALUES ('张三', '研发中心', 'zhangsan@example.com', 15, 5)
ON DUPLICATE KEY UPDATE department = VALUES(department), email = VALUES(email);

INSERT INTO employee (name, department, email, annual_leave_total, annual_leave_used)
VALUES ('李四', '人力资源部', 'lisi@example.com', 12, 2)
ON DUPLICATE KEY UPDATE department = VALUES(department), email = VALUES(email);

INSERT INTO leave_record (emp_id, start_date, end_date, status)
SELECT id, '2026-06-03', '2026-06-05', 'PENDING'
FROM employee
WHERE name = '张三'
  AND NOT EXISTS (
      SELECT 1
      FROM leave_record
      WHERE leave_record.emp_id = employee.id
        AND leave_record.start_date = '2026-06-03'
        AND leave_record.end_date = '2026-06-05'
  );
