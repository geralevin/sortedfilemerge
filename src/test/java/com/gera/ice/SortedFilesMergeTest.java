package com.gera.ice;

import static com.gera.ice.SortedFileMerge.mergeSortedFiles;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import junitx.framework.FileAssert;

public class SortedFilesMergeTest {

	private TemporaryFolder folder;
	@Before
	public void init() throws IOException {
		folder = new TemporaryFolder();
		folder.create();
	}

	@After
	public void exit() {
		folder.delete();
	}

	@Test
	public void testNonDistinctMerge() throws IOException {
		String mn = new Object() {
		}.getClass().getEnclosingMethod().getName();

		File sourceFile1 = folder.newFile(mn + "source1.txt");
		writeFile(sourceFile1, MergeConfiguration.defaultConfiguration().getCharset(),
				Arrays.asList("001", "002", "003", "003", "005", "006", "006", "010"));

		File sourceFile2 = folder.newFile(mn + "source2.txt");
		writeFile(sourceFile2, MergeConfiguration.defaultConfiguration().getCharset(),
				Arrays.asList("003", "003", "005", "006", "010", "011", "013"));

		File sourceFile3 = folder.newFile(mn + "source3.txt");
		writeFile(sourceFile3, MergeConfiguration.defaultConfiguration().getCharset(), Arrays.asList("005", "006", "006", "010"));

		File expectedResultFile = folder.newFile(mn + "expected_target.txt");
		writeFile(expectedResultFile, MergeConfiguration.defaultConfiguration().getCharset(),
				Arrays.asList("001", "002", "003", "003", "003", "003", "005", "005", "005", "006", "006", "006", "006",
						"006", "010", "010", "010", "011", "013"));

		File resultFile = folder.newFile(mn + "target.txt");
		mergeSortedFiles(resultFile, MergeConfiguration.defaultConfiguration(), sourceFile1, sourceFile2, sourceFile3);

		FileAssert.assertEquals(resultFile, expectedResultFile);
	}

	@Test
	public void testDistinctMerge() throws IOException {
		String mn = new Object() {
		}.getClass().getEnclosingMethod().getName();

		File sourceFile1 = folder.newFile(mn + "source1.txt");
		writeFile(sourceFile1, MergeConfiguration.defaultConfiguration().getCharset(),
				Arrays.asList("001", "002", "003", "003", "005", "006", "006", "010"));

		File sourceFile2 = folder.newFile(mn + "source2.txt");
		writeFile(sourceFile2, MergeConfiguration.defaultConfiguration().getCharset(),
				Arrays.asList("003", "003", "005", "006", "010", "011", "013"));

		File sourceFile3 = folder.newFile(mn + "source3.txt");
		writeFile(sourceFile3, MergeConfiguration.defaultConfiguration().getCharset(), Arrays.asList("005", "006", "006", "010"));

		File expectedResultFile = folder.newFile(mn + "expected_target.txt");
		writeFile(expectedResultFile, MergeConfiguration.defaultConfiguration().getCharset(),
				Arrays.asList("001", "002", "003", "005", "006", "010", "011", "013"));

		File resultFile = folder.newFile(mn + "target.txt");
		mergeSortedFiles(resultFile, MergeConfiguration.builder().withDistinct(true).build(), sourceFile1, sourceFile2,
				sourceFile3);

		FileAssert.assertEquals(resultFile, expectedResultFile);
	}

	@Test
	public void testMassiveMerge() throws IOException {
		File resultFile = folder.newFile("file12.txt");
		mergeSortedFiles(resultFile, MergeConfiguration.defaultConfiguration(),
				new File(getClass().getClassLoader().getResource("file1.txt").getFile()),
				new File(getClass().getClassLoader().getResource("file2.txt").getFile()));
		assertThat(checkIfFileIsSorted(resultFile, MergeConfiguration.defaultConfiguration().getCharset(),
				MergeConfiguration.defaultConfiguration().getComparator()), is(Boolean.TRUE));
	}

	private boolean checkIfFileIsSorted(File file, Charset charset, Comparator<String> comparator) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset))) {
			String prevLine = null;
			String line;
			while ((line = br.readLine()) != null) {
				if (prevLine != null && comparator.compare(line, prevLine) < 0) {
					return false;
				}

				prevLine = line;
			}
		}
		return true;
	}

	private void writeFile(File file, Charset charset, Iterable<String> strings) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file, false), charset))) {
			for (String s : strings) {
				writer.write(s);
				writer.newLine();
			}
		}
	}
}
