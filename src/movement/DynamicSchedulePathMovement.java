package movement;

import core.Coord;
import core.Settings;
import movement.helper.EnterExitHelper;
import movement.helper.RandomHelper;

/**
 * Created by Sattler Patrick on 27/11/16.
 */
public class DynamicSchedulePathMovement extends MovementModel {

    // Settings for enter exit helper
    public static final String LECTURE_PARTICIPATION_CHANCE_SETTING = "LectureParticipationChance";
    public static final String START_PEAK_ENTER_TIME_DIFFERENCE_SETTING = "StartPeakEnterTimeDifference";
    public static final String ENTER_LECTURE_STDDEV_SETTING = "EnterLectureStddev";
    public static final String EXIT_LECTURE_STDDEV_SETTING = "ExitLectureStddev";
    public static final String END_ENTER_LECTURE_DIFFERENCE_SETTING = "EndEnterLectureDifference";
    public static final String ENTER_EXIT_STDDEV_SETTING = "EnterExitStddev";
    public static final String EXIT_END_TIME_SETTING = "ExitEndTime";
    public static final String EXIT_START_TIME_SETTING = "ExitStartTime";
    public static final String ENTER_END_TIME_SETTING = "EnterEndTime";
    public static final String ENTER_START_TIME_SETTING = "EnterStartTime";
    public static final String ENTER_CHANCE_WITHOUT_SCHEDULE_SETTING = "EnterChanceWithoutSchedule";

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

        double lectureParticipationChance = settings.getDouble(LECTURE_PARTICIPATION_CHANCE_SETTING);
        int startPeakEnterTimeDifference = settings.getInt(START_PEAK_ENTER_TIME_DIFFERENCE_SETTING);
        double enterLectureStddev = settings.getDouble(ENTER_LECTURE_STDDEV_SETTING);
        double exitLectureStddev = settings.getDouble(EXIT_LECTURE_STDDEV_SETTING);
        int endEnterLectureDifference = settings.getInt(END_ENTER_LECTURE_DIFFERENCE_SETTING);
        double enterChanceWithoutSchedule = settings.getDouble(ENTER_CHANCE_WITHOUT_SCHEDULE_SETTING);
        int enterStartTime = settings.getInt(ENTER_START_TIME_SETTING);
        int enterEndTime = settings.getInt(ENTER_END_TIME_SETTING);
        int exitStartTime = settings.getInt(EXIT_START_TIME_SETTING);
        int exitEndTime = settings.getInt(EXIT_END_TIME_SETTING);
        double enterExitStddev = settings.getDouble(ENTER_EXIT_STDDEV_SETTING);
        this.enterExitHelper = new EnterExitHelper(lectureParticipationChance, startPeakEnterTimeDifference,
                enterLectureStddev, exitLectureStddev, endEnterLectureDifference, enterChanceWithoutSchedule,
                enterStartTime, enterEndTime, exitStartTime, exitEndTime, enterExitStddev);

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
