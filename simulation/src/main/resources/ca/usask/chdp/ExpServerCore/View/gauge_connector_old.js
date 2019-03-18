var gauge1A, gauge2;
var initGauge = false;

var gauge_connector = function () {
    gauge1A = new Gauge({
        renderTo: 'part1Gauge',
        width: 150,
        height: 150,
        glow: true,
        units: 'kph',
        title: 'Current Top Speed'
    });

    gauge1A.onready = function () {
        setInterval(function () {
            gauge1A.setValue(Math.random() * 100);
        }, 1000);
    };

    gauge1A.draw();

    gauge2 = new Gauge({
        renderTo: 'part2Gauge',
        width: 150,
        height: 150,
        glow: true,
        units: 'kph',
        title: 'Goal'
    });

    gauge2.onready = function () {
        setInterval(function () {
            gauge2.setValue(Math.random() * 100);
        }, 1000);
    };

    gauge2.draw();
};

