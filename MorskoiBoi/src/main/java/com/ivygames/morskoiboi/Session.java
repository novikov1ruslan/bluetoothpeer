package com.ivygames.morskoiboi;

import android.support.annotation.NonNull;

import com.ivygames.morskoiboi.model.Opponent;
import com.ivygames.morskoiboi.player.PlayerOpponent;

public class Session {
    @NonNull
    public final PlayerOpponent player;
    @NonNull
    public final Opponent opponent;

    public Session(@NonNull PlayerOpponent player, @NonNull Opponent opponent) {
        this.player = player;
        this.opponent = opponent;
        player.setOpponent(opponent);
        opponent.setOpponent(player);
    }
}