package mazealgo.frame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import mazealgo.util.WayPoint;

/**
 * @author Fabian
 */

@SuppressWarnings("serial")
public class Maze extends JPanel implements ActionListener {

	JFrame frame;
	public static int[] size;
	public static int[][] maze;
	public static int[][] mazeWork;
	public static int ratio = 16;
	public static ArrayList<int[]> work = new ArrayList<>();

	public static boolean working = false;

	public Maze(int[] size) {
		Maze.size = size;

		frame = new JFrame("Maze-Algorithm");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize((width() * ratio) + 6, (height() * ratio) + 29);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.add(this);

		init();
	}

	Timer time;

	public static boolean paint = false;
	public static boolean clear = false;

	public static int mouseX = 0;
	public static int mouseY = 0;

	public static int alg = 2; // can be 1 or 2
	public static boolean pathDiago = false;

	public static int resetDelay = 10; // 10
	public static int alg1Delay = 2; // 2
	public static int alg2Delay = 4; // 4
	public static int pathDelay = 4; // 4

	public static ArrayList<int[]> frontiers; // alg1
	public static ArrayList<int[]> moves; // alg2

	public static int startX = 1;
	public static int startY = 1;
	public static int endX = 1;
	public static int endY = 1;

	public static ArrayList<WayPoint> openList;
	public static ArrayList<WayPoint> closedList;
	public static WayPoint currentPoint;
	public static WayPoint finish;

	public static boolean search = false;

	private void init() {

		maze = new int[width()][height()];

		for (int x = 0; x < width(); x++) {
			for (int y = 0; y < height(); y++) {
				maze[x][y] = 0;
			}
		}
		
		mazeWork = new int[width()][height()];

		for (int x = 0; x < width(); x++) {
			for (int y = 0; y < height(); y++) {
				mazeWork[x][y] = 0;
			}
		}

		endX = width() - 2;
		endY = height() - 2;

		frontiers = new ArrayList<>();
		moves = new ArrayList<>();

		openList = new ArrayList<WayPoint>();
		closedList = new ArrayList<WayPoint>();

		frame.addKeyListener(new keyType());
		frame.addMouseListener(new mouseClick());

		time = new Timer(15, this);
		time.start();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		try {
			// g.drawRect(0, 0, width() * 64, height() * 64);
			int x = 0;
			int y = 0;
			for (int[] types : maze) {
				for (int type : types) {
					switch (type) {
					case 1:
						g.setColor(Color.BLACK);
						g.fillRect(x * ratio, y * ratio, ratio, ratio);
						break;

					default:
						g.setColor(Color.WHITE);
						g.fillRect(x * ratio, y * ratio, ratio, ratio);
						// g.drawRect(x * ratio, y * ratio, ratio, ratio);
						break;
					}
					y++;
				}
				y = 0;
				x++;
			}

			for (int[] pos : frontiers) {
				g.setColor(Color.BLUE);
				g.fillRect(pos[0] * ratio, pos[1] * ratio, ratio, ratio);
			}

			// for (int[] pos : moves) {
			// g.setColor(Color.BLUE);
			// g.fillRect(pos[0] * ratio, pos[1] * ratio, ratio, ratio);
			// }

			for (int[] workPos : work) {
				int workX = workPos[0];
				int workY = workPos[1];
				g.setColor(Color.RED);
				g.fillRect(workX * ratio, workY * ratio, ratio, ratio);
			}

			if (finish == null) {
				for (int i = 0; i < Maze.openList.size(); i++) {
					g.setColor(Color.BLUE);
					g.fillRect(Maze.openList.get(i).x * ratio, Maze.openList.get(i).y * ratio, ratio, ratio);
					// g.setColor(Color.WHITE);
					// g.drawString("" + Frame.openList.get(i).getCoasts(), (int)
					// (Frame.openList.get(i).x * ratio), (int) (Frame.openList.get(i).y * ratio +
					// 0.75*ratio));
				}

				for (int i = 0; i < Maze.closedList.size(); i++) {
					g.setColor(Color.YELLOW);
					g.fillRect(Maze.closedList.get(i).x * ratio, Maze.closedList.get(i).y * ratio, ratio, ratio);
				}
			}

			if (currentPoint != null && (search == true || finish != null)) {
				g.setColor(Color.GREEN);

				currentPoint.draw(g, ratio);

				g.setColor(Color.RED);
				g.fillRect(currentPoint.x * ratio, currentPoint.y * ratio, ratio, ratio);
				// g.setColor(Color.WHITE);
				// g.drawString("" + currentPoint.getCoasts(), (int) (currentPoint.x * ratio),
				// (int) (currentPoint.y * ratio + 0.75*ratio));
				g.setColor(Color.RED);
				g.fillRect(startX * ratio, startY * ratio, ratio, ratio);
			}

			// if (!working) {
			// g.setColor(Color.BLACK);
			// g.fillOval(mouseX - 10, mouseY - 10, 20, 20);
			// g.setColor(Color.WHITE);
			// g.fillOval(mouseX - 2, mouseY - 2, 4, 4);
			// }
		} catch (Exception e) {
		}
	}

	@SuppressWarnings("unused")
	@Override
	public void actionPerformed(ActionEvent action) {
		Point mousePos = getMousePosition();
		if (mousePos != null && !working) {
			int mouseX = (int) (mousePos.getX());
			int mouseY = (int) (mousePos.getY());
			// if (paint) {
			// maze[posY][posX] = 1;
			// }
			// if (clear) {
			// maze[posY][posX] = 0;
			// }

			HashMap<Rectangle, int[]> list = new HashMap<>();

			int x = 0;
			int y = 0;
			for (int[] types : maze) {
				for (int type : types) {
					list.put(new Rectangle(x * ratio, y * ratio, ratio, ratio), new int[] { x, y });
					y++;
				}
				y = 0;
				x++;
			}

			Line2D line = new Line2D.Float(new Point(Maze.mouseX, Maze.mouseY), new Point(mouseX, mouseY));

			for (Rectangle rec : list.keySet()) {
				int[] pos = list.get(rec);
				if (line.intersects(rec)) {
					int recX = pos[0];
					int recY = pos[1];

					if (paint) {
						maze[recX][recY] = 1;
					}
					if (clear) {
						maze[recX][recY] = 0;
					}
				}
			}

			Maze.mouseX = mouseX;
			Maze.mouseY = mouseY;
		}

		repaint();
	}

	private class keyType extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_SPACE && !working) {
				resetPath();
				randomMaze();
			}
			if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && !working) {
				resetPath();
				resetMaze();
			}
			if (e.getKeyCode() == KeyEvent.VK_ENTER && !working) {
				findPath();
			}
		}

		public void keyReleased(KeyEvent e) {
		}
	}

	private class mouseClick extends JComponent implements MouseListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			resetPath();
			if (e.getButton() == MouseEvent.BUTTON1 && !working) {
				paint = true;
			}
			if (e.getButton() == MouseEvent.BUTTON3 && !working) {
				clear = true;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1 && !working) {
				paint = false;
			}
			if (e.getButton() == MouseEvent.BUTTON3 && !working) {
				clear = false;
			}
		}
	}

	public static int width() {
		return size[0];
	}

	public static int height() {
		return size[1];
	}

	public static void resetMaze() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					working = true;
					for (int x = 0; x < width(); x++) {
						for (int y = 0; y < height(); y++) {
							maze[x][y] = 0;
						}
						Thread.sleep(Maze.resetDelay);
					}
					working = false;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static void findPath() {
		resetPath();

		Maze.search = true;
		openList.add(new WayPoint(startX, startY, 1, Maze.heuristic(Maze.startX, startY), null));
		mazeWork[startX][startY] = 1;
		new Thread(new Runnable() {
			@Override
			public void run() {
				working = true;
				currentPoint = null;
				while (true) {
					try {
						if (Maze.finish != null) {
							currentPoint = Maze.finish;
							Maze.search = false;
							break;
						} else if (Maze.openList.isEmpty() == false && Maze.search == true) {
							currentPoint = Maze.aStar();
						} else if (openList.isEmpty()) {
							currentPoint = null;
							break;
						}
						Thread.sleep(Maze.pathDelay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				working = false;
			}
		}).start();
	}

	private static WayPoint aStar() {

		WayPoint bestWayPoint;

		bestWayPoint = openList.get(Maze.getFirstBestListEntry(openList));
		closedList.add(openList.remove(Maze.getFirstBestListEntry(openList)));
		
		if (bestWayPoint.x == Maze.endX && bestWayPoint.y == Maze.endY) {
			// bestWayPoint.print();
			Maze.finish = bestWayPoint;
			return bestWayPoint;
		}

		Maze.openListAddHelper(bestWayPoint.x, bestWayPoint.y - 1, openList, closedList, bestWayPoint);

		Maze.openListAddHelper(bestWayPoint.x + 1, bestWayPoint.y, openList, closedList, bestWayPoint);

		Maze.openListAddHelper(bestWayPoint.x, bestWayPoint.y + 1, openList, closedList, bestWayPoint);

		Maze.openListAddHelper(bestWayPoint.x - 1, bestWayPoint.y, openList, closedList, bestWayPoint);
		
		
		if (pathDiago) {
			
			Maze.openListAddHelper(bestWayPoint.x - 1, bestWayPoint.y - 1, openList, closedList, bestWayPoint);

			Maze.openListAddHelper(bestWayPoint.x + 1, bestWayPoint.y - 1, openList, closedList, bestWayPoint);

			Maze.openListAddHelper(bestWayPoint.x + 1, bestWayPoint.y + 1, openList, closedList, bestWayPoint);

			Maze.openListAddHelper(bestWayPoint.x - 1, bestWayPoint.y + 1, openList, closedList, bestWayPoint);
			
		}

		return bestWayPoint;
	}

	private static void openListAddHelper(int x, int y, ArrayList<WayPoint> openList, ArrayList<WayPoint> closedList,
			WayPoint vorher) {
		if (x >= 0 && y >= 0 && x < width() && y < height()) {
			if (Maze.isPointInList(x, y, closedList) == false && Maze.maze[x][y] == 0
					&& Maze.isPointInList(x, y, openList) == false) {
				openList.add(new WayPoint(x, y, 1, Maze.heuristic(x, y), vorher));
				mazeWork[x][y] = 1;
			}
		}

	}

	private static int getFirstBestListEntry(ArrayList<WayPoint> list) {
		if (list == null)
			return 0;
		int best = list.get(0).getTotalCoasts();

		for (int i = 1; i < list.size(); i++) {
			if (list.get(i).getTotalCoasts() < best) {
				best = list.get(i).getTotalCoasts();
			}
		}
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getTotalCoasts() == best) {
				return i;
			}
		}
		return 0;
	}

	private static boolean isPointInList(int x, int y, ArrayList<WayPoint> list) {
//		for (WayPoint wp : list) {
//			if (wp.x == x && wp.y == y) {
//				return true;
//			}
//		}
		if (mazeWork[x][y] == 1) {
			return true;
		}
		
		return false;
	}

	private static int heuristic(int x, int y) {
		int dx = Math.abs(x - Maze.endX);

		int dy = Math.abs(y - Maze.endY);

		return dx + dy;

	}

	public static void resetPath() {
		openList = new ArrayList<WayPoint>();
		closedList = new ArrayList<WayPoint>();
		for (int x = 0; x < width(); x++) {
			for (int y = 0; y < height(); y++) {
				mazeWork[x][y] = 0;
			}
		}
		Maze.finish = null;
		Maze.currentPoint = null;
	}

	public static void randomMaze() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					generateMaze(width(), height());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static void generateMaze(int width, int height) throws InterruptedException {

		working = true;

		for (int x = 0; x < width(); x++) {
			for (int y = 0; y < height(); y++) {
				maze[x][y] = 1;
			}
			Thread.sleep(Maze.resetDelay);
		}

		switch (alg) {
		case 1:
			// --------------------------------------------------------------------------------------
			final Random random = new Random();
			int x = 1;
			int y = 1;
			frontiers.add(new int[] { x, y, x, y });

			while (!frontiers.isEmpty()) {
				final int[] f = frontiers.remove(random.nextInt(frontiers.size()));
				x = f[2];
				y = f[3];
				if (maze[x][y] == 1) {
					maze[f[0]][f[1]] = maze[x][y] = 0;
					if (x >= 2 && maze[x - 2][y] == 1) {
						frontiers.add(new int[] { x - 1, y, x - 2, y });
					}
					if (y >= 2 && maze[x][y - 2] == 1) {
						frontiers.add(new int[] { x, y - 1, x, y - 2 });
					}
					if (x < width - 3 && maze[x + 2][y] == 1) {
						frontiers.add(new int[] { x + 1, y, x + 2, y });
					}
					if (y < height - 3 && maze[x][y + 2] == 1) {
						frontiers.add(new int[] { x, y + 1, x, y + 2 });
					}
					work.add(new int[] { x, y });
				}
				Thread.sleep(Maze.alg1Delay);
				work.clear();
			}
			// --------------------------------------------------------------------------------------
			break;
		case 2:
			// --------------------------------------------------------------------------------------
			int posX = 1;
			int posY = 1;
			maze[posX][posY] = 0;

			moves.add(new int[] { posX, posY });

			while (true) {
				work.clear();
				String possibleDirections = "";
				if (posX + 2 > 0 && posX + 2 < width() - 1 && maze[posX + 2][posY] == 1) {
					possibleDirections += "E";
				}
				if (posX - 2 > 0 && posX - 2 < width() - 1 && maze[posX - 2][posY] == 1) {
					possibleDirections += "W";
				}
				if (posY - 2 > 0 && posY - 2 < height() - 1 && maze[posX][posY - 2] == 1) {
					possibleDirections += "S";
				}
				if (posY + 2 > 0 && posY + 2 < height() - 1 && maze[posX][posY + 2] == 1) {
					possibleDirections += "N";
				}
				if (possibleDirections != "") {
					// System.out.println(possibleDirections);
					String move = possibleDirections.split("")[new Random()
							.nextInt(possibleDirections.split("").length)];
					switch (move) {
					case "W":
						maze[posX - 2][posY] = 0;
						maze[posX - 1][posY] = 0;
						posX -= 2;
						break;
					case "E":
						maze[posX + 2][posY] = 0;
						maze[posX + 1][posY] = 0;
						posX += 2;
						break;
					case "S":
						maze[posX][posY - 2] = 0;
						maze[posX][posY - 1] = 0;
						posY -= 2;
						break;
					case "N":
						maze[posX][posY + 2] = 0;
						maze[posX][posY + 1] = 0;
						posY += 2;
						break;
					}
					moves.add(new int[] { posX, posY });
					work.add(new int[] { posX, posY });
				} else {
					if (moves.size() > 1) {
						moves.remove(moves.size() - 1);
						int[] newPos = moves.get(moves.size() - 1);
						posX = newPos[0];
						posY = newPos[1];
						work.add(new int[] { posX, posY });
					} else {
						moves.clear();
						break;
					}
				}
				Thread.sleep(Maze.alg2Delay);
			}
			// --------------------------------------------------------------------------------------
			break;
		default:
			break;
		}

		working = false;
	}
}
