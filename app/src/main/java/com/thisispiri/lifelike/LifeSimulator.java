package com.thisispiri.lifelike;

/**Simulates a life-like environment.*/
public class LifeSimulator {
	public int width, height;
	public boolean[] createNeighbors, killNeighbors;
	public LifeSimulator(int width, int height, boolean[] createNeighbors, boolean[] killNeighbors) {
		this.width = width;
		this.height = height;
		this.createNeighbors = createNeighbors;
		this.killNeighbors = killNeighbors;
	}
	/**Simulates one step of life and returns the result in a newly allocated array.
	 * Slow compared to {@link LifeSimulator#step(boolean[][], boolean[][])}*/
	public boolean[][] step(boolean[][] grid) {
		boolean[][] result = new boolean[height][width];
		step(grid, result);
		return result;
	}
	/**Simulates one step of life and writes the result in {@code result}.
	 * @param grid The current state of lives.
	 * @param result The array to write results to. Must be different from {@code grid}.*/
	public void step(boolean[][] grid, boolean[][] result) {
		int checkResult;
		for(int h = 0; h < height; h++) {
			for(int w = 0; w < width; w++) {
				checkResult = safeCellCheck(w, h, grid);
				result[h][w] = (createNeighbors[checkResult] && !grid[h][w]) || (!killNeighbors[checkResult] && grid[h][w]);
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
}
