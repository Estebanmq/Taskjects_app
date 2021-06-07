package com.app.taskjects.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class ValidadorTest {

    @Test
    public void validacionesCif() {
        assertTrue(Validador.validarCif("B00000001"));

        assertFalse(Validador.validarCif(""));
        assertFalse(Validador.validarCif("B"));
        assertFalse(Validador.validarCif("1"));
        assertFalse(Validador.validarCif("B00001"));
    }

    @Test
    public void validacionesNif() {
        assertTrue(Validador.validarNif("12345678Z"));

        assertFalse(Validador.validarNif(""));
        assertFalse(Validador.validarNif("1"));
        assertFalse(Validador.validarCif("Z"));
        assertFalse(Validador.validarNif("12345Z"));
    }

    @Test
    public void validacionesEmail() {
        assertTrue(Validador.validarEmail("direccion@dominio.com"));

        assertFalse(Validador.validarEmail(""));
        assertFalse(Validador.validarEmail("direccion"));
        assertFalse(Validador.validarEmail("@dominio.com"));
        assertFalse(Validador.validarEmail("direccion@dominio"));
        assertFalse(Validador.validarEmail("direccion@dominio."));
    }

    @Test
    public void validacionesPassword() {
        assertTrue(Validador.validarPassword("passwordCorrecta"));

        assertFalse(Validador.validarPassword(""));
        assertFalse(Validador.validarPassword("pass"));
        assertFalse(Validador.validarPassword("passwordIncorrectaPorExcesoDeLongitud"));
        assertFalse(Validador.validarPassword("password Incorrecta"));
        assertFalse(Validador.validarPassword("password/Incorrecta"));
    }
}