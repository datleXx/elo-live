package com.bookie.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class DownloadCsvService {
  private final String BASE_INGEST_LINK = "https://www.football-data.co.uk/mmz4281/";
  private final HttpClient client =
      HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

  public Path download(String season, String leagueCode) {
    String url = BASE_INGEST_LINK + season + "/" + leagueCode + ".csv";
    try {
      Path temp = Files.createTempFile(leagueCode + "-" + season, ".csv");
      HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
      HttpResponse<Path> res = client.send(req, HttpResponse.BodyHandlers.ofFile(temp));
      if (res.statusCode() != 200) {
        throw new RuntimeException("Download failed, status " + res.statusCode() + ": " + url);
      }

      return temp;
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("Failed to download CSV: " + url, e);
    }
  }
}
