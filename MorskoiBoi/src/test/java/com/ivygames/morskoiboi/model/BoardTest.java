package com.ivygames.morskoiboi.model;

import com.ivygames.morskoiboi.Dependencies;
import com.ivygames.morskoiboi.Placement;
import com.ivygames.morskoiboi.Rules;
import com.ivygames.morskoiboi.model.Ship.Orientation;
import com.ivygames.morskoiboi.variant.RussianRules;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BoardTest {

	private static final String EMPTY_BOARD = "{\"ships\":[],\"cells\":\"                                                                                                    \"}";
	private static final String BOARD_WITH_SHIP = "{\"ships\":[{\"size\":1,\"is_horizontal\":true,\"x\":5,\"y\":5,\"health\":1}],\"cells\":\"                                            000       000       000                                 \"}";

	private Board mBoard;
	private Placement mPlacement;

	@Before
	public void setUp() throws Exception {
		mBoard = new Board();
		Random random = mock(Random.class);
		when(random.nextInt(anyInt())).thenReturn(0);
		Rules rules = new RussianRules(new Random());
		Dependencies.inject(rules);
		Dependencies.inject(new Placement(random, rules));
		mPlacement = Dependencies.getPlacement();
	}

	@Test
	public void testEquals() {
		Board board1 = new Board();
		Board board2 = new Board();
		assertEquals(board1, board2);

		Ship ship = new Ship(3);
		mPlacement.putShipAt(board2, ship, 5, 5);
		assertFalse(board1.equals(board2));

		mPlacement.putShipAt(board1, ship, 5, 5);
		assertEquals(board1, board2);
	}

	@Test
	public void testWidth() {
		assertEquals(10, mBoard.horizontalDimension());
	}

	@Test
	public void testHeight() {
		assertEquals(10, mBoard.verticalDimension());
	}

	@Test
	public void testGetCellsAround() {
		mBoard.getCell(5, 4).setReserved();
		mBoard.getCell(5, 6).setReserved();
		mBoard.getCell(4, 5).setReserved();
		mBoard.getCell(6, 5).setReserved();
		Collection<Cell> cells = getCellsAround(mBoard, 5, 5);
		assertEquals(4, cells.size());
		for (Cell cell : cells) {
			assertTrue(cell.isReserved());
		}
	}

	@Test
	public void testGetHitsAround() {
		Collection<Vector2> hits = getHitsAround(mBoard, 5, 5);
		assertEquals(0, hits.size());

		mBoard.getCell(5, 6).setHit();
		hits = getHitsAround(mBoard, 5, 5);
		assertEquals(1, hits.size());
		Vector2 hit = hits.iterator().next();
		assertEquals(5, hit.getX());
		assertEquals(6, hit.getY());
	}

	private void putShipAt(Ship ship, int x, int y) {
		putShipAt(mBoard, ship, x, y);
	}

    private void putShipAt(Board board, Ship ship, int x, int y) {
        mPlacement.putShipAt(board, ship, x, y);
    }

	@Test
	public void testEmptyBoard() {
		assertEquals(100, mBoard.getEmptyCells().size());
	}

	@Test
	public void testAllShipsAreDestroyed() {
		assertFalse(!Board.allAvailableShipsAreDestroyed(mBoard));

		Ship ship = new Ship(2);
		putShipAt(ship, 5, 5);
		assertFalse(Board.allAvailableShipsAreDestroyed(mBoard));

		ship.shoot();
		assertFalse(Board.allAvailableShipsAreDestroyed(mBoard));

		ship.shoot();
		assertTrue(Board.allAvailableShipsAreDestroyed(mBoard));
	}

	private static void assertSingleShip(Board board, Ship ship) {
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				Collection<Ship> ships = board.getShipsAt(i, j);
				if (ship.isInShip(i, j)) {
					assertEquals(board + "\n" + i + "," + j, 1, ships.size());
				} else {
					assertEquals(board + "\n" + i + "," + j, 0, ships.size());
				}
			}
		}
	}

	@Test
	public void testGetShipsAt() {
		Collection<Ship> ships = mBoard.getShipsAt(Vector2.get(5, 5));
		assertEquals(0, ships.size());

		Ship ship = new Ship(3);
		putShipAt(ship, 5, 5);
		assertSingleShip(mBoard, ship);

		mBoard.getCell(5, 5).setHit();
		assertSingleShip(mBoard, ship);
		
//		mBoard.getCell(6, 5).setSunk();
//		assertSigleShip(mBoard, ship);
	}

	@Test
	public void testPutHorizontalShipSucceeded() {
		Ship ship = new Ship(2, Orientation.HORIZONTAL);
		putShipAt(ship, 8, 5);
		assertShipIsCorrectlyAllignedAt(mBoard, ship, 8, 5);
	}

	@Test
	public void testPutHorizontalShipFailed() {
		Ship ship = new Ship(2, Orientation.HORIZONTAL);
		try {
			putShipAt(ship, 9, 5);
			fail();
		} catch (IllegalArgumentException iae) {
		}
		assertEquals(0, ship.getX());
		assertEquals(0, ship.getY());
		assertBoardIsEmpty(mBoard);
	}

	@Test
	public void testPutVerticalShipSucceeded() {
		Ship ship = new Ship(3, Orientation.VERTICAL);
		putShipAt(ship, 3, 7);
		assertShipIsCorrectlyAllignedAt(mBoard, ship, 3, 7);
	}

	@Test
	public void testPutVerticalShipFailed() {
		Ship ship = new Ship(3, Orientation.VERTICAL);
		try {
			putShipAt(ship, 3, 8);
		} catch (IllegalArgumentException iae) {
		}
		assertEquals(0, ship.getX());
		assertEquals(0, ship.getY());
		assertBoardIsEmpty(mBoard);
	}

	// public void testPutShip() {
	// Ship ship = new Ship(3, Orientation.VERTICAL);
	// mBoard.putShip(ship);
	// assertPutShipSucceeded(mBoard, ship, ship.getX(), ship.getY());
	// }

	@Test
	public void testPutSunkShipAt() {
		Ship ship = new Ship(2, Orientation.VERTICAL);
		ship.shoot();
		ship.shoot();
		putShipAt(ship, 3, 3);
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				if (Ship.isInProximity(ship, i, j)) {
					Cell cell = mBoard.getCell(i, j);
					if (ship.isInShip(i, j)) {
						assertTrue(cell.toString() + " " + i + "," + j + "\n" + mBoard.toString(), cell.isHit());
					} else {
						assertTrue(cell.toString() + " " + i + "," + j + "\n" + mBoard.toString(), cell.isMiss());
					}
				}
			}
		}
	}

	private static void assertReservedOnlyInProximity(Board board, Ship ship, int i, int j) {
		Cell cell = board.getCell(i, j);
		if (Ship.isInProximity(ship, i, j)) {
			assertTrue(cell.toString(), cell.isReserved());
		} else {
			assertTrue(cell.toString(), cell.isEmpty());
		}
	}

	private static void assertShipIsCorrectlyAllignedAt(Board board, Ship ship, int x, int y) {
		assertEquals(x, ship.getX());
		assertEquals(y, ship.getY());
		assertReservedOnlyInProximityOnCleanBoard(board, ship);
	}

	private static void assertReservedOnlyInProximityOnCleanBoard(Board board, Ship ship) {
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				assertReservedOnlyInProximity(board, ship, i, j);
			}
		}
	}

	private static void assertBoardIsEmpty(Board board) {
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				assertTrue(board.getCell(i, j).isEmpty());
			}
		}
	}

	@Test
	public void testProximity() {
		Ship ship = new Ship(2);
		putShipAt(ship, 5, 5);
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				Cell cell = mBoard.getCell(i, j);
				if (Ship.isInProximity(ship, i, j)) {
					if (ship.isInShip(i, j)) {
						assertEquals(i + "," + j, 8, cell.getProximity());
					} else {
						assertEquals(i + "," + j, 1, cell.getProximity());
					}
				} else {
					assertEquals(i + "," + j, 0, cell.getProximity());
				}
			}
		}
	}
	
	@Test
	public void testGetCell() {
		Cell cell = mBoard.getCell(0, 0);
		assertNotNull(cell);
		cell.setReserved();
		cell = mBoard.getCell(0, 0);
		assertTrue(cell.isReserved());
		cell.setMiss();
		cell = mBoard.getCell(0, 0);
		assertTrue(cell.isMiss());
	}

	@Test
	public void testEmptyCells() {
		assertEquals(100, mBoard.getEmptyCells().size());

		putShipAt(new Ship(1), 5, 5);
		assertEquals(91, mBoard.getEmptyCells().size());

		putShipAt(new Ship(2, Orientation.VERTICAL), 9, 8);
		assertEquals(85, mBoard.getEmptyCells().size());
	}

	@Test
	public void testGetShips() {
		int totalShips = mBoard.getShips().size();
		assertEquals(0, totalShips);

		putShipAt(new Ship(1), 5, 5);
		totalShips = mBoard.getShips().size();
		assertEquals(1, totalShips);

		putShipAt(new Ship(2), 8, 9);
		totalShips = mBoard.getShips().size();
		assertEquals(2, totalShips);
	}

	@Test
	public void testCanPutShipAt() {
		Ship ship = new Ship(1);
		for (int i = -1; i < 11; i++) {
			for (int j = -1; j < 11; j++) {
				if (i >= 0 && i < 10 && j >= 0 && j < 10) {
					assertTrue(mBoard.shipFitsTheBoard(ship, i, j));
				} else {
					assertFalse(mBoard.shipFitsTheBoard(ship, i, j));
				}
			}
		}
	}

	@Test
	public void testContainsCell() {

		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				assertTrue(Board.contains(i, j));
			}
		}

		assertFalse(Board.contains(-1, 0));
		assertFalse(Board.contains(10, 0));
		assertFalse(Board.contains(0, 10));
		assertFalse(Board.contains(0, -1));
	}

	private static void assertBoardsEqual(Board board1, Board board2) {
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				assertEquals(board1.getCell(i, j), board2.getCell(i, j));
			}
		}

		assertEquals(board1.getShips(), board2.getShips());
	}
	
	@Test
	public void successful_Recreation_After_Serializing_To_Json_Empty_Board() {
		String json = BoardSerialization.toJson(mBoard).toString();
		Board board = BoardSerialization.fromJson(json);
		assertBoardsEqual(mBoard, board);
	}

	@Test
	public void ParsingEmptyBoard() {
		Board board = BoardSerialization.fromJson(EMPTY_BOARD);
		assertBoardIsEmpty(board);
	}

	@Test
	public void ParsingBoardWithShip() {
		Board board = BoardSerialization.fromJson(BOARD_WITH_SHIP);
		putShipAt(new Ship(1), 5, 5);
		assertBoardsEqual(mBoard, board);
	}

    @Test
    public void CopyOfABoard__IsIdenticalToOriginal() {
        Board board = BoardSerialization.fromJson(BOARD_WITH_SHIP);

        Board copy = BoardSerialization.copy(board);

        assertThat(copy, equalTo(board));
    }

	@Test
	public void successful_Recreation_After_Serializing_To_String_Board_With_Ship() {
		putShipAt(new Ship(1), 5, 5);
		String json = BoardSerialization.toJson(mBoard).toString();
		Board board = BoardSerialization.fromJson(json);
		assertBoardsEqual(mBoard, board);
	}
	
	@Test
	public void should_throw_IllegalArgumentException_On_Illegal_String() {
		String json = "just some garbage";
		try {
			BoardSerialization.fromJson(json);
			fail("should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException iae) {
		}
	}

	private static void addIfContains(Board board, Collection<Cell> cells, int x, int y) {
		if (Board.contains(x, y)) {
			cells.add(board.getCell(x, y));
		}
	}

	public static Collection<Cell> getCellsAround(Board board, int x, int y) {
		Collection<Cell> cells = new ArrayList<>();
		addIfContains(board, cells, x + 1, y);
		addIfContains(board, cells, x - 1, y);
		addIfContains(board, cells, x, y + 1);
		addIfContains(board, cells, x, y - 1);

		return cells;
	}

	public void addIfHit(Board board, Collection<Vector2> hits, int x, int y) {
		if (Board.contains(x, y) && board.getCell(x, y).isHit()) {
			hits.add(Vector2.get(x, y));
		}
	}

	public Collection<Vector2> getHitsAround(Board board, int x, int y) {
		Collection<Vector2> hits = new ArrayList<>();
		addIfHit(board, hits, x + 1, y);
		addIfHit(board, hits, x - 1, y);
		addIfHit(board, hits, x, y + 1);
		addIfHit(board, hits, x, y - 1);

		return hits;
	}
}
