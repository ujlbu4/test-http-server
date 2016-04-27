package ru.inn.httpserver.system.storage;

import ru.inn.httpserver.server.Constants;
import ru.inn.httpserver.system.Debugger;

public class Monitor implements Runnable {

    private Debugger debug = Debugger.getInstance();
    private Storage storage;
    private boolean operate = true;

    public Monitor(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        while (operate) {
            try {
                Thread.sleep(Constants.MONITOR_CYCLE);
                if (!operate)
                    break;
                if (storage.getState() > Constants.MONITOR_BUFFER_SIZE) {
                    long toRemove = (storage.getState() - Constants.MONITOR_BUFFER_SIZE) + Constants.MONITOR_BUFFER_SIZE / 2;
                    debug.println("Cleaning storage, removing first " + toRemove + " objects");
                    storage.clear(toRemove);
                } else {
                    debug.println("Storage state: " + storage.getState() + " objects");
                }
            } catch (Exception e) {
                debug.printExceptionToErrorOut(e);
            }
        }
    }

    public void setOperate(boolean operate) {
        this.operate = operate;
    }

}
