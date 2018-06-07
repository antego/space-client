package org.antego.dev.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.GdxRuntimeException;

import org.antego.dev.events.GameEvent;
import org.antego.dev.util.Constants;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by anton on 02.01.2016.
 */
public class SenderThread extends Thread {
    Queue<GameEvent> eventQueue = new LinkedList<GameEvent>();
    Socket socket;

    public SenderThread(Socket socket) {
        this.socket = socket;
    }

    public void addToQueue(GameEvent event) {
        if (event == null) {
            return;
        }
        synchronized (eventQueue) {
            eventQueue.add(event);
            eventQueue.notify();
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && socket.isConnected()) {
                synchronized (eventQueue) {
                    while (eventQueue.isEmpty()) {
                        eventQueue.wait();
                    }
                    socket.getOutputStream().write(eventQueue.poll().toByteMessage());
                }
            }
        } catch (InterruptedException ignore) {

        } catch (IOException e) {
            logException(e);
        } catch (GdxRuntimeException e) {
            logException(e);
        }
    }

    private void logException(Throwable e) {
        Gdx.app.log(Constants.LOG_TAG, "exception while sending event", e);
    }
}
