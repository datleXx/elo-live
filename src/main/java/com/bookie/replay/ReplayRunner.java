package com.bookie.replay;

import com.bookie.service.DownloadCsvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ReplayRunner {
  private final AtomicBoolean running = new AtomicBoolean(false);
  private volatile Thread runThread;
  private final DownloadCsvService downloadCsvService;
  private final ReplayCsvLoader replayCsvLoader;
  private final ReplayService replayService;
  private final Logger log = LoggerFactory.getLogger(ReplayRunner.class);

  public ReplayRunner(
      DownloadCsvService downloadCsvService,
      ReplayCsvLoader replayCsvLoader,
      ReplayService replayService) {
    this.downloadCsvService = downloadCsvService;
    this.replayCsvLoader = replayCsvLoader;
    this.replayService = replayService;
  }

  public boolean isRunning() {
    return running.get();
  }

  public boolean start(String league, String season, long delayMillis) {
    if (!running.compareAndSet(false, true)) return false;
    runThread =
        new Thread(
            () -> {
              try {
                Path tempFile = downloadCsvService.download(season, league);
                List<List<ReplayRow>> allRows = replayCsvLoader.load(tempFile);
                replayService.reset(league, allRows);

                for (List<ReplayRow> day : allRows) {
                  if (!running.get()) break;
                  replayService.tick(league, day);
                  Thread.sleep(delayMillis);
                }
              } catch (Exception e) {
                log.error("Replay failed for league={} season={}", league, season, e);
              } finally {
                running.set(false);
              }
            });
    runThread.start();

    return true;
  }

  public void stop() {
    running.set(false);
    if (runThread != null) {
      runThread.interrupt();
    }
  }
}
