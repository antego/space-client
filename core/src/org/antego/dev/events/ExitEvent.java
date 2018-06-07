package org.antego.dev.events;

/**
 * Created by anton on 29.01.2016.
 */
public class ExitEvent implements GameEvent {
    @Override
    public byte[] toByteMessage() {
        return new byte[]{5};
    }
}
