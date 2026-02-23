package com.surrogate.springfy.repositories.bussines;

import com.surrogate.springfy.models.bussines.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("Select u from Usuario u where u.nombre= :username")
    Usuario findUsuarioByNombre(String username);

    boolean existsByNombre(String nombre);
    @Query("""
SELECT u.nombre
FROM Usuario u
WHERE u.nombre <> :username
AND NOT EXISTS (
    SELECT 1
    FROM Duo d
    WHERE d.id_usuario1 = u
       OR d.id_usuario2 = u
)
""")
    List<String> getAllNombres(String username);
}