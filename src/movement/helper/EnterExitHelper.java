package movement.helper;

/**
 * Created by Sattler Patrick on 27/11/16.
 */
public class EnterExitHelper {

    private final double LectureParticipationChance;
    private final int StartPeakEnterTimeDifference;
    private final double EnterLectureStddev;
    private final double ExitLectureStddev;
    private final int EndEnterLectureDifference;
    private final double EnterChanceWithoutSchedule;
    private final int EnterStartTime;
    private final int EnterEndTime;
    private final int ExitStartTime;
    private final int ExitEndTime;
    private final double EnterExitStddev;

    private RandomHelper random = RandomHelper.getInstance();

    public EnterExitHelper(double lectureParticipationChance, int startPeakEnterTimeDifference, double enterLectureStddev,
                           double exitLectureStddev, int endEnterLectureDifference,
                           double enterChanceWithoutSchedule, int enterStartTime, int enterEndTime,
                           int exitStartTime, int exitEndTime, double enterExitStddev) {
        this.LectureParticipationChance = lectureParticipationChance;
        this.StartPeakEnterTimeDifference = startPeakEnterTimeDifference;
        this.EnterLectureStddev = enterLectureStddev;
        this.ExitLectureStddev = exitLectureStddev;
        this.EndEnterLectureDifference = endEnterLectureDifference;
        this.EnterChanceWithoutSchedule = enterChanceWithoutSchedule;
        this.EnterStartTime = enterStartTime;
        this.EnterEndTime = enterEndTime;
        this.ExitStartTime = exitStartTime;
        this.ExitEndTime = exitEndTime;
        this.EnterExitStddev = enterExitStddev;
    }

    public Integer enterTimeForSchedule(ScheduleInterface schedule, boolean byUbahn) {
        int[] startTimes = schedule.getStartTimesSorted();
        double normalEnterTime = random.getNormalRandomWithMeanAndStddev(
                (double)(this.EnterStartTime-this.EnterEndTime), this.EnterExitStddev);
        if (normalEnterTime < this.EnterStartTime) {
            normalEnterTime = this.EnterStartTime;
        }
        if (normalEnterTime > this.EnterEndTime) {
            return null;
        }
        for (int startTime: startTimes) {
            if (random.getRandomDouble() < LectureParticipationChance) {
                double enterTime = random.getNormalRandomWithMeanAndStddev(
                        (double)(startTime - this.StartPeakEnterTimeDifference),
                        this.EnterLectureStddev);
                if (enterTime > normalEnterTime) {
                    break;
                }
                if (enterTime - this.EndEnterLectureDifference > startTime) {
                    continue;
                }
                if (byUbahn) {
                    return (int) (Math.round(enterTime / 10.) * 10);
                } else {
                    return (int) Math.round(enterTime);
                }
            }
        }
        if (random.getRandomDouble() < EnterChanceWithoutSchedule) {
            if (byUbahn) {
                return (int) (Math.round(normalEnterTime / 10.) * 10);
            } else {
                return (int) Math.round(normalEnterTime);
            }
        }
        return null;
    }

    public Integer exitTimeForSchedule(ScheduleInterface schedule, boolean byUbahn, int minExitTime) {
        int[] endTimes = schedule.getEndTimesSorted();
        double normalExitTime = random.getNormalRandomWithMeanAndStddev(
                (double)(this.ExitStartTime-this.ExitEndTime), this.EnterExitStddev);
        if (normalExitTime < this.ExitStartTime || normalExitTime > this.EnterEndTime) {
            normalExitTime = this.EnterStartTime;
        }
        if (endTimes.length > 0) {
            int lastEndTime = endTimes[endTimes.length - 1];
            double exitTime = random.getNormalRandomWithMeanAndStddev(
                    (double)(lastEndTime + this.StartPeakEnterTimeDifference), this.ExitLectureStddev
                    );
            if (normalExitTime < exitTime) {
                if (byUbahn) {
                    return (int) (Math.round(exitTime / 10.) * 10);
                } else {
                    return (int) Math.round(exitTime);
                }
            }
        }

        if (normalExitTime < minExitTime) {
            normalExitTime = minExitTime;
        }

        if (byUbahn) {
            return (int) (Math.round(normalExitTime / 10.) * 10);
        } else {
            return (int) Math.round(normalExitTime);
        }
    }

}