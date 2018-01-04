package main;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class SimonFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6185762543700209859L;
	public static final Dimension SIZE = new Dimension(512, 512);
	public static final String GAME_NAME = "Simon";
	public static final double VERSION = 0.1;

	public static void main(String[] args) {
		new SimonFrame();
	}

	public SimonFrame() {
		super(GAME_NAME + " v" + VERSION);
		setSize(SIZE);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setFocusable(false);
		setResizable(false);
		init();
		pack();
	}

	private void init() {
		final SimonPanel panel = new SimonPanel(SIZE);
		add(panel);
		panel.requestFocus();

		setJMenuBar(bar);
		bar.add(menu);
		menu.add(highscores);
		menu.add(difficulty);
		menu.add(info);
		menu.addSeparator();
		menu.add(exit);

		difficulty.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String result;
				switch (difficulty.getText()) {
				case "Super Easy":
					result = "Easy";
					panel.changeDifficulty(1);
					break;
				case "Easy":
					result = "Medium";
					panel.changeDifficulty(2);
					break;
				case "Medium":
					result = "Hard";
					panel.changeDifficulty(3);
					break;
				case "Hard":
					result = "Harder";
					panel.changeDifficulty(4);
					break;
				case "Harder":
					result = "Super Hard";
					panel.changeDifficulty(5);
					break;
				case "Super Hard":
					result = "Super Easy";
					panel.changeDifficulty(0);
					break;
				default:
					result = "Undefined";
					panel.changeDifficulty(0);
				}
				difficulty.setText(result);
			}
		});

		highscores.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				panel.getDatabase().displayHighScores(SimonPanel.HIGHSCORE_IDENTIFIER, SimonPanel.level);
			}
		});
		exit.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				panel.getDatabase().write(SimonPanel.HIGHSCORE_IDENTIFIER);
				System.exit(0);
			}
		});

		info.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				StringBuilder message = new StringBuilder();
				message.append("Simon is a memory game\n");
				message.append("Memorize the flashing circles in the order that they turn on\n");
				message.append("Click on each circle in the correct order to continue\n");
				message.append("The sequence of flashing circles will get longer as you go\n");
				message.append("Some difficulties will add an interesting mechanic to the game! :)\n");
				message.append("Best of Luck! ~Jonathan Collins 2018\n");

				JOptionPane.showConfirmDialog(null, message.toString(), "Info", JOptionPane.PLAIN_MESSAGE);
			}
		});

		setVisible(true);
		new Thread(panel).start();
	}

	private JMenuBar bar = new JMenuBar();
	private JMenu menu = new JMenu("File");
	private JMenuItem highscores = new JMenuItem("Highscores");
	private JMenuItem info = new JMenuItem("Info");
	private JButton difficulty = new JButton("Super Easy");
	private JMenuItem exit = new JMenuItem("Exit");

}
