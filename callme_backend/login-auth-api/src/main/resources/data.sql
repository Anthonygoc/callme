INSERT INTO tb_roles (role_id, name) VALUES (1, 'ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO tb_roles (role_id, name) VALUES (2, 'RT') ON CONFLICT DO NOTHING;
