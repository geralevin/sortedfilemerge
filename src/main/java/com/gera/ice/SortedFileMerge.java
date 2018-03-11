package com.gera.ice;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * File Merge. This would be useful for the external sort using external merge
 * sort.
 */
public class SortedFileMerge {
	private SortedFileMerge() {
		// empty
	}

	/**
	 * This method merges n files into a single file preserving the sort order given
	 * by comparator. This a typical external merge routine most likely used for
	 * external merge sorts.
	 *
	 * @param target
	 *            Target file
	 * @param config
	 *            Merge configuration, sorting configuration must match the
	 *            configuration used when the sorted were obtained.
	 * @param sources
	 *            Sorted input files to be merged
	 * @throws IOException
	 */
	public static void mergeSortedFiles(File target, MergeConfiguration config, File... sources) throws IOException {
		checkArgument(target != null);
		checkArgument(config != null);
		checkArgument(sources != null && sources.length >= 2, "Atleast 2 source files are required");

		List<LineFromBufferedReader> lns = new ArrayList<>(sources.length);
		BufferedWriter targetWriter = null;
		try {
			for (File source : sources) {
				LineFromBufferedReader ln = new LineFromBufferedReader(
						new BufferedReader(new InputStreamReader(new FileInputStream(source), config.getCharset())));
				lns.add(ln);
			}

			targetWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(target, config.isAppend()), config.getCharset()));

			mergeSortedFiles(targetWriter, lns, config.getComparator(), config.isDistinct());
		} catch (IOException e) {
			throw e;
		} finally {
			if (targetWriter != null) {
				try {
					targetWriter.close();
				} catch (Exception ex) {
				}
			}
			for (LineFromBufferedReader ln : lns) {
				try {
					ln.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	/**
	 * This method merges n files into a single file preserving the sort order given
	 * by comparator
	 *
	 * @param targetWriter
	 *            Target file writer
	 * @param linesAndItsReaders
	 *            list of association of read line and the
	 *            <link>BufferedReader</link> it came from.
	 * @param comparator
	 *            comparator to define min/max heap behavior
	 * @param distinct
	 *            if true only distinct values will be in the merged file
	 * @throws IOException
	 */
	private static void mergeSortedFiles(BufferedWriter targetWriter, List<LineFromBufferedReader> linesAndItsReaders,
			Comparator<String> comparator, boolean distinct) throws IOException {
		checkArgument(targetWriter != null);
		checkArgument(linesAndItsReaders != null && linesAndItsReaders.size() >= 2);
		checkArgument(comparator != null);

		// create max/min heap, store only one element from each reader
		PriorityQueue<LineFromBufferedReader> heap = new PriorityQueue<>(linesAndItsReaders.size(),
				Comparator.comparing(LineFromBufferedReader::peek, comparator));

		linesAndItsReaders.stream().filter(lr -> !lr.empty()).forEach(heap::add);

		if (distinct) {
			distinctMerge(targetWriter, heap, comparator);
		} else {
			nonDistinctMerge(targetWriter, heap);
		}
	}

	/**
	 * This is non distinct merge routine. The idea behind it that we keep a min or
	 * max heap of elements, where an element represents the line read per file
	 * merged. In addition to the line we would need to know the reader that read
	 * that line. Therefore <link>LineFromBufferedReader</link> combines the line
	 * and associated reader to do just that. Once the heap is popped, we know from
	 * which reader it came from; read the next line and put it back into a heap.
	 *
	 * @param targetWriter
	 *            Target file writer
	 * @param heap
	 *            min/max heap
	 * @throws IOException
	 */
	private static void nonDistinctMerge(BufferedWriter targetWriter, PriorityQueue<LineFromBufferedReader> heap)
			throws IOException {
		while (heap.size() > 0) {
			LineFromBufferedReader ln = heap.poll();
			String line = ln.pop();
			targetWriter.write(line);
			targetWriter.newLine();
			if (ln.empty()) {
				ln.close();
			} else {
				heap.add(ln);
			}
		}
	}

	/**
	 * This method is very similar to nonDistinctMerge with the only difference than
	 * when heap will be popped the line will be discarded if it matches to
	 * previously poped line.
	 *
	 * @param targetWriter
	 *            Target file writer
	 * @param heap
	 *            min/max heap
	 * @param comparator
	 *            comparator that will define how min/max heap will be constructed
	 * @throws IOException
	 */
	private static void distinctMerge(BufferedWriter targetWriter, PriorityQueue<LineFromBufferedReader> heap,
			Comparator<String> comparator) throws IOException {

		String previousLine = writeFirstLine(targetWriter, heap);
		while (heap.size() > 0) {
			LineFromBufferedReader ln = heap.poll();
			String line = ln.pop();

			if (comparator.compare(line, previousLine) != 0) {
				targetWriter.write(line);
				targetWriter.newLine();
				previousLine = line;
			}
			if (ln.empty()) {
				ln.close();
			} else {
				heap.add(ln);
			}
		}
	}

	/**
	 * For the distinct merge we need to get the very first line so we can bootstrap
	 * comparison from that line. This line will determine the first line, write it,
	 * and return it
	 * 
	 * @param targetWriter
	 *            Target file writer
	 * @param heap
	 *            min/max heap
	 * @return the first line written to the target file
	 * @throws IOException
	 */
	private static String writeFirstLine(BufferedWriter targetWriter, PriorityQueue<LineFromBufferedReader> heap)
			throws IOException {
		String line = null;
		if (heap.size() > 0) {
			LineFromBufferedReader ln = heap.poll();
			line = ln.pop();
			targetWriter.write(line);
			targetWriter.newLine();
			if (ln.empty()) {
				ln.close();
			} else {
				heap.add(ln);
			}
		}

		return line;
	}

	/**
	 * We need an object that will associate the last read line and the reader it
	 * came from in one single object that wwe can push on min/max heap. This class
	 * will do precisely that.
	 */
	private static final class LineFromBufferedReader implements AutoCloseable {
		public BufferedReader reader;
		private String lastReadLine;

		public LineFromBufferedReader(BufferedReader bfr) throws IOException {
			reader = bfr;
			readNextLine();
		}

		@Override
		public void close() throws IOException {
			reader.close();
		}

		public boolean empty() {
			return lastReadLine == null;
		}

		public String peek() {
			return lastReadLine;
		}

		public String pop() throws IOException {
			String ret = lastReadLine;
			readNextLine();
			return ret;
		}

		private void readNextLine() throws IOException {
			this.lastReadLine = reader.readLine();
		}

	}
}
