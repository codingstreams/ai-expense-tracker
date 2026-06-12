INSERT INTO payment_mode (name)
VALUES
    ('Credit Card'),
    ('Debit Card'),
    ('Net Banking'),
    ('UPI'),
    ('Cash'),
    ('Digital Wallet');

ALTER TABLE payment_mode
ADD type VARCHAR(255);

UPDATE payment_mode
SET type = CASE
    WHEN LOWER(name) = 'credit card' THEN 'LIABILITY'
    ELSE 'ASSET'
END;

 INSERT INTO bank (name) VALUES
 ('State Bank of India'),
 ('HDFC Bank'),
 ('ICICI Bank'),
 ('Axis Bank'),
 ('Punjab National Bank'),
 ('Bank of Baroda'),
 ('Canara Bank'),
 ('Central Bank of India'),
 ('Union Bank of India'),
 ('IndusInd Bank'),
 ('Kotak Mahindra Bank');

 INSERT INTO system_category (name) VALUES
 ('Groceries'),
 ('Dining Out'),
 ('Rent/EMI'),
 ('Utilities (Electricity/Water)'),
 ('Fuel/Transportation'),
 ('Health & Medical'),
 ('Insurance'),
 ('Shopping (Clothing/Electronics)'),
 ('Entertainment & OTT'),
 ('Education'),
 ('Investments (SIP/Stocks)'),
 ('Gifts & Donations'),
 ('Travel & Vacation'),
 ('Maintenance & Repairs'),
 ('Miscellaneous');


