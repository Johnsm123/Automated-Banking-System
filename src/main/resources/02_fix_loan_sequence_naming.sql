-- Optional: Standardize the loan sequence naming to match other entities
-- This script renames 'loan_sequence' to 'LOAN_SEQ' for consistency

-- Drop the existing sequence
DROP SEQUENCE loan_sequence;

-- Create new sequence with standardized name
CREATE SEQUENCE LOAN_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

-- Note: If you run this script, you'll also need to update the Loan.java entity
-- to change sequenceName from "loan_sequence" to "LOAN_SEQ"
