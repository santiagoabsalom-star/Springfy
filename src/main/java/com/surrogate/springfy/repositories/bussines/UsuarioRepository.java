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
    @Query("Select u.nombre from Usuario u where u.nombre!= :username")
    List<String> getAllNombres(String username);
}