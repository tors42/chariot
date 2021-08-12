package chariot.model;

public record PageTeam (
        Integer currentPage,
        Integer maxPerPage,
        java.util.List<Team> currentPageResults,
        Integer nbResults,
        Integer previousPage,
        Integer nextPage,
        Integer nbPages
        ) implements Page<Team>, Model {}
