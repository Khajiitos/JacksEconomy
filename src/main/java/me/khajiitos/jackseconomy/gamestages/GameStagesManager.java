package me.khajiitos.jackseconomy.gamestages;

import net.minecraft.world.entity.player.Player;

public class GameStagesManager {

    public static boolean hasGameStage(Player player, String gameStage) {
        if (GameStagesCheck.isInstalled()) {
            return GameStagesIntegration.hasGameStage(player, gameStage);
        }
        return true;
    }
}
