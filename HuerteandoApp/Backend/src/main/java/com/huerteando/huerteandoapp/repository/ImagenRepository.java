package com.huerteando.huerteandoapp.repository;

import com.huerteando.huerteandoapp.model.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImagenRepository extends JpaRepository<Imagen, Long> {

    @Query("""
  select i
  from Imagen i
  where i.observacion.id = :id
  order by i.creadoEn asc
""")
    List<Imagen> findByObservacionIdOrderByFecha(@Param("id") Long id);

    // Cuenta cuántas imágenes tiene una observación.
    long countByObservacion_Id(Long idObservacion);

    // Borra todas las imágenes de una observación.
    void deleteByObservacion_Id(Long idObservacion);



}
