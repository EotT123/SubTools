package org.lodder.subtools.sublibrary.data.imdb;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.ApiIntf;
import org.lodder.subtools.sublibrary.data.imdb.exception.ImdbApiException;
import org.lodder.subtools.sublibrary.data.imdb.model.ImdbDetails;

@NullMarked
public class ImdbApi implements ApiIntf {

    private static final String DOMAIN = "https://www.imdb.com";
    private static final String API_DOMAIN = "https://graph.imdbapi.dev/v1";
    @val @override Manager manager;
    @val @override String provider = "IMDB";

    public ImdbApi(Manager manager) {
        this.manager = manager;
    }

    public ImdbDetails getDetails(String imdbId) throws ImdbApiException {
        return getCache("details", b -> b.add("imdbId", imdbId))
            .get(() -> {
                String query = """
                    {
                      title(id: "$imdbId") {
                        start_year
                        primary_title
                      }
                    }
                    """;
                try {
                    JsonNode jsonNode = post(query);
                    String title = jsonNode.get("primary_title").asText();
                    int year = jsonNode.get("start_year").asInt();
                    return new ImdbDetails(title, year);
                } catch (IOException | InterruptedException e) {
                    throw ImdbApiException.error(e,
                        "Error trying to fetch details for id [$imdbId], " + e.getMessage());
                }
            });
    }


    private JsonNode post(String query) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_DOMAIN))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(createJsonQuery(query)))
            .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(response.body());
        }
    }

    private String createJsonQuery(String query) {
        return String.format("{\"query\": \"%s\"}", query.replace("\"", "\\\"").replace("\n", ""));
    }
}
