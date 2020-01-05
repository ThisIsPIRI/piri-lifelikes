package com.thisispiri.lifelike;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LifeThread extends Thread {
	private final int lifecycle;
	private final Runnable callback;
	private boolean[][] grid, next;
	private LifeSimulator sim;
	public boolean stopped = false; //if true, thread will stop.
	public final List<Point> overrideList = Collections.synchronizedList(new ArrayList<Point>());

	public LifeThread(LifeSimulator sim, int lifecycle, Runnable callback, boolean[][] grid, boolean[][] next) {
		this.sim = sim;
		this.lifecycle = lifecycle;
		this.callback = callback;
		this.grid = grid;
		this.next = next;
	}

	@Override public void run() {
		while(!stopped) {
			long timeStarted = System.currentTimeMillis();
			//Check the entire grid to determine if cells are to die or not
			sim.step(grid, next);
			//Apply overrides
			synchronized(overrideList) {
				for(Point p : overrideList) {
					next[p.y][p.x] = true;
				}
				overrideList.clear();
			}
			//Swap arrays to avoid allocation. The old values aren't needed, so we can safely rename it to next to be overwritten
			//TODO: Sometimes, cells drawn while paused aren't shown, though they do when the simulation starts again.
			boolean[][] tempArray = grid;
			grid = next;
			next = tempArray;
			//Signal that a cycle has finished
			callback.run();
			//Handle interrupts
			long timeToSleep = lifecycle - (System.currentTimeMillis() - timeStarted);
			try {
				if(timeToSleep > 0) Thread.sleep(timeToSleep);
			}
			catch(InterruptedException e) {
				break;
			}
		}
	}
}
