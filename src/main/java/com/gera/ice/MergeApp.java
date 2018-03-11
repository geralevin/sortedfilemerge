package com.gera.ice;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.util.stream.IntStream;

public class MergeApp {
	public static void main(String... args) {
		checkArgument(args != null);
		checkArgument(args.length >= 3, "Need to have at least 3 arguments (target file, source1, source 2, ...)");

		try {
			SortedFileMerge.mergeSortedFiles(new File(args[0]), MergeConfiguration.defaultConfiguration(),
					IntStream.range(1, args.length ).mapToObj(i -> new File(args[i])).toArray(File[]::new));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return;
		}

		System.out.println("Done.");
	}
}
