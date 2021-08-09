package proj02.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import proj02.assembler.Loader;
import proj02.model.CodeMemory;
import proj02.model.Instruction;
import proj02.model.Job;
import proj02.model.Model;

public class CodeViewPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5281966007792010868L;
	Model model;
	CodeMemory code;
	JScrollPane scroller;
	JTextField[] codeHex; 
	JTextField[] codeText;
	private int lower = -1;
	private int upper = -1;
	int previousColor = -1;
	
	public CodeViewPanel(Model mdl,int lower,int upper) {
		super();
		model=mdl;
		this.upper=upper;
		this.lower=lower;
		code = model.getCodeMemory();
		codeText = new JTextField[upper-lower];
		codeHex = new JTextField[upper-lower];
	
		JPanel innerPanel = new JPanel();
		JPanel numPanel = new JPanel();
		JPanel textPanel = new JPanel();
		JPanel hexPanel = new JPanel();
		setPreferredSize(new Dimension(300,150));
		setLayout(new BorderLayout());
		Border border = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), 
				"Code Memory View",
				TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION);
		setBorder(border);
		innerPanel.setLayout(new BorderLayout());
		numPanel.setLayout(new GridLayout(0,1));
		textPanel.setLayout(new GridLayout(0,1));
		hexPanel.setLayout(new GridLayout(0,1));
		for(int i = lower; i < upper; i++) {
			numPanel.add(new JLabel(i+": ", JLabel.RIGHT));
			codeText[i-lower] = new JTextField(10);
			codeHex[i-lower] = new JTextField(10);
			textPanel.add(codeText[i-lower]);
			hexPanel.add(codeHex[i-lower]);
		}
		innerPanel.add(numPanel, BorderLayout.LINE_START);
		innerPanel.add(textPanel, BorderLayout.CENTER);
		innerPanel.add(hexPanel, BorderLayout.LINE_END);
		
		scroller = new JScrollPane(innerPanel);
		add(scroller);
	}
	
	public void loadCode() {
		int offset = model.getCpu().getCurrentJob().getStartCodeIndex();
		if (offset < lower) {
			if (offset + model.getCpu().getCurrentJob().getCodeSize() >= upper) return; 
			offset=lower;
		}
		for(int i = offset; 
				i < offset + model.getCpu().getCurrentJob().getCodeSize(); i++) {
			Instruction inst = code.getCode(i);
			codeText[i-lower].setText(inst.toString());
			codeHex[i-lower].setText(inst.getObjectText());
		}	
		previousColor = model.getCpu().getInstructionPointer();	
		if (previousColor >= lower && previousColor < upper) {
			codeHex[previousColor-lower].setBackground(Color.YELLOW);
			codeText[previousColor-lower].setBackground(Color.YELLOW);
		}
	}
	
	public void clearCode() {
		int offset = model.getCpu().getCurrentJob().getStartCodeIndex();
		int codeSize = model.getCpu().getCurrentJob().getCodeSize();
		for(int i = offset; 
			i < offset + codeSize; i++) {
			codeText[i-lower].setText("");
			codeHex[i-lower].setText("");
		}	
		if(previousColor >= lower && previousColor < upper) {
			codeText[previousColor-lower].setBackground(Color.WHITE);
			codeHex[previousColor-lower].setBackground(Color.WHITE);
		}
		previousColor = -1;
	}
	
	public void update(String arg1) {
		if(arg1 != null && arg1.equals("Load Code")) loadCode();
		else if(arg1 != null && arg1.equals("Clear")) clearCode();
		
		if(previousColor >= lower && previousColor < upper) {
			codeText[previousColor-lower].setBackground(Color.WHITE);
			codeHex[previousColor-lower].setBackground(Color.WHITE);
		}
		previousColor = model.getCpu().getInstructionPointer();
		if(previousColor >= lower && previousColor < upper) {
			codeText[previousColor-lower].setBackground(Color.YELLOW);
			codeHex[previousColor-lower].setBackground(Color.YELLOW);
		} 
		if(scroller != null && code != null && model!= null) {
			JScrollBar bar= scroller.getVerticalScrollBar();
			int pc = model.getCpu().getInstructionPointer();
			if(pc >= lower && pc < upper /* && codeHex[pc] != null */) {
				Rectangle bounds = codeHex[pc-lower].getBounds();
				bar.setValue(Math.max(0, bounds.y - 15*bounds.height));
			}
		}
	}
	
	public static void main(String[] args) {
		Model model = new Model();
		model.changeToJob(2);
		Job j = model.getCpu().getCurrentJob();
		CodeViewPanel panel = new CodeViewPanel(model,j.getStartCodeIndex(),j.getStartCodeIndex() + j.getCodePartitionSize());
		JFrame frame = new JFrame("TEST");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 700);
		frame.setLocationRelativeTo(null);
		frame.add(panel);
		frame.setVisible(true);
		Loader.load(model, new File("src/proj02/pexe/merge.pexe"));
		panel.update("Load Code");
	}
	
}
