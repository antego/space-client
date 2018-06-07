package org.antego.dev.events;

import com.badlogic.gdx.math.Vector2;

import java.nio.ByteBuffer;

public class ShootEvent implements GameEvent {
    private final Vector2 velocity;

    private final Vector2 position;
    public ShootEvent(Vector2 velocity, Vector2 position) {
        this.velocity = velocity;
        this.position = position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public Vector2 getPosition() {
        return position;
    }

    @Override
    public byte[] toByteMessage() {
        return ByteBuffer.allocate(17)
                .put((byte) 2)
                .putFloat(velocity.x)
                .putFloat(velocity.y)
                .putFloat(position.x)
                .putFloat(position.y)
                .array();
    }
}
