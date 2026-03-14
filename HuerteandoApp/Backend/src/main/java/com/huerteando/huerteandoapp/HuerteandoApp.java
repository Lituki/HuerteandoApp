package com.huerteando.huerteandoapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de arranque de la aplicación.
 */
@SpringBootApplication
public class HuerteandoApp implements CommandLineRunner {

	

	public static void main(String[] args) {
		SpringApplication.run(HuerteandoApp.class, args);
	}

	/**
	 * Método ejecutado automáticamente al arrancar la aplicación.
	 */
	@Override
	public void run(String... args) {


	}
}
