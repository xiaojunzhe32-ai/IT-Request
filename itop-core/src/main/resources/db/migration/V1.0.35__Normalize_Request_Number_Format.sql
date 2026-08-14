UPDATE user_request
SET request_no = CONCAT('REQ-', 10000 + id)
WHERE request_no IS NULL
   OR request_no = ''
   OR request_no LIKE 'REQ-%-%';
