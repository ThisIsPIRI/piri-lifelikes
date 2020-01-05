package com.thisispiri.lifelike;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class LifeSimulatorTest {
	boolean[] life_create = {false, false, false, true, false, false, false, false, false};
	private boolean[] life_kill = {true, true, false, false, true, true, true, true, true};
	@Test public void testSim() {
		boolean[][] grid = {{true, false}, {false, false}};
		LifeSimulator sim = new LifeSimulator(2, 2, life_create, life_kill);
		grid = sim.step(grid);
		assertTrue(Arrays.deepEquals(grid, new boolean[][]{{false, false}, {false, false}}));
	}
}
