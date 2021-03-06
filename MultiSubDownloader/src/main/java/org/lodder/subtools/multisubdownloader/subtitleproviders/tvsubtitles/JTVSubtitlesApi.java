package org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.model.TVsubtitlesSubtitleDescriptor;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.ManagerException;
import org.lodder.subtools.sublibrary.ManagerSetupException;
import org.lodder.subtools.sublibrary.data.Html;
import org.lodder.subtools.sublibrary.util.http.HttpClientException;

public class JTVSubtitlesApi extends Html {

	public JTVSubtitlesApi(Manager manager) {
		super(manager);
	}

	public List<TVsubtitlesSubtitleDescriptor> searchSubtitles(String name, int season, int episode,
			String title, String languageid) throws Exception {
		List<TVsubtitlesSubtitleDescriptor> lSubtitles = new ArrayList<TVsubtitlesSubtitleDescriptor>();

		String showUrl = this.getShowUrl(name);

		if (showUrl != null) {

			String episodeUrl = getEpisodeUrl(showUrl, season, episode);

			if (episodeUrl != null) {
				episodeUrl =
						"http://www.tvsubtitles.net/" + episodeUrl.substring(0, episodeUrl.indexOf(".")) + "-"
								+ languageid + ".html";
				String searchEpisode = this.getHtml(episodeUrl);
				Document searchEpisodeDoc = Jsoup.parse(searchEpisode);
				Elements searchEpisodes =
						searchEpisodeDoc.getElementsByClass("left_articles").get(0).getElementsByTag("a");

				for (Element ep : searchEpisodes) {
					String url = ep.attr("href");
					if (url.contains("subtitle-")) {
						String subtitlePage = this.getHtml("http://www.tvsubtitles.net/" + url);
						Document subtitlePageDoc = Jsoup.parse(subtitlePage);
						String filename = null, rip = null, download = null, author = null;
						Elements subtitlePageTableDoc = subtitlePageDoc.getElementsByClass("subtitle1");
						if (subtitlePageTableDoc.size() == 1) {
							for (Element item : subtitlePageTableDoc.get(0).getElementsByTag("tr")) {
								Elements row = item.getElementsByTag("td");
								if (row.size() == 3 && row.get(1).text().contains("filename:")) {
									filename = row.get(2).text();
								}
								if (row.size() == 3 && row.get(1).text().contains("rip:")) {
									rip = row.get(2).text();
								}
								if (row.size() == 3 && row.get(1).text().contains("author:")) {
									author = row.get(2).text();
								}
								if (item.toString().contains("download-")) {
									for (Element link : row.get(0).getElementsByTag("a")) {
										if (link.attr("href").contains("download-")) {
											download = "http://www.tvsubtitles.net/" + link.attr("href");
											break;
										}
									}
								}

								if (filename != null && rip != null && download != null) {
									TVsubtitlesSubtitleDescriptor sub = new TVsubtitlesSubtitleDescriptor();
									sub.Filename = filename;
									sub.Url = download;
									sub.Rip = rip;
									sub.Author = author;
									lSubtitles.add(sub);
									rip = null;
									filename = null;
									download = null;
									author = null;
								}
							}
						}
					}

				}
			}
		}

		return lSubtitles;
	}

	private String getEpisodeUrl(String showUrl, int season, int episode) throws IOException,
			HttpClientException, ManagerSetupException, ManagerException {
		String seasonUrl =
				"http://www.tvsubtitles.net/" + showUrl.substring(0, showUrl.indexOf(".")) + "-" + season
						+ ".html";
		String searchSeason = this.getHtmlDisk(seasonUrl);
		Document searchSeasonDoc = Jsoup.parse(searchSeason);
		if (searchSeasonDoc == null)
			return null;

		Element searchSeasonTable = searchSeasonDoc.getElementById("table5");
		String episodeUrl = null;

		boolean foundEp = false;
		for (Element ep : searchSeasonTable.getElementsByTag("td")) {
			if (foundEp) {
				Elements links = ep.getElementsByTag("a");
				if (links.size() == 1) {
					episodeUrl = links.get(0).attr("href");
					break;
				}
			}

			String formatedepisodenumber = "";
			if (episode < 10) {
				formatedepisodenumber = "0" + episode;
			} else {
				formatedepisodenumber = "" + episode;
			}
			if (ep.text().equals(season + "x" + formatedepisodenumber)) {
				foundEp = true;
			}
		}

		return episodeUrl;
	}

	private String getShowUrl(String showName) throws ManagerException {
		Map<String, String> data = new HashMap<String, String>();
		data.put("q", showName);

		String searchShow = this.postHtml("http://www.tvsubtitles.net/search.php", data);
		String showUrl = null;

		Document searchShowDoc = Jsoup.parse(searchShow);
		if (searchShowDoc == null)
			return showUrl;

		Elements shows = searchShowDoc.getElementsByTag("li");
		for (Element show : shows) {
			Elements links = show.getElementsByTag("a");
			if (links.size() == 1 && links.get(0).text().toLowerCase().contains(showName.toLowerCase())) {
				showUrl = links.get(0).attr("href");
				break;
			}
		}

		return showUrl;
	}
}
