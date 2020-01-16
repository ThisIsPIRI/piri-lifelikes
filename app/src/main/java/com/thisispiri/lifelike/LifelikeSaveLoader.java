package com.thisispiri.lifelike;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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
		boolean[][] grid;
		boolean[][] birthNumbers, surviveNumbers;
		LifeUniverse universe;
		InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
		int skipper;
		do {skipper = reader.read();}
		while(skipper != '(' && skipper != -1);
		if(reader.read() == ';') { //Plaintext format
			char previous = 0, now;
			while(reader.ready()) {
				now = (char)reader.read();
				//Process things
				previous = now;
			}
		}
		else {
			throw new IOException("PIRI Life-likes: stub called - other formats not supported yet");
		}
		reader.close();
		return null;
	}
}
