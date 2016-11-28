package movement;

import core.Coord;
import core.Settings;
import movement.helper.EnterExitHelper;
import movement.helper.RandomHelper;

/**
 * Created by Sattler Patrick on 27/11/16.
 */
public class DynamicSchedulePathMovement extends MovementModel {

    // Settings for initial location
    public static final String CHANCE_FOR_UBAHN_SETTING = "ChanceForUbahn";

    private RandomHelper randomHelper;
    private EnterExitHelper enterExitHelper;
    private double chanceForUbahn;
    private boolean byUbahn;

    //==========================================================================//
    // Implementation
    //==========================================================================//
    @Override
    public Path getPath() {
        // TODO: implement
        return null;
    }

    @Override
    public Coord getInitialLocation() {
        if (byUbahn) {
            // TODO: return ubahn enter location
            return null;
        } else {
            // TODO: return random other location
            return null;
        }
    }

    @Override
    public MovementModel replicate() {
        return new DynamicSchedulePathMovement(this);
    }

    //==========================================================================//
    // Construction
    //==========================================================================//
    public DynamicSchedulePathMovement(Settings settings) {
        super(settings);
        RandomHelper.createInstance(this.rng);
        this.randomHelper = RandomHelper.getInstance();

        this.enterExitHelper = new EnterExitHelper(settings);

        this.chanceForUbahn = settings.getDouble(CHANCE_FOR_UBAHN_SETTING);
        this.byUbahn = randomHelper.getRandomDouble() < chanceForUbahn;
    }

    public DynamicSchedulePathMovement(DynamicSchedulePathMovement other ) {
        super(other);
        this.randomHelper = other.randomHelper;
        this.enterExitHelper = other.enterExitHelper;
        this.chanceForUbahn = other.chanceForUbahn;
        this.byUbahn = randomHelper.getRandomDouble() < other.chanceForUbahn;
    }
    //==========================================================================//

}
