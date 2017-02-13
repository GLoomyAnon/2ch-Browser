package com.vortexwolf.chan2.interfaces;

import com.vortexwolf.chan2.models.domain.BoardModel;

import java.util.List;

public interface IBoardsListCallback {
    void listUpdated(List<BoardModel> newBoards);
}
