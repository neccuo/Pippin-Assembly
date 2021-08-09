package proj02.model;

public class Job {
	
	/* Create an enumeration that defines the possible states of a job
	 * Since this is static and public, it will be available both inside and outside the class.
	 * We can reference specific values outside of the class by using Job.State.<enum_value>
	 */
	public static enum State {
		NOTHING_LOADED, PROGRAM_LOADED, PROGRAM_HALTED
	};
	
	private final int id;
	private final int startCodeIndex;
	private final int codePartitionSize;
	private int codeSize;
	private final int startDataIndex;
	private final int dataPartitionSize;
	private int currentIP;
	private int currentAcc;
	private State currentState;

	public Job(int id, int startCodeIndex, int codePartitionSize,int startDataIndex,int dataPartitionSize) {
		this.id = id;
		this.startCodeIndex = startCodeIndex;
		this.codePartitionSize = codePartitionSize;
		this.startDataIndex = startDataIndex;
		this.dataPartitionSize = dataPartitionSize;
		this.currentIP = startCodeIndex;
		currentState=State.NOTHING_LOADED;
	}

	public int getCurrentIP() {
		return currentIP;
	}

	public void setCurrentIP(int currentIP) {
		this.currentIP = currentIP;
	}

	public int getCurrentAcc() {
		return currentAcc;
	}

	public void setCurrentAcc(int currentAcc) {
		this.currentAcc = currentAcc;
	}

	public State getCurrentState() {
		return currentState;
	}

	public void setCurrentState(State currentState) {
		this.currentState = currentState;
	}

	public int getId() {
		return id;
	}

	public int getStartCodeIndex() {
		return startCodeIndex;
	}

	public int getCodePartitionSize() {
		return codePartitionSize;
	}

	public int getStartDataIndex() {
		return startDataIndex;
	}
	
	public void setCode(CodeMemory cm,Instruction instr) {
		/* Write instruction at next available code memory location */
		setCode(cm,codeSize,instr);
	}

	public void setCode(CodeMemory cm,int loc,Instruction instr) {
		if (loc >= codePartitionSize) 
			throw new IllegalArgumentException("Code location exceeds Job code partition size");
		if (loc>=codeSize) codeSize=loc+1;
		cm.setCode(startCodeIndex + loc, instr);
	}
	
	public void clear(CodeMemory cm,Memory dm) {
		cm.clear(startCodeIndex, startCodeIndex+codeSize);
		dm.clear(startDataIndex, startDataIndex + dataPartitionSize);
		currentIP = startCodeIndex;
		codeSize=0;
		currentAcc = 0;
		setCurrentState(State.NOTHING_LOADED);
	}
	
	public void validInstructionPointer(int ip) {
		if (ip<startCodeIndex) throw new CodeAccessException();
		if (ip>=startCodeIndex + codeSize) throw new CodeAccessException();
	}
	
	public void validDataPointer(int loc) {
		if (loc < startDataIndex) 
			throw new MemoryAccessException("Invalid location: " + loc + " is less than partition start: " + startDataIndex);
		if (loc > startDataIndex + dataPartitionSize) 
			throw new MemoryAccessException("Invalid location: " + loc +" is greater than partition end:" + (startDataIndex+dataPartitionSize));
	}
	
	public int getCodeSize() {
		return codeSize; // added for view
	}

	
	@Override
	public String toString() {
		return "Job [id=" + id + "]";
	}
	
	/* Added for GUI */
	public int getDataPartitionSize() {
		return dataPartitionSize;
	}
}
