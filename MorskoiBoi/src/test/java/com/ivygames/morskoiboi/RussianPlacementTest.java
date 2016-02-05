package com.ivygames.morskoiboi;

import com.ivygames.morskoiboi.ai.PlacementAlgorithm;
import com.ivygames.morskoiboi.model.Board;
import com.ivygames.morskoiboi.model.Ship;
import com.ivygames.morskoiboi.variant.RussianPlacement;
import com.ivygames.morskoiboi.variant.RussianRules;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RussianPlacementTest {

    private PlacementAlgorithm mAlgorithm;
    private int mNumberOfDistinctShips;

    @BeforeClass
    public static void runBeforeClass() {

    }

    @Before
	public void setup() {
        RulesFactory.setRules(new RussianRules());
        mAlgorithm = new RussianPlacement(new Random(1), RulesFactory.getRules());
	}

    @Test
    public void russian_fleet_is_generated_4_3_3_2_2_2_1_1_1_1() {
        Collection<Ship> fleet = mAlgorithm.generateFullFleet();
        assertAllTheShipsAreRussianFleet(fleet);
    }

    @Test
    public void after_generating_full_board_it_has_russian_fleet() {
        Board board = mAlgorithm.generateBoard();
        assertAllTheShipsAreRussianFleet(board.getShips());
    }

    private void assertAllTheShipsAreRussianFleet(Collection<Ship> distinct) {
        mNumberOfDistinctShips = distinct.size();
        assertThat(mNumberOfDistinctShips, is(10));

        assertThereIsNewDistinctShip(distinct, 4);
        assertThereIsNewDistinctShip(distinct, 3);
        assertThereIsNewDistinctShip(distinct, 3);
        assertThereIsNewDistinctShip(distinct, 2);
        assertThereIsNewDistinctShip(distinct, 2);
        assertThereIsNewDistinctShip(distinct, 2);
        assertThereIsNewDistinctShip(distinct, 1);
        assertThereIsNewDistinctShip(distinct, 1);
        assertThereIsNewDistinctShip(distinct, 1);
        assertThereIsNewDistinctShip(distinct, 1);
    }

    private void assertThereIsNewDistinctShip(Collection<Ship> distinct, int shipSize) {
        assertThat(removeShipFromSet(distinct, shipSize), is(true));
        mNumberOfDistinctShips--;
        assertThat(distinct.size(), is(mNumberOfDistinctShips));
    }

    private boolean removeShipFromSet(Collection<Ship> distinct, int size) {
        for (Ship ship :
                distinct) {
            if (ship.getSize() == size) {
                distinct.remove(ship);
                return true;
            }
        }
        return false;
    }

}