package com.thisispiri.lifelike;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**Writes on and reads from .cells, .lif and .rle files a Life-like world.*/
public class LifelikeSaveLoader {
	public enum Format {
		PLAINTEXT, LIFE105, LIFE106, RLE
	}
	/**Saves the grid.*/
	public static void save(final boolean[][] grid, final File file, final Format format, final String name) throws IOException {
		if(format == Format.PLAINTEXT) {
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
			writer.write(String.format("!Name: %s\n!\n", name)); //Header
			for(boolean[] i : grid) {
				for(boolean j : i) {
					writer.write(j ? 'O' : '.');
				}
				writer.write('\n');
			}
			writer.close();
		}
		else {
			throw new IOException("PIRI Life-likes: stub called - other formats not supported yet");
		}
	}
	public static LifeUniverse load(final File file) throws IOException {
		final List<boolean[]> grid = new ArrayList<>();
		boolean[] birthNumbers = {true,false,false,false,false,false,false,false,false}, surviveNumbers = {false,false,false,false,false,false,false,false,false};
		final BufferedReader reader = new BufferedReader(new FileReader(file));
		String stringLine;
		while((stringLine = reader.readLine()) != null) {
			final boolean[] booleanLine = new boolean[stringLine.length()];
			if(stringLine.contains("!")) continue;
			for(int i =0;i < stringLine.length();i++) {
				booleanLine[i] = stringLine.charAt(i) == 'O';
			}
			grid.add(booleanLine);
		}
		reader.close();
		return new LifeUniverse(grid.toArray(new boolean[0][0]), birthNumbers, surviveNumbers);
	}
}
