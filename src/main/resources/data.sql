-- Digital Wallet - Initial Data
-- This file will be automatically executed by Spring Boot on startup

-- Insert initial customers with encrypted passwords
-- Password for both users is 'password' (encoded with BCrypt)
INSERT INTO customers (name, surname, tckn, username, password, role) VALUES
('Ayse', 'Yilmaz', '11111111111', 'employee', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'EMPLOYEE');

INSERT INTO customers (name, surname, tckn, username, password, role) VALUES
('Ali', 'Kaya', '22222222222', 'customer', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'CUSTOMER');

-- Insert initial wallets for the customer
INSERT INTO wallets (customer_id, wallet_name, currency, active_for_shopping, active_for_withdraw, balance, usable_balance, created_at) VALUES
(2, 'Main TRY', 'TRY', true, true, 0.00, 0.00, CURRENT_TIMESTAMP);

INSERT INTO wallets (customer_id, wallet_name, currency, active_for_shopping, active_for_withdraw, balance, usable_balance, created_at) VALUES
(2, 'USD Wallet', 'USD', true, true, 0.00, 0.00, CURRENT_TIMESTAMP);
