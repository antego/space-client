package org.antego.dev.events;


public class HitEvent implements GameEvent {
    @Override
    public byte[] toByteMessage() {
        return new byte[]{3};
    }
}
