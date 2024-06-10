package chariot.model;

import java.util.List;

/**
 * Example,
 *<pre>
 * ...
 * "verdicts": {
 *   "list": [
 *     {
 *       "condition": "Rated ≤ 2200 in Blitz for the last week",
 *       "verdict": "ok"
 *     },
 *     {
 *       "condition": "Rated ≥ 1800 in Blitz",
 *       "verdict": "ok"
 *     },
 *     {
 *       "condition": "7 days old account",
 *       "verdict": "ok"
 *     },
 *     {
 *       "condition": "Play your games",
 *       "verdict": "ok"
 *     }
 *   ],
 *   "accepted": true
 * }
 * ...
 *</pre>
 *
 */
public record Verdict (List<Condition> list, boolean accepted) {
    public record Condition(String condition, String verdict) {}
}


