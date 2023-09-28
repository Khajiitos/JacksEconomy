package me.khajiitos.jackseconomy.util;

import net.minecraft.core.Direction;
import net.minecraft.nbt.IntArrayTag;

import java.util.Arrays;

public class SideConfig {

    public SideConfig() {

    }

    public SideConfig(int[] intValues) {
        for (int i = 0; i < Math.min(intValues.length, 6); i++) {
            this.values[i] = Value.values()[intValues[i]];
        }
    }

    private final Value[] values = new Value[] {
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
        int ordinal = this.values[direction.ordinal()].ordinal();
        if (forward) {
            ordinal = (ordinal + 1) % Value.values().length;
        } else {
            ordinal = ordinal == 0 ? Value.values().length - 1 : ordinal - 1;
        }
        this.values[direction.ordinal()] = Value.values()[ordinal];
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

        for (int i = 0; i < Math.min(array.length, 6); i++) {
            sideConfig.values[i] = Value.values()[array[i]];
        }

        return sideConfig;
    }

    // For example, if "relative" is North, we will return "from"
    // If "relative" is East, we will return "from" turned clockwise
    public static Direction directionRelative(Direction from, Direction relative) {
        return switch (relative) {
            case NORTH -> from;
            case EAST -> from.getClockWise();
            case WEST -> from.getCounterClockWise();
            case SOUTH -> from.getOpposite();
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
