package urza_queue;

public class CrawlTask {
    public String listViewUrl;
    public String articleSelector;
    public String mostRecentArticleUrl;
    public String nextPageSelector;

    public CrawlTask(String listViewUrl, String articleSelector, String mostRecentArticleUrl, String nextPageSelector) {
        this.listViewUrl = listViewUrl;
        this.articleSelector = articleSelector;
        this.mostRecentArticleUrl = mostRecentArticleUrl;
        this.nextPageSelector = nextPageSelector;
    }

    @Override
    public String toString() {
        return listViewUrl;
    }

    @Override
    public int hashCode() {
        int result = 5;
        result = 31 * result + (listViewUrl != null ? listViewUrl.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj == this || !getClass().equals(obj.getClass()))
            return false;

        final CrawlTask other = (CrawlTask) obj;
        if (this.listViewUrl != null && other.listViewUrl != null) {
            return this.listViewUrl.equals(other.listViewUrl);
        }
        return true;
    }
}
