package proj02.assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import proj02.model.CodeAccessException;
import proj02.model.InstructionErrorException;
import proj02.model.MemoryAccessException;
import proj02.model.Model;

public class Loader {

	public static String load(Model pippin, File file) {
		if (pippin==null) return "Model argument is null";
		if (file==null) return "File argument is null";
		pippin.clearCurrentJob();
		String line=""; int lineNo=0;
		try (Scanner input = new Scanner(file)) {
			boolean incode=true;
			while(input.hasNextLine() ) {
				line = input.nextLine();
				lineNo++;
				Scanner parser = new Scanner(line);
				int obj1 = parser.nextInt(16);
				if (obj1==-1) {
					incode=false;
				} else if (incode) {
					int obj2 = parser.nextInt(16);
					pippin.setCode(obj1, obj2);
				} else { // we are in data
					int value = parser.nextInt(16);
					pippin.setData(obj1, value);
				}
				parser.close();
			}
		} catch(FileNotFoundException e) {
			return "File " + file.getName() + " Not Found";
		} catch(CodeAccessException e) {
			return "Code Memory Access Exception " + e.getMessage();
		} catch(MemoryAccessException e) {
			return "Data Memory Access Exception " + e.getMessage();
		} catch(InstructionErrorException e)  {
			return "Instruction error " + e.getMessage() + " reading object code line " + lineNo + " : " + line;
		} catch(NoSuchElementException e) {
			/* e.printStackTrace(); */
			return "Problem reading object code line " + lineNo + " : " + line;
		}
		pippin.currentJobLoaded();
		return "";
	}
	
	public static void main(String[] args) { // Tester for the Loader
		Model model = new Model();
		model.changeToJob(2);
		String fn="factorial";
		if (args.length >= 1) fn=args[0];
		String s = Loader.load(model, new File("src/proj01/pexe/" + fn + ".pexe"));
		if (s.equals("")) {
			System.out.println("Load completed with no errors");
			
		} else {
			System.out.println("Loader failed with message.");
			System.out.println("=> " + s);
		}
		model.getCodeMemory().dump("Code Memory after load...");
		model.getDataMemory().dump("Data Memory after load...");
	}
}
