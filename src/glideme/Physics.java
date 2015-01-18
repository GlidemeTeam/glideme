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
        
        World.CraneState state = world.getCraneState();

        double newPosition = state.position + state.velocity * world.TIME_QUANTUM;

        double tangensAlpha = (state.velocity - state.prevVelocity) / world.TIME_QUANTUM / 9.81;

        double newAngle = Math.tan(tangensAlpha);


        world.update(newPosition, null, newAngle);
        
    }
}
