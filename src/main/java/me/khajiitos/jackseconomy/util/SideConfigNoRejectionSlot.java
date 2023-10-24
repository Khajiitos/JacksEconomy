package me.khajiitos.jackseconomy.util;

public class SideConfigNoRejectionSlot extends SideConfig {
    @Override
    protected boolean hasRejectionSlot() {
        return false;
    }

    @Override
    protected void loadValues(int[] intValues) {
        for (int i = 0; i < Math.min(intValues.length, 6); i++) {
            Value value = Value.values()[intValues[i]];
            this.values[i] = value == Value.REJECTION_OUTPUT ? Value.NONE : value;
        }
    }

    public static SideConfigNoRejectionSlot fromIntArray(int[] array) {
        SideConfigNoRejectionSlot sideConfig = new SideConfigNoRejectionSlot();
        sideConfig.loadValues(array);
        return sideConfig;
    }
}
