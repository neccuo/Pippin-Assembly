package proj02.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import proj02.assembler.Assembler;
import proj02.assembler.Loader;
import proj02.model.Job;
import proj02.model.Model;

public class FilesMgr {

	private Model model;
	private String defaultDir; 
	private String sourceDir; 
	private String executableDir; 
	private Properties properties = null;
	private File[] currentlyExecutingFile = new File[4];
	
	public FilesMgr(Model model) {
		this.model = model;
	}

	public void initialize() {
		locateDefaultDirectory();
		loadPropertiesFile();
	}
	
	private void locateDefaultDirectory() {
		//CODE TO DISCOVER THE ECLIPSE DEFAULT DIRECTORY:
		//There will be a property file if the program has been used for a while
		//because it which will store the locations of the pasm and pexe files
		//but we allow the possibility that it does not exist yet.
		File temp = new File("propertyfile.txt");
		if(!temp.exists()) {
			PrintWriter out; // make a file that we will delete later
			try {
				out = new PrintWriter(temp);
				out.close();
				defaultDir = temp.getAbsolutePath();
				temp.delete();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			defaultDir = temp.getAbsolutePath();
		}
		// change to forward slashes, making it platform independent
		defaultDir = defaultDir.replace('\\','/');
		int lastSlash = defaultDir.lastIndexOf('/');
		//remove the file name and keep the directory path
		defaultDir  = defaultDir.substring(0, lastSlash + 1) + "src/proj02/";
	}
	
	void loadPropertiesFile() {
		try { // load properties file "propertyfile.txt", if it exists
			properties = new Properties();
			properties.load(new FileInputStream("propertyfile.txt"));
			sourceDir = properties.getProperty("SourceDirectory");
			executableDir = properties.getProperty("ExecutableDirectory");
			// CLEAN UP ANY ERRORS IN WHAT IS STORED:
			if (sourceDir == null || sourceDir.length() == 0 
					|| !new File(sourceDir).exists()) {
				sourceDir = defaultDir + "pasm";
			}
			if (executableDir == null || executableDir.length() == 0 
					|| !new File(executableDir).exists()) {
				executableDir = defaultDir + "pexe";
			}
		} catch (Exception e) {
			// PROPERTIES FILE DID NOT EXIST
			sourceDir = defaultDir + "pasm";
			executableDir = defaultDir + "pexe";
		}
	}
	
	public void assembleFile() {
		File source = null;
		File outputExe = null;
		JFileChooser chooser = new JFileChooser(sourceDir);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Pippin Source Files", "pasm");
		chooser.setFileFilter(filter);
		// CODE TO LOAD DESIRED FILE
		int openOK = chooser.showOpenDialog(null);
		if(openOK == JFileChooser.APPROVE_OPTION) {
			source = chooser.getSelectedFile();
		}
		if(source != null && source.exists()) {
			// CODE TO REMEMBER WHICH DIRECTORY HAS THE pexe FILES
			// WHICH WE WILL ALLOW TO BE DIFFERENT
			sourceDir = source.getAbsolutePath();
			sourceDir = sourceDir.replace('\\','/');
			int lastDot = sourceDir.lastIndexOf('.');
			String inName = source.getName();
			String outName = sourceDir.substring(0, lastDot + 1) + "pexe";			
			int lastSlash = sourceDir.lastIndexOf('/');
			sourceDir = sourceDir.substring(0, lastSlash + 1);
			outName = outName.substring(lastSlash+1); 
			filter = new FileNameExtensionFilter(
					"Pippin Executable Files", "pexe");
			if(executableDir.equals(defaultDir)) {
				chooser = new JFileChooser(sourceDir);
			} else {
				chooser = new JFileChooser(executableDir);
			}
			chooser.setFileFilter(filter);
			chooser.setSelectedFile(new File(outName));
			int saveOK = chooser.showSaveDialog(null);
			if(saveOK == JFileChooser.APPROVE_OPTION) {
				outputExe = chooser.getSelectedFile();
			}
			if(outputExe != null) {
				executableDir = outputExe.getAbsolutePath();
				executableDir = executableDir.replace('\\','/');
				lastSlash = executableDir.lastIndexOf('/');
				executableDir = executableDir.substring(0, lastSlash + 1);
				try { 
					properties.setProperty("SourceDirectory", sourceDir);
					properties.setProperty("ExecutableDirectory", executableDir);
					properties.store(new FileOutputStream("propertyfile.txt"), 
							"File locations");
				} catch (Exception e) {
					// Never seen this happen
					JOptionPane.showMessageDialog(
						null, 
						"Problem with Java.\n" +
						"Error writing properties file",
						"Warning",
						JOptionPane.OK_OPTION);
				}
				TreeMap<Integer, String> errors = new TreeMap<>();
				Assembler assembler = new Assembler(); // Change this to FullAssembler in installment 4
				int errorIndicator = assembler.assemble(source.getAbsolutePath(), 
						outputExe.getAbsolutePath(), errors);
				if (errorIndicator == 0){
					JOptionPane.showMessageDialog(
						null, // null, 
						"The source was assembled to an executable",
						"Success",
						JOptionPane.INFORMATION_MESSAGE);
				} else {
					StringBuilder sb = new StringBuilder(inName + " has one or more errors\n");
					for(Integer key : errors.keySet()) {
						sb.append(errors.get(key)); sb.append("\n");
					}
					JOptionPane.showMessageDialog(
						null, 
						sb.toString(),
						"Source code error",
						JOptionPane.INFORMATION_MESSAGE);
				}
			} else {// outputExe still null
				JOptionPane.showMessageDialog(
					null, 
					"The output file has problems.\n" +
					"Cannot assemble the program",
					"Warning",
					JOptionPane.OK_OPTION);
			}
		} else {// source file does not exist
			JOptionPane.showMessageDialog(
				null, 
				"The source file has problems.\n" +
				"Cannot assemble the program",
				"Warning",
				JOptionPane.OK_OPTION);				
		}
	}
	
	public void loadFile(Job job) {
		int index = job.getId();
		JFileChooser chooser = new JFileChooser(executableDir);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Pippin Executable Files", "pexe");
		chooser.setFileFilter(filter);
		// CODE TO LOAD DESIRED FILE
		int openOK = chooser.showOpenDialog(null);

		if(openOK == JFileChooser.APPROVE_OPTION) {
			currentlyExecutingFile[index] = chooser.getSelectedFile();
		}
		if(openOK == JFileChooser.CANCEL_OPTION) {
			currentlyExecutingFile[index] = null;
		}

		if(currentlyExecutingFile[index] != null && currentlyExecutingFile[index].exists()) {
			// CODE TO REMEMBER WHICH DIRECTORY HAS THE pexe FILES
			executableDir = currentlyExecutingFile[index].getAbsolutePath();
			executableDir = executableDir.replace('\\','/');
			int lastSlash = executableDir.lastIndexOf('/');
			executableDir = executableDir.substring(0, lastSlash + 1);
			try { 
				properties.setProperty("SourceDirectory", sourceDir);
				properties.setProperty("ExecutableDirectory", executableDir);
				properties.store(new FileOutputStream("propertyfile.txt"), 
						"File locations");
			} catch (Exception e) {
				// Never seen this happen
				JOptionPane.showMessageDialog(
					null, 
					"Problem with Java.\n" +
					"Error writing properties file",
					"Warning",
					JOptionPane.OK_OPTION);
			}			
		}
		if(currentlyExecutingFile[index] != null) {
			finalLoad_Reload(job);
		} else {
			JOptionPane.showMessageDialog(
				null,  
				"No file selected.\n" +
				"Cannot load the program",
				"Warning",
				JOptionPane.OK_OPTION);
		}
	}
	
	public boolean assembleLoadFile(Job job)
	{
		int index = job.getId();
		File pasmFile = null;
		JFileChooser chooser = new JFileChooser("src/proj02/pasm/");
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Pippin Assembly File", "pasm");
		chooser.setFileFilter(filter);
		int openOK = chooser.showOpenDialog(null);

		if(openOK == JFileChooser.APPROVE_OPTION) 
		{
			pasmFile = chooser.getSelectedFile();
		}
		if(openOK == JFileChooser.CANCEL_OPTION)
		{
			pasmFile = null;
			return false;
		}
		String pasmStr = pasmFile.getAbsolutePath();
		
		chooser = new JFileChooser("src/proj02/pexe/");
		filter = new FileNameExtensionFilter("Pippin Executable File", "pexe");
		openOK = chooser.showOpenDialog(null);

		if(openOK == JFileChooser.APPROVE_OPTION) 
		{
			currentlyExecutingFile[index] = chooser.getSelectedFile();
		}
		if(openOK == JFileChooser.CANCEL_OPTION)
		{
			currentlyExecutingFile[index] = null;
			return false;
		}
		String pexeStr = currentlyExecutingFile[index].getAbsolutePath();
		
		Assembler test = new Assembler();	
		SortedMap<Integer, String> errors = new TreeMap<>();
		
		int assemblerNum = test.assemble(pasmStr, pexeStr, errors);
		if(assemblerNum != 0)
		{
			StringBuilder sb = new StringBuilder(".pasm file contains error(s)\n");
			for(Integer key : errors.keySet()) {
				sb.append(errors.get(key)); sb.append("\n");
			}
			JOptionPane.showMessageDialog(null, sb.toString(), "Assembler error", JOptionPane.OK_OPTION);
			return false;
		}
		File file = new File(pexeStr);
		String loadStr = Loader.load(model, file);
		if(!loadStr.equals(""))
		{
			JOptionPane.showMessageDialog(null, loadStr, "Load error", JOptionPane.OK_OPTION);
			return false;
		}
		return true;
	}
	
	void finalLoad_Reload(Job job) {
		String str = Loader.load(model, currentlyExecutingFile[job.getId()]);
		if (!str.equals("")) {
			JOptionPane.showMessageDialog(
				null,  
				"The file being selected has problems.\n" +
				str + "\n" +
				"Cannot load the program",
				"Warning",
				JOptionPane.OK_OPTION);
		}
	}
	
}
