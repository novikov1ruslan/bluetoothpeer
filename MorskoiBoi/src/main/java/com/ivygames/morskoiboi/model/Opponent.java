package com.ivygames.morskoiboi.model;

public interface Opponent {

    int CURRENT_VERSION = 3;

    /**
     * This opponent is being shot at given coordinate. <br>
     * This call will trigger this opponent to call {@link #onShotResult(PokeResult)} on its opponent. <br>
     * If the result of the shot is {@link Cell#isHit()}, {@link #go()} method is called afterwards.
     */
    void onShotAt(Vector2 aim);

    /**
     * This opponent received result of his/her shot (Called on me)
     */
    void onShotResult(PokeResult pokeResult);

    /**
     * Called to indicate the turn is passed to this opponent.
     * <p/>
     * Called in 2 occasions:
     * 1) enemy knows that his bid is lower
     * 2) enemy is hit
     */
    void go();

    void setOpponent(Opponent opponent);

    /**
     * Called when enemy opponent sends this opponent his bid.
     * This can be the first method called on this opponent.
     * The only preceding call to this can be {@link #go()},
     * and only when enemy knows that his bid is lower.
     *
     * @param bid enemy's bid
     */
    void onEnemyBid(int bid);

    String getName();

    void onLost(Board board);

    void setOpponentVersion(int ver);

    void onNewMessage(String text);
}
