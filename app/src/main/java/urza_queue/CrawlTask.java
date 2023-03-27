package urza_queue;

import java.time.Instant;

public class CrawlTask {
    public String listViewURL;
    public String articleSelector;
    public String mostRecentArticleURL;
    public Instant lastCrawl;

    public CrawlTask(String listViewURL, String articleSelector, String mostRecentArticleURL) {
        this.listViewURL = listViewURL;
        this.articleSelector = articleSelector;
        this.mostRecentArticleURL = mostRecentArticleURL;
        this.lastCrawl = Instant.now();
    }

    @Override
    public String toString() {
        return "Task for " + listViewURL + ", Last crawl " + lastCrawl.toString();
    }
}
