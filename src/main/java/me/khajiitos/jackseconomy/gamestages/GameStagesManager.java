package me.khajiitos.jackseconomy.gamestages;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class GameStagesManager {

    public static boolean hasGameStage(Player player, String gameStage) {
        if (GameStagesCheck.isInstalled()) {
            return GameStagesIntegration.hasGameStage(player, gameStage);
        }
        return true;
    }

    public static void init() {
        if (GameStagesCheck.isInstalled()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> GameStagesIntegrationClient::init);
        }
    }
}
