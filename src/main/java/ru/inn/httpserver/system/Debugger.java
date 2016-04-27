package ru.inn.httpserver.system;

import java.io.PrintStream;
import java.text.SimpleDateFormat;

import ru.inn.httpserver.server.Constants;

public class Debugger {
	
	private static boolean isEnabled = true;
	private static PrintStream out = System.out;
	private static PrintStream err = System.err;
	private static final SimpleDateFormat dateFormatter= new SimpleDateFormat("HH.mm.ss.SSS");
	
    private Debugger() {
        if (DebugLoader.INSTANCE != null) {
        	throw new IllegalStateException("Debugger already instantiated!");
        }
    }
	
	static {
		isEnabled = Constants.ENABLE_DEBUG;
	}
	
	private static class DebugLoader {
        private static final Debugger INSTANCE = new Debugger();
    }

	public static Debugger getInstance() {
		return DebugLoader.INSTANCE;
	}
	
	public synchronized void printExceptionToErrorOut(Exception e) {
		err.println(getTimestamp() + "[ERROR]: " + threadId() + "Exception stacktrace: ");
		e.printStackTrace(err);
		err.flush();
	}

	public void println(Object o) {
		if (isEnabled) {
			out.println(getTimestamp() + "[DEBUG]: " + threadId() + o.toString());
		}
	}
	
	public void notice(Object o) {
		notice(false, o);
	}
	
	public void notice(boolean isSeparatorNeeded, Object o) {
		if (isSeparatorNeeded)
			out.println();
		out.println(getTimestamp() + "[NOTICE]: " + threadId() + o.toString());

	}
	
	public void error(Object o) {
		err.println(getTimestamp() + "[ERROR]: " + threadId() + o.toString());
	}
	
	private String threadId() {
		return "[" + Thread.currentThread().getName() +"]" ;
	}
	
	private String getTimestamp() {
		return "[" + dateFormatter.format(System.currentTimeMillis()) + "]";
	}

}
