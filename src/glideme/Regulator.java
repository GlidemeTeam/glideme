package glideme;

import glideme.fuzzy.FuzzySet;

import java.util.*;

/**
 * Responsible for controlling the crane using fuzzy-logical rules.
 */
public class Regulator {
    /**
     * Linguistic variables describing the regulated system:
     *  - DN, DZ, DP - distance negative/zero/positive,
     *  - AN, AZ, AP - angle negative/zero/positive,
     *  - VN, VZ, VP - velocity negative/zero/positive.
     */
    private static final FuzzySet DN = new FuzzySet(FuzzySet.MembershipType.FallingSlope, -100.0, -25.0, 0.0),
        DZ = new FuzzySet(FuzzySet.MembershipType.Pyramidal, -25.0, 0.0, 25.0),
        DP = new FuzzySet(FuzzySet.MembershipType.RisingSlope, 0.0, 25.0, 100.0),
        AN = new FuzzySet(FuzzySet.MembershipType.FallingSlope, -Math.PI/2.0, -Math.PI/6.0, 0.0),
        AZ = new FuzzySet(FuzzySet.MembershipType.Pyramidal, -Math.PI/6.0, 0.0, Math.PI/6.0),
        AP = new FuzzySet(FuzzySet.MembershipType.RisingSlope, 0.0, Math.PI/6.0, Math.PI/2.0),
        VN = new FuzzySet(FuzzySet.MembershipType.RisingSlope, 0.0, 0.5/6.0, 1.0/6.0),
        VZ = new FuzzySet(FuzzySet.MembershipType.Pyramidal, -0.5/6.0, 0.0, 0.5/6.0),
        VP = new FuzzySet(FuzzySet.MembershipType.FallingSlope, -1.0/6.0, -0.5/6.0, 0.0);

    /**
     * Minimal acceleration time modifier in milliseconds.
     */
    private static final double MIN_ACCEL_TIME = 500.0;

    /**
     * Calculate the maximum among the given values.
     * (Created to compensate for Java Math's own max taking only two arguments...).
     *
     * @param val - first value to compare (obligatory).
     * @param others - other values to compare (optional).
     *
     * @return - maximum of the doubles.
     */
    private static double max(final Double val, final Double... others)
    {
        return others.length == 0? val : Math.max(val, Collections.max(Arrays.asList(others)));
    }

    /**
     * Calculate the minimum among the given values.
     *
     * @param val - first value to compare (obligatory).
     * @param others - other values to compare (optional).
     *
     * @return - minimum of the doubles.
     */
    private static double min(final Double val, final Double... others)
    {
        return others.length == 0? val : Math.min(val, Collections.min(Arrays.asList(others)));
    }

    /**
     * Calculate the sum of the given values.
     *
     * @param values - other values to accumulate (optional).
     *
     * @return - sum of the doubles.
     */
    private static double sum(final double... values)
    {
        double sum = 0;

        for (final double v : values) {
            sum += v;
        }

        return sum;
    }

    /**
     * Return degrees of membership of each of the given fuzzy sets.
     *
     * @param value - the value to check membership for.
     * @param firstSet - the first fuzzy set to check against (obligatory).
     * @param otherSets - any other fuzzy sets to check (optional).
     *
     * @return - an array of doubles between 0.0 and 1.0 determining the degrees of membership of the given set
     * (in order of occurrence).
     */
    private static double[] fuzzify(final double value, final FuzzySet firstSet, final FuzzySet... otherSets) {
        double[] mship = new double[1 + otherSets.length];

        mship[0] = firstSet.grade(value);

        int i = 0;
        for (final FuzzySet set : otherSets) {
            mship[++i] = set.grade(value);
        }

        return mship;
    }

    /**
     * Perform knowledge-based reasoning and return degrees of membership of the output velocity
     * to the three fuzzy sets (in order): VN, VZ and VP.
     *
     * @param distMship - an array of degrees of membership of distance to DN, DZ and DN (in order).
     * @param angleMship - an array of degrees of membership of angle to AN, AZ and AN (in order).
     *
     * @return - an array of degrees of membership of velocity to VN, VZ and VP (in order).
     */
    private static double[] reason(final double[] distMship, final double[] angleMship) {
        double[] velMship = {0.0, 0.0, 0.0};

        // Negative velocity (VN) membership:
        velMship[0] = max(
                min(distMship[0], angleMship[0]), // min(DN, AN)
                min(distMship[1], angleMship[0]), // min(DZ, AN)
                min(distMship[2], angleMship[0]), // min(DP, AN)
                min(distMship[2], angleMship[1])  // min(DP, AZ)
        );

        // Zero velocity (VZ) membership:
        velMship[1] = min(distMship[1], angleMship[1]); // min(DZ, AZ)

        // Positive velocity (VP) membership:
        velMship[2] = max(
                min(distMship[0], angleMship[1]), // min(DN, AZ)
                min(distMship[0], angleMship[2]), // min(DN, AP)
                min(distMship[1], angleMship[2]), // min(DZ, AP)
                min(distMship[2], angleMship[2])  // min(DP, AP)
        );

        return velMship;
    }

    /**
     * Find the most representative value according to the given degrees of membership of velocity sets.
     * This is done by calculating a sum of representative members of the fuzzy sets weighted by membership degrees.
     *
     * @param velocityMembership - an array of three degrees of membership of the output value of the sets:
     * VN, VZ, VP (in order).
     *
     * @return - a new, defuzzified velocity value.
     */
    private static double defuzzify(final double[] velocityMembership) {
        // Determine the singletons and weigh them...
        final double vnVal = VN.peakValue() * velocityMembership[0],
                vzVal = VZ.peakValue() * velocityMembership[1],
                vpVal = VP.peakValue() * velocityMembership[2];

        // ... and find the weighted average of them.
        return sum(vnVal, vzVal, vpVal)/sum(velocityMembership);
    }

    /**
     * Uses destination, current position and angle as input. Updates the velocity.
     *
     * @param world - the world that's being updated.
     */
    public static void update(World world) {
        World.CraneState inputState = world.getCraneState();

        final double distance = world.getDestination() - inputState.position;

        // Fuzzification:
        double[] distMship = fuzzify(distance, DN, DZ, DP);
        double[] angleMship = fuzzify(inputState.angle, AN, AZ, AP);

        // Reasoning:
        double[] velMship = reason(distMship, angleMship);

        // Defuzzification:
        double destVelocity = defuzzify(velMship);

        // We cannot affect velocity directly, we only control acceleration.
        double newAccelleration = (destVelocity - inputState.velocity)/MIN_ACCEL_TIME;

        world.update(null, null, newAccelleration, null);
    }
}
