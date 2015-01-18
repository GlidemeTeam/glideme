package glideme.fuzzy;

import java.lang.reflect.Member;

/**
 * Represents a fuzzy set and implements basic fuzzy set operations.
 *
 * We only support a subset of piecewise-linear membership functions (with a single edge):
 *
 *  - Pyramidal:
 *    ^
 *    |
 *  1 |    /\
 *    |   /  \
 *    |  /    \
 *    | /      \
 *  0 #-------------->
 *
 *  - Falling slope:
 *    ^
 *    |
 *  1 |----\
 *    |     \
 *    |      \
 *    |       \
 *  0 #-------------->
 *
 *  - Rising slope:
 *    ^
 *    |
 *  1 |       /----
 *    |      /
 *    |     /
 *    |    /
 *  0 #-------------->
 */
public class FuzzySet {
    /**
     * Supported membership function types.
     */
    public enum MembershipType {
        Pyramidal,
        FallingSlope,
        RisingSlope
    }

    /**
     * Membership function type.
     */
    private final MembershipType membershipType;

    /**
     * Characteristic points of the membership function.
     */
    private final Double start, edge, end;

    /**
     * Create a fuzzy set given by a piecewise-linear membership function.
     *
     * @param membershipType - membership function type.
     * @param start - start point of the left piece of the function (or null if it's -infinity).
     * @param edge - the coordinate of the edge.
     * @param end - end point of the right piece of the function (or null if it's +infinity).
     *
     * @throws IllegalArgumentException - when either edge or both start and end parameters are null.
     */
    public FuzzySet(final MembershipType membershipType, final Double start, final Double edge, final Double end)
            throws IllegalArgumentException
    {
        if (edge == null || (start == null && end == null))
        {
            throw new IllegalArgumentException(
                    "FuzzySet: 'edge' and at least one of 'start' and 'end' arguments must be specified!");
        }

        this.membershipType = membershipType;
        this.start = start;
        this.edge = edge;
        this.end = end;
    }

    /**
     * Determines the degree of membership of the given point.
     *
     * @param point - the point checked.
     *
     * @return - a degree of membership (between 0.0 and 1.0).
     */
    public double grade(final double point)
    {
        if (point == edge)
        {
            return 1.0;
        }

        else if (point < edge) {
            // We are either on a constant...
            if (membershipType == MembershipType.FallingSlope) {
                return (start == null || point >= start)? 1.0 : 0.0;
            }

            // ... or rising piece of the function (or before it).
            return point >= start? (point - start)/(edge - start) : 0.0;
        }

        // We are either on a constant...
        if (membershipType == MembershipType.RisingSlope) {
            return (end == null || point <= end)? 1.0 : 0.0;
        }

        // ... or falling part of the function (or after it).
        return point <= end? (end - point)/(end - edge) : 0.0;
    }

    /**
     * Get the most representative value (singleton) of the set.
     *
     * @return - the most representative member value of the set.
     */
    public double peakValue()
    {
        // In case of a pyramidal function, the edge is the most representative.
        if (membershipType == MembershipType.Pyramidal) {
            return edge;
        }
        else if (membershipType == MembershipType.FallingSlope) {
            // In case of an infinite falling slope, it's technically -infinity,
            // but as it's not very helpful, we return the edge.
            if (start == null) {
                return edge;
            }

            // Otherwise (in case of a finite falling slope), it's a middle value between start and edge.
            return (start + edge)/2.0;
        }

        // Analogically to the falling slope case, for an infinite rising slope it's an edge
        // and for a finite one it's a mean of edge and end.
        if (end == null) {
            return edge;
        }

        return (edge + end)/2.0;
    }
}
