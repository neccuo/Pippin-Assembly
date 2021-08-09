package proj02.assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

import proj02.model.Instruction;
import proj02.model.Mode;
import proj02.model.Opcode;

public class Assembler {
	
	public int assemble(String inputFileName, String outputFileName, SortedMap<Integer, String> errors) {
		boolean inCode=true;
		int firstBlankLine=-1;
		int lineNumber=0;
		FileWriter output=null;
		try {
			output = new FileWriter(outputFileName);
		} catch(IOException e) {
			errors.put(-1, "Unable to open the object file");
		}
		try (Scanner input = new Scanner(new File(inputFileName))) {
			while(input.hasNextLine()) {
				String line=input.nextLine();
				lineNumber++;
				String tokens[] = line.split("\\s+");
				if (tokens.length==0 || (tokens.length==1 && tokens[0].equals(""))) { // handle blank lines
					// May be at the end of the file... if so, no problem
					if (firstBlankLine==-1) firstBlankLine=lineNumber;
					continue;
				}
				// Get here if the line is non-blank
				// If there were preceding blank lines, they are in error
				if (firstBlankLine>-1) {
					for(int j=firstBlankLine; j<lineNumber; j++) {
						errors.put(j, "Blank line not at the end of the file on line " + j);
					}
					firstBlankLine=-1;
				}
				if (tokens[0].equals("")) {
					errors.put(lineNumber, "Line starts with illegal white space on line " + lineNumber);
					continue;
				}
				if (inCode) {
					// First, check for the DATA delimiter
					if (tokens[0].equalsIgnoreCase("DATA")) {
						inCode=false;
						if (!tokens[0].equals("DATA")) {
							errors.put(lineNumber, "Illegal mixed or lower case DATA delimiter on line " + lineNumber);
						}
						else if (tokens.length !=1) {
							errors.put(lineNumber, "Illegal data after DATA delimiter on line " + lineNumber);
							
						}
						try {
							output.write("-1 0\n");
						} catch(IOException e)  {
							errors.put(lineNumber, "I/O error writing on line " + lineNumber);
						}
						continue;
					}
					// We've handled the data delimiter, must be an opcode
					Opcode op;
					try {
						op = Opcode.valueOf(tokens[0].toUpperCase());
					} catch(IllegalArgumentException e) {
						errors.put(lineNumber, "Illegal mnemonic on line " + lineNumber);
						continue;
					}
					if (!tokens[0].equals(tokens[0].toUpperCase())) {
						errors.put(lineNumber,  "Mnemonic not in uppercase on line " + lineNumber);
						continue;
					}
					// Next, check for the mode
					Mode mode=null;
					if (tokens.length==1) mode=Mode.NOMODE;
					else mode=Mode.getModeFromArgString(tokens[1]);
					if (mode==null) {
						errors.put(lineNumber, "Illegal mode prefix on line " + lineNumber);
						continue;
					}
					// Check for illegal mode/opcode combinations
					if (mode!=Mode.NOMODE && tokens.length<2) {
						errors.put(lineNumber, "Instruction requires argument on line " + lineNumber);
						continue;
					}
					switch(op) {
					case NOP:
					case NOT:
					case HALT:
						if (tokens.length>1) {
							errors.put(lineNumber, "Illegal argument in no-argument instruction on line " + lineNumber);
							continue;
						}
						break;
					case STO:
					case CMPL:
					case CMPZ:
						if (mode==Mode.IMMEDIATE) {
							errors.put(lineNumber, "Illegal immediate argument on line " + lineNumber);
							continue;
						}
						// Drop through to absolute check as well
					case LOD:
					case ADD:
					case SUB:
					case MUL:
					case DIV:
					case AND:
						if (mode==Mode.ABSOLUTE) {
							errors.put(lineNumber, "Illegal absolute argument on line " + lineNumber);
							continue;
						}
						break;
					case JUMP:
					case JMPZ:
						if (mode==Mode.NOMODE) {
							errors.put(lineNumber, "Instruction requires argument on line " + lineNumber);
							continue;
						}
						break;
					case UNUSED1:
					case UNUSED2:
						errors.put(lineNumber, "Illegal mnemonic on line " + lineNumber);
						continue;
					} // End of case on opcode
					int arg=0; // Finally get the argument value
					switch(mode) {
					case NOMODE:
						break;
					case INDIRECT:
					case IMMEDIATE:
					case ABSOLUTE:
						// Remove prefix
						tokens[1] = tokens[1].substring(1);
					case DIRECT:
						try {
							arg=Integer.parseInt(tokens[1],16);
						} catch(NumberFormatException e) {
							errors.put(lineNumber, "Argument is not a hex number on line " + lineNumber);
						}				
					} // end of case on mode
					if (tokens.length>2) {
						errors.put(lineNumber,  "Instruction has too many arguments on line " + lineNumber);
					}
					Instruction ins = new Instruction(op,mode,arg);
					try {
						output.write(ins.getObjectText() + "\n");
					} catch(IOException e) {
						errors.put(lineNumber, "I/O error writing on line " + lineNumber);
					}
					continue;
				} // end of if in code
				// Not in code.. check for second delimiter
				if (tokens[0].equalsIgnoreCase("DATA")) {
					errors.put(lineNumber, "Illegal second DATA delimiter on line " + lineNumber);
					continue;
				}
				// Must be a data line... double check values
				if (tokens.length<2) {
					errors.put(lineNumber, "No Data value on line " + lineNumber);
					continue;
				}
				if (tokens.length>2) {
					errors.put(lineNumber, "DATA has too many values on line " + lineNumber);
					continue;
				}
				try {
					Integer.parseInt(tokens[0],16);
				} catch(NumberFormatException e) {
					errors.put(lineNumber,	"DATA location is not a hex number on line " + lineNumber);
					continue;
				}
				try {
					Integer.parseInt(tokens[1],16);
				} catch(NumberFormatException e) {
					errors.put(lineNumber, "DATA value is not a hex number on line " + lineNumber);
					continue;
				}
				try {
					output.write(line + "\n");
				} catch(IOException e) {
					errors.put(lineNumber, "I/O error writing on line " + lineNumber);
				}
			} // end of loop through input lines
		} catch(FileNotFoundException e) { 
			errors.put(-1,"Unable to open the source file");
			return -1;
		}
		try {
			output.close();
		} catch (IOException e) {
			errors.put(-1,"Unable to close the output file");
			return -1;
		}
		if (errors.isEmpty()) return 0;
		return errors.firstKey();
	}
	
	public static void main(String[] args) {
		SortedMap<Integer, String> errors = new TreeMap<>();
		Assembler test = new Assembler();
		String pgm="allOpCodes";
		if (args.length > 0) pgm=args[0];
		test.assemble("src/proj02/pasm/" + pgm + ".pasm", "src/proj02/pexe/" + pgm + ".pexe", errors);
		if (!errors.isEmpty()) System.out.println(errors);
		else System.out.println("Converted " + pgm + " with no errors.");
	}

}
