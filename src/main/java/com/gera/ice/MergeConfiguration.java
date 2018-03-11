package com.gera.ice;

import java.nio.charset.Charset;
import java.util.Comparator;

import static com.google.common.base.Preconditions.checkArgument;

public class MergeConfiguration {
	private static final MergeConfiguration defaultConfig = builder().withAppend(true)
			.withComparator(Comparator.naturalOrder()).withDistinct(false).withCharset(Charset.defaultCharset())
			.build();
	private Comparator<String> comparator;
	private Charset charset;
	private boolean distinct;
	private boolean append;

	private MergeConfiguration() {
	}

	public static MergeConfigurationBuilder builder() {
		return new MergeConfigurationBuilder();
	}

	public static MergeConfiguration defaultConfiguration() {
		return defaultConfig;
	}

	public Comparator<String> getComparator() {
		return comparator;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public boolean isAppend() {
		return append;
	}

	public Charset getCharset() {
		return charset;
	}

	public static class MergeConfigurationBuilder {
		private Comparator<String> comparator = Comparator.naturalOrder();
		private Charset charset = Charset.defaultCharset();
		private boolean distinct = false;
		private boolean append = false;

		public MergeConfigurationBuilder withComparator(Comparator<String> comparator) {
			checkArgument(comparator != null);
			this.comparator = comparator;
			return this;
		}

		public MergeConfigurationBuilder withCharset(Charset charset) {
			checkArgument(charset != null);
			this.charset = charset;
			return this;
		}

		public MergeConfigurationBuilder withDistinct(boolean distinct) {
			this.distinct = distinct;
			return this;
		}

		public MergeConfigurationBuilder withAppend(boolean append) {
			this.append = append;
			return this;
		}

		public MergeConfiguration build() {
			MergeConfiguration mergeConfiguration = new MergeConfiguration();
			mergeConfiguration.comparator = this.comparator;
			mergeConfiguration.charset = this.charset;
			mergeConfiguration.append = this.append;
			mergeConfiguration.distinct = this.distinct;

			return mergeConfiguration;
		}
	}
}
