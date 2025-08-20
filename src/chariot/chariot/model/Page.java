package chariot.model;


public interface Page<T> {
    public Integer currentPage();
    public Integer maxPerPage();
    public java.util.List<T> currentPageResults();
    public Integer nbResults();
    public Integer previousPage();
    public Integer nextPage();
    public Integer nbPages();
}
