package proj02.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import proj02.model.Job;
import proj02.model.Model;

public class PippinGUI {

	private Model model;
	private FilesMgr filesMgr;
	private JFrame frame;
	private ProcessorViewPanel processorPanel;
	JTabbedPane jobsPane;

	public void setModel(Model m) {
		model = m;
	}

	public void changeToJob(int i) {
		model.changeToJob(i);
		notifyObservers("Switch to job" + i);
	}

	private void notifyObservers(String str) {
		processorPanel.update(str);
		for (int i = 0; i < jobsPane.getTabCount(); i++) {
			JobViewPanel j = (JobViewPanel) jobsPane.getComponentAt(i);
			j.update(str);
		}
	}

	private void createAndShowGUI() {
		filesMgr = new FilesMgr(model);
		filesMgr.initialize();
		processorPanel = new ProcessorViewPanel(model.getCpu());
		frame = new JFrame("Pippin Simulator");

		jobsPane = new JTabbedPane();
		Job[] jobs = model.getJobs();
		for (Job job : jobs) {
			jobsPane.add("Job " + job.getId(), new JobViewPanel(model, job, filesMgr, processorPanel));
		}
		jobsPane.addChangeListener(e -> changeToJob(jobsPane.getSelectedIndex()));

		JButton assembleButton = new JButton("Assemble...");
		assembleButton.setBackground(Color.WHITE);
		assembleButton.addActionListener(e -> filesMgr.assembleFile());

		JButton exitButton = new JButton("Exit");
		exitButton.setBackground(Color.WHITE);
		exitButton.addActionListener(e -> exit());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(Box.createVerticalGlue());
		buttonPanel.add(assembleButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		buttonPanel.add(exitButton);
		buttonPanel.add(Box.createVerticalGlue());

		frame.setSize(700, 600);
		frame.add(buttonPanel, BorderLayout.EAST);
		frame.add(jobsPane, BorderLayout.CENTER);
		frame.add(processorPanel, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(WindowListenerFactory.windowClosingFactory(e -> exit()));
		frame.setLocationRelativeTo(null);
		notifyObservers("");
		frame.setVisible(true);
	}

	public void exit() { // method executed when user exits the program
		int decision = JOptionPane.showConfirmDialog(frame, "Do you really wish to exit?", "Confirmation",
				JOptionPane.YES_NO_OPTION);
		if (decision == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(() -> {
			PippinGUI organizer = new PippinGUI();
			Model model = new Model();
			organizer.setModel(model);
			organizer.createAndShowGUI();
		});
	}

}
