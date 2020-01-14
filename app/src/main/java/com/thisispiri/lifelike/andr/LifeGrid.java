package com.thisispiri.lifelike.andr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**Draws a 2d array of booleans, with each element represented by a square.*/
public class LifeGrid extends View {
	private Paint background, cell;
	private int cellSize, height, width;
	private boolean[][] array;
	public LifeGrid(Context context, AttributeSet attrs) {
		super(context, attrs);
		background = new Paint();
		cell = new Paint();
	}
	/**Invalidates the View after changing the array to draw to {@code array} argument.*/
	public void invalidate(boolean[][] array) {
		this.array = array;
		super.invalidate();
	}
	@Override public void onDraw(Canvas canvas) {
		canvas.drawRect(0, 0, getWidth(), getHeight(), background);
		for(int h = 0;h < height;h++) {
			for(int w = 0;w < width;w++) {
				if (array[h][w]) {
					canvas.drawRect(w * cellSize, h * cellSize, w * cellSize + cellSize, h * cellSize + cellSize, cell);
				}
			}
		}
	}
	public void setData(boolean[][] array, int cellSize, int height, int width, int cellColor, int backgroundColor) { //set drawing data.
		this.array = array;
		this.cellSize = cellSize;
		this.height = height;
		this.width = width;
		cell.setColor(cellColor);
		background.setColor(backgroundColor);
	}
	public void setData(boolean[][] array) {
		this.array = array;
	}
}
