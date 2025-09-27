-- Insert loan configurations (amounts in INR)
INSERT INTO loan_config (loan_type, min_principal, max_principal, interest_rate, min_tenure_months, max_tenure_months, description, active) VALUES
('GENERAL', 50000.00, 2000000.00, 8.5, 12, 60, 'General purpose personal loan with flexible terms', true),
('STUDENT', 100000.00, 5000000.00, 6.5, 12, 120, 'Education loan for students with lower interest rates and longer tenure', true),
('VEHICLE', 200000.00, 10000000.00, 7.5, 24, 84, 'Vehicle loan for cars, bikes, and other vehicles with competitive rates', true);