
package com.huerteando.huerteandoapp.repository;

import com.huerteando.huerteandoapp.model.CatalogoEei;
import org.springframework.data.jpa.repository.JpaRepository;

/*
Acceso a tabla catalogo_eei.
Aquí solo se pregunta a la base de datos.
*/
public interface CatalogoEeiRepository extends JpaRepository<CatalogoEei, Long> {



}
