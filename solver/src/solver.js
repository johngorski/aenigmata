/**
 * An attempt to port my Scala implementation of my general puzzle-solving
 * approach to JavaScript.
 * No type-checking, streams, or tail recursion. Here we go!
 *
 * puzzle is the initial state of the Puzzle that solver will solve.
 * heuristics is an array of functions mapping a puzzle to a version of puzzle
 * which is closer to the solution (a heuristic).
 * ***IMPORTANT***: Heuristics must NEVER mutate the puzzle they act upon. Stupid state.
 */
var solve = function(puzzle, heuristics) {
    // There's a library Lazy.js which could get us an easy JavaScript implementation
    // of our steps. Might be worth looking into later.

    /**
     * @return The result of milking every last drop of Heuristic h from Puzzle p.
     */
    var apply = function(p, h) {
        var previous = p;
        var updated = h(p);
        while (updated !== previous) {
            previous = updated;
            updated = h(previous);
        }
        return updated;
    };

    /**
     * @param p a puzzle
     * @param hs an array of heuristics
     */
    var applyAll = function(p, hs) {
        var scratch = p;
        hs.forEach(function(h) { scratch = apply(scratch, h); });
        return scratch;
    };

    return apply(puzzle, function(p) { return applyAll(p, heuristics); } );
};
