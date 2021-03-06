package com.thisispiri.lifelike;

import com.thisispiri.common.Point;

import java.util.List;
import java.util.Random;

/**Simulates a rectangular life-like universe.
 * Coordinates begin at upper left (0,0) and end at lower right (height,width).
 * When birthNumbers[n] is true but surviveNumbers[n] is false, the cell will be flipped(change is favored over stasis).*/
public class LifeUniverse {
	public boolean[][] grid;
	/**A 9-element array. A cell is born if birthNumbers[the number of its alive neighbors] is true.*/
	public boolean[] birthNumbers;
	/**A 9-element array. An alive cell survives if surviveNumbers[the number of its alive neighbors] is true.*/
	public boolean[] surviveNumbers;
	public LifeUniverse(final boolean[][] grid, final boolean[] birthNumbers, final boolean[] surviveNumbers) {
		this.grid = grid;
		this.birthNumbers = birthNumbers;
		this.surviveNumbers = surviveNumbers;
	}
	/**Simulates one step of life and returns the result in a newly allocated array.
	 * Slow compared to {@link LifeUniverse#step(boolean[][], boolean[][])}*/
	public boolean[][] step(final boolean[][] grid) {
		this.grid = grid; //TODO DRY grid field update
		boolean[][] result = new boolean[grid.length][grid[0].length];
		step(grid, result);
		return result;
	}
	/**Simulates one step of life and writes the result in {@code result}.
	 * @param grid The current state of lives. Must be rectangular(i.e. all rows must have same length).
	 * @param result The array to write results to. Must be different from {@code grid}.*/
	public void step(final boolean[][] grid, final boolean[][] result) {
		this.grid = grid;
		int checkResult;
		for(int h = 0; h < grid.length; h++) {
			for(int w = 0; w < grid[0].length; w++) {
				checkResult = safeCellCheck(w, h, grid);
				result[h][w] = (birthNumbers[checkResult] && !grid[h][w]) || (surviveNumbers[checkResult] && grid[h][w]);
			}
		}
	}
	/***Returns the number of cells adjacent to grid[y][x].*/
	private int safeCellCheck(final int x, final int y, final boolean[][] grid) {
		int sum = 0;
		if(y > 0) {
			if(x > 0)
				if(grid[y - 1][x - 1]) sum++;
			if(grid[y - 1][x]) sum++;
			if(x < grid[0].length - 1)
				if (grid[y - 1][x + 1]) sum++;
		}
		if(y < grid.length - 1) {
			if(x > 0)
				if(grid[y+1][x-1]) sum++;
			if(grid[y+1][x]) sum++;
			if(x < grid[0].length - 1)
				if(grid[y+1][x+1]) sum++;
		}
		if(x > 0)
			if(grid[y][x-1]) sum++;
		if(x < grid[0].length - 1)
			if(grid[y][x+1]) sum++;

		return sum;
	}
	/**Revives or kills all cells in a square area centered on (x, y).
	 * @param grid The grid to work on. Must be rectangular(i.e. all rows must have same length).
	 * @param x x coordinate of the center.
	 * @param y y coordinate of the center.
	 * @param size The length of the square.
	 * @param revive If true, cells inside the area are revived. If false, they are killed.*/
	public void paintSquare(final boolean[][] grid, final int x, final int y, final int size, final boolean revive) {
		this.grid = grid;
		int offset = (size - 1) / 2;
		for(int i = y - offset;i < y - offset + size;i++) {
			for(int j = x - offset;j < x - offset + size;j++) {
				if(inBoundary(j, i)) grid[i][j] = revive;
			}
		}
	}
	/**Adds (x, y), then up to 4 random {@code Point}s no farther than 4 cells from (x, y), to the {@code list}.
	 * @param random the {@code Random} object to use.*/
	public void paintRandom(final List<Point> list, final int x, final int y, final Random random) {
		list.add(new Point(x, y));
		int[] xT = {1, -1, -1, 1}, yT = {1, -1, 1, -1};
		for(int i = 0;i < xT.length;i++) {
			int cX = x + xT[i] * random.nextInt(4);
			int cY = y + yT[i] * random.nextInt(4);
			if(inBoundary(cX, cY)) list.add(new Point(cX, cY));
		}
	}
	public boolean inBoundary(final int x, final int y) {
		return x < grid[0].length && x >= 0 && y < grid.length && y >= 0;
	}
}
