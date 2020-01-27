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
	/**Saves a life-like universe.
	 * When using @link{LifelikeSaveLoader#Format#PLAINTEXT}(https://www.conwaylife.com/wiki/Plaintext),
	 * after "!Name: name\n!Rule:0123/4567\n!\n", dead cells will be written as '.' and alive cells as 'O'.*/
	public static void save(final LifeUniverse universe, final File file, final Format format, final String name) throws IOException {
		if(format == Format.PLAINTEXT) {
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
			writer.write(String.format("!Name: %s\n", name)); //Header
			//Write the rules
			writer.write("!Rule:");
			for(int i = 0;i < universe.birthNumbers.length;i++) {
				if(universe.birthNumbers[i]) writer.write(Character.forDigit(i, 10));
			}
			writer.write('/');
			for(int i = 0;i < universe.surviveNumbers.length;i++) {
				if(universe.surviveNumbers[i]) writer.write(Character.forDigit(i, 10));
			}
			writer.write("\n!\n");
			//Write the grid
			for(boolean[] i : universe.grid) {
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
	/**Loads a life-like universe from a file.*/
	public static LifeUniverse load(final File file) throws IOException {
		final List<boolean[]> grid = new ArrayList<>();
		boolean[] birthNumbers = null, surviveNumbers = null;
		final BufferedReader reader = new BufferedReader(new FileReader(file));
		String stringLine;
		while((stringLine = reader.readLine()) != null) {
			final boolean[] booleanLine = new boolean[stringLine.length()];
			if(stringLine.contains("!")) {
				if(stringLine.contains("!Rule:")) {
					birthNumbers = new boolean[9];
					surviveNumbers = new boolean[9];
					readRules(stringLine, birthNumbers, surviveNumbers);
				}
				continue;
			}
			for(int i =0;i < stringLine.length();i++) {
				booleanLine[i] = stringLine.charAt(i) == 'O';
			}
			grid.add(booleanLine);
		}
		reader.close();
		return new LifeUniverse(grid.toArray(new boolean[0][0]), birthNumbers, surviveNumbers);
	}
	/**Reads rules from the rule line in Life 1.05, RLE and PIRI version of Plaintext.
	 * The line is two string of digits between [0, 8], separated by one slash(/).
	 * The line must not contain 9.
	 * This looks like "Rules: 23/3"
	 * Arbitrary characters between meaningful characters(digits and slashes) are allowed, but discouraged: "Rule /1(looks good),4,5(does nothing)"
	 * Any slashes after the first have no effect.
	 * @param line The line to read from.
	 * @param birthNumbers All elements must be initialized to false.
	 * @param surviveNumbers All elements must be initialized to false.*/
	private static void readRules(final String line, final boolean[] birthNumbers, final boolean[] surviveNumbers) {
		boolean[] target = birthNumbers;
		for(int i = 0;i < line.length();i++) {
			if(line.charAt(i) == '/')
				target = surviveNumbers;
			else if(Character.isDigit(line.charAt(i)))
				target[Character.getNumericValue(line.charAt(i))] = true;
		}
	}
}
