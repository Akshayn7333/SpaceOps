function loadTelemetry() {
    fetch('/api/telemetry')
        .then(function (res) { return res.json(); })
        .then(function (t) {
            document.getElementById('name').textContent = t.spacecraft;
            document.getElementById('battery').textContent = t.battery + '%';
            document.getElementById('temperature').textContent = t.temperature + ' °C';
            document.getElementById('fuel').textContent = t.fuel + '%';

            var status = document.getElementById('status');
            status.textContent = t.status;
            status.className = 'value ' + t.status;

            document.getElementById('updated').textContent =
                'Last updated: ' + new Date().toLocaleTimeString();
        })
        .catch(function () {
            document.getElementById('updated').textContent = 'Could not load telemetry.';
        });
}

document.getElementById('refresh').addEventListener('click', loadTelemetry);
loadTelemetry();
