package movement.helper;

import java.util.Random;

/**
 * Created by Sattler Patrick on 27/11/16.
 */
public class RandomHelper {


    private static RandomHelper instance = null;
    private Random random;

    private RandomHelper(Random rng) {
        this.random = rng;
    }

    public static RandomHelper getInstance() {
        return RandomHelper.instance;
    }

    /**
     * Only needs to be called once afterwards it has no effect
     * @param rng the random object
     */
    public static void createInstance(Random rng) {
        if (RandomHelper.instance == null) {
            RandomHelper.instance = new RandomHelper(rng);
        }
    }

    /**
     *
     * @param from inclusive
     * @param until exclusive
     * @return new random number between [from, until[
     */
    public int getRandomIntBetween(int from, int until) {
        return random.nextInt(until - from) + from;
    }

    /**
     *
     * @return a new random Integer value
     */
    public int getRandomInt() {
        return random.nextInt();
    }

    /**
     *
     * @param from inclusive
     * @param until exclusive
     * @return new random number between [from, until[
     */
    public double getRandomDoubleBetween(double from, double until) {
        return random.nextDouble() * (until - from) + from;
    }

    /**
     *
     * @return double between 0.0 and 1.0
     */
    public double getRandomDouble() {
        return random.nextDouble();
    }

    /**
     *
     * @param mean the mean of the normal distribuiton
     * @param stddev the standard deviation of the normal distribiution
     * @return
     */
    public double getNormalRandomWithMeanAndStddev(double mean, double stddev) {
        return random.nextGaussian() * stddev + mean;
    }
}
