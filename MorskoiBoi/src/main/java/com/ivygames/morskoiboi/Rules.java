package com.ivygames.morskoiboi;

import android.support.annotation.NonNull;

import com.ivygames.morskoiboi.model.Board;
import com.ivygames.morskoiboi.model.Cell;
import com.ivygames.morskoiboi.model.Game;
import com.ivygames.morskoiboi.model.ScoreStatistics;
import com.ivygames.morskoiboi.model.Ship;

import java.util.Collection;

public interface Rules {
    boolean isBoardSet(@NonNull Board board);

    boolean isCellConflicting(@NonNull Board board, int i, int j);

    /**
     * @return true if board has full fleet and all the ships are destroyed
     */
    boolean isItDefeatedBoard(@NonNull Board board);

    @NonNull
    int[] getAllShipsSizes();

    int calcTotalScores(@NonNull Collection<Ship> ships, @NonNull Game.Type type,
                        @NonNull ScoreStatistics statistics, boolean surrendered);

    boolean canHaveAdjacentShip();

    int calcSurrenderPenalty(@NonNull Collection<Ship> ships);
}
