package urza_queue;

import java.time.Instant;

public class CrawlTask {
    public String listViewUrl;
    public String articleSelector;
    public String mostRecentArticleUrl;
    private transient Instant lastCrawl;

    public CrawlTask(String listViewURL, String articleSelector, String mostRecentArticleURL) {
        this.listViewUrl = listViewURL;
        this.articleSelector = articleSelector;
        this.mostRecentArticleUrl = mostRecentArticleURL;
        this.lastCrawl = Instant.now();
    }

    @Override
    public String toString() {
        return listViewUrl;
    }

    @Override
    public int hashCode() {
        int result=5;
        result = 31 * result + (listViewUrl != null ? listViewUrl.hashCode() : 0);
        result = 31 * result + (articleSelector != null ? articleSelector.hashCode() : 0);
        result = 31 * result + (mostRecentArticleUrl != null ? mostRecentArticleUrl.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj == this || !getClass().equals(obj.getClass()))
            return false;

        final CrawlTask other = (CrawlTask) obj;
        if (this.listViewUrl != null && other.listViewUrl != null) {
            if (!this.listViewUrl.equals(other.listViewUrl))
                return false;
        }
        if (this.articleSelector != null && other.articleSelector != null) {
            if (!this.articleSelector.equals(other.articleSelector))
                return false;
        }
        if (this.mostRecentArticleUrl != null && other.mostRecentArticleUrl != null) {
            if (!this.mostRecentArticleUrl.equals(other.mostRecentArticleUrl))
                return false;
        }
        return true;
    }
}
