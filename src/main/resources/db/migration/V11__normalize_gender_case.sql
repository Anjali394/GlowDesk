UPDATE customer SET gender = UPPER(gender) WHERE gender IS NOT NULL;
UPDATE stylist  SET gender = UPPER(gender) WHERE gender IS NOT NULL;
