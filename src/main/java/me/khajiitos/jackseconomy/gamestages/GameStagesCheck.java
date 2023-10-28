package me.khajiitos.jackseconomy.gamestages;

import net.darkhax.gamestages.GameStages;

class GameStagesCheck {
    private static boolean installed;

    static {
        try {
            Class.forName("net.darkhax.gamestages.GameStages");
            installed = true;
        } catch (ClassNotFoundException e) {
            installed = false;
        }
    }

    public static boolean isInstalled() {
        return installed;
    }
}
