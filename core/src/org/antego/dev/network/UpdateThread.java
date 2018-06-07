package org.antego.dev.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.net.Socket;

import org.antego.dev.events.AccelEvent;
import org.antego.dev.events.ExitEvent;
import org.antego.dev.events.ShootEvent;
import org.antego.dev.events.GameEvent;
import org.antego.dev.events.RotateEvent;
import org.antego.dev.events.HitEvent;
import org.antego.dev.events.StatusEvent;
import org.antego.dev.screen.GameScreen;
import org.antego.dev.util.Constants;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UpdateThread extends Thread {
    private final Object gameScreenLock = new Object();
    private GameScreen gameScreen;
    private Socket socket;

    public UpdateThread(Socket socket) {
        this.socket = socket;
    }

    public void registerScreen(GameScreen gameScreen) {
        synchronized (gameScreenLock) {
            this.gameScreen = gameScreen;
            gameScreenLock.notify();
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && socket.isConnected()) {
                byte[] messageType = new byte[1];
                int len;
                do {
                    len = socket.getInputStream().read(messageType);
                    if (len == -1) {
                        throw new EOFException();
                    }
                } while (getMessageLength(messageType[0]) == -1);
                byte[] message = new byte[getMessageLength(messageType[0])];
                len = socket.getInputStream().read(message);
                if (len == -1) {
                    throw new EOFException();
                }
                while (len < getMessageLength(messageType[0])) {
                    len += socket.getInputStream().read(message, len, getMessageLength(messageType[0]) - len);
                }
                doBusiness(messageType[0], message);
            }
        } catch (IOException e) {
            Gdx.app.log(Constants.LOG_TAG, "exception while reading socket", e);
        }
    }

    private void doBusiness(byte messageType, byte[] message) {
        try {
            switch (messageType) {
                case 0: {
                    final float x_pos = ByteBuffer.wrap(message, 0, 4).order(ByteOrder.BIG_ENDIAN).getFloat();
                    final float y_pos = ByteBuffer.wrap(message, 4, 4).order(ByteOrder.BIG_ENDIAN).getFloat();
                    final float x_velo = ByteBuffer.wrap(message, 8, 4).order(ByteOrder.BIG_ENDIAN).getFloat();
                    final float y_velo = ByteBuffer.wrap(message, 12, 4).order(ByteOrder.BIG_ENDIAN).getFloat();
                    final float angle = ByteBuffer.wrap(message, 16, 4).order(ByteOrder.BIG_ENDIAN).getFloat();
                    sendEventToGameScreen(new StatusEvent(new Vector2(x_pos, y_pos), new Vector2(x_velo, y_velo), angle));
                    break;
                }
                case 1: {
                    float angularVelocity = ByteBuffer.wrap(message, 0, 4).order(ByteOrder.BIG_ENDIAN).getFloat();
                    sendEventToGameScreen(new RotateEvent(angularVelocity));
                    break;
                }
                case 2: {
                    final float x_velo = ByteBuffer.wrap(message, 0, 4).order(ByteOrder.BIG_ENDIAN).getFloat();
                    final float y_velo = ByteBuffer.wrap(message, 4, 4).order(ByteOrder.BIG_ENDIAN).getFloat();
                    final float x_pos = ByteBuffer.wrap(message, 8, 4).order(ByteOrder.BIG_ENDIAN).getFloat();
                    final float y_pos = ByteBuffer.wrap(message, 12, 4).order(ByteOrder.BIG_ENDIAN).getFloat();
                    sendEventToGameScreen(new ShootEvent(new Vector2(x_velo, y_velo), new Vector2(x_pos, y_pos)));
                    break;
                }
                case 3: {
                    sendEventToGameScreen(new HitEvent());
                    break;
                }
                case 4: {
                    final float accel = ByteBuffer.wrap(message, 0, 4).order(ByteOrder.BIG_ENDIAN).getFloat();
                    sendEventToGameScreen(new AccelEvent(accel));
                    break;
                }
                case 5: {
                    sendEventToGameScreen(new ExitEvent());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void sendEventToGameScreen(GameEvent event) throws InterruptedException {
        synchronized (gameScreenLock) {
            while (gameScreen == null) {
                gameScreenLock.wait();
            }
            gameScreen.addGameEvent(event);
        }
    }

    private int getMessageLength(byte messageType) {
        switch (messageType){
            case 0:
                return 20;
            case 1:
                return 4;
            case 2:
                return 16;
            case 3:
                return 0;
            case 4:
                return 4;
            case 5:
                return 0;
            default:
                return -1;
        }
    }
}

