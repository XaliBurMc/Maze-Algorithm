package mazealgo.util;

import java.awt.Graphics;

/**
 * @author Fabian
 */

public class WayPoint {
	public int x;
	public int y;

	public int coasts;
	public int allCoasts;
	public int heuristic;

	WayPoint before;

	public WayPoint(int x, int y, int coasts, int heuristic, WayPoint before) {
		this.x = x;
		this.y = y;
		this.coasts = coasts;
		this.heuristic = heuristic;
		this.before = before;

		if (this.before == null) {
			this.allCoasts = 0;
		} else {
			this.allCoasts = this.before.allCoasts + this.coasts;
		}
	}

	public int getTotalCoasts() {
		return this.getCoasts() + this.coasts + this.heuristic;
	}

	public int getCoasts() {
		return allCoasts;
	}

	public void setBefore(WayPoint newPoint) {
		this.before = newPoint;
	}

	public void draw(Graphics g, int ratio) {
		if (this.before != null) {
			WayPoint b = this.before;
			g.fillRect(b.x * ratio, b.y * ratio, ratio, ratio);
			boolean finish = false;
			while (finish == false) {
				if (b.before != null) {
					b = b.before;
					g.fillRect(b.x * ratio, b.y * ratio, ratio, ratio);
				} else {
					finish = true;
					break;
				}
			}
		}
	}
}
