package com.inventario.models;

import java.util.Objects;

/**
 * Representa un correo electrónico válido para recibir un mail donde
 * se avisa de alguna averia en equipos de aire acondicionado (cassettes o condensadoras).
 * <p>
 * Los valores permitidos están definidos en la hoja PARAM_CORREOS_ELECTRONICOS del archivo Excel
 * Inventario AA V2.xlsx.
 * </p>
 *
 * @author Luis Gil
 */
public class Correo_Electronico {
    private String correoElectronico;

    /**
     * Constructor por defecto (requerido por JavaFX y frameworks de serialización).
     */
    public Correo_Electronico() {
    }

    /**
     * Constructor que inicializa el correo electrónico con un valor válido.
     *
     * @param correoElectronico valor del correo electrónico (debe ser uno de los definidos en PARAM_CORREO_ELECTRONICO)
     */
    public Correo_Electronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    /**
     * Obtiene el valor del correo electrónico.
     *
     * @return correo electrónico actual (ej. jose@dominio.com)
     */
    public String getCorreoElectronico() {
        return correoElectronico;
    }

    /**
     * Establece el correo electrónico, validando que sea uno de los valores permitidos.
     *
     * @param correoElectronico nuevo valor del correo electrónico
     */
    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    /** Equals y hashCode basados en el parametro correo_electronico. */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Correo_Electronico that = (Correo_Electronico) o;
        return Objects.equals(correoElectronico, that.correoElectronico);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(correoElectronico);
    }

    @Override
    public String toString() {
        return "Correo_Electronico{" +
                "correoElectronico='" + correoElectronico + '\'' +
                '}';
    }
}
