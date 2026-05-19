package project.models.others;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Vector;

/**
 * Represents a university news item.
 * Research-topic news is pinned (sorted first) via Comparable.
 * Observer pattern: NewsService notifies subscribers when new news is added.
 */
public class News implements Comparable<News>, Serializable {
    private static final long serialVersionUID = 1L;
    private String newsId;
    private String title;
    private String content;
    private boolean isResearchNews;
    private Vector<String> comments;
    private Date publishedAt;

    public News() { comments = new Vector<>(); publishedAt = new Date(); }
    public News(String title, String content, boolean isResearchNews) {
        this(); this.title = title; this.content = content; this.isResearchNews = isResearchNews;
        this.newsId = "NEWS-" + System.currentTimeMillis();
    }

    public void addComment(String comment) { if (comment != null && !comment.isBlank()) comments.add(comment); }

    @Override public int compareTo(News o) {
        if (this.isResearchNews && !o.isResearchNews) return -1;
        if (!this.isResearchNews && o.isResearchNews) return 1;
        return o.publishedAt != null && this.publishedAt != null ? o.publishedAt.compareTo(this.publishedAt) : 0;
    }

    public String getNewsId() { return newsId; }
    public void setNewsId(String v) { this.newsId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getContent() { return content; }
    public void setContent(String v) { this.content = v; }
    public boolean isResearchNews() { return isResearchNews; }
    public void setResearchNews(boolean v) { this.isResearchNews = v; }
    public Vector<String> getComments() { return comments; }
    public void setComments(Vector<String> v) { this.comments = v; }
    public Date getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Date v) { this.publishedAt = v; }

    @Override public int hashCode() { return Objects.hash(newsId, title, content, isResearchNews, publishedAt); }
    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof News)) return false;
        News o = (News) obj;
        return Objects.equals(newsId, o.newsId) && Objects.equals(title, o.title);
    }
    @Override public String toString() {
        return "News[" + (isResearchNews ? "RESEARCH" : "GENERAL") + " | " + title + " | " + publishedAt + "]";
    }
}
