package chariot.model;

public record PageStudy (
        Integer currentPage,
        Integer maxPerPage,
        java.util.List<Study> currentPageResults,
        Integer nbResults,
        Integer previousPage,
        Integer nextPage,
        Integer nbPages
        ) implements Page<Study> {}
