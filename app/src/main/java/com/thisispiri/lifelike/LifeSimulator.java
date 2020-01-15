package com.thisispiri.lifelike;

import java.util.List;
import java.util.Random;

/**Simulates a life-like environment.
 * Coordinates begin at upper left (0,0) and end at lower right (height,width).
 * When birthNumbers[n] is true but surviveNumbers[n] is false, the cell will be flipped(change is favored over stasis).*/
public class LifeSimulator {
	public int width, height;
	/**A 9-element array. A cell is born if birthNumbers[the number of its alive neighbors] is true.*/
	public boolean[] birthNumbers;
	/**A 9-element array. An alive cell survives if surviveNumbers[the number of its alive neighbors] is true.*/
	public boolean[] surviveNumbers;
	public LifeSimulator(int width, int height, boolean[] birthNumbers, boolean[] surviveNumbers) {
		this.width = width;
		this.height = height;
		this.birthNumbers = birthNumbers;
		this.surviveNumbers = surviveNumbers;
	}
	/**Simulates one step of life and returns the result in a newly allocated array.
	 * Slow compared to {@link LifeSimulator#step(boolean[][], boolean[][])}*/
	public boolean[][] step(boolean[][] grid) {
		boolean[][] result = new boolean[height][width];
		step(grid, result);
		return result;
	}
	/**Simulates one step of life and writes the result in {@code result}.
	 * @param grid The current state of lives. Must be rectangular(i.e. all rows must have same length).
	 * @param result The array to write results to. Must be different from {@code grid}.*/
	public void step(boolean[][] grid, boolean[][] result) {
		int checkResult;
		for(int h = 0; h < height; h++) {
			for(int w = 0; w < width; w++) {
				checkResult = safeCellCheck(w, h, grid);
				result[h][w] = (birthNumbers[checkResult] && !grid[h][w]) || (surviveNumbers[checkResult] && grid[h][w]);
			}
		}
	}
	/***Returns the number of cells adjacent to grid[y][x].*/
	private int safeCellCheck(int x, int y, boolean[][] grid) {
		int sum = 0;
		if(y > 0) {
			if(x > 0)
				if(grid[y - 1][x - 1]) sum++;
			if(grid[y - 1][x]) sum++;
			if(x < width - 1)
				if (grid[y - 1][x + 1]) sum++;
		}
		if(y < height - 1) {
			if(x > 0)
				if(grid[y+1][x-1]) sum++;
			if(grid[y+1][x]) sum++;
			if(x < width - 1)
				if(grid[y+1][x+1]) sum++;
		}
		if(x > 0)
			if(grid[y][x-1]) sum++;
		if(x < width - 1)
			if(grid[y][x+1]) sum++;

		return sum;
	}
	/**Revives or kills all cells in a square area centered on (x, y).
	 * @param grid The grid to work on. Must be rectangular(i.e. all rows must have same length).
	 * @param x x coordinate of the center.
	 * @param y y coordinate of the center.
	 * @param size The length of the square.
	 * @param revive If true, cells inside the area are revived. If false, they are killed.*/
	public void paintSquare(boolean[][] grid, int x, int y, int size, boolean revive) {
		grid[y][x] = revive;
		for (int i = 2; i <= size; i++) {
			if (i % 2 == 0) {
				if (y + i / 2 < height)
					//(x - i / 2 + 1, y + i / 2) ~ (x + i / 2, y + i / 2) horizontal rightward
					for (int j = x - i / 2 + 1; j <= x + i / 2; j++) {
						if (xInBoundary(j)) grid[y + i / 2][j] = revive;
					}
				if (x + i / 2 < width)
					//(x + i / 2, y + i / 2) ~ (x + i / 2, y - i / 2 + 1) vertical upward
					for (int j = y + i / 2 - 1; j >= y - i / 2 + 1; j--) {
						if (yInBoundary(j)) grid[j][x + i / 2] = revive;
					}
			}
			else {
				if (y - i / 2 >= 0)
					//(x + i / 2, y - i / 2) ~ (x - i / 2, y - i / 2) horizontal leftward
					for (int j = x + i / 2; j >= x - i / 2; j--) {
						if (xInBoundary(j)) grid[y - i / 2][j] = revive;
					}
				if (x - i / 2 >= 0)
					//(x - i / 2, y - i / 2) ~ (x - i / 2, y + i / 2) vertical downward
					for (int j = y - i / 2 + 1; j <= y + i / 2; j++) {
						if (yInBoundary(j)) grid[j][x - i / 2] = revive;
					}
			}
		}
	}
	/**Adds (x, y), then up to 4 random {@code Point}s no farther than 4 cells from (x, y), to the {@code list}.
	 * @param random the {@code Random} object to use.*/
	public void paintRandom(List<Point> list, int x, int y, Random random) {
		list.add(new Point(x, y));
		int[] xT = {1, -1, -1, 1}, yT = {1, -1, 1, -1};
		for(int i = 0;i < xT.length;i++) {
			int cX = x + xT[i] * random.nextInt(4);
			int cY = y + yT[i] * random.nextInt(4);
			if(inBoundary(cX, cY)) list.add(new Point(cX, cY));
		}
	}
	private boolean xInBoundary(int x) {return x < width && x >= 0;}
	private boolean yInBoundary(int y) {return y < height && y >= 0;}
	private boolean inBoundary(int x, int y) {return xInBoundary(x) && yInBoundary(y);}
}
