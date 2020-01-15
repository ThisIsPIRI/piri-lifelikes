package com.thisispiri.lifelike;

import com.thisispiri.common.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**Periodically advances the universe one step at a time.
 * <br>
 * Important note:
 * Arrays {@code grid} and {@code next} are swapped internally after each step to avoid allocating new arrays.
 * This means your original 'grid' will actually point to the next grid and your 'next' to the current one, every other step.
 * The Thread will pass the boolean[][] reference to the next grid it just computed to the callback, so you know which one's the most current.
 * <br>
 * <img src="doc-files/LifeThread-gridnext.png">*/
public class LifeThread extends Thread {
	private final int lifecycle;
	private final ParameteredRunnable callback;
	private boolean[][] grid, next;
	private final LifeSimulator sim;
	public boolean stopped = false; //if true, thread will stop.
	/**Add a {@code Point} here to set that point's value to true no matter what in the next cycle.*/
	public final List<Point> overrideList = Collections.synchronizedList(new ArrayList<Point>());

	/**Constructor.
	 * @param sim The {@link LifeSimulator} instance to use.
	 * @param lifecycle How many milliseconds to wait after computing a step.
	 * @param callback The ParameteredRunnable to call after every step.
	 *                 Reference to the most recent array will be passed as the argument. See the class description.
	 * @param grid The initial array.
	 * @param next Another array of same size as {@code grid}. See the class description for an important note on the naming.*/
	public LifeThread(LifeSimulator sim, int lifecycle, ParameteredRunnable callback, boolean[][] grid, boolean[][] next) {
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
			boolean[][] tempArray = grid;
			grid = next;
			next = tempArray;
			//Signal that a cycle has finished
			callback.run(grid);
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
