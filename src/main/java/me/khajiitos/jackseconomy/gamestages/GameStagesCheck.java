package me.khajiitos.jackseconomy.gamestages;

public class GameStagesCheck {
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
