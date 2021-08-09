package proj02.model;

public enum Mode {
	NOMODE(""),
	INDIRECT("@"),
	DIRECT(""),
	IMMEDIATE("#"),
	ABSOLUTE("&");
	
	private String prefix;
	
	private Mode(String prefix) {
		this.prefix = prefix;
	}
	
	public static Mode getMode(int mn) { return Mode.values()[mn]; }

	public int getModeNumber() {
		return ordinal();
	}

	public String getPrefix() {
		return prefix;
	}
	
	public static Mode getModeFromArgString(String arg) {
		if (arg.length()==0) return NOMODE;
		char mode=arg.charAt(0);
		for( Mode m : Mode.values()) {
			if (m.prefix.equals("")) continue;
			if (m.prefix.charAt(0)==mode) return m;
		}
		if (arg.matches("-?[0-9a-fA-F]+")) return Mode.DIRECT;
		return null;
	} 
}
