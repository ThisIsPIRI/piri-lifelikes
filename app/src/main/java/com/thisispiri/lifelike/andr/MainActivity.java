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

import com.thisispiri.common.andr.AndrUtil;
import com.thisispiri.dialogs.DialogListener;
import com.thisispiri.dialogs.EditTextDialogFragment;
import com.thisispiri.lifelike.LifeUniverse;
import com.thisispiri.lifelike.LifeThread;
import com.thisispiri.lifelike.LifelikeSaveLoader;
import com.thisispiri.lifelike.ParameteredRunnable;
import com.thisispiri.lifelike.R;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements DialogListener { //TODO add save/load, add action_view
	private int cellSize = -1, width, height;
	private int screenHeight, screenWidth, pauseBrushSize, lifecycle;
	private @ColorInt int cellColor, backgroundColor;
	/**The most recent grid. Use this for everything--displaying, saving, recovering...*/
	private boolean[][] currentGrid;
	/**The empty slate for the next step. The data it holds should be treated as garbage.*/
	private boolean[][] nextGrid;
	private LifeView lifeView;
	private final LifeTouchListener tLis = new LifeTouchListener();
	private LifeThread mainThread;
	private Button start, eraserToggle;
	private boolean isPlaying = false;
	private boolean brushEnabled = true;
	private final boolean[] birthNumbers = new boolean[9], surviveNumbers = new boolean[9];
	private LifeUniverse simulator;
	private final static String TAG_EDITTEXT = "TAG_EDITTEXT";
	private final static String TAG_IN_BUNDLE = "i_tagInBundle";
	private final static String DIRECTORY_NAME = "PIRI/Life-likes";

	private final ParameteredRunnable threadCallback = new ParameteredRunnable() {
		@Override public void run(final Object param) {
			if(MainActivity.this.currentGrid != param) {
				boolean[][] tempGrid = currentGrid;
				currentGrid = nextGrid;
				nextGrid = tempGrid;
			}
			handler.sendEmptyMessage(0);
		}
	};

	//screen update
	private static class UiHandler extends Handler {
		private final WeakReference<MainActivity> activity;
		@Override public void handleMessage(final Message msg) {
			activity.get().lifeView.invalidate(activity.get().currentGrid);
		}
		UiHandler(final WeakReference<MainActivity> m) {
			activity = m;
		}
	}
	private final Handler handler = new UiHandler(new WeakReference<>(this));

	//SECTION: Android callbacks
	private void updatePreferences() {
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		cellSize = pref.getInt("cellSize", 16);
		pauseBrushSize = pref.getInt("pauseBrushSize", 1);
		lifecycle = pref.getInt("lifecycle", 0);
		cellColor = pref.getInt("cellColor", 0xFF000000);
		backgroundColor = pref.getInt("backgroundColor", 0xFFFFFFFF);
		//Fill the arrays with false first as we only take true values from the Preferences
		Arrays.fill(birthNumbers, false);
		Arrays.fill(surviveNumbers, false);
		Set<String> set = pref.getStringSet("birthNumbers", null);
		if(set == null) birthNumbers[3] = true;
		else for (String s : set) birthNumbers[Integer.valueOf(s)] = true;
		set = pref.getStringSet("surviveNumbers", null);
		if(set == null) {
			surviveNumbers[2] = surviveNumbers[3] = true;
		}
		else for (String s : set) surviveNumbers[Integer.valueOf(s)] = true;
	}
	@Override public void onStart() {
		super.onStart();
		final int cellSizeTemp = cellSize;
		updatePreferences();
		//If cell size has changed, readjust array size and redraw. Always true after onCreate
		if(cellSize != cellSizeTemp) {
			width = screenWidth / cellSize;
			height = screenHeight / cellSize;
			allocateGrids(true);
		}
		simulator = new LifeUniverse(currentGrid, birthNumbers, surviveNumbers);
		lifeView.setData(currentGrid, cellSize, height, width, cellColor, backgroundColor);
		lifeView.invalidate();
		//Just so setting and clear buttons don't crash the app when pressed before start
		mainThread = new LifeThread(simulator, lifecycle, threadCallback, currentGrid, nextGrid);
	}
	@Override protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Save screen resolution
		android.graphics.Point screenSize = new android.graphics.Point();
		getWindowManager().getDefaultDisplay().getSize(screenSize);
		screenHeight = (int) (screenSize.y * 0.9); //Subtract height of the button bar
		screenWidth = (screenSize.x);
		//Get Views from the layout and attach listeners
		setContentView(R.layout.activity_main);
		lifeView = findViewById(R.id.view);
		lifeView.setOnTouchListener(tLis);
		final LifeButtonListener bLis = new LifeButtonListener();
		start = findViewById(R.id.start);
		start.setOnClickListener(bLis);
		findViewById(R.id.setting).setOnClickListener(bLis);
		findViewById(R.id.clear).setOnClickListener(bLis);
		eraserToggle = findViewById(R.id.eraserToggle);
		eraserToggle.setOnClickListener(bLis);
		findViewById(R.id.save).setOnClickListener(bLis);
		findViewById(R.id.load).setOnClickListener(bLis);
	}
	@Override public void onBackPressed() {
		pause(true);
		super.onBackPressed();
	}
	//SECTION: Listeners
	//handle button click events
	private class LifeButtonListener implements View.OnClickListener {
		@Override public void onClick(final View v) {
			switch(v.getId()) {
			case R.id.start:
				pause(isPlaying);
				break;
			case R.id.setting:
				pause(true);
				final Intent toSetting = new Intent(MainActivity.this, SettingActivity.class);
				MainActivity.this.startActivityForResult(toSetting, 0);
				break;
			case R.id.clear:
				pause(true);
				allocateGrids(false);
				lifeView.invalidate(currentGrid);
				break;
			case R.id.eraserToggle:
				brushEnabled = !brushEnabled;
				eraserToggle.setText(brushEnabled ? R.string.eraserToggleWriting : R.string.eraserToggleErasing);
				break;
			case R.id.save:
				saveUniverse(null);
				break;
			case R.id.load:
				loadUniverse(null);
				break;
			}
		}
	}
	private class LifeTouchListener implements View.OnTouchListener {
		private final Random random = new Random();
		@Override public boolean onTouch(final View v, final MotionEvent m) {
			final int x = Math.round(m.getX()), y = Math.round(m.getY());
			final int iX = x * width / screenWidth, iY = y * height / screenHeight;
			if ((iY >= 0) && (iX >= 0) && (iY < (height - 1)) && (iX < (width - 1))) {
				//If not paused, randomly resurrect cells near touch location according to brush size.
				if (isPlaying) simulator.paintRandom(mainThread.overrideList, iX, iY, random);
				//If paused, resurrect brush size * brush size cells.
				else simulator.paintSquare(currentGrid, iX, iY, pauseBrushSize, brushEnabled);
			}
			lifeView.invalidate();
			return true;
		}
	}
	@Override public <T>void giveResult(final T result, final Bundle arguments) {
		if(arguments == null) {
			AndrUtil.showToast(this, "Error: giveResults arguments were null.");
			return;
		}
		final String tag = arguments.getString(TAG_IN_BUNDLE);
		if(tag == null) {
			AndrUtil.showToast(this, "Error: giveResult arguments didn't contain a tag.");
			return;
		}
		switch(tag) {
		case TAG_EDITTEXT:
			final String message = arguments.getString(getString(R.string.piri_dialogs_messageArgument));
			if(result != null && message != null) {
				if(message.equals(getString(R.string.save))) saveUniverse((String) result);
				else if(message.equals(getString(R.string.load))) loadUniverse((String) result);
			}
			break;
		}
	}
	//SECTION: Other logic
	/**Starts or pauses the game so you don't have to change the start button's text or deal with mainThread all over the place.
	 * @param pause If true, pauses the game. If false, starts the game.*/
	private void pause(final boolean pause) {
		if(pause != isPlaying) //Do nothing if we're already in the desired state
			return;
		if(pause) {
			isPlaying = false;
			mainThread.stopped = true;
			start.setText(R.string.start);
		}
		else {
			isPlaying = true;
			mainThread = new LifeThread(simulator, lifecycle, threadCallback, currentGrid, nextGrid);
			mainThread.start();
			start.setText(R.string.pause);
		}
	}
	private void showEditTextDialog(final String message, final String hint) { //TODO: Move to PIRI Dialogs
		final EditTextDialogFragment f = new EditTextDialogFragment();
		f.setArguments(AndrUtil.bundleWith(TAG_IN_BUNDLE, TAG_EDITTEXT));
		f.show(getSupportFragmentManager(), TAG_EDITTEXT, message, hint);
	}
	private void saveUniverse(final String filename) {
		pause(true);
		if(filename == null) showEditTextDialog(getString(R.string.save), getString(R.string.filename));
		else try {
			LifelikeSaveLoader.save(simulator, AndrUtil.getFile(DIRECTORY_NAME, filename, true),
					LifelikeSaveLoader.Format.PLAINTEXT, filename);
		}
		catch(IOException e) {
			AndrUtil.showToast(this, getString(R.string.couldntWriteFile));
		}
	}
	private void loadUniverse(final String filename){
		pause(true);
		if(filename == null) showEditTextDialog(getString(R.string.load), getString(R.string.filename));
		else try {
			simulator = LifelikeSaveLoader.load(AndrUtil.getFile(DIRECTORY_NAME, filename, false));
			//Grid references in mainThread will be updated when the universe is unpaused. No need to refresh them here.
			//TODO: Handle different cellSize
			if(simulator.birthNumbers == null) {
				simulator.birthNumbers = birthNumbers;
				simulator.surviveNumbers = surviveNumbers;
			}
			currentGrid = simulator.grid;
			nextGrid = new boolean[currentGrid.length][currentGrid[0].length];
			lifeView.invalidate(currentGrid);
		}
		catch(IOException e) {
			AndrUtil.showToast(this, getString(R.string.couldntReadFile));
		}
	}
	/**Allocates new boolean[height][width]s for currentGrid and nextGrid.
	 * @param copyPrevious If true, the values in previous currentGrid will be copied to the new currentGrid.
	 *                     If the previous grid is smaller, it will be copied to upper left region of the new one.
	 *                     If it is larger, its upper left region will be copied to the new one.*/
	private void allocateGrids(final boolean copyPrevious) {
		final boolean[][] tempGrid = currentGrid;
		currentGrid = new boolean[height][width];
		nextGrid = new boolean[height][width];
		if(copyPrevious && tempGrid != null) { //Copy over the previous grid if one exists
			for(int i = 0;i < Math.min(tempGrid.length, height);i++)
				currentGrid[i] = Arrays.copyOf(tempGrid[i], width);
		}
	}
}
