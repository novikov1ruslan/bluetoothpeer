package com.ivygames.morskoiboi.variant;

import android.support.annotation.NonNull;

import com.ivygames.morskoiboi.Rules;
import com.ivygames.morskoiboi.model.Board;
import com.ivygames.morskoiboi.model.Game;
import com.ivygames.morskoiboi.model.Ship;
import com.ivygames.morskoiboi.screen.boardsetup.BoardSetupUtils;

import java.util.Collection;

public abstract class AbstractRules implements Rules {

    @Override
    public boolean isBoardSet(@NonNull Board board) {
        return allShipsAreOnBoard(board) && !isThereConflictingCell(board);
    }

    private boolean allShipsAreOnBoard(@NonNull Board board) {
        return board.getShips().size() == getAllShipsSizes().length;
    }

    private boolean isThereConflictingCell(@NonNull Board board) {
        for (int i = 0; i < board.horizontalDimension(); i++) {
            for (int j = 0; j < board.verticalDimension(); j++) {
                if (BoardSetupUtils.isCellConflicting(board, i, j, allowAdjacentShip())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @return true if board has 10 ships and all of them are destroyed
     */
    @Override
    public boolean isItDefeatedBoard(@NonNull Board board) {
        return allShipsAreOnBoard(board) && Board.allAvailableShipsAreDestroyed(board);
    }

    @Override
    public int calcSurrenderPenalty(@NonNull Collection<Ship> ships) {
        int decksLost = getTotalHealth() - getRemainedHealth(ships);
        return decksLost * Game.SURRENDER_PENALTY_PER_DECK + Game.MIN_SURRENDER_PENALTY;
    }

    private int getTotalHealth() {
        int[] ships = getAllShipsSizes();
        int totalHealth = 0;
        for (int ship : ships) {
            totalHealth += ship;
        }
        return totalHealth;
    }

    private static int getRemainedHealth(@NonNull Collection<Ship> ships) {
        int health = 0;
        for (Ship ship : ships) {
            health += ship.getHealth();
        }
        return health;
    }
}
