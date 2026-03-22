package umg.biometrico.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Punto de entrada principal de la aplicacion web UMG Biometrico.
 * Sistema de Control Biometrico — Universidad Mariano Galvez
 * Sede La Florida, Zona 19
 */
@SpringBootApplication
@EnableAsync
public class AplicacionWebUMG {

    public static void main(String[] args) {
        SpringApplication.run(AplicacionWebUMG.class, args);
    }
}
