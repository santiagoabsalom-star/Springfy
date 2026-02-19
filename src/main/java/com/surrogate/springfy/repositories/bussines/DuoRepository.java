package com.surrogate.springfy.repositories.bussines;

import com.surrogate.springfy.models.bussines.streaming.Duo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DuoRepository extends JpaRepository<Duo, Long> {


    @Query("""
SELECT
  CASE\s
    WHEN d.id_usuario1.nombre = :username THEN d.id_usuario2.nombre
    ELSE d.id_usuario1.nombre
  END
FROM Duo d
WHERE d.id_usuario1.nombre = :username OR d.id_usuario2.nombre = :username
""")
    String getOtherUsername(@Param("username") String username);


}
