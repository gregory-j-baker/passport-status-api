INSERT INTO status_code (id, code, cdo_code, description, is_active, created_by, created_date, last_modified_by, last_modified_date) VALUES
  ('8b4ecef5-9617-4dbc-91ca-ff24b2367b66', 'UNKNOWN', '-1', 'The passport application status is unknown.', true, 'flyway-community-edition', CURRENT_TIMESTAMP, 'flyway-community-edition', CURRENT_TIMESTAMP),
  ('57fe687e-50a6-411f-af63-2a659622127d', 'APPROVED', '1', 'The passport application was approved.', true, 'flyway-community-edition', CURRENT_TIMESTAMP, 'flyway-community-edition', CURRENT_TIMESTAMP),
  ('c4c3d083-39f6-4cae-bb18-1e65dd2f60a3', 'IN_EXAMINATION', '2', 'The passport application is being processed.', true, 'flyway-community-edition', CURRENT_TIMESTAMP, 'flyway-community-edition', CURRENT_TIMESTAMP),
  ('20edcb59-b217-4ed8-8378-f78184f634f2', 'REJECTED', '3', 'The passport application has been rejected.', true, 'flyway-community-edition', CURRENT_TIMESTAMP, 'flyway-community-edition', CURRENT_TIMESTAMP);