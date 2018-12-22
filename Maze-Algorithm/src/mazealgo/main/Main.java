package mazealgo.main;

import mazealgo.frame.Maze;

/**
 * @author Fabian
 */

public class Main {

	static Maze frame;

	public static void main(String[] args) {
		int[] size = { 101, 61 };
		frame = new Maze(size);
	}
}
