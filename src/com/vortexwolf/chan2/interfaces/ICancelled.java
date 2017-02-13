package com.vortexwolf.chan2.interfaces;

public interface ICancelled {
    /**
     * Возвращает true, если был вызван метод cancel. Нужно проверять несколько
     * раз в методе doBackground
     */
    boolean isCancelled();
}
