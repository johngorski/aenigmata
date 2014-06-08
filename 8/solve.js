(function() { "use strict;"
    var initializeCells = function() { "use strict";
        var cells = [];
        var i, j, row;
        for (i = 0; i < 12; i += 1) {
            row = [];
            for (j = 0; j < 21; j += 1) {
                row[j] = {character: 'A'};
            }
            cells[i] = row;
        }
        return cells;
    };

    var drawCells = function(cells) { "use strict";
        var CELL_DIMENSIONS = {width: 40, height: 40};
        var canvas = document.getElementById("puzzle");
        var context = puzzle.getContext("2d");
        canvas.width = cells[0].length * CELL_DIMENSIONS.width;
        canvas.height = cells.length * CELL_DIMENSIONS.height;
        context.fillRect(50, 25, 150, 100);
    };

    var cells = initializeCells();
    drawCells(cells);
})();
