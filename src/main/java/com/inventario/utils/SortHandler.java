package com.inventario.utils;

/**
 * Interfaz funcional para manejar la lógica de ordenación personalizada en la aplicación de inventario.
 * <p>
 * Esta interfaz se utiliza para definir un metodo que se ejecutará cuando el usuario seleccione una opción de ordenación
 * en la interfaz gráfica, permitiendo ordenar los datos de manera ascendente o descendente según corresponda.
 * </p>
 *
 * @author Luis Gil
 */
@FunctionalInterface
public interface SortHandler {
        /**
        * Metodo funcional para manejar la lógica de ordenación personalizada.
        *
        * @param ascending  true para orden ascendente, false para descendente
        */
        void sort(boolean ascending);
}
