package com.ivygames.morskoiboi.model;

import android.support.annotation.NonNull;

import com.ivygames.morskoiboi.Dependencies;
import com.ivygames.morskoiboi.Placement;
import com.ivygames.morskoiboi.variant.RussianRules;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BoardSerializationTest {
    private static final String EMPTY_BOARD = "{\"ships\":[],\"cells\":\"                                                                                                    \"}";
    private static final String BOARD_WITH_SHIP = "{\"ships\":[{\"size\":1,\"is_horizontal\":true,\"x\":5,\"y\":5,\"health\":1}],\"cells\":\"                                            000       000       000                                 \"}";

    private Board mBoard = new Board();
    private Placement mPlacement;

    @Before
    public void setup() {
        Random random = mock(Random.class);
        when(random.nextInt(anyInt())).thenReturn(0);
        mPlacement = new Placement(random, new RussianRules());
        Dependencies.inject(mPlacement);
    }

    @Test
    public void successful_Recreation_After_Serializing_To_Json_Empty_Board() {
        String json = BoardSerialization.toJson(mBoard).toString();
        Board board = BoardSerialization.fromJson(json);
        assertEquals(mBoard, board);
    }

    @Test
    public void ParsingEmptyBoard() {
        Board board = BoardSerialization.fromJson(EMPTY_BOARD);
        assertBoardIsEmpty(board);
    }

    @Test
    public void ParsingBoardWithShip() {
        Board board2 = new Board();
        putShipAt(board2, new Ship(1), 5, 5);

        Board board1 = BoardSerialization.fromJson(BOARD_WITH_SHIP);

        assertEquals(board1, board2);
    }

    @Test
    public void successful_Recreation_After_Serializing_To_String_Board_With_Ship() {
        putShipAt(new Ship(1), 5, 5);
        String json = BoardSerialization.toJson(mBoard).toString();
        Board board = BoardSerialization.fromJson(json);
        assertEquals(mBoard, board);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_IllegalArgumentException_On_Illegal_String() {
        BoardSerialization.fromJson("just some garbage");
    }

    @Test
    public void CopyOfABoard__IsIdenticalToOriginal() {
        Board board = BoardSerialization.fromJson(BOARD_WITH_SHIP);

        Board copy = copy(board);

        assertThat(copy, equalTo(board));
    }

    @NonNull
    private static Board copy(@NonNull Board board) {
        return BoardSerialization.fromJson(BoardSerialization.toJson(board));
    }

    private static void assertBoardIsEmpty(Board board) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                assertTrue(board.getCell(i, j) == Cell.EMPTY);
            }
        }
    }

    private void putShipAt(Ship ship, int x, int y) {
        putShipAt(mBoard, ship, x, y);
    }

    private void putShipAt(Board board, Ship ship, int x, int y) {
        mPlacement.putShipAt(board, ship, x, y);
    }
}