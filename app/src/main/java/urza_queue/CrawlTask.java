package urza_queue;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import static urza_queue.Main.*;

public class CrawlTask {
    public String ticker;
    public String listViewUrl;
    public String articleSelector;
    public String mostRecentArticleUrl;
    public String nextPageSelector;
    public boolean oldArticlesScraped;
    public int maxPageDepth;

    public CrawlTask(String ticker, String listViewUrl, String articleSelector, String mostRecentArticleUrl, String nextPageSelector, boolean oldArticlesScraped, int maxPageDepth) {
        this.ticker = ticker;
        this.listViewUrl = listViewUrl;
        this.articleSelector = articleSelector;
        this.mostRecentArticleUrl = mostRecentArticleUrl;
        this.nextPageSelector = nextPageSelector;
        this.oldArticlesScraped = oldArticlesScraped;
        this.maxPageDepth = maxPageDepth;
    }

    public void updateDB() {
        String query = "UPDATE target SET most_recent_article_url=?, old_articles_scraped=? WHERE list_view_url=?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, mostRecentArticleUrl);
            stmt.setBoolean(2, oldArticlesScraped);
            stmt.setString(3, listViewUrl);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Exception: " + e.getMessage());
        }
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
        if (obj == null || getClass() != obj.getClass())
            return false;

        final CrawlTask other = (CrawlTask) obj;
        if (this.listViewUrl != null && other.listViewUrl != null) {
            return this.listViewUrl.equals(other.listViewUrl);
        }
        return false;
    }
}
