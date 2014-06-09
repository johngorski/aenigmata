(function() { "use strict;"
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
        for (i = 0; i < 12; i += 1) {
            row = [];
            for (j = 0; j < 21; j += 1) {
                row[j] = {character: characters[i][j]};
                if (directions[i][j] !== ' ') {
                    row[j].direction = directions[i][j];
                }
            }
            cells[i] = row;
        }
        return cells;
    };

    var drawCells = function(cells) { "use strict";
        var CELL_DIMENSIONS = {width: 40, height: 40};
        var canvas = document.getElementById("puzzle");
        var context = puzzle.getContext("2d");
        var i, j;

        canvas.width = cells[0].length * CELL_DIMENSIONS.width;
        canvas.height = cells.length * CELL_DIMENSIONS.height;

        var drawEdge = function(row, col) { "use strict";
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

        var cellCenter = function(row, col) { "use strict";
            return {
                x: CELL_DIMENSIONS.width / 2 + col * CELL_DIMENSIONS.width,
                y: CELL_DIMENSIONS.height / 2 + row * CELL_DIMENSIONS.height
            };
        };

        var drawCharacter = function(row, col, cell) { "use strict";
            var center = cellCenter(row, col);
            context.font = "24px sans-serif";
            context.textBaseline = "middle";
            context.textAlign = "center";
            context.fillText(cell.character, center.x, center.y);
        };

        var last = function(a) { "use strict";
            return a[a.length - 1];
        };

        var drawDirection = function(row, col, cell) { "use strict";
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
                lastPoint = last(points);

                context.beginPath();
                context.moveTo(lastPoint.x, lastPoint.y);
                points.forEach(function(p) { "use strict";
                    context.lineTo(p.x, p.y);
                });
                context.stroke();
            }
        };

        for (i = 0; i < cells.length; i += 1) {
            for (j = 0; j < cells[0].length; j += 1) {
                drawEdge(i, j);
                drawDirection(i, j, cells[i][j]);
                drawCharacter(i, j, cells[i][j]);
            }
        }
    };

    var cells = initializeCells();
    drawCells(cells);
})();
