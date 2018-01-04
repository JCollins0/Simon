package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import com.jrc.math.Circle;
import com.jrc.util.Colors;

public class Bubble extends Circle {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1956355301779644156L;

	private int flashCount = 0;
	public static final int QUICK = 40;
	public static final int NORMAL = 100;

	public Bubble(int centerX, int centerY, int radius, Color color) {
		super(centerX, centerY, radius, color);
	}

	public synchronized void flash() {
		flash(NORMAL, 70);
	}

	public synchronized void quickFlash() {
		flash(QUICK, 10);
	}

	public synchronized void flash(int time, int wait) {
		flashCount = 0;
		try {
			wait(wait);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		flashCount = time;
		while (flashCount > 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void tick() {
		if (flashCount - 1 >= 0) {
			flashCount--;
			if (flashCount == 0) {
				notify();
			}
		}
	}

	public void swap(Bubble b) {
		Rectangle bounds1 = b.getBounds();
		Rectangle bounds2 = getBounds();
		Point center1 = b.getCenter();
		Point center2 = getCenter();
		int radius1 = b.getRadius();
		int radius2 = getRadius();

		int dx = (int) Math.signum(-(bounds1.x - bounds2.x));
		int dy = (int) Math.signum(-(bounds1.y - bounds2.y));
		int dr = (int) Math.signum(-(radius1 - radius2));

		while ((b.getBounds().y != bounds2.y || getBounds().x != bounds1.x)
				|| (getBounds().x != bounds1.x || getBounds().y != bounds1.y)
				|| (b.getRadius() != radius2 || getRadius() != radius1)) {
			if (b.getBounds().x != bounds2.x) {
				b.setBounds(b.getBounds().x + dx, b.getBounds().y, b.getBounds().width, b.getBounds().height);
			}
			if (b.getBounds().y != bounds2.y) {
				b.setBounds(b.getBounds().x, b.getBounds().y + dy, b.getBounds().width, b.getBounds().height);
			}

			if (b.getRadius() != radius2) {
				b.setRadius(b.getRadius() + dr);
				b.setBounds(b.getBounds().x, b.getBounds().y, b.getBounds().width + (dr << 1),
						b.getBounds().height + (dr << 1));
			}

			if (getBounds().x != bounds1.x) {
				setBounds(getBounds().x - dx, getBounds().y, getBounds().width, getBounds().height);
			}
			if (getBounds().y != bounds1.y) {
				setBounds(getBounds().x, getBounds().y - dy, getBounds().width, getBounds().height);
			}

			if (getRadius() != radius1) {
				setRadius(getRadius() - dr);
				setBounds(getBounds().x, getBounds().y, getBounds().width - (dr << 1), getBounds().height - (dr << 1));
			}

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		b.setBounds(bounds2);
		b.setCenter(center2);
		b.setRadius(radius2);
		setCenter(center1);
		setBounds(bounds1);
		setRadius(radius1);
	}

	@Override
	public void render(Graphics g) {
		if (flashCount == 0) {
			g.setColor(Colors.transparent(getColor()));
			g.fillOval(x, y, width, height);
		} else {
			super.render(g);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Bubble))
			return false;
		Bubble other = (Bubble) obj;
		if (!getColor().equals(other.getColor()))
			return false;
		return true;
	}

}
