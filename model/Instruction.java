package proj02.model;

public class Instruction {
	private Opcode opcode;
	private Mode mode;
	private int arg;

	public Instruction(CodeMemory cm,int index) {
		this(cm.getInstructionWord1(index),cm.getInstructionWord2(index));
	}
	
	public Instruction(int objWord1,int objWord2) {
		opcode=Opcode.getOpcode(objWord1/16);
		mode=Mode.getMode(objWord1%16);
		arg=objWord2;
		checkInstruction();
	}

	public Instruction(int opcode, int mode, int arg) {
		this.opcode = Opcode.getOpcode(opcode);
		this.mode = Mode.getMode(mode);
		this.arg = arg;
		checkInstruction();
	}
	
	

	public Instruction(Opcode opcode, Mode mode, int arg) {
		this.opcode = opcode;
		this.mode = mode;
		this.arg = arg;
		checkInstruction();
	}

	public int objWord1() { return opcode.getOpNum()*16 + mode.getModeNumber(); }
	public int objWord2() { return arg; }

	public void execute(CPU cpu) {
		if (opcode==Opcode.STO) {
			int argVal=arg;
			if (mode==Mode.INDIRECT) argVal = cpu.getData(arg);
			opcode.execute(cpu, argVal, mode); // Special case for store!
		}
		else opcode.execute(cpu,getArgValue(cpu),mode);
	}

	public int getArgValue(CPU cpu) {
		switch(mode) {
		case NOMODE: return 0;
		case IMMEDIATE: return arg;
		case DIRECT: return cpu.getData(arg);
		case ABSOLUTE: return cpu.getData(arg);
		case INDIRECT:
			int argPtr=cpu.getData(arg);
			return cpu.getData(argPtr);
		}
		throw new InstructionErrorException("Expected a valid mode");
	}

	public String toString() {
		return opcode + " " + mode.getPrefix() + (mode!=Mode.NOMODE ?""+arg:"");
	}
	
	private void checkInstruction() {
		switch(opcode) {
		case NOP:
		case NOT:
		case HALT:
			if (mode!=Mode.NOMODE) throw new InstructionErrorException("Illegal mode in Instruction - must be no mode");
			break;
		case LOD:
		case ADD:
		case SUB:
		case MUL:
		case DIV:
		case AND:
			if ((mode!=Mode.DIRECT) && (mode!=Mode.IMMEDIATE) && (mode != Mode.INDIRECT)) throw new InstructionErrorException("Illegal mode in Instruction - must be direct or immed");
			break;
		case STO:
		case CMPL:
		case CMPZ:
			if ((mode!=Mode.DIRECT) && (mode!=Mode.INDIRECT)) throw new InstructionErrorException("Illegal mode in Instruction - must be direct");
			break;
		case JUMP:
		case JMPZ:
			if ((mode!=Mode.DIRECT) && (mode!=Mode.IMMEDIATE) && (mode!=Mode.ABSOLUTE) &&(mode!=Mode.INDIRECT)) throw new InstructionErrorException("Illegal mode in Instruction - must be direct, immed, or absolute");
			break;
		case UNUSED1:
		case UNUSED2:
			throw new InstructionErrorException("Illegal opcode of 13 or 14");
		}
		if (mode==Mode.NOMODE && arg!=0) throw new InstructionErrorException("Illegal argument in no mode Instruction");
	}
	
	public String getObjectText() {
		return Integer.toHexString(objWord1()).toUpperCase() + " " + 
				getHexArg();
	}
	
	public String getHexArg() {
		if (mode==Mode.NOMODE) return "0";
		if(arg >= 0) return Integer.toHexString(arg).toUpperCase();
		return "-" + Integer.toHexString(-arg).toUpperCase();
	}

} // End of class