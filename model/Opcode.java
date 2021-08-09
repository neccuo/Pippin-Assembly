package proj02.model;

public enum Opcode {
	NOP {
		public void execute(CPU cpu,int argVal,Mode mode) { 
			this.traceMsg("");
		}
	},
	LOD {
		public void execute(CPU cpu,int argVal,Mode mode) {
			cpu.setAccumulator(argVal);
			traceMsg("ACCUM<-" + argVal);
		}
	},
	STO {
		public void execute(CPU cpu,int argVal,Mode mode) {	
			cpu.setData(argVal);
			traceMsg("ACCUM=" + cpu.getAccumulator() + " -> data memory @ " + argVal);
		}
	},
	ADD {
		public void execute(CPU cpu,int argVal,Mode mode) {
			int op1=cpu.getAccumulator();
			cpu.setAccumulator(op1+argVal);
			traceMsg("ACCUM<-(" + op1 + " + " + argVal + ") = " +(op1+argVal));
		}
	},
	SUB {
		public void execute(CPU cpu,int argVal,Mode mode) {
			int op1=cpu.getAccumulator();
			cpu.setAccumulator(op1 - argVal);
			traceMsg("ACCUM<-(" + op1 + " - " + argVal + ") = " +(op1-argVal));
		}
	},
	MUL { 
		public void execute(CPU cpu,int argVal,Mode mode) {
			int op1=cpu.getAccumulator();
			cpu.setAccumulator(op1*argVal);
			traceMsg("ACCUM<-(" + op1 + " * " + argVal + ") = " +(op1*argVal));
		}
	},
	DIV { 
		public void execute(CPU cpu,int argVal,Mode mode) {
			int op1=cpu.getAccumulator();
			if (argVal==0) throw new DivideByZeroException("Divide by Zero");
			cpu.setAccumulator(op1/argVal);
			traceMsg("ACCUM<-(" + op1 + " / " + argVal + ") = " +(op1/argVal));
		}
	},
	AND {
		public void execute(CPU cpu,int argVal,Mode mode) {
			int op1=cpu.getAccumulator();
			int newValue = ((op1 != 0) && (argVal != 0) ? 1 : 0);
			cpu.setAccumulator(newValue);
			traceMsg("ACCUM<-(" + op1 + " && " + argVal + ") = " +newValue);
		}
	},
	NOT {
		public void execute(CPU cpu,int argVal,Mode mode) {
			int op1=cpu.getAccumulator();
			int newValue = ((op1 != 0) ? 0 : 1);
			cpu.setAccumulator(newValue);
			traceMsg("ACCUM<-(!" + op1 + ") = " +newValue);
		}
	},
	CMPL {
		public void execute(CPU cpu,int argVal,Mode mode) {
			int newValue = (argVal<0) ? 1 : 0;
			cpu.setAccumulator(newValue);
			traceMsg("ACCUM<-(" + argVal + " < 0)  = " +newValue);
		}
	},
	CMPZ {
		public void execute(CPU cpu,int argVal,Mode mode) {
			int newValue = (argVal==0) ? 1 : 0;
			cpu.setAccumulator(newValue);
			traceMsg("ACCUM<-(" + argVal + " == 0) = " +newValue);
		}
	},
	JUMP {
		public void execute(CPU cpu,int argVal,Mode mode) {
			if (mode==Mode.ABSOLUTE) {
				cpu.setAbsoluteIP(argVal);
				traceMsg("IP<- (Absolute)" + argVal + " = " + cpu.getInstructionPointer());
			} else { // Mode may be direct or immediate
				int oldIp = cpu.getInstructionPointer();
				cpu.setInstructionPointer(oldIp -1 + argVal);
				traceMsg("IP<- ip: " + (oldIp-1) + " + " + argVal + " = " + cpu.getInstructionPointer());
			}
		}
	},
	JMPZ {
		public void execute(CPU cpu,int argVal,Mode mode) {
			if (0==cpu.getAccumulator()) {
				if (mode==Mode.ABSOLUTE) {
					cpu.setAbsoluteIP(argVal);
					traceMsg("IP<- (Absolute)" + argVal + " = " + cpu.getInstructionPointer());
				} else { // Mode may be direct or immediate
					int oldIp = cpu.getInstructionPointer();
					cpu.setInstructionPointer(oldIp - 1 + argVal);
					traceMsg("IP<- ip: " + (oldIp-1) + " + " + argVal + " = " + cpu.getInstructionPointer());
				}
			} else {
				traceMsg("No jump, ACCUM!= 0");
			}
		}
	},
	UNUSED1 {
		public void execute(CPU cpu,int argVal,Mode mode) {
			throw new UnsupportedOperationException("Opcode 13 is unsupported");
		}
	},
	UNUSED2 {
		public void execute(CPU cpu,int argVal,Mode mode) {
			throw new UnsupportedOperationException("Opcode 14 is unsupported");
		}
	},
	HALT {
		public void execute(CPU cpu,int argVal,Mode mode) {
			traceMsg("");
			cpu.halt();
		}
	};
	
	public abstract void execute(CPU cpu,int argVal,Mode mode);
	
	public int getOpNum() { return ordinal(); }
	
	
	public static Opcode getOpcode(int op) { return Opcode.values()[op]; }
	
	// Note... if we have a string, we can use Opcode.valueOf(string) to get the opcode
	
	public void traceMsg(String info) {
		Trace.instMsg(String.format("%4s", this) + " : " + info);
	}
	
	
	
}
