package proj02.model;

public class CodeMemory {
	private int code_size;
	private Memory backingStore;

	public CodeMemory(int code_size) {
		this.code_size = code_size;
		backingStore = new Memory(code_size*2);
	}

	public void setCode(int index,Instruction inst) {
		backingStore.setData(2*index,inst.objWord1());
		backingStore.setData(2*index+1,inst.objWord2());
	}
	
	public Instruction getCode(int index) {
		return new Instruction(this,index);
	}

	int getInstructionWord1(int index) { return backingStore.getData(2*index); }
	int getInstructionWord2(int index) { return backingStore.getData(2*index+1); }

	public void dump(String title) {
		dump(title,0,code_size-1);
	}

	public boolean isEmpty(int index) {
		if (getInstructionWord1(index)==0 && getInstructionWord2(index)==0) return true;
		return false;
	}

	public void dump(String title,int start,int stop) {
		System.out.println(title);
		boolean inEmpty=false;
		int estart=start,estop=start;
		for(int i=start;i<=stop;i++) {
			if (isEmpty(i)) {
				if (inEmpty) estop=i;
				else {
					inEmpty=true;
					estart=estop=i;
				}
			} else {
				if (inEmpty) {
					if (estart==estop) System.out.println("   " + String.format("%05d",estart) + " : ---");
					else System.out.println("      ---- " + String.format("%05d",estart) + " - " + String.format("%05d",estop) + " unused --- ");
					inEmpty=false;
				}
				System.out.println(String.format("   %05d",i) + " : " + (new Instruction(this,i)));
			}
		}
		if (inEmpty) {
			if (estart==estop) System.out.println(String.format("   %05d",estart) + " : ---");
			else System.out.println("      ---- " + String.format("%05d",estart) + " - " + String.format("%05d",estop) + " unused --- ");
		}
	}
	
	public void clear(int fromIndex,int toIndex) {
		backingStore.clear(2*fromIndex, 2*toIndex);
	}


} // End of class