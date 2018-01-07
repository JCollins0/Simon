package main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import com.jrc.game.GameTimer;
import com.jrc.highscores.Database;
import com.jrc.util.Colors;
import com.jrc.util.Fonts;

public class SimonPanel extends Canvas implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7102612947251210684L;
	public static final String HIGHSCORE_IDENTIFIER = "simon";
	private BufferedImage buffer;
	private MouseHandler mouseHandle;
	private Database database;
	public static int level = 0;
	private static boolean running = false;

	private boolean sequencePlaying = false;
	private boolean sequenceInputting = false;
	private int inputFlashing = 0;
	private ArrayList<Integer> sequence;
	private int index = 0;
	private int score;
	private boolean gameOver = false;
	private boolean gameNotStartedYet = true;
	private boolean doSwap;

	private static final int RED = 2, BLUE = 3, YELLOW = 5, GREEN = 7, WHITE = 11, PURPLE = RED * BLUE,
			ORANGE = RED * YELLOW, LIME = YELLOW * GREEN, TEAL = GREEN * BLUE;

	private static final int[] colors = { RED, BLUE, YELLOW, GREEN, PURPLE, ORANGE, LIME, TEAL, WHITE };
	private int bound = 4;

	private TreeMap<Integer, Bubble> bubbles;

	public SimonPanel(Dimension size) {
		database = new Database("scores.jrc");
		database.read(HIGHSCORE_IDENTIFIER);
		database.setNumberOfScoresToKeep(HIGHSCORE_IDENTIFIER, 10);
		database.write(HIGHSCORE_IDENTIFIER);
		database.addLevelIdentity(HIGHSCORE_IDENTIFIER, 0, "Super Easy");
		database.addLevelIdentity(HIGHSCORE_IDENTIFIER, 1, "Easy");
		database.addLevelIdentity(HIGHSCORE_IDENTIFIER, 2, "Medium");
		database.addLevelIdentity(HIGHSCORE_IDENTIFIER, 3, "Hard");
		database.addLevelIdentity(HIGHSCORE_IDENTIFIER, 4, "Harder");
		database.addLevelIdentity(HIGHSCORE_IDENTIFIER, 5, "Super Hard");
		mouseHandle = new MouseHandler(this);
		addMouseListener(mouseHandle);
		setPreferredSize(size);
		setMaximumSize(size);
		setMinimumSize(size);
		buffer = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);

		bubbles = new TreeMap<>();
		loadCircles();

		sequence = new ArrayList<>();
		running = true;
	}

	public void loadCircles() {
		bubbles.clear();
		switch (level) {
		case 5:
		case 4:
			bubbles.put(WHITE, new Bubble(256, 256, 96, Color.WHITE));
		case 3:
		case 2:
			bubbles.put(PURPLE, new Bubble(256, 64, getRadiusByLevel(), Colors.PURPLE));
			bubbles.put(ORANGE, new Bubble(64, 256, getRadiusByLevel(), Colors.ORANGE));
			bubbles.put(TEAL, new Bubble(448, 256, getRadiusByLevel(), Colors.TEAL));
			bubbles.put(LIME, new Bubble(256, 448, getRadiusByLevel(), Colors.LIME));
		case 1:
		case 0:
			bubbles.put(RED, new Bubble(128, 128, getRadiusByLevel(), Color.RED));
			bubbles.put(BLUE, new Bubble(384, 128, getRadiusByLevel(), Color.BLUE));
			bubbles.put(YELLOW, new Bubble(128, 384, getRadiusByLevel(), Color.YELLOW));
			bubbles.put(GREEN, new Bubble(384, 384, getRadiusByLevel(), Color.GREEN));
		}

		switch (level) {
		case 0:
			doSwap = false;
			bound = 4;
			break;
		case 1:
			bound = 4;
			doSwap = true;
			break;
		case 2:
			doSwap = false;
			bound = 8;
			break;
		case 3:
			bound = 8;
			doSwap = true;
			break;
		case 4:
			bound = 9;
			doSwap = false;
			break;
		case 5:
			bound = 9;
			doSwap = true;
			break;
		}

	}

	private int getRadiusByLevel() {
		switch (level) {
		case 0:
		case 1:
			return 128;
		default:
			return 64;
		}
	}

	public Database getDatabase() {
		return database;
	}

	public void submitScore(int level, int score, Date date) {
		database.addScore(HIGHSCORE_IDENTIFIER, level, score, date);
		database.write(HIGHSCORE_IDENTIFIER);
	}

	public void render() {
		BufferStrategy bs = getBufferStrategy();

		if (bs == null) {
			createBufferStrategy(3);
			return;
		}

		Graphics b = buffer.getGraphics();
		b.clearRect(0, 0, buffer.getWidth(), buffer.getHeight());

		synchronized (this) {
			for (Bubble bubble : bubbles.values()) {
				bubble.render(b);
			}
		}

		if (gameOver) {
			b.setColor(Colors.TRANSPARENT_WHITE);
			b.fillRoundRect(96, 128, 320, 72, 32, 32);
			b.setFont(Fonts.GAME_FONT);
			b.setColor(Color.BLACK);
			b.drawString("Game Over", 184, 152);
			b.drawString(String.format("Score: %d", score), 184, 172);
			b.drawString("Click to play again", 112, 192);

		} else if (gameNotStartedYet) {
			b.setColor(Colors.TRANSPARENT_WHITE);
			b.fillRoundRect(128, 128, 256, 64, 32, 32);
			b.setFont(Fonts.GAME_FONT);
			b.setColor(Color.BLACK);
			b.drawString("Click to start", 148, 172);
		}
		Graphics g = bs.getDrawGraphics();
		g.drawImage(buffer, 0, 0, null);
		b.dispose();
		g.dispose();
		bs.show();
	}

	public synchronized void changeDifficulty(int difficulty) {
		level = difficulty;
		loadCircles();
		reset();
		gameOver = false;
		gameNotStartedYet = true;
	}

	public void showSequence() {
		for (int color : sequence) {
			bubbles.get(color).flash();
		}
		sequencePlaying = false;
	}

	public void addToSequence() {

		if (doSwap) {
			int swap = (int) (Math.random() * 10) + 1;
			if (swap < 5) {
				int b1 = (int) (Math.random() * bound), b2 = (int) (Math.random() * bound);
				bubbles.get(colors[b1]).swap(bubbles.get(colors[b2]));
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		int random = (int) (Math.random() * bound);
		int color = colors[random];
		sequence.add(color);
	}

	public void doSequence() {
		addToSequence();
		showSequence();
		sequenceInputting = true;
	}

	private void startSequenceThread(boolean delay) {

		if (delay) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		sequencePlaying = true;
		new Thread(() -> doSequence(), "Sequencer").start();
	}

	public void processClick(MouseEvent e) {

		if (gameOver) {
			gameOver = false;
			startSequenceThread(true);
		} else {
			if (gameNotStartedYet) {
				gameNotStartedYet = false;
			}
			if (!sequencePlaying && !sequenceInputting) {
				startSequenceThread(true);
			} else if (sequencePlaying) {
				e.consume();
			} else {
				if (inputFlashing > 0) {
					e.consume();
				} else {

					Bubble b = getBubbleMouseOver(e.getX(), e.getY());
					if (b == null) {
						return;
					}
					inputFlashing++;
					new Thread(() -> {
						b.quickFlash();
						inputFlashing--;
					}, "Flasher").start();

					if (bubbles.get(sequence.get(index)).equals(b)) {
						index++;
						if (index == sequence.size()) {
							sequenceInputting = false;
							sequencePlaying = false;
							while (inputFlashing > 0) {
								try {
									Thread.sleep(10);
								} catch (InterruptedException e1) {
									e1.printStackTrace();
								}
							}
							index = 0;
							startSequenceThread(true);
						}
					} else {
						while (inputFlashing > 0) {
							try {
								Thread.sleep(10);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
						gameOver();
					}

				}
			}
		}
	}

	private void reset() {
		sequencePlaying = false;
		sequenceInputting = false;
		sequence.clear();
		index = 0;
	}

	private void gameOver() {
		score = sequence.size() - 1;
		gameOver = true;
		database.addScore(HIGHSCORE_IDENTIFIER, level, score, Date.from(Instant.now()));
		database.write(HIGHSCORE_IDENTIFIER);
		reset();
	}

	private Bubble getBubbleMouseOver(int x, int y) {
		for (Bubble b : bubbles.values()) {
			if (b.contains(x, y)) {
				return b;
			}
		}
		return null;
	}

	private void tick() {
		synchronized (this) {
			for (Bubble b : bubbles.values()) {
				b.tick();
			}
		}
	}

	@Override
	public void run() {
		GameTimer gt = new GameTimer();

		while (running) {
			tick();
			if (gt.getElapsedTime() < 30) {
				render();
				gt.restart();
			}
		}

	}

}
