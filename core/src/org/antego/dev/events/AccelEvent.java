package org.antego.dev.events;

import java.nio.ByteBuffer;

/**
 * Created by anton on 14.01.2016.
 */
public class AccelEvent implements GameEvent {
    private final float acceleration;

    public AccelEvent(float acceleration) {
        this.acceleration = acceleration;
    }

    public float getAcceleration() {
        return acceleration;
    }

    @Override
    public byte[] toByteMessage() {
        return ByteBuffer.allocate(5)
                .put((byte) 4)
                .putFloat(acceleration)
                .array();
    }
}
