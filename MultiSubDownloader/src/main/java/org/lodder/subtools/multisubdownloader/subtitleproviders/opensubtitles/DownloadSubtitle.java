package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception.OpenSubtitlesException;
import org.opensubtitles.api.DownloadApi;
import org.opensubtitles.invoker.ApiClient;
import org.opensubtitles.model.Download200Response;
import org.opensubtitles.model.DownloadRequest;

@Accessors(fluent = true, chain = true)
@Setter
@RequiredArgsConstructor
public final class DownloadSubtitle extends OpenSubtitlesExecuter {
    private final ApiClient apiClient;

    private int fileId;

    public Download200Response download() throws OpenSubtitlesException {
        try {
            return execute(() -> new DownloadApi(apiClient).download("SubTools",
                new DownloadRequest().fileId(fileId)));
        } catch (Exception e) {
            throw new OpenSubtitlesException(e);
        }
    }
}
