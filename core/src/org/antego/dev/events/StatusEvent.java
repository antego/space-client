package org.antego.dev.events;

import com.badlogic.gdx.math.Vector2;

import java.nio.ByteBuffer;

/**
 * Created by anton on 06.01.2016.
 */
public class StatusEvent implements GameEvent {
    private final Vector2 position;
    private final Vector2 velocity;
    private final float angle;

    public StatusEvent(Vector2 position, Vector2 velocity, float angle) {
        this.position = position;
        this.velocity = velocity;
        this.angle = angle;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public float getAngle() {
        return angle;
    }

    @Override
    public byte[] toByteMessage() {
        //todo message format
        return ByteBuffer.allocate(21)
                .put((byte) 0)
                .putFloat(position.x)
                .putFloat(position.y)
                .putFloat(velocity.x)
                .putFloat(velocity.y)
                .putFloat(angle)
                .array();
    }
}
