var CELL_DIMENSIONS = {width: 40, height: 40};

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

var context = puzzle.getContext("2d");
var canvas = document.getElementById("puzzle");

function Vector(x, y) {
    this.x = x;
    this.y = y;
};

Vector.prototype.plus = function(that) {
    return new Vector(this.x + that.x, this.y + that.y);
};
Vector.prototype.times = function(factor) {
    return new Vector(this.x * factor, this.y * factor);
};

var up = function(y) {  return new Vector(0, -y); };
var down = function(y) {  return new Vector(0, y); };
var right = function(x) {  return new Vector(x, 0); };
var left = function(x) {  return new Vector(-x, 0); };

var movementVectors = {
    'a': up(4),
    'b': up(2),
    'c': right(3),
    'd': down(3),
    'e': left(3),
    'f': right(2),
    'g': right(1),
    'h': down(3),
    'i': right(4),
    'j': left(2),
    'k': left(1),
    'l': up(3),
    'm': right(3),
    'n': up(1),
    'o': up(2),
    'p': down(5),
    'q': down(1),
    'r': right(5),
    's': left(1),
    't': up(5),
    'u': left(3),
    'v': right(3),
    'w': down(2),
    'x': left(3),
    'y': down(1),
    'z': down(2)
};

var segment = function(word) { 
    var sum = new Vector(0, 0);
    var i;
    for (i = 0; i < word.length; i += 1) {
        sum = sum.plus(movementVectors[word[i]]);
    }
    return sum;
};

