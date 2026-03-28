package com.inventario.models;

import java.util.Objects;

/**
 * Representa el parámetro de configuración para el intervalo entre revisiones,
 * definido en la hoja PARAM_DIAS_REVISION del archivo Excel.
 * <p>
 * Contiene un único campo: número de días entre revisiones (por defecto 365).
 * </p>
 *
 * @author Luis Gil
 */
public class DiaRevision {
    private int dias;

    /**
     * Constructor por defecto (requerido por frameworks de serialización).
     */
    public DiaRevision() {
        this.dias = 365; // valor por defecto
    }

    /**
     * Constructor que inicializa el número de días entre revisiones.
     *
     * @param dias número de días (ej. 365 para revisiones anuales)
     */
    public DiaRevision(int dias) {
        this.dias = dias;
    }

    /**
     * Obtiene el número de días entre revisiones.
     *
     * @return número de días (ej. 365)
     */
    public int getDias() {
        return dias;
    }

    /**
     * Establece el número de días entre revisiones.
     *
     * @param dias nuevo valor (debe ser > 0)
     */
    public void setDias(int dias) {
        if (dias <= 0) {
            throw new IllegalArgumentException("El número de días debe ser mayor que 0.");
        }
        this.dias = dias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiaRevision that = (DiaRevision) o;
        return dias == that.dias;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dias);
    }

    @Override
    public String toString() {
        return "DiaRevision{" +
                "dias=" + dias +
                '}';
    }
}
