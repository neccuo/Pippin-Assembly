package proj02.model;

public class TestExceptions {

	public static void main(String[] args) {
		boolean trace=false;
		int argVals[] = {7,214,312}; // Initialize with defaults

		for(int i=0;i<args.length;i++) {
			if (args[i].equals("-trace")) {
				Trace.startTrace();
				trace=true;
				continue;
			}
			if (trace && i>4) break;
			else if (i>3) break;
			
			if (args[i].matches("\\d+")) {
				argVals[trace?i-1:i] = Integer.parseInt(args[i]);
			}
		}

		Model pippin = new Model();
		CodeMemory cm=pippin.getCodeMemory();
		Memory dm=pippin.getDataMemory();
		CPU cpu = pippin.getCpu();
		Job job = cpu.getCurrentJob();
		
		/* Load program into job 0 */
		Trace.message("Loading divide by 1 program into job 0");
		job.setCode(cm,new Instruction(1,3,1)); 
		job.setCode(cm,new Instruction(2,2,0)); 
		job.setCode(cm,new Instruction(1,3,1)); 
		job.setCode(cm,new Instruction(6,2,0)); 
		job.setCode(cm,new Instruction(2,2,0)); 
		job.setCode(cm,new Instruction(15,0,0));		
		job.setCurrentState(Job.State.PROGRAM_LOADED);
		
		/* Switch to job 1, and load that job */
		pippin.changeToJob(1);
		job = cpu.getCurrentJob();
		
		Trace.message("Loading instruction error program into job 1");
		job.setCode(cm,new Instruction(1,3,0)); 
		job.setCode(cm,new Instruction(2,2,0)); 
		job.setCode(cm,new Instruction(1,3,1)); 
		job.setCode(cm,new Instruction(6,2,0)); 
		job.setCode(cm,new Instruction(2,2,0)); 
		job.setCode(cm,new Instruction(15,0,0));	
		job.setCurrentState(Job.State.PROGRAM_LOADED);
		
		/* Switch to job 2, and load that job */
		pippin.changeToJob(2);
		job = cpu.getCurrentJob();
		
		Trace.message("Loading code access error program into job 2");
		job.setCode(cm,new Instruction(1,3,0)); 
		job.setCode(cm,new Instruction(2,2,0)); 
		job.setCode(cm,new Instruction(11,3,-8)); 
		job.setCode(cm,new Instruction(15,0,0));	
		job.setCurrentState(Job.State.PROGRAM_LOADED);
		
		/* Switch to job 3, and load that job */
		pippin.changeToJob(3);
		job = cpu.getCurrentJob();
		
		Trace.message("Loading memory access error program into job 3");
		job.setCode(cm,new Instruction(1,3,0)); 
		job.setCode(cm,new Instruction(2,2,0)); 
		job.setCode(cm,new Instruction(1,2,2498)); 
		job.setCode(cm,new Instruction(15,0,0));	
		job.setCurrentState(Job.State.PROGRAM_LOADED);
		
		if (Trace.getTrace()) cm.dump("After Load / Before Execution");
		
		boolean jobRunning=true;
		while(jobRunning) {
			jobRunning=false; 
			for(int j=0;j<4;j++) { // Note.. we should have a model getNumberOfJobs method!
				pippin.changeToJob(j);
				job = cpu.getCurrentJob();
				if (job.getCurrentState()==Job.State.PROGRAM_LOADED) {
					jobRunning=true;
					// Give this job 10 cycles
					for(int s=0;s<10 && job.getCurrentState()== Job.State.PROGRAM_LOADED; s++) {
						cpu.step();
					}
				}
				
			}
		}
		
		if (Trace.getTrace()) dm.dump("After Execution");
		
	}

}
