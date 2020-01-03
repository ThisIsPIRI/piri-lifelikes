package com.thisispiri.lifelike;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.preference.PreferenceManager;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
	public int cellSize, width, height;
	int screenHeight, screenWidth, pauseBrushSize, lifecycle;
	@ColorInt int cellColor, backgroundColor;
	boolean[][] gridArray;
	boolean[][] nextArray;
	LifeGrid lifeGrid;
	LifeTouchListener tLis = new LifeTouchListener();
	LifeSimulator mainThread;
	private Button start; //start and pause
	private LifeButtonListener bLis;
	boolean isPlaying = false;
	boolean[] createNeighbors = new boolean[9], killNeighbors = new boolean[9];

	//screen update
	private static class UiHandler extends Handler {
		WeakReference<MainActivity> activity;
		@Override public void handleMessage(Message msg) {
			activity.get().lifeGrid.invalidate();}
		UiHandler(WeakReference<MainActivity> m) { activity = m; }
	}
	Handler handler = new UiHandler(new WeakReference<>(this));

	private void updatePreferences() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		cellSize = pref.getInt("cellSize", 16);
		pauseBrushSize = pref.getInt("pauseBrushSize", 1);
		lifecycle = pref.getInt("lifecycle", 0);
		cellColor = pref.getInt("cellColor", 0xFF000000);
		backgroundColor = pref.getInt("backgroundColor", 0xFFFFFFFF);
		//retrieve data from MultiSelectListPreferences
		Set<String> set = pref.getStringSet("neighborCreate", null);
		Arrays.fill(createNeighbors, false);
		if(set == null) createNeighbors[3] = true;
		else for (String s : set) createNeighbors[Integer.valueOf(s)] = true;
		set = pref.getStringSet("neighborKill", null);
		if(set == null) {
			Arrays.fill(killNeighbors, true);
			killNeighbors[2] = killNeighbors[3] = false;
		}
		else {
			Arrays.fill(killNeighbors, false);
			for (String s : set)
				killNeighbors[Integer.valueOf(s)] = true;
		}
	}
	@Override public void onStart() { //refresh values
		super.onStart();
		int cellSizeTemp = cellSize;
		updatePreferences();
		//if cell size has changed
		if(cellSize != cellSizeTemp) {
			//readjust array size and redraw.
			boolean[][] temp = new boolean[height][width];
			for(int i = 0;i < height;i++)
				temp[i] = Arrays.copyOf(gridArray[i], width);
			width = screenWidth / cellSize;
			height = screenHeight / cellSize;
			gridArray = new boolean[height][width];
			for(int i = 0;i < (temp.length > height ? height : temp.length);i++)
				gridArray[i] = Arrays.copyOf(temp[i], width);
			nextArray = new boolean[height][width];
		}
		lifeGrid.setData(gridArray, cellSize, height, width, cellColor, backgroundColor);
		lifeGrid.invalidate();
	}

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//save screen resolution
		Point screenSize = new Point();
		getWindowManager().getDefaultDisplay().getSize(screenSize);
		screenHeight = (int) (screenSize.y * 0.9);
		screenWidth = (screenSize.x);
		updatePreferences();

		//initialize grid
		width = screenWidth / cellSize;
		height = screenHeight / cellSize;
		gridArray = new boolean[height][width];
		nextArray = new boolean[height][width];
		setContentView(R.layout.activity_main);
		lifeGrid = findViewById(R.id.view);
		lifeGrid.setOnTouchListener(tLis);

		//initialize Button references
		bLis = new LifeButtonListener(this);
		start = findViewById(R.id.start);
		start.setOnClickListener(bLis);
		findViewById(R.id.setting).setOnClickListener(bLis);
		findViewById(R.id.clear).setOnClickListener(bLis);
		//start calculation thread
		//mainThread = new Thread(new LifeSimulator());
		mainThread = new LifeSimulator();
		//mainThread.start();
	}
	@Override public void onBackPressed() {
		isPlaying = false;
		mainThread.stopped = true;
		super.onBackPressed();
	}

	//handle button click events
	private class LifeButtonListener implements View.OnClickListener
	{
		MainActivity activity;
		LifeButtonListener(MainActivity activity) {
			this.activity = activity;
		}
		@Override public void onClick(View v) {
			switch(v.getId()) {
			case R.id.start:
				if(isPlaying) {
					isPlaying = false;
					mainThread.stopped = true;
					start.setText(R.string.start);
				}
				else {
					isPlaying = true;
					mainThread = new LifeSimulator();
					mainThread.stopped = false;
					mainThread.start();
					start.setText(R.string.pause);
				}
				break;
			case R.id.setting:
				isPlaying = false;
				mainThread.stopped = true;
				start.setText(R.string.start);
				Intent toSetting = new Intent(MainActivity.this, SettingActivity.class);
				activity.startActivityForResult(toSetting, 0);
				break;
			case R.id.clear:
				isPlaying = false;
				mainThread.stopped = true;
				gridArray = new boolean[height][width];
				nextArray = new boolean[height][width];
				lifeGrid.setData(gridArray);
				lifeGrid.invalidate();
				start.setText(R.string.start);
				break;
			}
		}
	}

	boolean xInBoundary(int x) { return x < width && x >= 0; }
	boolean yInBoundary(int y) { return y < height && y >= 0; }
	//handle touch events
	private class LifeTouchListener implements View.OnTouchListener {
		private final Random r = new Random();
		@Override public boolean onTouch(View v, MotionEvent m) {
			int x, y;
			x = Math.round(m.getX());
			y = Math.round(m.getY());
			final int iX = x * width / screenWidth, iY = y * height / screenHeight;

			//if playing now, randomly resurrect cells near touch location according to brush size. if paused now, resurrect brush size * brush size cells.
			if((iY >= 0) && (iX >= 0) && (iY < (height - 1)) && (iX < (width - 1))) {
				if(isPlaying) {
					int temp1, temp2;
					//Resurrect random cells.
					mainThread.simulatorTouchArray[iY][iX] = true;
					temp1 = r.nextInt(4);
					temp2 = r.nextInt(4);
					if (iY + temp1 > 0 && iY + temp1 < height - 1 && iX + temp2 > 0 && iX + temp2 < width - 1)
						mainThread.simulatorTouchArray[iY + temp1][iX + temp2] = true;

					temp1 = r.nextInt(4);
					temp2 = r.nextInt(4);
					if (iY - temp1 > 0 && iY - temp1 < height - 1 && iX - temp2 > 0 && iX - temp2 < width - 1)
						mainThread.simulatorTouchArray[iY - temp1][iX - temp2] = true;

					temp1 = r.nextInt(4);
					temp2 = r.nextInt(4);
					if (iY + temp1 > 0 && iY + temp1 < height - 1 && iX - temp2 > 0 && iX - temp2 < width - 1)
						mainThread.simulatorTouchArray[iY + temp1][iX - temp2] = true;

					temp1 = r.nextInt(4);
					temp2 = r.nextInt(4);
					if (iY - temp1 > 0 && iY - temp1 < height - 1 && iX + temp2 > 0 && iX + temp2 < width - 1)
						mainThread.simulatorTouchArray[iY - temp1][iX + temp2] = true;
				}
				else {
					//Resurrect a rectangular area.
					gridArray[iY][iX] = true;
					for(int i = 2;i <= pauseBrushSize;i++) {
						if(i % 2 == 0) {
							if(iY + i / 2 < height)
								for(int j = iX - i / 2 + 1;j <= iX + i / 2;j++) { //(x - i / 2 + 1, y + i / 2) ~ (x + i / 2, y + i / 2) horizontal rightward
									if(xInBoundary(j)) gridArray[iY + i / 2][j] = true;
								}
							if(iX + i / 2 < width)
								for(int j = iY + i / 2 - 1;j >= iY - i / 2 + 1;j--) { //(x + i / 2, y + i / 2) ~ (x + i / 2, y - i / 2 + 1) vertical upward
									if(yInBoundary(j)) gridArray[j][iX + i / 2] = true;
								 }
						}
						else {
							if(iY - i / 2 >= 0)
								for(int j = iX + i / 2;j >= iX - i / 2;j--) { //(x + i / 2, y - i / 2) ~ (x - i / 2, y - i / 2) horizontal leftward
									if(xInBoundary(j)) gridArray[iY - i / 2][j] = true;
								}
							if(iX - i / 2 >= 0)
								for(int j = iY - i / 2 + 1;j <= iY + i / 2;j++) { //(x - i / 2, y - i / 2) ~ (x - i / 2, y + i / 2) vertical downward
									if(yInBoundary(j)) gridArray[j][iX - i / 2] = true;
								}
						}
					}
				}
			}
			lifeGrid.invalidate();
			return true;
		}
	}

	private class LifeSimulator extends Thread {
		private final int[] xP = {0, 1, 1, 1, 0, -1, -1, -1};
		private final int[] yP = {-1, -1, 0, 1, 1, 1, 0, -1};
		boolean stopped = false; //if true, thread will stop.
		boolean[][] simulatorTouchArray = new boolean[height][width]; //Array for getting touch input from the main thread
		@Override public void run() {
			int checkResult;
			while(!stopped) {
				//Check the entire grid to determine if cells are to die or not
				for(int h = 0; h < height; h++) {
					for(int w = 0; w < width; w++) {
						checkResult = safeCellCheck(w, h);
						nextArray[h][w] = ((createNeighbors[checkResult] && !gridArray[h][w]) || (!killNeighbors[checkResult] && gridArray[h][w]) || simulatorTouchArray[h][w]);
					}
				}
				//Swap Arrays. We don't need the values in the previous gridArray anymore, so we can safely rename it to nextArray to be overwritten.
				//TODO: Sometimes, cells drawn while paused aren't shown, though they do when the simulation starts again.
				boolean[][] tempArray = gridArray;
				gridArray = nextArray;
				nextArray = tempArray;
				//Request UI update to the handler.
				handler.sendEmptyMessage(0);
				simulatorTouchArray = new boolean[height][width];
				//Handle interrupts.
				try {
					Thread.sleep(lifecycle);
				}
				catch(InterruptedException e) {
					break;
				}
			}
		}
		/***Returns the number of cells adjacent to gridArray[y][x].*/
		private int safeCellCheck(int x, int y) {
			int sum = 0;
			if(y > 0) {
				if(x > 0)
					if(gridArray[y - 1][x - 1]) sum++;
				if(gridArray[y - 1][x]) sum++;
				if(x < width - 1)
					if (gridArray[y - 1][x + 1]) sum++;
			}
			if(y < height - 1) {
				if(x > 0)
					if(gridArray[y+1][x-1]) sum++;
				if(gridArray[y+1][x]) sum++;
				if(x < width - 1)
					if(gridArray[y+1][x+1]) sum++;
			}
			if(x > 0)
				if(gridArray[y][x-1]) sum++;
			if(x < width - 1)
				if(gridArray[y][x+1]) sum++;

			return sum;
		}
		private int cellCheck(int x, int y) {
			int sum = 0;
			for(int i = 0;i < 8;i++) {
				if(gridArray[y + yP[i]][x + xP[i]]) sum++;
			}
			return sum;
		}
	}
}

