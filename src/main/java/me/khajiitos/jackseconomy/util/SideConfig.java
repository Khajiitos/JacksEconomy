package me.khajiitos.jackseconomy.util;

import net.minecraft.core.Direction;
import net.minecraft.nbt.IntArrayTag;

public class SideConfig {

    public SideConfig() {

    }

    protected boolean hasRejectionSlot() {
        return true;
    }

    protected void loadValues(int[] intValues) {
        for (int i = 0; i < Math.min(intValues.length, 6); i++) {
            this.values[i] = Value.values()[intValues[i]];
        }
    }

    public SideConfig(int[] intValues) {
        for (int i = 0; i < Math.min(intValues.length, 6); i++) {
            this.values[i] = Value.values()[intValues[i]];
        }
    }

    protected final Value[] values = new Value[] {
            Value.OUTPUT,
            Value.INPUT,
            Value.NONE,
            Value.NONE,
            Value.OUTPUT,
            Value.INPUT
    };

    public Value getValue(Direction direction) {
        return values[direction.ordinal()];
    }

    public int[] getIntValues() {
        int[] array = new int[values.length];

        for (int i = 0; i < values.length; i++) {
            array[i] = values[i].ordinal();
        }

        return array;
    }

    public void switchValue(Direction direction, boolean forward) {
        Value[] allValues = this.hasRejectionSlot() ? Value.values() : new Value[]{Value.NONE, Value.INPUT, Value.OUTPUT};

        int ordinal = this.values[direction.ordinal()].ordinal();
        if (forward) {
            ordinal = (ordinal + 1) % allValues.length;
        } else {
            ordinal = ordinal == 0 ? allValues.length - 1 : ordinal - 1;
        }
        this.values[direction.ordinal()] = allValues[ordinal];
    }

    public void setValue(Direction direction, Value value) {
        this.values[direction.ordinal()] = value;
    }

    public IntArrayTag toNbt() {
        return new IntArrayTag(getIntValues());
    }

    public static SideConfig fromNbt(IntArrayTag tag) {
        SideConfig sideConfig = new SideConfig();

        int[] array = tag.getAsIntArray();
        for (int i = 0; i < Math.min(array.length, 6); i++) {
            sideConfig.values[i] = Value.values()[array[i]];
        }

        return sideConfig;
    }

    public static SideConfig fromIntArray(int[] array) {
        SideConfig sideConfig = new SideConfig();
        sideConfig.loadValues(array);
        return sideConfig;
    }

    public static Direction directionRelative(Direction from, Direction relative) {
        // Alias for "from": "facing"

        if (relative == Direction.UP || relative == Direction.DOWN) {
            return relative;
        }

        return switch (from) {
            case NORTH -> relative;
            case SOUTH -> relative.getOpposite();
            case EAST -> relative.getCounterClockWise();
            case WEST -> relative.getClockWise();

            // This function is only for horizontal facing machines!
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
        };
    }

    public enum Value {
        NONE,
        INPUT,
        OUTPUT,
        REJECTION_OUTPUT
    }
}
