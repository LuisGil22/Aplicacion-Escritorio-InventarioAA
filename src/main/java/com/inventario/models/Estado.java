package com.inventario.models;

import java.util.Objects;

/**
 * Representa un estado válido para equipos de aire acondicionado (cassettes o condensadoras).
 * <p>
 * Los valores permitidos están definidos en la hoja PARAM_ESTADO del archivo Excel
 * Inventario AA V2.xlsx.
 * </p>
 *
 * @author Luis Gil
 */
public class Estado {
    private String estado;

    /**
     * Constructor por defecto (requerido por JavaFX y frameworks de serialización).
     */
    public Estado() {}

    /**
     * Constructor que inicializa el estado con un valor válido.
     *
     * @param estado valor del estado (debe ser uno de los definidos en PARAM_ESTADO)
     */
    public Estado (String estado){
        this.estado = estado;
    }

    /**
     * Obtiene el valor del estado.
     *
     * @return estado actual (ej. "ACTIVA", "BAJA", "AVERIADO")
     */
    public String getEstado(){
        return estado;
    }

    /**
     * Establece el estado, validando que sea uno de los valores permitidos.
     *
     * @param estado nuevo valor del estado
     */
    public void setEstado(String estado){
        this.estado = estado;
    }

    /** Equals y hashCode basados en el parametro estado. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Estado estado1 = (Estado) o;
        return Objects.equals(estado, estado1.estado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(estado);
    }

    @Override
    public String toString() {
        return "Estado{" +
                "estado='" + estado + '\'' +
                '}';
    }
}
