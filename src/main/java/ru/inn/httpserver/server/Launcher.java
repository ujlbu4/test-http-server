package ru.inn.httpserver.server;

import org.eclipse.jetty.server.Server;
import ru.inn.httpserver.system.Debugger;
import ru.inn.httpserver.system.config.Options;
import ru.inn.httpserver.system.storage.Storage;

public class Launcher {
    private static Debugger debugger = Debugger.getInstance();

    public static void main(String[] args) throws Exception {
        System.out.println("Working Directory = " +
                               System.getProperty("user.dir"));
        Options.getInstance().loadConfiguration();
        Storage.getInstance().init();

        Server server = new Server(Constants.SERVICE_PORT);
        server.setHandler(new AbstractMockHandler());

        server.start();
        server.join();

        Storage.getInstance().shutdown();
    }
}
