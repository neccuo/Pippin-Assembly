package proj02.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;

import proj02.model.CPU;
import proj02.model.CodeAccessException;
import proj02.model.CodeMemory;
import proj02.model.DivideByZeroException;
import proj02.model.Job;
import proj02.model.Memory;
import proj02.model.MemoryAccessException;
import proj02.model.Model;



public class JobViewPanel extends JPanel {

	private static final long serialVersionUID = -8119836553133532644L;
	private Model model;
	private CPU cpu;
	private CodeMemory codeMemory;
	private Memory dataMemory;
	private Job job;
	private FilesMgr filesMgr;
	private TimerControl stepControl;
	private ProcessorViewPanel cpuView;
	private JFrame frame;
	private CodeViewPanel codeViewPanel;
	private DataViewPanel dataViewPanel;
	JButton assembleLoadButton;
	JButton loadButton;
	JButton reloadButton;
	JButton clearButton;
	JButton stepButton;
	JButton runButton;
	JButton executeButton;

	public JobViewPanel(Model model, Job job, FilesMgr filesMgr, ProcessorViewPanel cpuView) {
		super();
		this.model = model;
		this.job = job;
		this.filesMgr = filesMgr;
		this.cpu = model.getCpu();
		this.codeMemory = model.getCodeMemory();
		this.dataMemory = model.getDataMemory();
		this.stepControl = new TimerControl(this);
		this.cpuView = cpuView;

		// Define Buttons
		assembleLoadButton = new JButton("Assemble and Load");
		assembleLoadButton.setBackground(Color.WHITE);
		assembleLoadButton.addActionListener(e -> assembleLoadJob());
		
		loadButton = new JButton("Load...");
		loadButton.setBackground(Color.WHITE);
		loadButton.addActionListener(e -> loadJob());

		reloadButton = new JButton("Reload");
		reloadButton.setBackground(Color.WHITE);
		reloadButton.addActionListener(e -> reload());

		clearButton = new JButton("Clear");
		clearButton.setBackground(Color.WHITE);
		clearButton.addActionListener(e -> clearJob());

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
		buttonPanel.add(assembleLoadButton);
		buttonPanel.add(loadButton);
		buttonPanel.add(reloadButton);
		buttonPanel.add(clearButton);

		codeViewPanel = new CodeViewPanel(model, job.getStartCodeIndex(),
				job.getStartCodeIndex() + job.getCodePartitionSize());
		dataViewPanel = new DataViewPanel(model, job.getStartDataIndex(),
				job.getStartDataIndex() + job.getDataPartitionSize());
		setLayout(new BorderLayout(1, 1));
		add(buttonPanel, BorderLayout.NORTH);
		add(codeViewPanel, BorderLayout.WEST);
		add(dataViewPanel, BorderLayout.CENTER);

		stepButton = new JButton("Step");
		stepButton.setBackground(Color.WHITE);
		stepButton.addActionListener(e -> step());

		runButton = new JButton("Run/Pause");
		runButton.setBackground(Color.WHITE);
		runButton.addActionListener(e -> toggleAutoStep());
		stepControl.start();

		executeButton = new JButton("Execute");
		executeButton.setBackground(Color.WHITE);
		executeButton.addActionListener(e -> execute());

		JPanel actionButtonPanel = new JPanel(new GridLayout(1, 0));
		actionButtonPanel.add(stepButton);
		actionButtonPanel.add(runButton);
		actionButtonPanel.add(executeButton);

		JPanel actionPanel = new JPanel(new GridLayout(0, 1));
		actionPanel.add(actionButtonPanel);

		JSlider slider = new JSlider(0, 1000);
		slider.addChangeListener(e -> stepControl.setPeriod(1000 - slider.getValue()));

		BufferedImage img = null;
		try {
			img = ImageIO.read(new File("src/proj02/view/turtle.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Image dimg = img.getScaledInstance(40, 30, Image.SCALE_SMOOTH);
		JLabel turtleLabel = new JLabel();
		ImageIcon imageIcon = new ImageIcon(dimg);
		turtleLabel.setIcon(imageIcon);

		try {
			img = ImageIO.read(new File("src/proj02/view/rabbit.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		dimg = img.getScaledInstance(40, 30, Image.SCALE_SMOOTH);
		JLabel rabbitLabel = new JLabel();
		imageIcon = new ImageIcon(dimg);
		rabbitLabel.setIcon(imageIcon);

		JPanel sliderPanel = new JPanel(new BorderLayout(1, 1));
		sliderPanel.add(turtleLabel, BorderLayout.WEST);
		sliderPanel.add(slider, BorderLayout.CENTER);
		sliderPanel.add(rabbitLabel, BorderLayout.EAST);
		actionPanel.add(sliderPanel);

		add(actionPanel, BorderLayout.SOUTH);
	}

	public void assembleLoadJob()
	{
		clearJob();
		filesMgr.assembleLoadFile(job);
		update("Load Code");
	}

	public void clearJob() {
		codeViewPanel.clearCode();
		dataViewPanel.clear();
		job.clear(codeMemory, dataMemory);
		cpu.setInstructionPointer(job.getStartCodeIndex());
		update("Clear");
	}

	public void loadJob() {
		clearJob();
		filesMgr.loadFile(job);
		update("Load Code");
	}

	public void reload() {
		clearJob();
		filesMgr.finalLoad_Reload(job);
		update("Load Code");
	}

	public void update(String arg1) {
		codeViewPanel.update(arg1);
		dataViewPanel.update(arg1);
		cpuView.update(arg1);
		if (stepControl.isAutoStepOn()) {
			loadButton.setEnabled(false);
			reloadButton.setEnabled(false);
			clearButton.setEnabled(false);
			stepButton.setEnabled(false);
			runButton.setEnabled(true);
			executeButton.setEnabled(false);
		} else {
			switch (job.getCurrentState()) {
			case PROGRAM_LOADED:
				loadButton.setEnabled(false);
				reloadButton.setEnabled(true);
				clearButton.setEnabled(true);
				stepButton.setEnabled(true);
				runButton.setEnabled(true);
				executeButton.setEnabled(true);
				break;
			case PROGRAM_HALTED:
				loadButton.setEnabled(true);
				reloadButton.setEnabled(true);
				clearButton.setEnabled(true);
				stepButton.setEnabled(false);
				runButton.setEnabled(false);
				executeButton.setEnabled(false);
				break;
			case NOTHING_LOADED:
				loadButton.setEnabled(true);
				reloadButton.setEnabled(false);
				clearButton.setEnabled(false);
				stepButton.setEnabled(false);
				runButton.setEnabled(false);
				executeButton.setEnabled(false);
			}
		}
	}

	public void step() {
		stepModel();
		update("");
	}

	public void stepModel() {
		if (job.getCurrentState() != Job.State.PROGRAM_LOADED)
			return;
		try {
			model.step();
		} catch (MemoryAccessException e) {
			stepControl.setAutoStep(false);
			JOptionPane.showMessageDialog(frame, "Illegal access to data from line " + cpu.getCurrentInstruction()
					+ "\n" + "Exception message: " + e.getMessage(), "Run time error", JOptionPane.OK_OPTION);
		} catch (CodeAccessException e) {
			stepControl.setAutoStep(false);
			JOptionPane.showMessageDialog(frame, "Illegal access to code from line " + cpu.getCurrentInstruction()
					+ "\n" + "Exception message: " + e.getMessage(), "Run time error", JOptionPane.OK_OPTION);
			cpu.setInstructionPointer(cpu.getCurrentInstruction());
		} catch (NullPointerException e) {
			stepControl.setAutoStep(false);
			JOptionPane.showMessageDialog(frame, "NullPointerException from line " + cpu.getCurrentInstruction() + "\n"
					+ "Exception message: " + e.getMessage(), "Run time error", JOptionPane.OK_OPTION);
		} catch (IllegalArgumentException e) {
			stepControl.setAutoStep(false);
			JOptionPane.showMessageDialog(frame, "Program Error from line " + cpu.getCurrentInstruction() + "\n"
					+ "Exception message: " + e.getMessage(), "Run time error", JOptionPane.OK_OPTION);
		} catch (DivideByZeroException e) {
			stepControl.setAutoStep(false);
			JOptionPane.showMessageDialog(frame, "Divide by zero from line " + cpu.getCurrentInstruction() + "\n"
					+ "Exception message: " + e.getMessage(), "Run time error", JOptionPane.OK_OPTION);
		}
		if (job.getCurrentState() == Job.State.PROGRAM_HALTED) {
			stepControl.setAutoStep(false);
			cpu.setInstructionPointer(cpu.getCurrentInstruction());
		}
	}

	public void execute() {
		while (job.getCurrentState() == Job.State.PROGRAM_LOADED) {
			stepModel();
		}
		update("");
	}

	public void toggleAutoStep() {
		stepControl.toggleAutoStep();
		update("");
	}

	public static void main(String[] args) {
		Model model = new Model();
		FilesMgr filesMgr = new FilesMgr(model);
		filesMgr.initialize();
		ProcessorViewPanel processorPanel = new ProcessorViewPanel(model.getCpu());
		JobViewPanel panel = new JobViewPanel(model, model.getCpu().getCurrentJob(), filesMgr, processorPanel);
		JFrame frame = new JFrame("TEST");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 700);
		frame.setLocationRelativeTo(null);
		frame.add(panel);
		frame.setVisible(true);
		panel.update("");
		// Loader.load(model, new File("src/proj02/pexe/factorial.pexe"));
		// panel.update("Load Code");
	}
}
