$(function () {
    $('#size').keypress(function (event) {
        event.preventDefault();
        return false;
    });
});

function scaleModal(groupName, regularRunning, auxiliaryRunning, auxiliaryStarting, auxiliaryStopping, auxiliaryStopped) {
    var min = regularRunning;
    var runningServers = +regularRunning + +auxiliaryRunning + +auxiliaryStarting;
    var max = +runningServers + +auxiliaryStopped;
    $("#exampleModalLongTitle").text("Scale " + groupName);
    $("#modalBody").text("How many servers do you want running?");
    $("#size").attr('min', min).attr('max', max).attr('value', runningServers);
    $("#executeBtn").off('click').click(function () {
        var sizeValue = $("#size").val();
        var scaleValue = +sizeValue - +runningServers;
        $.ajax({
                type: "GET",
                url: "/scale",
                data: {
                    groupName: groupName,
                    num: scaleValue
                },
                cache: false,
                success: function () {
                    $("#success-alert").show();
                    window.setTimeout(function () {
                        $("#success-alert").alert('close');
                    }, 2000);
                    $("#modal").modal('toggle');
                    window.location.replace('main')
                },
                error: function () {
                    $("#error-alert").show();
                    window.setTimeout(function () {
                        $("#error-alert").alert('close');
                    }, 2000);
                    $("#modal").modal('toggle');
                    window.location.replace('main')
                }
            }
        )
    });
    $("#modal").modal();
}

function createModal(groupName) {
    $("#exampleModalLongTitle").text("Create new auxiliary Servers for " + groupName);
    $("#modalBody").text("How many servers do you want to create?");
    $("#size").attr('min', 1).attr('value', 0).removeAttr('max');
    $("#executeBtn").off('click').click(function () {
        var num = $("#size").val();
        alert('Create value=' + num);
    });
    $("#modal").modal();
}
