package ru.inn.httpserver.system.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.inn.httpserver.system.Debugger;
import ru.inn.httpserver.system.enums.Routes;

public class Storage {

    private Debugger debug = Debugger.getInstance();
    private Map query = new HashMap();
    private Monitor monitor;
    private Thread monitorThread;

    private Storage() {
        if (StorageLoader.INSTANCE != null) {
            throw new IllegalStateException("Storage already instantiated!");
        }
        this.monitor = new Monitor(this);
        this.monitorThread = new Thread(this.monitor);
        this.monitorThread.setName("Storage-Monitor");
        this.monitorThread.start();
    }

    private static class StorageLoader {
        private static final Storage INSTANCE = new Storage();
    }

    public static Storage getInstance() {
        return StorageLoader.INSTANCE;
    }

    public void init() {
        //STUB
    }

    public void put(String user_id, String event, String value) {
        synchronized (query) {
            query.put(user_id+event, value);
        }
    }

    public void delete(String user_id, String event) {
        synchronized (query) {
            if (query.containsKey(user_id+event)) {
                query.remove(user_id + event);
            }
        }
    }

    //FIXME: Refactor this
    public Map getByParameter(Routes route, String parameterKey, String parameterValue, Boolean delete) {
        Map result = new HashMap();
        synchronized (query) {
            List<Integer> toDelete = new ArrayList<>();

            for (int i = 0; i < query.size(); i++) {
                //RequestEntity current = query.get(i);
                Object current = query.get(i);

                //if (current.getRoute().equals(route) && current.isParameterPresent(parameterKey, parameterValue)) {
                if (query.containsKey(parameterKey)) {
                    result.put(query.keySet().toArray()[i], query.get(i));
                    if (delete != null && delete)
                        toDelete.add(i);
                }
            }

            if (delete != null && delete) {
                for (int i = 0; i < toDelete.size(); i++) {
                    query.remove(toDelete.get(i).intValue() - i);
                }
            }
        }
        return result;
    }

    public String getByParameter(String parameterKey) {
        String result = null;

        synchronized (query) {
            if (query.containsKey(parameterKey))
                result = (String) query.get(parameterKey);
        }

        return result;
    }

    public void clear(long offset) {
        synchronized (query) {
            for (int i = 0; i < offset; i++) {
                query.remove(i);  //Remove first items
            }
        }
    }

    public void clearAll() {
        debug.println("Clear all storage");
        synchronized (query) {
            query = new HashMap();
        }
    }

    public long getState() {
        long result;
        synchronized (query) {
            result = query.size();
        }
        return result;
    }

    public void shutdown() throws InterruptedException {
        monitor.setOperate(false);
        monitorThread.join();
    }

}
