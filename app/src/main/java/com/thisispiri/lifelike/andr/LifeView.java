package com.thisispiri.lifelike.andr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**Draws a 2d array of booleans, with each element represented by a square.*/
public class LifeView extends View {
	private final Paint background, cell;
	private int cellSize, height, width;
	private boolean[][] array;
	public LifeView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		background = new Paint();
		cell = new Paint();
	}
	/**Invalidates the View after changing the array to draw to {@code array} argument.*/
	public void invalidate(final boolean[][] array) {
		this.array = array;
		super.invalidate();
	}
	@Override public void onDraw(final Canvas canvas) {
		canvas.drawRect(0, 0, getWidth(), getHeight(), background);
		for(int h = 0;h < height;h++) {
			for(int w = 0;w < width;w++) {
				if (array[h][w]) {
					canvas.drawRect(w * cellSize, h * cellSize, w * cellSize + cellSize, h * cellSize + cellSize, cell);
				}
			}
		}
	}
	/**Call this once to ensure cells are drawn.
	 * @param array The grid to draw.
	 * @param cellSize The length of individual cell squares.
	 * @param height The height of the grid in cells.
	 * @param width The width of the grid in cells.
	 * @param cellColor The color of cells in ARGB.
	 * @param backgroundColor The background color in ARGB.*/
	public void setData(final boolean[][] array, final int cellSize, final int height, final int width,
						final int cellColor, final int backgroundColor) {
		setData(array, cellSize, height, width);
		cell.setColor(cellColor);
		background.setColor(backgroundColor);
	}
	/**@see LifeView#setData(boolean[][], int, int, int, int, int)*/
	public void setData(final boolean[][] array, final int cellSize, final int height, final int width) {
		this.array = array;
		this.cellSize = cellSize;
		this.height = height;
		this.width = width;
	}
}
