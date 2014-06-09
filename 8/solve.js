var CELL_DIMENSIONS = {width: 40, height: 40};

var initializeCells = function() { "use strict";
    var characters = [
        "6EUZWVEFESAH3NOUIRTMN",
        "1OITIRHG4ATWNYKOET3UE",
        "YSHL0EHTREI3EAFD3AOSM",
        "OROKLO1NDYLHI2ESAAEOS",
        "YIGOUUGHTTSRNEEAESVS2",
        "AN3EOKOORP2TINNG2E1CY",
        "NC3HWOAU2LL1DNAOERODR",
        "R3RTIICOCLSO3AANWBAAY",
        "RSM3EDFIEC2DBEYFMRMOO",
        "U2A1ISTONYEFTOATTHDN4",
        "PSY2AWEUNR2HOEWUWEOWF",
        "ITYSE3NADDATR2OEHNHET"
    ];

    var directions = [
        "r           r        ",
        "r       d         d  ",
        "    l      r    l    ",
        "      d      r       ",
        "                    u",
        "  r       u     l u  ",
        "  r     l  d         ",
        " u          r        ",
        "   r      l          ",
        " r l                u",
        "   u      r          ",
        "     u       r       "
    ];

    var cells = [];
    var i, j, row;
    for (i = 0; i < characters.length; i += 1) {
        row = [];
        for (j = 0; j < characters[i].length; j += 1) {
            row[j] = {character: characters[i][j]};
            if (directions[i][j] !== ' ') {
                row[j].direction = directions[i][j];
            }
        }
        cells[i] = row;
    }
    return cells;
};

var drawEdge = function(context, row, col) { "use strict";
    // Subtracting 0.5 keeps our grid one pixel wide
    var leftEdge = col * CELL_DIMENSIONS.width - 0.5;
    var rightEdge = leftEdge + CELL_DIMENSIONS.width;
    var topEdge = row * CELL_DIMENSIONS.height - 0.5;
    var bottomEdge = topEdge + CELL_DIMENSIONS.height;

    context.beginPath();
    context.moveTo(rightEdge, topEdge);
    context.lineTo(rightEdge, bottomEdge);
    context.lineTo(leftEdge, bottomEdge);
    context.strokeStyle = "#aaa";
    context.stroke();
};

var drawCharacter = function(context, row, col, cell) { "use strict";
    var center = cellCenter(row, col);
    context.font = "24px sans-serif";
    context.textBaseline = "middle";
    context.textAlign = "center";
    context.fillText(cell.character, center.x, center.y);
};

var cellCenter = function(row, col) { "use strict";
    return {
        x: CELL_DIMENSIONS.width / 2 + col * CELL_DIMENSIONS.width,
        y: CELL_DIMENSIONS.height / 2 + row * CELL_DIMENSIONS.height
    };
};

var drawDirection = function(context, row, col, cell) { "use strict";
    var center, points, lastPoint;
    if (cell.direction) {
        center = cellCenter(row, col);
        if (cell.direction === 'l') {
            points = [
                {x: -1, y: 0},
                {x: 1, y: 1},
                {x: 1, y: -1}
            ];
        } else if (cell.direction === 'u') {
            points = [
                {x: 0, y: -1},
                {x: -1, y: 1},
                {x: 1, y: 1}
            ];
        } else if (cell.direction === 'r') {
            points = [
                {x: 1, y: 0},
                {x: -1, y: 1},
                {x: -1, y: -1}
            ];
        } else if (cell.direction === 'd') {
            points = [
                {x: 0, y: 1},
                {x: -1, y: -1},
                {x: 1, y: -1}
            ];
        }

        if (points) {
            points.forEach(function(p) { "use strict";
                p.x = center.x + p.x * CELL_DIMENSIONS.width / 3;
                p.y = center.y + p.y * CELL_DIMENSIONS.height / 3;
            });
        }
        lastPoint = points[points.length - 1];

        context.beginPath();
        context.moveTo(lastPoint.x, lastPoint.y);
        points.forEach(function(p) { "use strict";
            context.lineTo(p.x, p.y);
        });
        context.stroke();
    }
};

var cells = initializeCells()

var heuristics = [initializeCells];
var applyHeuristics = function(heuristics) { "use strict";
    heuristics.forEach(function(h) { "use strict";
        h(cells);
    });
};

var sketches = [drawEdge, drawDirection, drawCharacter];
var context = puzzle.getContext("2d");
var canvas = document.getElementById("puzzle");

var drawSketches = function(cells, sketches) { "use strict";
    var i, j;
    canvas.width = cells[0].length * CELL_DIMENSIONS.width;
    canvas.height = cells.length * CELL_DIMENSIONS.height;
    for (i = 0; i < cells.length; i += 1) {
        for (j = 0; j < cells[0].length; j += 1) {
            sketches.forEach(function(sketch) { "use strict";
                sketch(context, i, j, cells[i][j]);
            });
        }
    }
};
