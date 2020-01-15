package com.thisispiri.lifelike;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class LifeSimulatorTest {
	@Test public void testConway0() {
		boolean[] conway_birth = {false, false, false, true, false, false, false, false, false};
		boolean[] conway_survive = {false, false, true, true, false, false, false, false, false};
		boolean[][] grid = {{true, false}, {false, false}};
		LifeSimulator sim = new LifeSimulator(2, 2, conway_birth, conway_survive);
		//Killing cells
		grid = sim.step(grid);
		assertTrue(Arrays.deepEquals(grid, new boolean[][]{{false, false}, {false, false}}));
		//Reviving cells
		grid[0][0] = true; grid[0][1] = true; grid[1][1] = true;
		grid = sim.step(grid);
		assertTrue(Arrays.deepEquals(grid, new boolean[][]{{true, true}, {true, true}}));
		//Letting cells survive
		for(int i = 0; i < 30;i++) grid = sim.step(grid);
		assertTrue(Arrays.deepEquals(grid, new boolean[][]{{true, true}, {true, true}}));
		//Killing cells
		grid[0][0] = false; grid[1][0] = false;
		grid = sim.step(grid);
		assertTrue(Arrays.deepEquals(grid, new boolean[][]{{false, false}, {false, false}}));
	}
}
