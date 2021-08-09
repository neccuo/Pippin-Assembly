package proj02.model;

public class CPU {
	private int accumulator;
	private int instructionPointer;
	private int currentInstruction;
	private int memoryBase;
	private CodeMemory codeMemory;
	private Memory dataMemory;
	private Job currentJob;

	public CPU(CodeMemory codeMemory, Memory dataMemory,Job currentJob) {
		this.codeMemory=codeMemory;
		this.dataMemory=dataMemory;
		this.currentJob = currentJob;
		// The following are not required... Java initializes fields to 0 for us
		// accumulator=0;
		// memoryBase=0;
		// instructionPointer=0;
		this.currentInstruction=-1;
	}

	public Job getCurrentJob() {
		return currentJob;
	}

	public int getData(int loc) {
		currentJob.validDataPointer(memoryBase + loc);
		return dataMemory.getData(memoryBase + loc);
	}
	
	public void setData(int loc,int value) {
		currentJob.validDataPointer(memoryBase+loc);
		dataMemory.setData(memoryBase + loc, value);
	}
	
	public void setData(int loc) {
		setData(loc,accumulator);
	}
	
	public void setAbsoluteIP(int offset) {
		instructionPointer = currentJob.getStartCodeIndex() + offset;
	}
	
	public void switchJob(Job newJob) {
		currentJob.setCurrentAcc(accumulator);
		currentJob.setCurrentIP(instructionPointer);
		accumulator=newJob.getCurrentAcc();
		instructionPointer = newJob.getCurrentIP();
		memoryBase = newJob.getStartDataIndex();
		currentJob = newJob;
		Trace.message("Switched to job " + newJob);
	}

	public int getAccumulator() { return accumulator; }
	public void setAccumulator(int newVal) { accumulator=newVal; }

	public int getInstructionPointer() { return instructionPointer; }
	public void setInstructionPointer(int newVal) { instructionPointer=newVal; }

	public int getMemoryBase() { return memoryBase; }
	public void setMemoryBase(int newVal) { memoryBase=newVal; }

	public Memory getDataMemory() { return dataMemory; }
	public CodeMemory getCodeMemory() { return codeMemory; }

	public void halt() {
		currentJob.setCurrentState(Job.State.PROGRAM_HALTED);
	}

	public void step() {
		if (currentJob.getCurrentState()==Job.State.PROGRAM_LOADED) {
			try {
				  	currentJob.validInstructionPointer(instructionPointer);
					Instruction instruction = new Instruction(codeMemory,instructionPointer);
					Trace.setCurIp(instructionPointer);
					currentInstruction=instructionPointer;
					instructionPointer++;
					instruction.execute(this);
				} catch(Exception e) {
				  currentJob.setCurrentState(Job.State.PROGRAM_HALTED);
				  // System.out.println("Pippin Exception: " + e + " occured... Job " + currentJob + " halted.");
				  throw e; // Uncomment to stop Pippin altogether
				}
		}
	}

	public void run() {
		while(currentJob.getCurrentState()==Job.State.PROGRAM_LOADED) step();
	}
	
	/* Added for GUI */
	public void clearCurrentJob( ) {
		currentJob.clear(codeMemory, dataMemory);
	}
	
	public int getCurrentInstruction() {
		return currentInstruction;
	}

}