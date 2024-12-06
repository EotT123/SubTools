package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception.OpenSubtitlesException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model.OpensubtitleSerieId;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.util.http.HttpClientException;
import org.opensubtitles.api.AuthenticationApi;
import org.opensubtitles.invoker.ApiClient;
import org.opensubtitles.invoker.ApiException;
import org.opensubtitles.model.Login200Response;
import org.opensubtitles.model.LoginRequest;

public class OpenSubtitlesApi implements SubtitleApi {

    private static final String APIKEY = "3IlyaP0KNv6QmJ1gOBX8IXwzD1P9b8c0";//"lNNp0yv0ah8gytkmYPbHwuaATJqr4rS9";
    private static final ApiClient API_CLIENT;
    private static final String USER_AGENT = "SubTools";
    @val Manager manager;
    @val @override SubtitleSource subtitleSource = SubtitleSource.OPENSUBTITLES;

    static {
        API_CLIENT = new ApiClient();
        API_CLIENT.setApiKey(APIKEY);
    }

    public OpenSubtitlesApi(Manager manager) {
        this.manager = manager;
    }

    public OpenSubtitlesApi(Manager manager, String userName, String password) throws OpenSubtitlesException {
        this(manager);
        login(userName, password);
    }

    public void login(String userName, String password) throws OpenSubtitlesException {
        try {
            Login200Response loginResponse =
                    new AuthenticationApi(API_CLIENT).login("application/json", USER_AGENT,
                            new LoginRequest().username(userName).password(password));
            API_CLIENT.setBearerToken(loginResponse.getToken());
        } catch (ApiException e) {
            throw new OpenSubtitlesException(e);
        }
    }

    public static boolean isValidCredentials(String userName, String password) {
        try {
            new AuthenticationApi(API_CLIENT).login("application/json", USER_AGENT,
                    new LoginRequest().username(userName).password(password));
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    public SearchSubtitles searchSubtitles() {
        return new SearchSubtitles(manager, API_CLIENT);
    }

    public DownloadSubtitle downloadSubtitle() {
        return new DownloadSubtitle(API_CLIENT);
    }

    public List<OpensubtitleSerieId> getProviderSerieIds(String serieName) throws OpenSubtitlesException {
        try {
            return manager.getPageContentBuilder()
                    .url("https://www.opensubtitles.org/libs/suggest.php?format=json3&MovieName="
                            + URLEncoder.encode(serieName.toLowerCase(), StandardCharsets.UTF_8))
                    .userAgent("")
                    .cacheType(CacheType.MEMORY)
                    .retries(1)
                    .retryPredicate(
                            exception -> exception instanceof HttpClientException e && e.getResponseCode() == 429)
                    .retryWait(5)
                    .getAsJsonArray()
                    .stream()
                    .filter(show -> "tv".equals(show.getString("kind")))
                    .map(show -> new OpensubtitleSerieId(show.getString("name"), show.getInt("id"),
                            show.getString("year")))
                    .toList();
        } catch (Exception e) {
            throw new OpenSubtitlesException(e);
        }
    }
}
