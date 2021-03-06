package glideme;

/**
 * Responsible for updating the state of the world using laws of physics.
 */
public class Physics {
    /**
     * Previous acceleration value.
     */
    private static double prevAcceleration = 0.0;

    /**
     * Gravitational acceleration (corrected to work with our custom distance units).
     */
    private static final double GRAVITY = 9.81/1000.0/12.0;

    /**
     * Determine and set updated values of crane's position and angle.
     *
     * @param world - the world that's being updated.
     */
    public static void update(World world) {
        final World.CraneState state = world.getCraneState();

        // Update current velocity given current acceleration.
        double newVelocity = state.velocity + state.acceleration * World.TIME_QUANTUM;

        // Update position given current velocity. Position's bounded by track's length.
        double newPosition = state.position + newVelocity * World.TIME_QUANTUM;
        if (newPosition > World.TRACK_LENGTH) {
            newPosition = World.TRACK_LENGTH;
        }
        else if (newPosition < 0) {
            newPosition = 0;
        }

        // Calculate current angle change - it depends on acceleration.
        final double deltaAcc = prevAcceleration - state.acceleration;
        final double tanDeltaAlpha = deltaAcc / GRAVITY;
        final double newAngle = state.angle + Math.atan(tanDeltaAlpha);

        world.update(newPosition, newVelocity, null, newAngle);

        prevAcceleration = state.acceleration;
    }
}
