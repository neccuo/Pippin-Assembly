package proj02.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class InstructionTester {

	Model machine = new Model(); 
	Memory dataMemory = machine.getDataMemory();
	int[] dataCopy = new int[machine.getDataSize()];
	CPU cpu;
	int accInit;
	int ipInit;
	int offsetInit;
	Random rand = new Random();
	
	public void preSet(int loc,int val) {
		int rloc = cpu.getMemoryBase() + loc;
		dataCopy[rloc]=val;
		dataMemory.setData(rloc, val);
	}
	
	public int preSetDirect(int val) {
		// Randomly choose an index between 0 and codeSize
		int index=rand.nextInt(cpu.getCurrentJob().getCodePartitionSize());
		preSet(index,val);
		return index;
	}
	
	public int preSetIndirect(int val) {
		int index=rand.nextInt(cpu.getCurrentJob().getCodePartitionSize());
		int index2=preSetDirect(val);
		if (index2==index) index++;
		preSet(index,index2);
		return index;
	}
	
	public void checkModel(int accum,int ip) {
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, dataMemory.getData(),"Data Unchanged"),
				//Test instruction pointer incremented
				() -> assertEquals(ip, cpu.getInstructionPointer(), "Instruction pointer correct"),
				//Test accumulator untouched
				() -> assertEquals(accum, cpu.getAccumulator(), "Accumulator correct")
		);
	}
	
	@BeforeEach
	public void setup() {
		for (int i = 0; i < machine.getDataSize(); i++) {
			dataCopy[i] = -5*machine.getDataSize() + 10*i;
			dataMemory.setData(i, -5*machine.getDataSize() + 10*i);
			// Initially the machine will contain a known spread
			// of different numbers: 
			// -10240, -10230, -10220, ..., 0, 10, 20, ..., 10230 
			// This allows us to check that the Model.Instructions do 
			// not corrupt machine unexpectedly.
			// 0 is at index 1024
		}
		accInit = 30;
		machine.changeToJob(1);
		cpu=machine.getCpu();
		offsetInit = cpu.getMemoryBase();
		ipInit = cpu.getInstructionPointer() + 30;
		cpu.setAccumulator(accInit);
		cpu.setInstructionPointer(ipInit);
		// cpu.setMemoryBase(offsetInit);
	}

	@Test
	public void testNOP(){
		new Instruction(Opcode.NOP,Mode.NOMODE,0).execute(cpu);
		checkModel(accInit,ipInit);
	}

	@Test 
	// Test whether NOP throws exception with immediate addressing mode
	public void testNOPimmediateMode() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.NOP,Mode.IMMEDIATE,0).execute(cpu)); 
		assertEquals("Illegal mode in Instruction - must be no mode", exception.getMessage());
	}
	 
	@Test 
	// Test whether NOP throws exception with direct addressing mode
	public void testNOPdirectMode() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.NOP,Mode.DIRECT,0).execute(cpu));
		assertEquals("Illegal mode in Instruction - must be no mode", exception.getMessage());

	}
	
	@Test 
	// Test whether NOP throws exception with indirect addressing mode
	public void testNOPindirectMode() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.NOP,Mode.INDIRECT,0).execute(cpu));
		assertEquals("Illegal mode in Instruction - must be no mode", exception.getMessage());

	}
	
	@Test
	// Test whether load is correct with immediate addressing
	public void testLODimmediate(){
		cpu.setAccumulator(27);
		int arg = 12;
		// should load 12 into the accumulator
		new Instruction(Opcode.LOD,Mode.IMMEDIATE,arg).execute(cpu);
		checkModel(arg,ipInit);
	}

	@Test
	// Test whether load is correct with direct addressing
	public void testLODdirect(){
		cpu.setAccumulator(27);
		int arg = 12;
		new Instruction(Opcode.LOD,Mode.DIRECT,arg).execute(cpu);
		checkModel(cpu.getData(arg),ipInit);
	}

	@Test
	// Test whether load is correct with direct addressing
	public void testLODindirect() {
		cpu.setAccumulator(-1);
		int arg=preSetIndirect(338);
		new Instruction(Opcode.LOD,Mode.INDIRECT,arg).execute(cpu);
		checkModel(338,ipInit);
	}	

	@Test 
	// Test whether LOD throws exception with null addressing mode
	public void testLODnullArg() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.LOD,Mode.NOMODE,0).execute(cpu)); 
		assertEquals("Illegal mode in Instruction - must be direct or immed", exception.getMessage());

	}
	
	@Test
	// Test whether store is correct with direct addressing
	public void testSTOdirect() {
		int arg = 12;
		cpu.setAccumulator(567);
		new Instruction(Opcode.STO,Mode.DIRECT,arg).execute(cpu);
		dataCopy[offsetInit + 12] = 567;
		checkModel(567,ipInit);
	}

	@Test
	// Test whether store is correct with indirect addressing
	public void testSTOindirect() {
		int arg = 25;
		cpu.setAccumulator(567);
		preSet(25,41);
		int offset1 = 41 + cpu.getMemoryBase();
		dataCopy[offset1] = 567;
		new Instruction(Opcode.STO,Mode.INDIRECT,arg).execute(cpu);
		checkModel(567,ipInit);
	}

	@Test 
	// Test whether STO throws exception with null addressing
	public void testSTOnullArg() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.STO,Mode.NOMODE,0).execute(cpu)); 	
		assertEquals("Illegal mode in Instruction - must be direct", exception.getMessage());
	}
	
	@Test 
	// Test whether STO throws exception with immediate addressing
	public void testSTOimmediateArg() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.STO,Mode.IMMEDIATE,0).execute(cpu)); 	
		assertEquals("Illegal mode in Instruction - must be direct", exception.getMessage());

	}
	
	@Test 
	// this test checks whether the add is done correctly, when
	// addressing is immediate
	public void testADDimmediate() {
		int arg = 12; 
		cpu.setAccumulator(200); 
		new Instruction(Opcode.ADD,Mode.IMMEDIATE,arg).execute(cpu);
		// should have added 12 to accumulator
		checkModel(200+12,ipInit);
	}

	@Test 
	// this test checks whether the add is done correctly, when
	// addressing is direct
	public void testADDdirect() {
		int arg = preSetDirect(540); 
		cpu.setAccumulator(250);
		new Instruction(Opcode.ADD,Mode.DIRECT,arg).execute(cpu);
		checkModel(250+540,ipInit);
	}

	@Test 
	// this test checks whether the add is done correctly, when
	// addressing is indirect
	public void testADDindirect() {
		cpu.setAccumulator(250);
		new Instruction(Opcode.ADD,Mode.INDIRECT,preSetIndirect(-3840)).execute(cpu);
		checkModel(250+(-3840),ipInit);
	}

	@Test 
	// Test whether ADD throws exception with null addressing mode
	public void testADDnullArg() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.ADD,Mode.NOMODE,0).execute(cpu)); 
		assertEquals("Illegal mode in Instruction - must be direct or immed", exception.getMessage());

	}
	
	@Test 
	// this test checks whether the subtraction is done correctly, when
	// addressing is immediate
	public void testSUBimmediate() {
		int arg = 12; 
		cpu.setAccumulator(200);
		new Instruction(Opcode.SUB,Mode.IMMEDIATE,arg).execute(cpu);
		checkModel(200-12,ipInit);
	}

	@Test 
	// this test checks whether the subtraction is done correctly, when
	// addressing is direct
	public void testSUBdirect() {
		cpu.setAccumulator(250);
		new Instruction(Opcode.SUB,Mode.DIRECT,preSetDirect(8120)).execute(cpu);
		checkModel(250-8120,ipInit);
	}

	@Test 
	// this test checks whether the subtraction is done correctly, when
	// addressing is indirect
	public void testSUBindirect() {
		cpu.setAccumulator(250);
		new Instruction(Opcode.SUB,Mode.INDIRECT,preSetIndirect(-3840)).execute(cpu);
		checkModel(250-(-3840),ipInit);
	}

	@Test 
	// Test whether SUB throws exception with null addressing mode
	public void testSUBnullArg() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.SUB,Mode.NOMODE,0).execute(cpu));
		assertEquals("Illegal mode in Instruction - must be direct or immed", exception.getMessage());

	}
	
	@Test 
	// this test checks whether the multiplication is done correctly, when
	// addressing is immediate
	public void testMULimmediate() {
		int arg = 12; 
		cpu.setAccumulator(200);
		new Instruction(Opcode.MUL,Mode.IMMEDIATE,arg).execute(cpu);
		checkModel(200*12,ipInit);
	}

	@Test 
	// this test checks whether the multiplication is done correctly, when
	// addressing is direct
	public void testMULdirect() {
		cpu.setAccumulator(250);
		new Instruction(Opcode.MUL,Mode.DIRECT,preSetDirect(-8120)).execute(cpu);
		checkModel(250*(-8120),ipInit);
	}

	@Test 
	// this test checks whether the multiplication is done correctly, when
	// addressing is indirect
	public void testMULindirect() {
		cpu.setAccumulator(250);
		new Instruction(Opcode.MUL,Mode.INDIRECT,preSetIndirect(-3840)).execute(cpu);
		checkModel(250*(-3840),ipInit);
	}

	@Test 
	// Test whether MUL throws exception with null addressing mode
	public void testMULnullArg() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.MUL,Mode.NOMODE,0).execute(cpu)); 
		assertEquals("Illegal mode in Instruction - must be direct or immed", exception.getMessage());

	}
	
	@Test 
	// this test checks whether the division is done correctly, when
	// addressing is immediate
	public void testDIVimmediate() {
		int arg = 12; 
		cpu.setAccumulator(200);
		new Instruction(Opcode.DIV,Mode.IMMEDIATE,arg).execute(cpu);
		checkModel(200/12,ipInit);
	}

	@Test 
	// this test checks whether the division is done correctly, when
	// addressing is direct
	public void testDIVdirect() { 
		cpu.setAccumulator(1024011);
		new Instruction(Opcode.DIV,Mode.DIRECT,preSetDirect(-8120)).execute(cpu);
		checkModel(1024011/(-8120),ipInit);
	}

	@Test 
	// this test checks whether the division is done correctly, when
	// addressing is indirect
	public void testDIVindirect() {
		cpu.setAccumulator(400000);
		new Instruction(Opcode.DIV,Mode.INDIRECT,preSetIndirect(-3840)).execute(cpu);
		checkModel(400000/(-3840),ipInit);
	}

	@Test 
	// Test whether DIV throws exception with null addressing mode
	public void testDIVnullArg() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.DIV,Mode.NOMODE,0).execute(cpu));
		assertEquals("Illegal mode in Instruction - must be direct or immed", exception.getMessage());

	}
	
	@Test 
	// Test whether DIV throws divide by zero exception with immediate addressing mode
	public void testDIVzerodivisionImmed() {
		Throwable exception = assertThrows(DivideByZeroException.class,
				() -> new Instruction(Opcode.DIV,Mode.IMMEDIATE,0).execute(cpu)); 	
		assertEquals("Divide by Zero", exception.getMessage());

	}

	@Test 
	// Test whether DIV throws divide by zero exception with direct addressing mode
	public void testDIVzerodivisionDirect() {
		Throwable exception = assertThrows(DivideByZeroException.class,
				() -> new Instruction(Opcode.DIV,Mode.DIRECT,preSetDirect(0)).execute(cpu)); 
		assertEquals("Divide by Zero", exception.getMessage());

	}

	@Test 
	// Test whether DIV throws divide by zero exception with indirect addressing mode
	public void testDIVzerodivisionIndirect() {
		Throwable exception = assertThrows(DivideByZeroException.class,
				() -> new Instruction(Opcode.DIV,Mode.INDIRECT,preSetIndirect(0)).execute(cpu));
		assertEquals("Divide by Zero", exception.getMessage());

	}

	@Test 
	// Check AND when accum and arg equal to 0 gives false
	// addressing is immediate
	public void testANDimmediateAccEQ0argEQ0() {
		int arg = 0;
		cpu.setAccumulator(0);
		new Instruction(Opcode.AND,Mode.IMMEDIATE,arg).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check AND when accum and arg equal to 0 gives false
	// addressing is immediate
	public void testANDimmediateAccLT0argEQ0() {
		int arg = 0;
		cpu.setAccumulator(-1);
		new Instruction(Opcode.AND,Mode.IMMEDIATE,arg).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check AND when accum > 0 and arg equal to 0 gives false
	// addressing is immediate
	public void testANDimmediateAccGT0argEQ0() {
		int arg = 0;
		cpu.setAccumulator(1);
		new Instruction(Opcode.AND,Mode.IMMEDIATE,arg).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check AND when accum = 0 and arg < 0 gives false
	// addressing is immediate
	public void testANDimmediateAccEQ0argLT0() {
		int arg = -1;
		cpu.setAccumulator(0);
		new Instruction(Opcode.AND,Mode.IMMEDIATE,arg).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check AND when accum < 0 and arg < 0 gives true
	// addressing is immediate
	public void testANDimmediateAccLT0argLT0() {
		int arg = -1;
		cpu.setAccumulator(-1);
		new Instruction(Opcode.AND,Mode.IMMEDIATE,arg).execute(cpu);
		checkModel(1,ipInit);
	}

	@Test 
	// Check AND when accum = 0 and arg > 0 gives false
	// addressing is immediate
	public void testANDimmediateAccEQ0argGT0() {
		int arg = 1;
		cpu.setAccumulator(0);
		new Instruction(Opcode.AND,Mode.IMMEDIATE,arg).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check AND when accum > 0 and arg > 0 gives true
	// addressing is immediate
	public void testANDimmediateAccGT0argGT0() {
		int arg = 0;
		cpu.setAccumulator(1);
		new Instruction(Opcode.AND,Mode.IMMEDIATE,arg).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check AND when accum < 0 and arg > 0 gives true
	// addressing is immediate
	public void testANDimmediateAccLT0argGT0() {
		int arg = 1;
		cpu.setAccumulator(-1);
		new Instruction(Opcode.AND,Mode.IMMEDIATE,arg).execute(cpu);
		checkModel(1,ipInit);
	}

	@Test 
	// Check AND when accum > 0 and arg > 0 gives true
	// addressing is immediate
	public void testANDimmediateAccGT0argLT0() {
		int arg = -1;
		cpu.setAccumulator(1);
		new Instruction(Opcode.AND,Mode.IMMEDIATE,arg).execute(cpu);
		checkModel(1,ipInit);
	}
	
	@Test 
	// Check AND when accum direct mem equal to 0 gives false
	// addressing is direct
	public void testANDdirectAccEQ0memEQ0() {
		cpu.setAccumulator(0);
		new Instruction(Opcode.AND,Mode.DIRECT,preSetDirect(0)).execute(cpu); 
		checkModel(0,ipInit);
	}

	@Test 
	// Check AND when accum < 0 direct mem equal to 0 gives false
	// addressing is direct
	public void testANDdirectAccLT0memEQ0() {
		cpu.setAccumulator(-1);
		new Instruction(Opcode.AND,Mode.DIRECT,preSetDirect(0)).execute(cpu); 
		checkModel(0,ipInit);
	}
	
	@Test
	// Check AND when accum > 0 direct mem equal to 0 gives false
	// addressing is direct
	public void testANDdirectAccGT0memEQ0() {
		cpu.setAccumulator(1);
		new Instruction(Opcode.AND,Mode.DIRECT,preSetDirect(0)).execute(cpu); 
		checkModel(0,ipInit);
	}

	@Test 
	// Check AND when accum = 0 direct mem < 0 gives false
	// addressing is direct
	public void testANDdirectAccEQ0memLT0() {
		cpu.setAccumulator(0);
		new Instruction(Opcode.AND,Mode.DIRECT,preSetDirect(-10)).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check AND when accum < 0 direct mem < 0 gives true
	// addressing is direct
	public void testANDdirectAccLT0memLT0() {
		cpu.setAccumulator(-1);
		new Instruction(Opcode.AND,Mode.DIRECT,preSetDirect(-10)).execute(cpu);
		checkModel(1,ipInit);
	}

	@Test 
	// Check AND when accum > 0 direct mem < 0 gives true
	// addressing is direct
	public void testANDdirectAccGT0memLT0() {
		cpu.setAccumulator(1);
		new Instruction(Opcode.AND,Mode.DIRECT,preSetDirect(-10)).execute(cpu);
		checkModel(1,ipInit);
	}

	@Test 
	// Check AND when accum = 0 direct mem > 0 gives false
	// addressing is direct
	public void testANDdirectAccEQ0memGT0() {
		cpu.setAccumulator(0);
		new Instruction(Opcode.AND,Mode.DIRECT,preSetDirect(100)).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check AND when accum < 0 direct mem > 0 gives true
	// addressing is direct
	public void testANDdirectAccLT0memGT0() {
		cpu.setAccumulator(-1);
		new Instruction(Opcode.AND,Mode.DIRECT,preSetDirect(45)).execute(cpu);
		checkModel(1,ipInit);
	}

	@Test 
	// Check AND when accum > 0 direct mem > 0 gives true
	// addressing is direct
	public void testANDdirectAccGT0memGT0() {
		cpu.setAccumulator(1);
		new Instruction(Opcode.AND,Mode.DIRECT,preSetDirect(1)).execute(cpu);
		checkModel(1,ipInit);
	}

	@Test 
	// Check AND when accum indirect mem equal to 0 gives false
	// addressing is indirect
	public void testANDindirectAccEQ0memEQ0() {
		cpu.setAccumulator(0);
		new Instruction(Opcode.AND,Mode.INDIRECT,preSetIndirect(0)).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check AND when accum < 0 indirect mem equal to 0 gives false
	// addressing is indirect
	public void testANDindirectAccLT0memEQ0() {
		cpu.setAccumulator(-1);
		new Instruction(Opcode.AND,Mode.INDIRECT,preSetIndirect(0)).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check AND when accum > 0 indirect mem equal to 0 gives false
	// addressing is indirect
	public void testANDindirectAccGT0memEQ0() {
		cpu.setAccumulator(1);
		new Instruction(Opcode.AND,Mode.INDIRECT,preSetIndirect(0)).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check AND when accum = 0 indirect mem < 0 gives false
	// addressing is indirect
	public void testANDindirectAccEQ0memLT0() {
		cpu.setAccumulator(0);
		new Instruction(Opcode.AND,Mode.INDIRECT,preSetIndirect(-10)).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check AND when accum < 0 indirect mem < 0 gives true
	// addressing is indirect
	public void testANDindirectAccLT0memLT0() {
		cpu.setAccumulator(-1);
		new Instruction(Opcode.AND,Mode.INDIRECT,preSetIndirect(-83)).execute(cpu);
		checkModel(1,ipInit);
	}

	@Test 
	// Check AND when accum > 0 indirect mem < 0 gives true
	// addressing is indirect
	public void testANDindirectAccGT0memLT0() {
		cpu.setAccumulator(1);
		new Instruction(Opcode.AND,Mode.INDIRECT,preSetIndirect(-14)).execute(cpu);
		checkModel(1,ipInit);
	}

	@Test 
	// Check AND when accum = 0 indirect mem > 0 gives false
	// addressing is indirect
	public void testANDindirectAccEQ0memGT0() {
		cpu.setAccumulator(0);
		new Instruction(Opcode.AND,Mode.INDIRECT,preSetIndirect(23)).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check AND when accum < 0 indirect mem > 0 gives true
	// addressing is indirect
	public void testANDindirectAccLT0memGT0() {
		cpu.setAccumulator(-1);
		new Instruction(Opcode.AND,Mode.INDIRECT,preSetIndirect(1)).execute(cpu);
		checkModel(1,ipInit);
	}

	@Test 
	// Check AND when accum > 0 indirect mem > 0 gives true
	// addressing is indirect
	public void testANDindirectAccGT0memGT0() {
		cpu.setAccumulator(1);
		new Instruction(Opcode.AND,Mode.INDIRECT,preSetIndirect(12)).execute(cpu);
		checkModel(1,ipInit);
	}

	@Test 
	// Test whether AND throws exception with null addressing mode
	public void testANDnullArg() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.AND,Mode.NOMODE,0).execute(cpu));	
		assertEquals("Illegal mode in Instruction - must be direct or immed", exception.getMessage());

	}
	
	@Test 
	// Check NOT greater than 0 gives false
	// there is no argument and mode is null
	public void testNOTaccGT0() {
		cpu.setAccumulator(1);
		new Instruction(Opcode.NOT,Mode.NOMODE,0).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check NOT equal to 0 gives true
	// there is no argument and mode is null
	public void testNOTaccEQ0() {
		cpu.setAccumulator(0);
		new Instruction(Opcode.NOT,Mode.NOMODE,0).execute(cpu); 
		checkModel(1,ipInit);
	}

	@Test 
	// Check NOT less than 0 gives false
	// there is no argument and mode is null
	public void testNOTaccLT0() {
		cpu.setAccumulator(-1);
		new Instruction(Opcode.NOT,Mode.NOMODE,0).execute(cpu); 
		checkModel(0,ipInit);
	}

	@Test 
	// Test whether NOT throws exception with immediate addressing mode
	public void testNOTimmediateMode() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.NOT,Mode.IMMEDIATE,0).execute(cpu));
		assertEquals("Illegal mode in Instruction - must be no mode", exception.getMessage());
	}

	@Test 
	// Test whether NOT throws exception with direct addressing mode
	public void testNOTdirectMode() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.NOT,Mode.DIRECT,0).execute(cpu));		
		assertEquals("Illegal mode in Instruction - must be no mode", exception.getMessage());
	}
	
	@Test 
	// Test whether NOT throws exception with immediate addressing mode
	public void testNOTindirectMode() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.NOT,Mode.INDIRECT,0).execute(cpu));		
		assertEquals("Illegal mode in Instruction - must be no mode", exception.getMessage());
	}

	@Test 
	// Check CMPL when comparing less than 0 gives true
	// addressing is direct
	public void testCMPLdirectMemLT0() {
		new Instruction(Opcode.CMPL,Mode.DIRECT,preSetDirect(-34)).execute(cpu);
		checkModel(1,ipInit);
	}

	@Test 
	// Check CMPL when comparing grater than 0 gives false
	// addressing is direct
	public void testCMPLdirectMemGT0() {
		new Instruction(Opcode.CMPL,Mode.DIRECT,preSetDirect(42)).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check CMPL when comparing equal to 0 gives false
	// addressing is direct
	public void testCMPLdirectMemEQ0() {
		new Instruction(Opcode.CMPL,Mode.DIRECT,preSetDirect(0)).execute(cpu); 
		checkModel(0,ipInit);
	}

	@Test 
	// Check CMPL when comparing less than 0 gives true
	// addressing is indirect
	public void testCMPLindirectMemLT0() {
		new Instruction(Opcode.CMPL,Mode.INDIRECT,preSetIndirect(-10)).execute(cpu);
		checkModel(1,ipInit);
	}

	@Test 
	// Check CMPL when comparing greater than 0 gives false
	// addressing is indirect
	public void testCMPLindirectMemGT0() {
		new Instruction(Opcode.CMPL,Mode.INDIRECT,preSetIndirect(10)).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check CMPL when comparing equal to 0 gives false
	// addressing is indirect
	public void testCMPLindirectMemEQ0() {
		new Instruction(Opcode.CMPL,Mode.INDIRECT,preSetIndirect(0)).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Test whether CMPL throws exception with null addressing mode
	public void testCMPLnullMode() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.CMPL,Mode.NOMODE,0).execute(cpu)); 		
		assertEquals("Illegal mode in Instruction - must be direct", exception.getMessage());
	}

	@Test 
	// Test whether CMPL throws exception with immediate addressing mode
	public void testCMPLimmediateMode() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.CMPL,Mode.IMMEDIATE,0).execute(cpu)); 
		assertEquals("Illegal mode in Instruction - must be direct", exception.getMessage());
	}
	
	@Test 
	// Check CMPZ when comparing less than 0 gives false
	// addressing is direct
	public void testCMPZdirectMemLT0() {
		new Instruction(Opcode.CMPZ,Mode.DIRECT,preSetDirect(-667)).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check CMPZ when comparing grater than 0 gives false
	// addressing is direct
	public void testCMPZdirectMemGT0() {
		new Instruction(Opcode.CMPZ,Mode.DIRECT,preSetDirect(23)).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check CMPZ when comparing equal to 0 gives true
	// addressing is direct
	public void testCMPZdirectMemEQ0() {
		new Instruction(Opcode.CMPZ,Mode.DIRECT,preSetDirect(0)).execute(cpu); 
		checkModel(1,ipInit);
	}

	@Test 
	// Check CMPL when comparing less than 0 gives false
	// addressing is indirect
	public void testCMPZindirectMemLT0() {
		new Instruction(Opcode.CMPZ,Mode.INDIRECT,preSetIndirect(-34)).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check CMPZ when comparing greater than 0 gives false
	// addressing is indirect
	public void testCMPZindirectMemGT0() {
		new Instruction(Opcode.CMPZ,Mode.INDIRECT,preSetIndirect(42)).execute(cpu);
		checkModel(0,ipInit);
	}

	@Test 
	// Check CMPZ when comparing equal to 0 gives true
	// addressing is indirect
	public void testCMPZindirectMemEQ0() {
		new Instruction(Opcode.CMPZ,Mode.INDIRECT,preSetIndirect(0)).execute(cpu);
		checkModel(1,ipInit);
	}

	@Test 
	// Test whether CMPZ throws exception with null addressing mode
	public void testCMPZnullMode() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.CMPZ,Mode.NOMODE,0).execute(cpu));		
		assertEquals("Illegal mode in Instruction - must be direct", exception.getMessage());
	}

	@Test 
	// Test whether CMPZ throws exception with immediate addressing mode
	public void testCMPZimmediateMode() {
		Throwable exception = assertThrows(InstructionErrorException.class,
				() -> new Instruction(Opcode.CMPZ,Mode.IMMEDIATE,0).execute(cpu));	
		assertEquals("Illegal mode in Instruction - must be direct", exception.getMessage());
	}
	
	@Test 
	// this test checks whether the relative JUMP is done correctly, when
	// addressing is immediate
	public void testJUMPimmediate() {
		int arg = 260;  
		new Instruction(Opcode.JUMP,Mode.IMMEDIATE,arg).execute(cpu);
		checkModel(accInit,arg-1+ipInit);
	}

	@Test 
	// this test checks whether the relative JUMP is done correctly, when
	// addressing is direct
	public void testJUMPdirect() {
		new Instruction(Opcode.JUMP,Mode.DIRECT,preSetDirect(400)).execute(cpu);
		checkModel(accInit,400-1+ipInit);
	}

	@Test 
	// this test checks whether the relative JUMP is done correctly, when
	// addressing is indirect
	public void testJUMPindirect() {
		new Instruction(Opcode.JUMP,Mode.INDIRECT,preSetIndirect(360)).execute(cpu);
		checkModel(accInit,360-1+ipInit);
	}

	@Test 
	// this test checks whether the non-relative JUMP is done correctly, when
	// addressing is not relative to current instruction pointer
	public void testJUMPnonrelative() {
		new Instruction(Opcode.JUMP,Mode.ABSOLUTE,preSetDirect(400)).execute(cpu);
		checkModel(accInit,400+cpu.getCurrentJob().getStartCodeIndex());
	}

	@Test 
	// this test checks whether the relative JMPZ is done like JUMP when accumulator is 0
	// addressing is immediate
	public void testJMPZimmediate() {
		cpu.setAccumulator(0);
		new Instruction(Opcode.JMPZ,Mode.IMMEDIATE,260).execute(cpu);
		checkModel(0,260-1+ipInit);
	}

	@Test 
	// this test checks whether the relative JMPZ is done like JUMP when accumulator is 0
	// addressing is direct
	public void testJMPZdirect() {
		cpu.setAccumulator(0);
		new Instruction(Opcode.JMPZ,Mode.DIRECT,preSetDirect(400)).execute(cpu);
		checkModel(0,400-1+ipInit);
	}

	@Test 
	// this test checks whether the relative JMPZ is done like JUMP when accumulator is 0
	// addressing is indirect
	public void testJMPZindirect() {
		cpu.setAccumulator(0);
		new Instruction(Opcode.JMPZ,Mode.INDIRECT,preSetIndirect(400)).execute(cpu);
		checkModel(0,400-1+ipInit);
	}

	@Test 
	// this test checks whether the non-relative JMPZ is done like JUMP when accumulator is 0
	// addressing is not relative to current instruction pointer
	public void testJMPZnonrelative() {
		cpu.setAccumulator(0);
		new Instruction(Opcode.JMPZ,Mode.ABSOLUTE,preSetDirect(400)).execute(cpu);
		checkModel(0,400+cpu.getCurrentJob().getStartCodeIndex());
	}

	@Test 
	// this test checks whether the relative JMPZ only increments instruction pointer
	// addressing is immediate
	public void testJMPZimmediateAccNZ() { 
		new Instruction(Opcode.JMPZ,Mode.IMMEDIATE,260).execute(cpu);
		checkModel(accInit,ipInit);
	}

	@Test 
	// this test checks whether the relative JMPZ only increments instruction pointer
	// addressing is direct
	public void testJMPZdirectAccNZ() {
		new Instruction(Opcode.JMPZ,Mode.DIRECT,preSetDirect(400)).execute(cpu);
		checkModel(accInit,ipInit);
	}

	@Test 
	// this test checks whether the relative JMPZ only increments instruction pointer
	// addressing is indirect
	public void testJMPZindirectAccNZ() {
		new Instruction(Opcode.JMPZ,Mode.INDIRECT,preSetIndirect(350)).execute(cpu);
		checkModel(accInit,ipInit);
	}

	@Test 
	// this test checks whether the non-relative JMPZ only increments instruction pointer
	// addressing is not relative to current instruction pointer
	public void testJMPZnonrelativeAccNZ() {
		new Instruction(Opcode.JMPZ,Mode.ABSOLUTE,preSetDirect(12)).execute(cpu);
		checkModel(accInit,ipInit);
	}
	
	@Test
	// This test checks to make sure halt does nothing bad
	public void testHALT() {
		new Instruction(Opcode.HALT,Mode.NOMODE,0).execute(cpu);
		checkModel(accInit,ipInit);
	}

}

