package com.ivygames.morskoiboi.screen.gameplay;

import android.support.annotation.NonNull;

import com.ivygames.battleship.ship.Ship;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

class GameplayUtils {
    @NonNull
    static Collection<Ship> getWorkingShips(@NonNull Collection<Ship> ships) {
        LinkedList<Ship> workingShips = new LinkedList<>();
        for (Ship ship : ships) {
            if (!ship.isDead()) {
                workingShips.add(ship);
            }
        }
        return workingShips;
    }

    static void removeShipFromFleet(@NonNull Collection<Ship> fleet, @NonNull Ship ship) {
        Iterator<Ship> iterator = fleet.iterator();
        while (iterator.hasNext()) {
            Ship next = iterator.next();
            if (ship.size == next.size) {
                iterator.remove();
                break;
            }
        }
    }
}
