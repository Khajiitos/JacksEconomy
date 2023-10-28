package me.khajiitos.jackseconomy.gamestages;

import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.data.IStageData;
import net.minecraft.world.entity.player.Player;

class GameStagesIntegration {

    static boolean hasGameStage(Player player, String gameStage) {
        IStageData stageData = GameStageHelper.getPlayerData(player);
        return stageData != null && stageData.hasStage(gameStage);
    }
}
