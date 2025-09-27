-- Create all required Oracle sequences for the banking application
-- This script resolves the "sequence doesn't exist" error

-- Sequence for Customer entity
CREATE SEQUENCE CUSTOMER_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

-- Sequence for Account entity
CREATE SEQUENCE ACCOUNT_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

-- Sequence for Transaction entity
CREATE SEQUENCE TXN_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

-- Sequence for Loan entity (note: uses different naming convention)
CREATE SEQUENCE loan_sequence
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

-- Sequence for LoanInstallment entity
CREATE SEQUENCE INSTALLMENT_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

-- Sequence for StudentLoan entity
CREATE SEQUENCE STUDENT_LOAN_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

-- Sequence for VehicleLoan entity
CREATE SEQUENCE VEHICLE_LOAN_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

-- Verify sequences were created
SELECT sequence_name, min_value, max_value, increment_by, last_number
FROM user_sequences
WHERE sequence_name IN (
    'CUSTOMER_SEQ',
    'ACCOUNT_SEQ',
    'TXN_SEQ',
    'LOAN_SEQUENCE',
    'INSTALLMENT_SEQ',
    'STUDENT_LOAN_SEQ',
    'VEHICLE_LOAN_SEQ'
);
