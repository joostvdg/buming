package com.github.joostvdg.buming.mining.constants;

/**
 * We should use a non-instantiable class for constants.
 * As per: https://dzone.com/articles/constants-in-java-the-anti-pattern-1.
 *
 * It should also be final, else we can extend this and create a constructor allowing us to instantiate it anyway.
 */
public final class Constants {
    private Constants() {} // we should not instantiate this class

    public static final int NUMBER_OF_SWEEPERS = 100;
    public static final int NUMBER_OF_MINE_LAYERS = 100;
    public static final int BOARD_ROWS = 10;
    public static final int BOARD_COLUMNS = 10;
    public static final int BOARD_WIDTH_PXL = 800;
    public static final int BOARD_HEIGHT_PXL = 600;
    public static final String APP_NAME = "Mine Sweeping";
}
