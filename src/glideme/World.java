package glideme;

/**
 * Encapsulates the invariants and state of the modelled world, that is:
 *  - crane's track length,
 *  - the state of the crane (position, velocity, angle),
 *  - the destination point.
 *
 * The world is uni-dimensional, location is given by a single element vector.
 *
 * It would be nice to achieve access synchronization using atomics,
 * but Java's stdlib does only support integral values. Therefore
 * we need to synchronize method calls.
 */
public class World {
    /**
     * Time quantum in milliseconds. It controls how long should the pause between update() calls be.
     */
    public static final int TIME_QUANTUM = 1;

    /**
     * Length of the crane's track in arbitrary units.
     */
    public static final int TRACK_LENGTH = 100;

    /**
     * Holds current physical quantities associated with the crane.
     */
    public class CraneState
    {
        /**
         * Crane's position on the track as a distance from its start (in units).
         */
        public double position;

        /**
         * Crane's velocity in units/msec. Negative values mean motion towards the beginning of the track.
         */
        public double velocity;

        /**
         * Crane's acceleration in units/msec^2.
         */
        public double acceleration;

        /**
         * Crane's offset from its balance point (perpendicular to the floor) in radians.
         * Negative values mean that the crane leans in the direction opposite to the velocity.
         */
        public double angle;

        /**
         * Construct a crane's state.
         *
         * @param position - the starting position
         * @param velocity - the starting velocity
         * @param angle - the starting angle
         */
        public CraneState(final double position, final double velocity, final double acceleration, final double angle)
        {
            this.position = position;
            this.velocity = velocity;
            this.acceleration = acceleration;
            this.angle = angle;
        }
    }

    /**
     * Current state of the crane.
     * The crane always starts at position 50, still, in balance.
     */
    private CraneState craneState = new CraneState(50.0, 0.0, 0.0, 0.0);

    /**
     * Crane's destination point (as a distance in units from the track's start).
     * At the beginning it's always where the crane starts.
     */
    private double destPoint = 50.0;

    /**
     * Determine updated values of physical quantities for current time quantum.
     */
    public void refresh() {
        /*System.out.printf("dest=%f, d=%f, v=%f, acc=%f, a=%f\n",
                destPoint,
                craneState.position,
                craneState.velocity,
                craneState.acceleration,
                craneState.angle);*/

        Physics.update(this);
        Regulator.update(this);
    }

    /**
     * Atomically update the crane's state.
     */
    synchronized
    public void update(final CraneState newCraneState) {
        assert(newCraneState.position >= 0);
        assert(newCraneState.angle >= -Math.PI/2.0 && newCraneState.angle <= Math.PI/2.0);

        craneState = newCraneState;
    }

    /**
     * Atomically update the crane's position, velocity and angle.
     * To avoid changing some of these quantities, pass null instead of a new value.
     */
    synchronized
    public void update(Double newPosition, Double newVelocity, Double newAcceleration, Double newAngle) {
        if (newPosition == null)
        {
            newPosition = craneState.position;
        }

        if (newVelocity == null)
        {
            newVelocity = craneState.velocity;
        }

        if (newAcceleration == null)
        {
            newAcceleration = craneState.acceleration;
        }

        if (newAngle == null)
        {
            newAngle = craneState.angle;
        }

        final CraneState newState = new CraneState(newPosition, newVelocity, newAcceleration, newAngle);
        update(newState);
    }

    /**
     * Change the crane's destination point.
     */
    synchronized
    public void setDestination(final double newDestination) {
        assert(newDestination >= 0.0);
        assert(newDestination <= TRACK_LENGTH);

        destPoint = newDestination;
    }

    /**
     * Get current crane state.
     */
    synchronized
    public CraneState getCraneState() {
        return craneState;
    }

    /**
     * Get current destination point.
     */
    synchronized
    public double getDestination() {
        return destPoint;
    }
}
