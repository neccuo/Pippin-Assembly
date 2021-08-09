package proj02.model;

public class Trace {
	private static boolean trace=false;
	private static int curIp=0;
	
	static public void startTrace() { trace=true; }
	static public void stopTrace() { trace=false; }
	
	static public void setCurIp(int cip) { curIp = cip; }
	
	public static void message(String msg) {
		if (trace) System.out.println("Trace: " + msg);
	}
	
	public static void instMsg(String msg) {
		if (trace) System.out.println("Trace: " + String.format("%04d", curIp) + ": " + msg);
	}
	
	public static boolean getTrace() { return trace; }

}
