package glideme;

/**
 * Responsible for updating the state of the world using laws of physics.
 */
public class Physics {
    /**
     * Determine and set updated values of crane's position and angle.
     *
     * @param world - the world that's being updated.
     */
    public static void update(World world) {
        final World.CraneState state = world.getCraneState();

        double newPosition = state.position + state.velocity * (world.TIME_QUANTUM/1000.0);
        if (newPosition > world.TRACK_LENGTH) {
            newPosition = world.TRACK_LENGTH;
        }
        else if (newPosition < 0) {
            newPosition = 0;
        }

        final double tangensAlpha = (state.velocity - state.prevVelocity) / (world.TIME_QUANTUM/1000.0) / 9.81;
        final double newAngle = Math.atan(tangensAlpha);

        world.update(newPosition, null, newAngle);
    }
}
