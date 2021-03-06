package org.lodder.subtools.multisubdownloader.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.lodder.subtools.multisubdownloader.listeners.IndexingProgressListener;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.multisubdownloader.settings.model.SettingsExcludeItem;
import org.lodder.subtools.multisubdownloader.settings.model.SettingsExcludeType;
import org.lodder.subtools.sublibrary.control.VideoPatterns;
import org.lodder.subtools.sublibrary.util.FilenameExtensionFilter;
import org.lodder.subtools.sublibrary.util.NamedMatcher;
import org.lodder.subtools.sublibrary.util.NamedPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileListAction {

	private IndexingProgressListener indexingProgressListener;
	private int progressFileIndex;
	private int progressFilesTotal;
	private Settings settings;
	private final String[] dutchFilters = new String[] { "nld", "ned", "dutch", "dut", "nl" };
	private final String[] englishFilters = new String[] { "eng", "english", "en" };
	private final static String subtitleExtension = ".srt";

	private static final Logger LOGGER = LoggerFactory.getLogger(FileListAction.class);

	public FileListAction(Settings settings) {
		this.settings = settings;
	}

	public List<File> getFileListing(File dir, boolean recursieve, String languagecode,
			boolean forceSubtitleOverwrite) {
		LOGGER.trace(
				"getFileListing: dir [{}] Recursive [{}] languageCode [{}] forceSubtitleOverwrite [{}]",
				dir, recursieve, languagecode, forceSubtitleOverwrite);
		/* Reset progress counters */
		this.progressFileIndex = 0;
		this.progressFilesTotal = 0;

		/* Start listing process */
		return this._getFileListing(dir, recursieve, languagecode, forceSubtitleOverwrite);
	}

	private List<File> _getFileListing(File dir, boolean recursieve, String languagecode,
			boolean forceSubtitleOverwrite) {
		final List<File> filelist = new ArrayList<File>();
		final File[] contents = dir.listFiles();

		if (contents == null)
			return filelist;

		/* Increase progressTotalFiles count */
		this.progressFilesTotal += contents.length;

		for (final File file : contents) {
			/* Increase progressFileIndex */
			this.progressFileIndex++;

			/* Update progressListener */
			if (this.indexingProgressListener != null) {
				/* Tell the progresslistener which directory we are handling */
				this.indexingProgressListener.progress(dir.getPath());
				/* Tell the progresslistener the overall progress */
				int progress =
						(int) Math.floor((float) this.progressFileIndex / this.progressFilesTotal * 100);
				this.indexingProgressListener.progress(progress);
			}

			if (file.isFile() && isValidVideoFile(file)
					&& (!fileHasSubtitles(file, languagecode) || forceSubtitleOverwrite)
					&& isNotExcluded(file)) {
				filelist.add(file);
			} else if (file.isDirectory() && recursieve && !isExcludedDir(file)) {
				filelist.addAll(getFileListing(file, recursieve, languagecode, forceSubtitleOverwrite));
			}
		}
		return filelist;
	}

	private Boolean isExcludedDir(File file) {
		Boolean status = false;

		Iterator<SettingsExcludeItem> itemIterator = settings.getExcludeList().iterator();
		while (!status && itemIterator.hasNext()) {
			SettingsExcludeItem item = itemIterator.next();
			if (item.getType() != SettingsExcludeType.FOLDER)
				continue;

			File excludeFile = new File(item.getDescription());
			if (!excludeFile.equals(file))
				continue;

			LOGGER.trace("isExcludedDir, skipping [{}]", file);
			status = true;
		}

		return status;
	}

	private boolean isNotExcluded(File file) {
		for (int j = 0; j < settings.getExcludeList().size(); j++) {
			if (settings.getExcludeList().get(j).getType() == SettingsExcludeType.REGEX) {
				NamedPattern np =
						NamedPattern.compile(
								settings.getExcludeList().get(j).getDescription().replace("*", ".*") + ".*$",
								Pattern.CASE_INSENSITIVE);
				NamedMatcher namedMatcher = np.matcher(file.getName());
				if (namedMatcher.find()) {
					LOGGER.trace("isNotExcluded, skipping [{}]", file);
					return false;
				}
			}
		}
		for (int j = 0; j < settings.getExcludeList().size(); j++) {
			if (settings.getExcludeList().get(j).getType() == SettingsExcludeType.FILE) {
				File excludeFile = new File(settings.getExcludeList().get(j).getDescription());
				if (excludeFile.equals(file)) {
					LOGGER.trace("isNotExcluded, skipping [{}]", file);
					return false;
				}
			}
		}
		return true;
	}

	public boolean isValidVideoFile(File file) {
		final String filename = file.getName();
		final int mid = filename.lastIndexOf(".");
		final String ext = filename.substring(mid + 1, filename.length());
		if (filename.contains("sample"))
			return false;
		for (String allowedExtension : VideoPatterns.EXTENSIONS) {
			if (ext.equalsIgnoreCase(allowedExtension))
				return true;
		}
		return false;
	}

	public boolean fileHasSubtitles(File file, String languageCode) {
		String subname = "";
		for (String allowedExtension : VideoPatterns.EXTENSIONS) {
			if (file.getName().contains("." + allowedExtension))
				subname = file.getName().replace("." + allowedExtension, subtitleExtension);
		}

		final File f = new File(file.getParentFile(), subname);
		if (f.exists()) {
			return true;
		} else {
			List<String> filters = null;

			if (languageCode.equals("nl")) {
				filters = getFilters(dutchFilters);
				if (!settings.getEpisodeLibrarySettings().getDefaultNlText().equals(""))
					filters.add("."
							+ settings.getEpisodeLibrarySettings().getDefaultNlText().concat(subtitleExtension));
			} else if (languageCode.equals("en")) {
				filters = getFilters(englishFilters);
				if (!settings.getEpisodeLibrarySettings().getDefaultEnText().equals(""))
					filters.add("."
							+ settings.getEpisodeLibrarySettings().getDefaultEnText().concat(subtitleExtension));
			}

			if (filters == null)
				return false;

			String[] contents =
					file.getParentFile().list(
							new FilenameExtensionFilter(filters.toArray(new String[filters.size()])));
			if (contents == null)
				contents = new String[] {};

			return checkFileListContent(contents, subname.replace(subtitleExtension, ""));
		}
	}

	private List<String> getFilters(String[] words) {
		List<String> filters = new ArrayList<String>();

		for (String s : words) {
			filters.add(s + subtitleExtension);
		}

		return filters;
	}

	public boolean checkFileListContent(String[] contents, String subname) {
		if (contents.length > 0) {
			for (final String file : contents) {
				if (file.contains(subname)) {
					return true;
				}
			}
		}
		return false;
	}

	public void setIndexingProgressListener(IndexingProgressListener indexingProgressListener) {
		this.indexingProgressListener = indexingProgressListener;
	}
}
