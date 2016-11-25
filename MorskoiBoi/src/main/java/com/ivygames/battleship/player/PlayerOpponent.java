package com.ivygames.battleship.player;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ivygames.battleship.BoardUtils;
import com.ivygames.battleship.Opponent;
import com.ivygames.battleship.board.Board;
import com.ivygames.battleship.board.Cell;
import com.ivygames.battleship.board.Vector;
import com.ivygames.battleship.ship.LocatedShip;
import com.ivygames.battleship.ship.Ship;
import com.ivygames.battleship.shot.ShotResult;
import com.ivygames.morskoiboi.PlayerCallback;
import com.ivygames.morskoiboi.model.ChatMessage;
import com.ivygames.morskoiboi.player.ChatListener;

import org.commons.logger.Ln;

import java.util.HashSet;
import java.util.Set;

import static com.ivygames.common.analytics.ExceptionHandler.reportException;

public class PlayerOpponent implements Opponent {
    public static volatile Board debug_board;
    private final Handler debug_handler = new Handler(Looper.getMainLooper());
    private final Runnable debug_thread_break_task = new Runnable() {
        @Override
        public void run() {
            Ln.v("---------------------- main thread ----------------------");
        }
    };

    private static final int NOT_READY = -1;

    @NonNull
    private final Set<PlayerCallback> mCallbacks = new HashSet<>();
    @NonNull
    private Board mMyBoard = new Board();
    @NonNull
    private Board mEnemyBoard = new Board();
    private int mMyBid = NOT_READY;
    private int mEnemyBid = NOT_READY;

    @Nullable
    private ChatListener mChatListener;
    private boolean mPlayerReady;
    private int mOpponentVersion;
    protected QueuedCommandOpponent mOpponent;
    private boolean mOpponentReady;

    @NonNull
    private final String mName;
    private boolean mGameStarted;
    private final int mNumberOfShips;

    protected PlayerOpponent(@NonNull String name, int numberOfShips) {
        mName = name;
        mNumberOfShips = numberOfShips;
        Ln.v("new player created: " + name);
    }

    private void reset() {
        mEnemyBoard = new Board();
        mMyBoard = new Board();
        mMyBid = NOT_READY;
        mEnemyBid = NOT_READY;
        mOpponentReady = false;
        mPlayerReady = false;
        mGameStarted = false;
    }

    public void setBoard(@NonNull Board board) {
        Ln.v(mName + ": my board is: " + board);
        mMyBoard = board;
        if (!board.getShips().isEmpty()) {
            debug_board = board;
        }
    }

    public void setChatListener(@NonNull ChatListener listener) {
        mChatListener = listener;
        Ln.v(mName + ": my chat listener is: " + listener);
    }

    public void startBidding(int bid) {
        debug_handler.post(debug_thread_break_task);

        mMyBid = bid;
        mPlayerReady = true;

        if (mOpponentReady && opponentStarts()) {
            Ln.d(mName + ": my bid: " + bid + ", opponent is ready and it is his turn");
            passFirstTurn();
        } else {
            Ln.d(mName + ": opponent " + (mOpponentReady ? "is not ready" : "has higher bid")
                    + " - sending him my bid: " + bid);
            mOpponent.onEnemyBid(bid);
        }

        mOpponent.executePendingCommands();
    }

    // TODO: what this game started mean?
    private void passFirstTurn() {
        if (!mGameStarted) {
            mGameStarted = true;
            mOpponent.go();
        }
    }

    @Override
    public void onEnemyBid(int bid) {
        debug_handler.post(debug_thread_break_task);

        setOpponentBid(bid);
        notifyOpponentReady();

        if (mPlayerReady && opponentStarts()) {
            Ln.d(mName + ": I'm ready , but it's opponent's turn, " + mOpponent + " begins");
            passFirstTurn();

            notifyOpponentTurn();
        }

        mOpponent.executePendingCommands();
    }

    private void setOpponentBid(int bid) {
        mEnemyBid = bid;
        if (mEnemyBid == mMyBid) {
            reportException("stall");
        }
        mOpponentReady = true;
        Ln.d(this + ": opponent is ready, bid = " + bid);
    }

    public void registerCallback(@NonNull PlayerCallback callback) {
        mCallbacks.add(callback);
        Ln.v(mName + ": [" + callback + "] callback added");

        if (mOpponentReady) {
            Ln.d(mName + ": opponent ready, notifying");
            callback.opponentReady();

            if (mPlayerReady) {
                if (opponentStarts()) {
                    callback.onOpponentTurn();
                } else {
                    callback.onPlayersTurn();
                }
            }
        }
    }

    public void unregisterCallback(@NonNull PlayerCallback callback) {
        mCallbacks.remove(callback);
        Ln.v(mName + ": callback removed: " + callback);
    }

    @Override
    public void go() {
        debug_handler.post(debug_thread_break_task);

        Ln.d(mName + ": I go");
        if (!mOpponentReady) {
            Ln.v(mName + ": opponent was not ready, but ready now");
            mOpponentReady = true;

            notifyOpponentReady();
        }

        notifyPlayersTurn();

        mOpponent.executePendingCommands();
    }

    @Override
    public void onShotResult(@NonNull ShotResult result) {
        debug_handler.post(debug_thread_break_task);

        Ln.v(mName + ": -> my shot result: " + result);
        updateEnemyBoard(result);

        notifyOnShotResult(result);
        if (result.isaKill()) {
            notifyOnKillEnemy();
            if (BoardUtils.isItDefeatedBoard(mEnemyBoard, mNumberOfShips)) {
                Ln.d(mName + ": actually opponent lost");

                // If the opponent's version does not support board reveal, just switch screen in 3 seconds.
                // In the later version of the protocol opponent notifies about players defeat sending his board along.
                if (versionSupportsBoardReveal()) {
                    mOpponent.onLost(mMyBoard);
                } else {
                    Ln.d(this + ": opponent version doesn't support board reveal = " + mOpponentVersion);
                }

                Ln.d(mName + ": resetting my state before win");
                reset();
                notifyOnWin();
            }
        } else if (result.cell == Cell.MISS) {
            Ln.d(mName + ": I missed - passing the turn to " + mOpponent);
            mOpponent.go();
            notifyOnMiss();
            notifyOpponentTurn();
        } else {
            Ln.d(mName + ": it's a hit!");
            notifyOnHit();
        }

        mOpponent.executePendingCommands();
    }

    private boolean versionSupportsBoardReveal() {
        return mOpponentVersion >= PROTOCOL_VERSION_SUPPORTS_BOARD_REVEAL;
    }

    @Override
    public void onShotAt(@NonNull Vector aim) {
        debug_handler.post(debug_thread_break_task);

        ShotResult result = createResultForShootingAt(aim);
        Ln.v(mName + ": <- hitting my board at " + aim + " yields: " + result);

        notifyOnShotAt(aim);
        if (result.isaKill()) {
            Ln.d(mName + ": my ship is destroyed - " + result.locatedShip);
            // FIXME: add unit test for removed below line
//            Placement.putShipAt(mMyBoard, result.ship, result.ship.getX(), result.ship.getY());
            notifyOnKillPlayer();
        } else if (result.cell == Cell.MISS) {
            notifyOnMiss();
        } else {
            Ln.d(mName + ": my ship is hit");
            notifyOnHit();
        }

        mOpponent.onShotResult(result);

        if (result.cell == Cell.HIT) {
            if (BoardUtils.isItDefeatedBoard(mMyBoard, mNumberOfShips)) {
                Ln.d(mName + ": I'm defeated, no turn to pass");
                if (!versionSupportsBoardReveal()) {
                    Ln.d(mName + ": opponent version doesn't support board reveal = " + mOpponentVersion);
                    Ln.d(mName + ": resetting my state before win");
                    reset();
                    notifyOnWin();
                }
            } else {
                Ln.d(mName + ": I'm hit - " + mOpponent + " continues");
                mOpponent.go();
            }
        }

        mOpponent.executePendingCommands();
    }

    @Override
    public void setOpponent(@NonNull Opponent opponent) {
        mOpponent = new QueuedCommandOpponent(opponent);
        Ln.d(mName + ": my opponent is " + mOpponent);
        opponent.setOpponentVersion(Opponent.CURRENT_VERSION);
    }

    @Override
    public void onNewMessage(@NonNull String text) {
        ChatMessage message = ChatMessage.newEnemyMessage(text);
        Ln.d(mName + ": I received message: " + message);
        if (mChatListener != null) {
            mChatListener.showChatCrouton(message);
        }
        notifyOnMessage(text);
    }

    @Override
    public void onLost(@NonNull Board board) {
        debug_handler.post(debug_thread_break_task);

        if (!BoardUtils.isItDefeatedBoard(mMyBoard, mNumberOfShips)) {
            reportException("lost while not defeated: " + mMyBoard.getShips());
        }

        Ln.d(mName + ": resetting my state before lost");
        reset();
        notifyOnLost(board);
    }

    @Override
    public void setOpponentVersion(int ver) {
        debug_handler.post(debug_thread_break_task);

        mOpponentVersion = ver;
        Ln.d(mName + ": opponent's protocol version: v" + ver);
    }

    public boolean isOpponentReady() {
        return mOpponentReady;
    }

    /**
     * marks the aimed cell
     */
    @NonNull
    private ShotResult createResultForShootingAt(@NonNull Vector aim) {
        // ship if found will be shot and returned
        LocatedShip locatedShip = mMyBoard.getShipAt(aim);

        if (locatedShip == null) {
            mMyBoard.setCell(Cell.MISS, aim);
        } else {
            Ship ship = locatedShip.ship;
            mMyBoard.setCell(Cell.HIT, aim);
            ship.shoot();

            if (ship.isDead()) {
                return new ShotResult(aim, mMyBoard.getCell(aim), locatedShip);
            }
        }

        return new ShotResult(aim, mMyBoard.getCell(aim));
    }

    private void updateEnemyBoard(@NonNull ShotResult result) {
        if (result.locatedShip == null) {
            mEnemyBoard.setCell(result.cell, result.aim);
        } else {
            Ship ship = result.locatedShip.ship;
            mEnemyBoard.setCell(result.cell, result.aim);
            Vector location = findShipLocation(mEnemyBoard, ship, result.aim);
            mEnemyBoard.addShip(new LocatedShip(ship, location));
        }
        Ln.v(this + ": opponent's board: " + mEnemyBoard);
    }

    @NonNull
    private Vector findShipLocation(@NonNull Board board, @NonNull Ship ship, @NonNull Vector lastKnownCoordinate) {
        Vector coordinate = lastKnownCoordinate;
        if (ship.isHorizontal()) {
            while (isHit(board, coordinate)) {
                lastKnownCoordinate = coordinate;
                coordinate = goLeft(coordinate);
            }
        } else {
            while (isHit(board, coordinate)) {
                lastKnownCoordinate = coordinate;
                coordinate = goUp(coordinate);
            }
        }

        return lastKnownCoordinate;
    }

    @NonNull
    private Vector goUp(@NonNull Vector coordinate) {
        return Vector.get(coordinate.x, coordinate.y - 1);
    }

    @NonNull
    private Vector goLeft(@NonNull Vector coordinate) {
        return Vector.get(coordinate.x - 1, coordinate.y);
    }

    private boolean isHit(@NonNull Board board, @NonNull Vector coordinate) {
        return BoardUtils.contains(coordinate) && board.getCell(coordinate) == Cell.HIT;
    }

    private boolean opponentStarts() {
        return mMyBid < mEnemyBid;
    }

    @NonNull
    public Board getEnemyBoard() {
        return mEnemyBoard;
    }

    @NonNull
    public Board getBoard() {
        return mMyBoard;
    }

    protected final boolean ready() {
        return mPlayerReady;
    }

    @NonNull
    @Override
    public String getName() {
        return mName;
    }

    private void notifyOpponentTurn() {
        for (PlayerCallback callback : mCallbacks) {
            callback.onOpponentTurn();
        }
    }

    private void notifyOnHit() {
        for (PlayerCallback callback : mCallbacks) {
            callback.onHit();
        }
    }

    private void notifyOnMiss() {
        for (PlayerCallback callback : mCallbacks) {
            callback.onMiss();
        }
    }

    private void notifyOnKillEnemy() {
        for (PlayerCallback callback : mCallbacks) {
            callback.onKillEnemy();
        }
    }

    private void notifyOnKillPlayer() {
        for (PlayerCallback callback : mCallbacks) {
            callback.onKillPlayer();
        }
    }

    private void notifyOnLost(@NonNull Board board) {
        for (PlayerCallback callback : mCallbacks) {
            callback.onLost(board);
        }
    }

    private void notifyOnMessage(@NonNull String text) {
        for (PlayerCallback callback : mCallbacks) {
            callback.onMessage(text);
        }
    }

    private void notifyOnWin() {
        for (PlayerCallback callback : mCallbacks) {
            callback.onWin();
        }
    }

    private void notifyOnShotAt(@NonNull Vector aim) {
        for (PlayerCallback callback : mCallbacks) {
            callback.onShotAt(aim);
        }
    }

    private void notifyPlayersTurn() {
        for (PlayerCallback callback : mCallbacks) {
            callback.onPlayersTurn();
        }
    }

    private void notifyOnShotResult(@NonNull ShotResult result) {
        for (PlayerCallback callback : mCallbacks) {
           callback.onShotResult(result);
        }
    }

    private void notifyOpponentReady() {
        for (PlayerCallback callback : mCallbacks) {
            callback.opponentReady();
        }
    }

    @Override
    public String toString() {
        return mName;
    }
}