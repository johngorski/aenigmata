"use strict";

var CELL_DIMENSIONS = {width: 40, height: 40};

var updateDebug = function(coordinates) {
    document.getElementById('debug').value = "cell " + JSON.stringify(coordinates) + " clicked";
};

var clearOf = function(fraction, margin) {
    return fraction > margin && (1 - fraction) > margin;
};

var dispatchCellClickEvents = function(location) {
    var margin = 0.1;
    var closest = {
        r: location.y / CELL_DIMENSIONS.height,
        c: location.x / CELL_DIMENSIONS.width
    };

    if (clearOf(closest.r % 1, margin) && clearOf(closest.c % 1, margin)) {
        var coordinates = {
            r: Math.floor(closest.r),
            c: Math.floor(closest.c)
        };

        cellClickEvents(coordinates);
    }
};

var puzzleClickEvents = function(location) {
    [
        dispatchCellClickEvents
    ].forEach(function(f) { f(location); });
};

var cellClickEvents = function(coordinates) {
    [
        updateDebug
    ].forEach(function(f) { f(coordinates); });
};

var puzzleClick = function(e) {
    var location = {
        x: e.x - this.offsetLeft,
        y: e.y - this.offsetTop
    };
    puzzleClickEvents(location);
};

var eachCell = function(f) {
    var i, j;
    for (i = 0; i < cells.length; i += 1) {
        for (j = 0; j < cells[0].length; j += 1) {
            f(cells[i][j], i, j);
        }
    }
};

var initializeCells = function(cells) {
    var characters = [
        "22F22EB22S 32X2DP2GP2",
        "22Z2PQD133L13S231L22Y",
        "BL3K12SOLDYBY3MDGE32M",
        "32FY3J13U3C3Q32CCERX2",
        "2K22T3HS2N2D2NSPWHO3B",
        "XC3P1M222D2UBZN1OQJ1J",
        "AMM2C3SFBD3B232BZ1R33",
        "23Y1J2J13QYI3X2IS32IL",
        "32KQ32R21J1YM3WP2BO22",
        "C1D3TPKCP2F2ZOWBA3M23",
        "CHPR112Q2SRZ2S312BGGZ",
        "32FGQPGAR3 2KXUNS3T3D"
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
};

var findCellsExcludedFromPath = function(cells) {
    eachCell(function(cell) {
        cell.excluded = isNumber(cell.character);
    });
};

var findPossiblePaths = function(cells) {
    eachCell(function(cell, i, j) {
        if (!isNumber(cell.character)) {
            cell.canGoUp = i > 0 && !cells[i - 1][j].excluded;
            cell.canGoDown = i < cells.length - 1 && !cells[i + 1][j].excluded;
            cell.canGoLeft = j > 0 && !cells[i][j - 1].excluded;
            cell.canGoRight = j < cells[0].length - 1 && !cells[i][j + 1].excluded;
        }
    });
};

var countCellDegree = function(cells) {
    // run after findPossiblePaths()
    eachCell(function(cell) {
        var degree = 0;
        if (!(cell.excluded || cell.shaded)) {
            if (cell.canGoUp) { degree += 1; }
            if (cell.canGoDown) { degree += 1; }
            if (cell.canGoLeft) { degree += 1; }
            if (cell.canGoRight) { degree += 1; }
        }
        cell.degree = degree;
    });
};

var drawEdge = function(context, center, cell) {
    var leftEdge = center.x - CELL_DIMENSIONS.width / 2;
    var rightEdge = leftEdge + CELL_DIMENSIONS.width;
    var topEdge = center.y - CELL_DIMENSIONS.height / 2;
    var bottomEdge = topEdge + CELL_DIMENSIONS.height;

    context.strokeStyle = "#aaa";
    context.rect(leftEdge + 1, topEdge + 1, CELL_DIMENSIONS.width, CELL_DIMENSIONS.height);
    context.stroke();
};

var drawCharacter = function(context, center, cell) {
    context.font = "24px sans-serif";
    context.textBaseline = "middle";
    context.textAlign = "center";
    context.fillStyle = '#000';
    context.fillText(cell.character, center.x, center.y);
};

var cellCenter = function(row, col) {
    // Subtracting 0.5 keeps our grid one pixel wide
    return {
        x: CELL_DIMENSIONS.width / 2 + col * CELL_DIMENSIONS.width - 0.5,
        y: CELL_DIMENSIONS.height / 2 + row * CELL_DIMENSIONS.height - 0.5
    };
};

var isNumber = function(str) {
    return str.match(/\d/) ? true : false;
};

var drawDirection = function(context, center, cell) {
    var points, lastPoint;
    if (cell.direction) {
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
            points.forEach(function(p) {
                p.x = center.x + p.x * CELL_DIMENSIONS.width / 3;
                p.y = center.y + p.y * CELL_DIMENSIONS.height / 3;
            });
        }
        lastPoint = points[points.length - 1];

        context.beginPath();
        context.moveTo(lastPoint.x, lastPoint.y);
        points.forEach(function(p) {
            context.lineTo(p.x, p.y);
        });
        context.fillStyle = '#aaa';
        context.fill();
        context.stroke();
    }
};

var drawUpPossible = function(context, center, cell) {
    if (cell.canGoUp) {
        context.beginPath();
        context.moveTo(center.x, center.y);
        context.lineTo(center.x, center.y - CELL_DIMENSIONS.height / 2 + 1);
        context.strokeStyle = "#aa0";
        context.stroke();
    }
};

var drawDownPossible = function(context, center, cell) {
    if (cell.canGoDown) {
        context.beginPath();
        context.moveTo(center.x, center.y);
        context.lineTo(center.x, center.y + CELL_DIMENSIONS.height / 2);
        context.strokeStyle = "#aa0";
        context.stroke();
    }
};

var drawLeftPossible = function(context, center, cell) {
    if (cell.canGoLeft) {
        context.beginPath();
        context.moveTo(center.x, center.y);
        context.lineTo(center.x - CELL_DIMENSIONS.width / 2 + 1, center.y);
        context.strokeStyle = "#aa0";
        context.stroke();
    }
};

var drawRightPossible = function(context, center, cell) {
    if (cell.canGoRight) {
        context.beginPath();
        context.moveTo(center.x, center.y);
        context.lineTo(center.x + CELL_DIMENSIONS.width / 2, center.y);
        context.strokeStyle = "#aa0";
        context.stroke();
    }
};

var rowColDeltaFromDirection = function(direction) {
    if (direction === 'r') {
        return {i: 0, j: 1};
    } else if (direction === 'l') {
        return {i: 0, j: -1};
    } else if (direction === 'u') {
        return {i: -1, j: 0};
    } else if (direction === 'd') {
        return {i: 1, j: 0};
    }
};

var drawContainsPath = function(context, center, cell) {
    if (cell.containsPath) {
        context.beginPath();
        context.arc(center.x, center.y, CELL_DIMENSIONS.width / 10, 0, 2 * Math.PI);
        context.fillStyle = "#f00";
        context.fill();
    }
};

var eachCellInDirection = function(startRow, startCol, direction, f) {
    var dCell = rowColDeltaFromDirection(direction);
    for (i = row, j = col;
         i >= 0 && j >= 0 && i < cells.length && j < cells[0].length;
         i += dCell.i, j += dCell.j) {
             f(cells[i][j]);
     }
};

var markPathCellsFromExhaustedShade = function(cells) {
    eachCell(function(cell, row, col) {
        var i, j, shadedCellCount, dCell;
        if (cell.direction) {
            shadedCellCount = 0;
            dCell = rowColDeltaFromDirection(cell.direction);
            for (i = row, j = col;
                 i >= 0 && j >= 0 && i < cells.length && j < cells[0].length;
                 i += dCell.i, j += dCell.j) {
                if (cells[i][j].shaded) {
                    shadedCellCount += 1;
                }
            }
            if (shadedCellCount == cell.character) {
                for (i = row, j = col;
                     i >= 0 && j >= 0 && i < cells.length && j < cells[0].length;
                     i += dCell.i, j += dCell.j) {
                    if (!cells[i][j].shaded && !isNumber(cells[i][j].character)) {
                        cells[i][j].containsPath = true;
                    }
                 }
            }
        }
    });
};

var markPathWhenDegree2CellsContainPath = function(cells) {
    eachCell(function(cell, i, j) {
        if (cell.degree === 2 && cell.containsPath) {
            if (cell.canGoUp) {
                cell.pathUp = true;
                if (i > 0) {
                    cells[i - 1][j].pathDown = true;
                }
            }
            if (cell.canGoDown) {
                cell.pathDown = true;
                if (i + 1 < cells.length) {
                    cells[i + 1][j].pathUp = true;
                }
            }
            if (cell.canGoLeft) {
                cell.pathLeft = true;
                if (j > 0) {
                    cells[i][j - 1].pathRight = true;
                }
            }
            if (cell.canGoRight) {
                cell.pathRight = true;
                if (j + 1 < cells[0].length) {
                    cells[i][j + 1].pathLeft = true;
                }
            }
        }
    });
};

var drawPath = function(context, center, cell) {
    var w = CELL_DIMENSIONS.width / 2;
    var h = CELL_DIMENSIONS.height / 2;
    var centerTo = function(x, y) { "use strict"
        context.moveTo(center.x, center.y);
        context.lineTo(x, y);
    };
    context.beginPath();
    context.strokeStyle = "#00f";
    context.lineWidth = CELL_DIMENSIONS.width / 10;

    if (cell.pathUp) {
        centerTo(center.x, center.y - h);
    }
    if (cell.pathDown) {
        centerTo(center.x, center.y + h);
    }
    if (cell.pathLeft) {
        centerTo(center.x - w, center.y);
    }
    if (cell.pathRight) {
        centerTo(center.x + w, center.y);
    }

    context.stroke();
};

var cells = [];

var heuristics = [
    initializeCells,
    findCellsExcludedFromPath,
    findPossiblePaths,
    markPathCellsFromExhaustedShade,
    countCellDegree,
    markPathWhenDegree2CellsContainPath
];

var applyHeuristics = function(heuristics) {
    heuristics.forEach(function(h) {
        try {
            h(cells);
        } catch (e) {
            // Don't let failed past heuristics stop future heuristics
        }
    });
};

var sketches = [
    drawEdge,
    drawDirection,
    drawCharacter,
    drawUpPossible, drawDownPossible, drawLeftPossible, drawRightPossible,
    drawContainsPath,
    drawPath
];
var context = puzzle.getContext("2d");
var canvas = document.getElementById("puzzle");

var drawSketches = function(cells, sketches) {
    var i, j;
    // Extra pixel for pixel offsets
    canvas.width = cells[0].length * CELL_DIMENSIONS.width + 1;
    canvas.height = cells.length * CELL_DIMENSIONS.height + 1;
    sketches.forEach(function(sketch) {
        for (i = 0; i < cells.length; i += 1) {
            for (j = 0; j < cells[0].length; j += 1) {
                try {
                    sketch(context, cellCenter(i, j), cells[i][j]);
                } catch (e) {
                    // Don't let failed past sketches stop future sketches
                }
            }
        }
    });
};
