package project.models.others;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Represents an academic research paper with full citation support.
 * Implements Comparable for natural ordering by citations (descending).
 */
public class ResearchPaper implements Comparable<ResearchPaper>, Serializable {
    private static final long serialVersionUID = 1L;
    private String title;
    private String authors;
    private String journal;
    private int citations;
    private String doi;
    private Date publishDate;
    private int pages;

    public ResearchPaper() {}

    public ResearchPaper(String title, String authors, String journal, int citations, String doi, Date publishDate, int pages) {
        this.title = title; this.authors = authors; this.journal = journal;
        this.citations = citations; this.doi = doi;
        this.publishDate = publishDate; this.pages = pages;
    }

    /**
     * Returns citation string in Plain Text or BibTeX format.
     * @param format "Plain Text" or "Bibtex"
     */
    public String getCitation(String format) {
        String year = publishDate != null ? new SimpleDateFormat("yyyy").format(publishDate) : "n.d.";
        if ("Bibtex".equalsIgnoreCase(format)) {
            String key = (authors != null && !authors.isEmpty() ? authors.split(",")[0].trim().replaceAll("\\s+", "") : "Unknown") + year;
            return "@article{" + key + ",\n" +
                   "  title={" + title + "},\n" +
                   "  author={" + authors + "},\n" +
                   "  journal={" + journal + "},\n" +
                   "  year={" + year + "},\n" +
                   "  pages={" + pages + "},\n" +
                   "  doi={" + doi + "}\n}";
        }
        // Default: Plain Text
        return authors + " (" + year + "). " + title + ". " + journal + ". Pages: " + pages + ". DOI: " + doi;
    }

    // Natural order: higher citations first
    @Override public int compareTo(ResearchPaper o) { return Integer.compare(o.citations, this.citations); }

    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }
    public String getAuthors() { return authors; }
    public void setAuthors(String a) { this.authors = a; }
    public String getJournal() { return journal; }
    public void setJournal(String j) { this.journal = j; }
    public int getCitations() { return citations; }
    public void setCitations(int c) { this.citations = c; }
    public String getDoi() { return doi; }
    public void setDoi(String d) { this.doi = d; }
    public Date getPublishDate() { return publishDate; }
    public void setPublishDate(Date pd) { this.publishDate = pd; }
    public int getPages() { return pages; }
    public void setPages(int p) { this.pages = p; }

    @Override public int hashCode() { return Objects.hash(authors, citations, doi, journal, pages, publishDate, title); }
    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ResearchPaper)) return false;
        ResearchPaper o = (ResearchPaper) obj;
        return citations == o.citations && pages == o.pages && Objects.equals(doi, o.doi)
               && Objects.equals(title, o.title) && Objects.equals(authors, o.authors);
    }
    @Override public String toString() {
        return "ResearchPaper[title=" + title + ", authors=" + authors + ", citations=" + citations + ", doi=" + doi + "]";
    }
}
