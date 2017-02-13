package com.vortexwolf.chan2.interfaces;

import com.vortexwolf.chan2.models.domain.BoardModel;

public interface IBoardsListView extends IListView<BoardModel[]> {

    /**
     * Вызывается в случае ошибки при частичном обновлении
     *
     * @param error
     *            Текст ошибки
     */
    void showUpdateError(String error);

    /**
     * Показывает индикатор загрузки при частичном обновлении
     */
    void showUpdateLoading();

    /**
     * Прячет индикатор загрузки при частичном обновлении
     */
    void hideUpdateLoading();
}
