-- Promueve el administrador inicial a SUPERADMIN
-- Este usuario tiene acceso completo para gestionar otros administradores.
UPDATE usuarios
SET rol = 'SUPERADMIN'
WHERE correo = 'admin@unab.edu.co';
