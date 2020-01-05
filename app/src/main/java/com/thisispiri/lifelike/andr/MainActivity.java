package com.thisispiri.lifelike.andr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.thisispiri.lifelike.LifeSimulator;
import com.thisispiri.lifelike.LifeThread;
import com.thisispiri.lifelike.Point;
import com.thisispiri.lifelike.R;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
	private int cellSize = -1, width, height;
	private int screenHeight, screenWidth, pauseBrushSize, lifecycle;
	private @ColorInt int cellColor, backgroundColor;
	private boolean[][] grid;
	private boolean[][] next;
	private LifeGrid lifeGrid;
	private LifeTouchListener tLis = new LifeTouchListener();
	private LifeThread mainThread;
	private Button start; //start and pause
	private boolean isPlaying = false;
	private boolean[] createNeighbors = new boolean[9], killNeighbors = new boolean[9];
	private LifeSimulator simulator;

	Runnable threadCallback = new Runnable() {
		@Override public void run() {
			handler.sendEmptyMessage(0);
		}
	};

	//screen update
	private static class UiHandler extends Handler {
		WeakReference<MainActivity> activity;
		@Override public void handleMessage(Message msg) {
			activity.get().lifeGrid.invalidate();
		}
		UiHandler(WeakReference<MainActivity> m) {
			activity = m;
		}
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
	@Override public void onStart() {
		super.onStart();
		int cellSizeTemp = cellSize;
		updatePreferences();
		//If cell size has changed, readjust array size and redraw. Always true after onCreate
		if(cellSize != cellSizeTemp) {
			boolean[][] gridTemp = grid;
			width = screenWidth / cellSize;
			height = screenHeight / cellSize;
			grid = new boolean[height][width];
			next = new boolean[height][width];
			if(gridTemp != null) { //Copy over the previous grid if one exists
				for(int i = 0;i < Math.min(gridTemp.length, height);i++)
					grid[i] = Arrays.copyOf(gridTemp[i], width);
			}
		}
		simulator = new LifeSimulator(width, height, createNeighbors, killNeighbors);
		lifeGrid.setData(grid, cellSize, height, width, cellColor, backgroundColor);
		lifeGrid.invalidate();
	}

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//save screen resolution
		android.graphics.Point screenSize = new android.graphics.Point();
		getWindowManager().getDefaultDisplay().getSize(screenSize);
		screenHeight = (int) (screenSize.y * 0.9);
		screenWidth = (screenSize.x);

		setContentView(R.layout.activity_main);
		lifeGrid = findViewById(R.id.view);
		lifeGrid.setOnTouchListener(tLis);

		//initialize Button references
		LifeButtonListener bLis = new LifeButtonListener(this);
		start = findViewById(R.id.start);
		start.setOnClickListener(bLis);
		findViewById(R.id.setting).setOnClickListener(bLis);
		findViewById(R.id.clear).setOnClickListener(bLis);
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
					mainThread = new LifeThread(simulator, lifecycle, threadCallback, grid, next);
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
				grid = new boolean[height][width];
				next = new boolean[height][width];
				lifeGrid.setData(grid);
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

		@Override
		public boolean onTouch(View v, MotionEvent m) {
			int x = Math.round(m.getX()), y = Math.round(m.getY());
			final int iX = x * width / screenWidth, iY = y * height / screenHeight;

			if ((iY >= 0) && (iX >= 0) && (iY < (height - 1)) && (iX < (width - 1))) {
				//If not paused, randomly resurrect cells near touch location according to brush size.
				if (isPlaying) {
					int rasY, rasX;
					mainThread.overrideList.add(new Point(iX, iY));
					rasY = r.nextInt(4);
					rasX = r.nextInt(4);
					if (iY + rasY > 0 && iY + rasY < height - 1 && iX + rasX > 0 && iX + rasX < width - 1)
						mainThread.overrideList.add(new Point(iX + rasX, iY + rasY));

					rasY = r.nextInt(4);
					rasX = r.nextInt(4);
					if (iY - rasY > 0 && iY - rasY < height - 1 && iX - rasX > 0 && iX - rasX < width - 1)
						mainThread.overrideList.add(new Point(iX - rasX, iY - rasY));

					rasY = r.nextInt(4);
					rasX = r.nextInt(4);
					if (iY + rasY > 0 && iY + rasY < height - 1 && iX - rasX > 0 && iX - rasX < width - 1)
						mainThread.overrideList.add(new Point(iX - rasX, iY + rasY));

					rasY = r.nextInt(4);
					rasX = r.nextInt(4);
					if (iY - rasY > 0 && iY - rasY < height - 1 && iX + rasX > 0 && iX + rasX < width - 1)
						mainThread.overrideList.add(new Point(iX + rasX, iY - rasY));
				}
				//If paused, resurrect brush size * brush size cells.
				else {
					grid[iY][iX] = true;
					for (int i = 2; i <= pauseBrushSize; i++) {
						if (i % 2 == 0) {
							if (iY + i / 2 < height)
								for (int j = iX - i / 2 + 1; j <= iX + i / 2; j++) { //(x - i / 2 + 1, y + i / 2) ~ (x + i / 2, y + i / 2) horizontal rightward
									if (xInBoundary(j)) grid[iY + i / 2][j] = true;
								}
							if (iX + i / 2 < width)
								for (int j = iY + i / 2 - 1; j >= iY - i / 2 + 1; j--) { //(x + i / 2, y + i / 2) ~ (x + i / 2, y - i / 2 + 1) vertical upward
									if (yInBoundary(j)) grid[j][iX + i / 2] = true;
								}
						}
						else {
							if (iY - i / 2 >= 0)
								for (int j = iX + i / 2; j >= iX - i / 2; j--) { //(x + i / 2, y - i / 2) ~ (x - i / 2, y - i / 2) horizontal leftward
									if (xInBoundary(j)) grid[iY - i / 2][j] = true;
								}
							if (iX - i / 2 >= 0)
								for (int j = iY - i / 2 + 1; j <= iY + i / 2; j++) { //(x - i / 2, y - i / 2) ~ (x - i / 2, y + i / 2) vertical downward
									if (yInBoundary(j)) grid[j][iX - i / 2] = true;
								}
						}
					}
				}
			}
			lifeGrid.invalidate();
			return true;
		}
	}
}
