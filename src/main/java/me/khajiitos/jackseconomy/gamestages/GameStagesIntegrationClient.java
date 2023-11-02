package me.khajiitos.jackseconomy.gamestages;

import net.darkhax.gamestages.GameStages;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameStagesIntegrationClient {
    public static final Set<String> recentlyUnlockedStages = new HashSet<>();

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(GameStagesIntegrationClient::onStageUnlocked);
    }

    private static void onStageUnlocked(GameStageEvent.Added e) {
        recentlyUnlockedStages.add(e.getStageName());
    }
}
