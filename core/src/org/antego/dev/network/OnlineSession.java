package org.antego.dev.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;

import org.antego.dev.screen.GameScreen;
import org.antego.dev.util.Constants;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Created by anton on 02.01.2016.
 */
public class OnlineSession implements Callable<Void> {
    //1. подклюение к серверу (открытие сокета)
    //2. приянть ответ от сервера о возможности подключения
    // здесь же проверка, что имя не занято
    // хэндшейкинг: абоненто -> сервер -> абонент/разрыв соединения
    //4. сервер - создание сессии
    // клиент - создание игрового окружения
    //5. сервер - переход в режим игры, обратный отсчет
    //6. конец игры - сервер отсылает специальный пакет с причиной остановки и дополнительными данными
    //7. клиент - итоги боя - возврат на экран подключения - закрытие соединения

    private Socket socket;
    private SenderThread senderThread;
    private UpdateThread updateThread;
    private volatile WorldParameters worldParameters;
    private boolean started;

    @Override
    public Void call() {
        try {
            socket = openSocket();
        } catch (GdxRuntimeException e) {
            Gdx.app.error(Constants.LOG_TAG, "Exception on socket opening", e);
            if (socket != null) {
                socket.dispose();
            }
            throw e;
        }
        doHandshake();
        worldParameters = setupMultiplayerWorld();
        if (worldParameters == null) {
            return null;
        }
        senderThread = new SenderThread(socket);
        updateThread = new UpdateThread(socket);
        senderThread.start();
        updateThread.start();
        started = true;
        return null;
    }

    public void closeSocket() {
        if (socket != null) {
            socket.dispose();
            Gdx.app.log(Constants.LOG_TAG, "socket closed in OnlineSession");
        }
    }

    public SenderThread getSenderThread() {
        return senderThread;
    }

    public UpdateThread getUpdateThread() {
        return updateThread;
    }

    public WorldParameters getWorldParameters() {
        return worldParameters;
    }

    public void registerGameScreen(GameScreen gameScreen) {
        updateThread.registerScreen(gameScreen);
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    private WorldParameters setupMultiplayerWorld() {
        byte[] worldParam = new byte[1];
        try {
            int len;
            do {
                len = socket.getInputStream().read(worldParam);
                if (len == -1) {
                    throw new EOFException("eof");
                }
            } while (worldParam[0] != 0 && worldParam[0] != 1);
        } catch (IOException e) {
            Gdx.app.error(Constants.LOG_TAG, "Exception while reading world param", e);
            return null;
        }
        return new WorldParameters(worldParam[0] == 1); //если 1, то левый игрок - человек
    }

    private boolean doHandshake() {
        //todo handshake
        return true;
    }

    private Socket openSocket() throws GdxRuntimeException {
        return Gdx.net.newClientSocket(
                Net.Protocol.TCP,
                Constants.HOST_ADDRESS,
                Constants.HOST_PORT,
                new SocketHints());
    }

    public void cancel() {
//        if (senderThread != null) {
//            senderThread.addToQueue(new CancelEvent());
//        }
        closeSocket();
    }
}
