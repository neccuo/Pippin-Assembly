package proj02.model;

public class Model {
	
	public static final int CODE_SIZE=1024;
	public static final int DATA_SIZE=2048;

	private CPU cpu;
	private Memory dataMemory;
	private CodeMemory codeMemory;
	private Job[] jobs;

	public Model() {
		dataMemory = new Memory(DATA_SIZE);
		codeMemory = new CodeMemory(CODE_SIZE);
		jobs = new Job[4];
		int codePartitionSize=CODE_SIZE/jobs.length;
		int dataPartitionSize=DATA_SIZE/jobs.length;
		for(int i=0;i<jobs.length;i++) {
			jobs[i] = new Job(i,i*codePartitionSize,codePartitionSize,i*dataPartitionSize,dataPartitionSize);
		}
		cpu = new CPU(codeMemory,dataMemory,jobs[0]);
	}
	
	public void changeToJob(int i) {
		cpu.switchJob(jobs[i]);
	}

	public Memory getDataMemory() { return dataMemory; }
	public CodeMemory getCodeMemory() { return codeMemory; }
	public CPU getCpu() { return cpu; }

	public void step() {
		cpu.step();
	}
	
	public void clearCurrentJob() {
		cpu.getCurrentJob().clear(codeMemory, dataMemory);
	}
	
	public void currentJobLoaded() {
		cpu.getCurrentJob().setCurrentState(Job.State.PROGRAM_LOADED);
		cpu.setInstructionPointer(cpu.getCurrentJob().getStartCodeIndex());
	}
	
	public void setCode(int objWord1,int objWord2) {
		Instruction inst = new Instruction(objWord1,objWord2);
		cpu.getCurrentJob().setCode(codeMemory, inst);
	}
	
	public void setData(int loc,int value) {
		cpu.setData(loc,value);
	}

	public int getDataSize() {
		return DATA_SIZE;
	}
	
	/* Added for GUI */
	public Job[] getJobs() { return jobs; }
}