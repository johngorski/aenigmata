"use strict";

var CELL_DIMENSIONS = {width: 40, height: 40};

var updateDebug = function(coordinates) {
    document.getElementById('debug').value = "cell " + JSON.stringify(coordinates) + " clicked";
};

var clearOf = function(fraction, margin) {
    return fraction > margin && (1 - fraction) > margin;
};

var toggleVerticalEdge = function(row, leftIndex) {
    return function(puzzle) {
        var copy = {
            characters: puzzle.characters,
            horizontalEdges: puzzle.horizontalEdges,
            verticalEdges: puzzle.verticalEdges
        };

        copy.verticalEdges[row][leftIndex] = (copy.verticalEdges[row][leftIndex] === '|') ? 'o'
            : (copy.verticalEdges[row][leftIndex] === '.') ? '|'
            : '.';
        return copy;
    };
};

var dispatchCellClickEvents = function(location) {
    var margin = 0.1;
    var closest = {
        r: location.y / CELL_DIMENSIONS.height,
        c: location.x / CELL_DIMENSIONS.width
    };
    var cellX = closest.c % 1;
    var cellY = closest.r % 1;

    var coordinates = {
        r: Math.floor(closest.r),
        c: Math.floor(closest.c)
    };

    if (clearOf(cellY, margin) && clearOf(cellX, margin)) {
        // var coordinates = {
            // r: Math.floor(closest.r),
            // c: Math.floor(closest.c)
        // };

        updateDebug(coordinates);
    }

    return [
        toggleVerticalEdge(coordinates.r, coordinates.c)
    ];
};

var gerrymanderPuzzle = function() {
    return {
        characters: [
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
        ],
        verticalEdges: [
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
        ].map(function(row) { return row.split(''); }),
        horizontalEdges: [
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
        ].map(function(row) { return row.split(''); })
    };
};

var drawVerticalEdges = function(ves) {
    return function(context) {
        var row, col, left, ceiling;
        for (row = 0; row < ves.length; row += 1) {
            for (col = 0; col < ves[0].length; col += 1) {
                left = 0.5 + col * CELL_DIMENSIONS.width;
                ceiling = 0.5 + row * CELL_DIMENSIONS.height;
                if (ves[row][col] === '|') {
                    context.beginPath();
                    context.setLineDash([1]);
                    context.lineWidth = 4;
                    context.strokeStyle = "#000000";
                    context.moveTo(left, ceiling);
                    context.lineTo(left, ceiling + CELL_DIMENSIONS.height);
                    context.stroke();
                } else if (ves[row][col] === '.') {
                    context.fillText('x', left, ceiling + CELL_DIMENSIONS.height / 2);
                }
            }
        }
    }
};

var drawHorizontalEdges = function(hes) {
    return function(context) {
        var row, col, left, ceiling;
        for (row = 0; row < hes.length; row += 1) {
            for (col = 0; col < hes[0].length; col += 1) {
                left = 0.5 + col * CELL_DIMENSIONS.width;
                ceiling = 0.5 + row * CELL_DIMENSIONS.height;
                if (hes[row][col] === '-') {
                    context.setLineDash([1]);
                    context.lineWidth = 4;
                    context.strokeStyle = "#000000";
                    context.beginPath();
                    context.moveTo(left, ceiling);
                    context.lineTo(left + CELL_DIMENSIONS.width, ceiling);
                    context.stroke();
                } else if (hes[row][col] === '.') {
                    context.fillText('x', left + CELL_DIMENSIONS.width / 2, ceiling);
                }
            }
        }
    };
};
var cellCenter = function(row, col) {
    // Subtracting 0.5 keeps our grid one pixel wide
    return {
        x: CELL_DIMENSIONS.width / 2 + col * CELL_DIMENSIONS.width - 0.5,
        y: CELL_DIMENSIONS.height / 2 + row * CELL_DIMENSIONS.height - 0.5
    };
};

var drawCellText = function(chars) {
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

        context.strokeStyle = "#888";
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

var draw = function(puzzle, canvas) {
    var context = canvas.getContext('2d');
    // Extra pixel for pixel offsets
    canvas.width = puzzle.characters[0].length * CELL_DIMENSIONS.width + 1;
    canvas.height = puzzle.characters.length * CELL_DIMENSIONS.height + 1;
    [
        drawInnerGrid(puzzle.characters),
        drawCellText(puzzle.characters),
        drawHorizontalEdges(puzzle.horizontalEdges),
        drawVerticalEdges(puzzle.verticalEdges)
    ].forEach(function(drawLayer) { drawLayer(context); });
};

