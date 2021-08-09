package proj02.model;

public class Memory {
	private int data_size;
	private int data[]; // data is a reference to an array of integers (not initialized yet)
	private int changeIndex;
	private int minChange;
	private int maxChange;

	public Memory(int data_size) {
		this.data_size=data_size;
		data = new int[data_size]; // Create a new data array of the size specified
		changeIndex=-1; // Nothing changed yet
		resetMinMaxChange();
	}

	public int getDataSize() { return data_size; }

	public int getData(int index) { return data[index]; }

	public void setData(int index,int value) { 
		data[index]=value;
		changeIndex=index;
		if (changeIndex < minChange) minChange=changeIndex;
		if (changeIndex > maxChange) maxChange=changeIndex;
	}

	public void dump(String title) {
		dump(title,0,data_size-1);
	}

	public void dump(String title,int start,int stop) {
		boolean in0=false;
		int start0=start,stop0=start;
		System.out.println(title);
		for(int i=start;i<=stop;i++) {
			if (data[i]==0) {
				if (in0) { stop0=i; }
				else {
					in0=true;
					start0=stop0=i;
				}
			} else {
				if (in0) {
					if (start0==stop0) {
						System.out.println(String.format("   %08d           ",start0) + " :        0");
					} else {
						System.out.println(String.format("   %08d",start0) + " - " + String.format("%08d",stop0) + " :        0");
					}
					in0=false;
				}
				System.out.println(String.format("   %08d",i) + "            : " +String.format("%8d",data[i]));
			}
		}
		if (in0) {
			if (start0==stop0) {
				System.out.println(String.format("   %08d",start0) + "           :        0");
			} else {
				System.out.println(String.format("   %08d",start0) + " - " + String.format("%08d",stop0) + " :        0");
			}
		}
	}

	void clear(int startIndex,int endIndex) {
		changeIndex=-1;
		resetMinMaxChange();
		for(int i=startIndex;i<endIndex;i++) { 
			if (data[i]!=0) setData(i,0);
		}
	}


	public int[] getData() {
		return data;
	}
	
	/* The following added to support GUI */
	
	public void resetMinMaxChange() {
		minChange=data_size+1;
		maxChange=-1;
		changeIndex=-1;
	}
	
	public int getChangeIndex() { return changeIndex; }
	public int getMinChangeIndex() {return minChange; }
	public int getMaxChangeIndex() {return maxChange; }
}