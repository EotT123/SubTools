package org.lodder.subtools.sublibrary.data.thetvdb;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.thetvdb.model.TheTVDBEpisode;
import org.lodder.subtools.sublibrary.data.thetvdb.model.TheTVDBSerie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uwetrottmann.thetvdb.TheTvdb;
import com.uwetrottmann.thetvdb.entities.Episode;
import com.uwetrottmann.thetvdb.entities.EpisodesResponse;
import com.uwetrottmann.thetvdb.entities.Series;
import com.uwetrottmann.thetvdb.entities.SeriesResponse;
import com.uwetrottmann.thetvdb.entities.SeriesResultsResponse;

import retrofit2.Response;

public class TheTVDBApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(TheTVDBApi.class);
	private final TheTvdb theTvdb;

	public TheTVDBApi(String apikey, Manager manager) throws TheTVDBException {
		theTvdb = new TheTvdb(apikey);
	}

	public int searchSerie(String seriename, String language) throws TheTVDBException {
		try {
			String encodedSerieName = URLEncoder.encode(seriename, "UTF-8");
			Response<SeriesResultsResponse> response = theTvdb.search().series(encodedSerieName, null, null, language).execute();
			if (response.isSuccessful()) {
				List<Series> series = response.body().data;
				if (series.size() > 0) {
					return series.get(0).id;
				}
			}
			return 0;
		} catch (IOException e) {
			throw new TheTVDBException(e);
		}
	}

	public TheTVDBSerie getSerie(int tvdbid, String language) throws TheTVDBException {
		try {
			if (tvdbid != 0) {
				Response<SeriesResponse> response = theTvdb.series().series(tvdbid, language).execute();
				if (response.isSuccessful()) {
					return seriesToTVDBSerie(response.body().data, language);
				}
			} else {
				LOGGER.warn("TVDB ID is 0! please fix");
			}
			return null;
		} catch (IOException e) {
			throw new TheTVDBException(e);
		}
	}

	public List<TheTVDBEpisode> getAllEpisodes(int tvdbid, String language) throws TheTVDBException {
		try {
			if (tvdbid != 0) {
				Response<EpisodesResponse> response = theTvdb.series().episodes(tvdbid, 1, language).execute();
				if (response.isSuccessful()) {
					// return response.body().data.stream().map(episode -> episodeToTVDBEpisode(episode,
					// language)).collect(Collectors.toList());
					List<TheTVDBEpisode> tvdpEpisodes = new ArrayList<>();
					for (Episode episode : response.body().data) {
						tvdpEpisodes.add(episodeToTVDBEpisode(episode, language));
					}
					return tvdpEpisodes;
				}
			} else {
				LOGGER.warn("TVDB ID is 0! please fix");
			}
			return null;
		} catch (IOException e) {
			throw new TheTVDBException(e);
		}
	}

	public TheTVDBEpisode getEpisode(int tvdbid, int season, int episode, String language) throws TheTVDBException {
		try {
			Response<EpisodesResponse> response =
					theTvdb.series().episodesQuery(tvdbid, null, season, episode, null, null, null, null, null, language).execute();
			if (response.isSuccessful()) {
				List<Episode> series = response.body().data;
				if (series.size() > 0) {
					return episodeToTVDBEpisode(series.get(0), language);
				}
				return null;
			}
			throw new TheTVDBException(response.errorBody().string());
		} catch (IOException e) {
			throw new TheTVDBException(e);
		}
	}

	private TheTVDBSerie seriesToTVDBSerie(Series serie, String lang) {
		TheTVDBSerie TheTVDBSerie = new TheTVDBSerie();

		// try {
		// LOGGER.trace("parseSerieNode: eElement [{}]", XMLHelper.getXMLAsString(eElement));
		// } catch (Exception e) {
		// LOGGER.error("Trying to display eElement", e);
		// }

		TheTVDBSerie.setId(toString(serie.id));
		// TheTVDBSerie.setActors(parseList(XMLHelper.getStringTagValue("Actors", eElement), "|,"));
		TheTVDBSerie.setAirsDayOfWeek(serie.airsDayOfWeek);
		TheTVDBSerie.setAirsTime(serie.airsTime);
		TheTVDBSerie.setContentRating(serie.rating);
		TheTVDBSerie.setFirstAired(serie.firstAired);
		TheTVDBSerie.setGenres(serie.genre);
		TheTVDBSerie.setImdbId(serie.imdbId);
		TheTVDBSerie.setLanguage(lang);
		TheTVDBSerie.setNetwork(serie.network);
		TheTVDBSerie.setOverview(serie.overview);
		TheTVDBSerie.setRating(serie.rating);
		TheTVDBSerie.setRuntime(serie.runtime);
		TheTVDBSerie.setSerieId(toString(serie.id));
		TheTVDBSerie.setSerieName(serie.seriesName);
		TheTVDBSerie.setStatus(serie.status);

		// String artwork = XMLHelper.getValueFromElement(eTheTVDBSerie, TYPE_BANNER);
		// if (!artwork.isEmpty()) {
		// TheTVDBSerie.setBanner(bannerMirror + artwork);
		// }
		//
		// artwork = XMLHelper.getValueFromElement(eTheTVDBSerie, TYPE_FANART);
		// if (!artwork.isEmpty()) {
		// TheTVDBSerie.setFanart(bannerMirror + artwork);
		// }
		//
		// artwork = XMLHelper.getValueFromElement(eTheTVDBSerie, TYPE_POSTER);
		// if (!artwork.isEmpty()) {
		// TheTVDBSerie.setPoster(bannerMirror + artwork);
		// }
		//
		TheTVDBSerie.setLastUpdated(toString(serie.lastUpdated));
		TheTVDBSerie.setZap2ItId(serie.zap2itId);

		return TheTVDBSerie;
	}

	private TheTVDBEpisode episodeToTVDBEpisode(Episode episode, String lang) {
		TheTVDBEpisode tvdbEpisode = new TheTVDBEpisode();

		tvdbEpisode.setId(toString(episode.id));
		// tvdbEpisode.setDvdChapter(XMLHelper.getStringTagValue("DVD_chapter", eElement));
		// tvdbEpisode.setDvdDiscId(XMLHelper.getStringTagValue("DVD_discid", eElement));
		tvdbEpisode.setDvdEpisodeNumber(toString(episode.dvdEpisodeNumber));
		tvdbEpisode.setDvdSeason(toString(episode.dvdSeason));
		// tvdbEpisode.setDirectors(parseList(XMLHelper.getStringTagValue("Director", eElement), "|,"));
		// tvdbEpisode.setEpImgFlag(XMLHelper.getStringTagValue("EpImgFlag", eElement));
		tvdbEpisode.setEpisodeName(episode.episodeName);
		tvdbEpisode.setEpisodeNumber(episode.airedEpisodeNumber);
		tvdbEpisode.setFirstAired(episode.firstAired);
		// tvdbEpisode.setGuestStars(parseList(XMLHelper.getStringTagValue("GuestStars", eElement), "|,"));
		// tvdbEpisode.setImdbId(XMLHelper.getStringTagValue("IMDB_ID", eElement));
		tvdbEpisode.setLanguage(lang);
		tvdbEpisode.setOverview(episode.language.overview);
		// tvdbEpisode.setProductionCode(XMLHelper.getStringTagValue("ProductionCode", eElement));
		// tvdbEpisode.setRating(XMLHelper.getStringTagValue("Rating", eElement));
		tvdbEpisode.setSeasonNumber(episode.airedSeason);
		// tvdbEpisode.setWriters(parseList(XMLHelper.getStringTagValue("Writer", eElement), "|,"));
		tvdbEpisode.setAbsoluteNumber(toString(episode.absoluteNumber));
		// String s = XMLHelper.getStringTagValue(eElement, "filename");
		// if (!s.isEmpty()) {
		// episode.setFilename(TheTVDB.getBannerMirror() + s);
		// }
		tvdbEpisode.setLastUpdated(toString(episode.lastUpdated));
		tvdbEpisode.setSeasonId(toString(episode.airedSeasonID));
		// tvdbEpisode.setSeriesId(XMLHelper.getStringTagValue("seriesid", eElement));

		// try {
		// tvdbEpisode.setAirsAfterSeason(XMLHelper.getIntTagValue("airsafter_season", eElement));
		// } catch (Exception ignore) {
		tvdbEpisode.setAirsAfterSeason(0);
		// }

		// try {
		// tvdbEpisode.setAirsBeforeEpisode(XMLHelper.getIntTagValue("airsbefore_episode", eElement));
		// } catch (Exception ignore) {
		tvdbEpisode.setAirsBeforeEpisode(0);
		// }

		return tvdbEpisode;
	}

	private String toString(Object value) {
		if (value != null) {
			return value.toString();
		}
		return null;
	}
}
