/**
 * Store constants for the simulation
 * Created by Jack on 5/5/2017.
 */
public class Constant {
	// the maximum amount of grain which a patch can have
	public final static int MAX_GRAIN = 50;

	// the width of board (world space)
	public static final int BOARD_WIDTH = 50;

	// the height of board (world space)
	public static final int BOARD_HEIGHT = 50;

	// extension features below

	public static final boolean WEALTH_INHERITANCE_ENABLED = false;
	public static final double WEALTH_INHERITANCE = 0.5;

	// if set to true, patch growth rate will be a proportion of its maximum grain,
	// and absolute growth rate setting will be useless
	public static final boolean PROPORTIONAL_GROWTH_ENABLED = false;

	// the proportion of max grain as growth rate
	public static final double PATCH_GROWTH_PROPORTION = 0.2;
}
