package org.lodder.subtools.sublibrary.control;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lodder.subtools.sublibrary.exception.ReleaseParseException;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.util.NamedMatcher;
import org.lodder.subtools.sublibrary.util.NamedPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReleaseParser {

	private NamedMatcher namedMatcher;
	private static VideoPatterns videoPatterns = new VideoPatterns();
	private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseParser.class);

	@SuppressWarnings("unchecked")
	public final Release parse(final File file) throws ReleaseParseException {
		String foldername = "";
		if (file.getParentFile() != null)
			foldername = file.getParentFile().getName();
		String[] parsenames = new String[] { file.getName(), foldername };

		for (String fileparsename : parsenames) {
			for (NamedPattern np : videoPatterns.getCompiledPatterns()) {
				namedMatcher = np.matcher(fileparsename);
				if (namedMatcher.find()) {
					LOGGER.trace("Parsing match found using file name: {}", fileparsename);
					Object[] parseResults = parsePatternResult();
					Release vFile = null;
					if (parseResults.length == 4) {
						vFile =
								new TvRelease((String) parseResults[0], (Integer) parseResults[1],
										(List<Integer>) parseResults[2], file,
										extractFileNameExtension(file.getName()),
										removeExtension((String) parseResults[3]), extractReleasegroup(file.getName(),
												true),
										isSpecialEpisode((Integer) parseResults[1],
												(List<Integer>) parseResults[2]));
					} else if (parseResults.length == 3) {
						vFile =
								new MovieRelease((String) parseResults[0], (Integer) parseResults[1], file,
										extractFileNameExtension(file.getName()),
										removeExtension((String) parseResults[2]), extractReleasegroup(file.getName(),
												true));
					}
					if (vFile == null)
						return vFile;

					vFile.setQuality(getQualityKeyword(fileparsename));
					return vFile;
				}
			}
		}
		throw new ReleaseParseException("Unknow format, can't be parsed: " + file.getAbsolutePath());
	}

	protected final Object[] parsePatternResult() throws ReleaseParseException {
		List<String> namedgroups = namedMatcher.namedPattern().groupNames();
		String seriesname = "";
		List<Integer> episodenumbers = new ArrayList<Integer>();
		int seasonnumber = 0;
		int year = 0;
		String description = "";

		if (namedgroups.contains("description")) {
			description = namedMatcher.group("description").substring(1);
		}

		if (namedgroups.contains("year")) {
			year = Integer.parseInt(namedMatcher.group("year"));
		}

		if (namedgroups.contains("moviename")) {
			if (namedgroups.contains("part")) {
				String number = "";
				if (namedgroups.contains("partnumber"))
					number = namedMatcher.group("partnumber");
				if (namedgroups.contains("romanepisode"))
					number = namedMatcher.group("romanepisode");

				return new Object[] {
						cleanUnwantedChars(namedMatcher.group("moviename") + " " + namedMatcher.group("part")
								+ " " + number),
						year, description };
			} else {
				return new Object[] { cleanUnwantedChars(namedMatcher.group("moviename")), year, description };
			}
		}

		if (namedgroups.contains("episodenumber1")) {
			LOGGER.trace("parsePatternResult: episodenumber1: {}", namedMatcher.group("episodenumber1"));
			// Multiple episodes, have episodenumber1, 2 ....
			for (String group : namedgroups) {
				Pattern pattern = Pattern.compile("episodenumber(\\d+)");
				Matcher match = pattern.matcher(group);
				if (match.matches()) {
					episodenumbers.add(Integer.parseInt(namedMatcher.group(group)));
				}
			}
			Collections.sort(episodenumbers);
		} else if (namedgroups.contains("episodenumberstart")) {
			LOGGER.trace("parsePatternResult: episodenumberstart: {}",
					namedMatcher.group("episodenumberstart"));
			// Multiple episodes, regex specifies start and end number
			int start = Integer.parseInt(namedMatcher.group("episodenumberstart"));
			int end = Integer.parseInt(namedMatcher.group("episodenumberend"));
			if (start > end) {
				int temp = start;
				start = end;
				end = temp;
			}
			for (int i = start; i <= end; i++) {
				episodenumbers.add(i);
			}
		} else if (namedgroups.contains("episodenumber")) {
			LOGGER.trace("parsePatternResult: episodenumber: {}", namedMatcher.group("episodenumber"));
			episodenumbers.add(Integer.parseInt(namedMatcher.group("episodenumber")));
		} else if (namedgroups.contains("year") || namedgroups.contains("month")
				|| namedgroups.contains("day")) {
			// need to implement
		} else if (namedgroups.contains("romanepisode") && !namedgroups.contains("year")) {
			episodenumbers.add(Roman.decode(namedMatcher.group("romanepisode")));
		}

		if (namedgroups.contains("seriesname")) {
			LOGGER.trace("parsePatternResult: seriesname: {}", namedMatcher.group("seriesname"));
			seriesname = cleanUnwantedChars(namedMatcher.group("seriesname"));
			if (namedgroups.contains("year")) {
				seriesname = seriesname + " " + namedMatcher.group("year");
			}
		}

		if (namedgroups.contains("seasonnumber")) {
			LOGGER.trace("parsePatternResult: seasonnumber: {}", namedMatcher.group("seasonnumber"));
			seasonnumber = Integer.parseInt(namedMatcher.group("seasonnumber"));
			return new Object[] { seriesname, seasonnumber, episodenumbers, description };
		} else if (namedgroups.contains("part") && !namedgroups.contains("year")) {
			seasonnumber = 1;
			return new Object[] { seriesname, seasonnumber, episodenumbers, description };
		} else if (namedgroups.contains("year") && namedgroups.contains("month")
				&& namedgroups.contains("day")) {
			// need to implement
		} else if (namedgroups.contains("season_episode")) {
			LOGGER.trace("parsePatternResult: season_episode: {}", namedMatcher.group("season_episode"));
			if (namedMatcher.group("season_episode").length() == 3) {
				episodenumbers.add(Integer.parseInt(namedMatcher.group("season_episode").substring(1, 3)));
				seasonnumber = Integer.parseInt(namedMatcher.group("season_episode").substring(0, 1));
			} else if (namedMatcher.group("season_episode").length() == 4) {
				episodenumbers.add(Integer.parseInt(namedMatcher.group("season_episode").substring(2, 4)));
				seasonnumber = Integer.parseInt(namedMatcher.group("season_episode").substring(0, 2));
			}
			return new Object[] { seriesname, seasonnumber, episodenumbers, description };
		} else {
			// No season number specified, usually for Anime
			// need to implement
		}
		throw new ReleaseParseException("Unable to parse the namedmatcher");
	}

	protected final String cleanUnwantedChars(String text) {
		if (text.contains("cd1")) {
			text = text.replace("cd1", " ");
		}
		if (text.contains("cd2")) {
			text = text.replace("cd2", " ");
		}

		text = text.replace(".", " "); // remove point bones.01x01
		text = text.replace("_", " "); // remove underscore bones_01x01
		text = text.replace(" -", " "); // remove space dash "ncis - 01x01"
		text = text.replace(":", ""); // remove double point "CSI: NY"
		text = text.replace("(", ""); // remove ( for castle (2009)
		text = text.replace(")", ""); // remove ) for castle (2009)
		text = text.replace("'", "");

		if (text.endsWith("-")) // implemented if for "hawaii five-0"
		{
			text = text.replace("-", ""); // remove space dash "altiplano-cd1"
		}

		// remove multiple spaces between text Back to the Future[][]Part II
		text = text.replaceAll(" +", " ");

		return text.trim();
	}

	public static final String getQualityKeyword(final String name) {
		LOGGER.trace("getQualityKeyword: name: {}", name);
		Pattern p = Pattern.compile(videoPatterns.getQualityKeysRegex(), Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(name);
		StringBuilder builder = new StringBuilder();
		while (m.find()) {
			builder.append(m.group(0).replace(".", " ")).append(" ");
		}
		LOGGER.trace("getQualityKeyWords: keyswords: {}", builder.toString().trim());
		return builder.toString().trim();
	}

	public static List<String> getQualityKeyWords(String name) {
		LOGGER.trace("getQualityKeyWords: name: {}", name);
		name = name.trim().toLowerCase();
		Pattern p = Pattern.compile(videoPatterns.getQualityKeysRegex(), Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(name);
		List<String> keywords = new ArrayList<>();
		while (m.find()) {
			keywords.add(m.group(0));
		}
		LOGGER.trace("getQualityKeyWords: keyswords: {}", keywords);
		return keywords;
	}

	public static String extractFileNameExtension(final String fileName) {
		int mid = fileName.lastIndexOf(".");
		return fileName.substring(mid + 1, fileName.length());
	}

	public static String extractReleasegroup(final String fileName, boolean hasExtension) {
		LOGGER.trace("extractReleasegroup: name: {} , hasExtension: {}", fileName, hasExtension);
		Pattern releaseGroupPattern;
		if (hasExtension) {
			releaseGroupPattern = Pattern.compile("-([\\w]+).[\\w]+$");
		} else {
			releaseGroupPattern = Pattern.compile("-([\\w]+)$");
		}
		Matcher matcher = releaseGroupPattern.matcher(fileName);
		String releaseGroup = "";
		if (matcher.find()) {
			releaseGroup = matcher.group(1);
		}

		LOGGER.trace("extractReleasegroup: release group: {}", releaseGroup);
		return releaseGroup;
	}

	public static String removeExtension(final String fileName) {
		final int index = fileName.lastIndexOf('.');

		if (-1 == index) {
			return fileName;
		} else {
			return fileName.substring(0, index);
		}
	}

	public static boolean isSpecialEpisode(final int season, final List<Integer> episodeNumbers) {
		if (season == 0)
			return true;
		return episodeNumbers.size() == 1 && episodeNumbers.get(0) == 0;
	}
}
