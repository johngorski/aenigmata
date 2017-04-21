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

var cellClickEvents = function(coordinates) {
    [
        updateDebug
    ].forEach(function(f) { f(coordinates); });
};

var verticalEdges = [
    "|.F22EB22S||2X2DP2GP.|",
    "|2Z2PQD133L13S231L22Y|",
    "|L3K12SOLDYBY3MDGE32M|",
    "|2FY3J13U3C3Q32CCERX2|",
    "|K22T3HS2N2D2NSPWHO3B|",
    "|C3P1M222D2UBZN1OQJ1J|",
    "|MM2C3SFBD3B232BZ1R33|",
    "|3Y1J2J13QYI3X2IS32IL|",
    "|2KQ32R21J1YM3WP2BO22|",
    "|1D3TPKCP2F2ZOWBA3M23|",
    "|HPR112Q2SRZ2S312BGGZ|",
    "|2FGQPGAR3||.XUNS3T3D|"
];

var drawVerticalEdges = function(ves) {
    return function(context) {
        var row, col, left, ceiling, floor;
        for (row = 0; row < ves.length; row += 1) {
            for (col = 0; col < ves[0].length; col += 1) {
                if (ves[row][col] === '|') {
                    left = 0.5 + col * CELL_DIMENSIONS.width;
                    ceiling = 0.5 + row * CELL_DIMENSIONS.height;
                    floor = ceiling + CELL_DIMENSIONS.height;
                    context.beginPath();
                    context.setLineDash([1]);
                    context.lineWidth = 4;
                    context.strokeStyle = "#000000";
                    context.moveTo(left, ceiling);
                    context.lineTo(left, floor);
                    context.stroke();
                }
            }
        }
    }
};

var horizontalEdges = [
    "---------- ----------",
    ".2Z2PQD133-13S231L22.",
    "BL3K12SOLDYBY3MDGE32M",
    "32FY3J13U3C3Q32CCERX2",
    "2K22T3HS2N2D2NSPWHO3B",
    "XC3P1M222D2UBZN1OQJ1J",
    "AMM2C3SFBD3B232BZ1R33",
    "23Y1J2J13QYI3X2IS32IL",
    "32KQ32R21J1YM3WP2BO22",
    "C1D3TPKCP2F2ZOWBA3M23",
    "CHPR112Q2SRZ2S312BGGZ",
    "32FGQPGAR3-.KXUNS3T3D",
    "---------- ----------"
];

var drawHorizontalEdges = function(hes) {
    return function(context) {
        var row, col, left, right, ceiling;
        for (row = 0; row < hes.length; row += 1) {
            for (col = 0; col < hes[0].length; col += 1) {
                if (hes[row][col] === '-') {
                    left = 0.5 + col * CELL_DIMENSIONS.width;
                    right = left + CELL_DIMENSIONS.width;
                    ceiling = 0.5 + row * CELL_DIMENSIONS.height;
                    context.setLineDash([1]);
                    context.lineWidth = 4;
                    context.strokeStyle = "#000000";
                    context.beginPath();
                    context.moveTo(left, ceiling);
                    context.lineTo(right, ceiling);
                    context.stroke();
                }
            }
        }
    };
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
    var centerTo = function(x, y) {
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

var sketches = [
];
var context = puzzle.getContext("2d");
var canvas = document.getElementById("puzzle");

var drawSketches = function(cells, sketches) {
};

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

var drawCells = function(chars) {
    return function(context) {
        var r, c, center;
        context.font = "24px sans-serif";
        context.textBaseline = "middle";
        context.textAlign = "center";
        context.fillStyle = '#000';

        for (r = 0; r < chars.length; r += 1) {
            for (c = 0; c < chars[0].length; c += 1) {
                if (chars[r][c] !== ' ') {
                    center = cellCenter(r, c);
                    context.fillText(chars[r][c], center.x, center.y);
                }
            }
        }
    };
};

var drawInnerGrid = function(chars) {
    return function(context) {
        var r, c, x, y;

        context.strokeStyle = "#aaa";
        context.setLineDash([5, 5]);

        for (r = 1; r < chars.length; r += 1) {
            y = 1 + r * CELL_DIMENSIONS.height;
            context.beginPath();
            context.moveTo(1, y);
            context.lineTo(1 + chars[0].length * CELL_DIMENSIONS.width, y);
            context.stroke();
        }

        for (c = 1; c < chars[0].length; c += 1) {
            x = 1 + c * CELL_DIMENSIONS.width;
            context.beginPath();
            context.moveTo(x, 1);
            context.lineTo(x, 1 + chars.length * CELL_DIMENSIONS.width);
            context.stroke();
        }
    };
};

var draw = function(context) {
    // Extra pixel for pixel offsets
    canvas.width = characters[0].length * CELL_DIMENSIONS.width + 1;
    canvas.height = characters.length * CELL_DIMENSIONS.height + 1;
    [
        drawInnerGrid(characters),
        drawCells(characters),
        drawHorizontalEdges(horizontalEdges),
        drawVerticalEdges(verticalEdges)
    ].forEach(function(drawLayer) { drawLayer(context); });
};





// Save for later? //////////

var eachCellInDirection = function(startRow, startCol, direction, f) {
    var dCell = rowColDeltaFromDirection(direction);
    for (i = row, j = col;
         i >= 0 && j >= 0 && i < cells.length && j < cells[0].length;
         i += dCell.i, j += dCell.j) {
             f(cells[i][j]);
     }
};

var eachCell = function(f) {
    var i, j;
    for (i = 0; i < cells.length; i += 1) {
        for (j = 0; j < cells[0].length; j += 1) {
            f(cells[i][j], i, j);
        }
    }
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


