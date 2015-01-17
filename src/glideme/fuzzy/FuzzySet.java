package glideme.fuzzy;

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
     * Characteristic points of the membership function.
     */
    private final Double start, edge, end;

    /**
     * Create a fuzzy set given by a piecewise-linear membership function.
     * If all parameters are specified, the membership function will be a pyramidal function.
     * If either start or end is null (but not both!), the function will be a *-slope function.
     * In any other case, IllegalArgumentException will be thrown.
     *
     * @param start - the start point of the rising slope (or null if there is none).
     * @param edge - the coordinate of the edge.
     * @param end - the end point of the falling slope (or null if there is none).
     *
     * @throws IllegalArgumentException - when either edge or both start and end parameters are null.
     */
    public FuzzySet(final Double start, final Double edge, final Double end) throws IllegalArgumentException
    {
        if (edge == null || (start == null && end == null))
        {
            throw new IllegalArgumentException(
                    "FuzzySet: 'edge' and at least one of 'start' and 'end' arguments must be specified!");
        }

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
            // We are either on a constant or rising part of the function.

            if (start == null) {
                return 1.0;
            }

            return (point - start)/(edge - start);
        }

        // We are either on a constant or falling part of the function.

        if (end == null) {
            return 1.0;
        }

        return (end - point)/(end - edge);
    }
}
