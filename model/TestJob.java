package proj02.model;

import java.io.File;

import proj02.assembler.Loader;

public class TestJob {

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
		/* Load program into job 0 */
		// Load the factorial program into code memory
		Trace.message("Loading factorial program into job 0");
		String e=Loader.load(pippin, new File("src/proj02/pexe/factorial.pexe"));
		if (!e.equals("")) {
			System.out.println("Error loading factorial program: " + e);
		}
		
		// Load the first argument into loc 0
		cpu.setData(0,argVals[0]);
		
		/* Switch to job 1, and load that job */
		pippin.changeToJob(1);
		
		// Copy the divisible by 3 program into job 1
		Trace.message("Loading divide by 3 checker into job 1");
		e=Loader.load(pippin, new File("src/proj02/pexe/div3.pexe"));
		if (!e.equals("")) {
			System.out.println("Error loading div3 program: " + e);
		}
		
		// Load the second and third arguments into loc 0 and loc 1
		cpu.setData(0,argVals[1]);
		cpu.setData(1,argVals[2]);
		
		pippin.changeToJob(2);
		// Copy the indirect factorial program into job 2
		Trace.message("Loading sumVector into job 2");
		e=Loader.load(pippin, new File("src/proj02/pexe/sumVector.pexe"));
		if (!e.equals("")) {
			System.out.println("Error loading sumVector program: " + e);
		}
			
		if (Trace.getTrace()) cm.dump("After Load / Before Execution");
		
		boolean jobRunning=true;
		while(jobRunning) {
			jobRunning=false; 
			for(int j=0;j<4;j++) { // Note.. we should have a model getNumberOfJobs method!
				pippin.changeToJob(j);
				Job job = cpu.getCurrentJob();
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
		
		// All jobs are now finished
		pippin.changeToJob(0);
		System.out.println("Job 0 results... factorial(" + argVals[0] + ") = " + cpu.getData(1) );
		
		pippin.changeToJob(1);
		System.out.println("Job 1 results... " + argVals[1] + (cpu.getData(2)==1 ? " is" : " is not") + " divisible by 3. " 
				+ argVals[2] + (cpu.getData(3)==1 ? " is " : " is not ") + "divisible by 3" + 
				(cpu.getData(4)==1? " both are " : " but both are not ") + "divisible by 3.");
		
		pippin.changeToJob(2);
		System.out.println("Job 2 results... sumVector(5,4,3,2,1) = " + cpu.getData(0) );

	}

}
