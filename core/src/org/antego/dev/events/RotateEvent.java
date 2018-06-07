package org.antego.dev.events;


import java.nio.ByteBuffer;

/**
 * Created by anton on 02.01.2016.
 */
public class RotateEvent implements GameEvent {
    final private float angularVelocity;

    public RotateEvent(float angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public float getAngularVelocity() {
        return angularVelocity;
    }

    @Override
    public byte[] toByteMessage() {
        return ByteBuffer.allocate(5)
                .put((byte) 1)
                .putFloat(angularVelocity)
                .array();
    }
}
