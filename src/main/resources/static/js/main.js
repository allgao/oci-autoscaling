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
            success: function (data) {
                showMsg(data.success, data.msg);
            },
            error: function (data) {
                showMsg(data.success, data.msg)
            }
        })
    });
    $("#modal").modal();
}

function createModal(groupName) {
    $("#exampleModalLongTitle").text("Create new auxiliary Servers for " + groupName);
    $("#modalBody").text("How many servers do you want to create?");
    $("#size").attr('min', 1).attr('value', 0).removeAttr('max');
    $("#executeBtn").off('click').click(function () {
        var sizeValue = $("#size").val();
        $.ajax({
                type: "GET",
                url: "/createAuxiliary",
                data: {
                    groupName: groupName,
                    num: sizeValue
                },
                cache: false,
                success: function (data) {
                    showMsg(data.success, data.msg);
                },
                error: function (data) {
                    showMsg(data.success, data.msg)
                }
            }
        )
    });
    $("#modal").modal();
}

function showMsg(isSuccess, msg) {
    var options = {
        icon: 'glyphicon glyphicon-warning-sign',
        title: 'System Notify',
        message: msg
    };
    var settings = {
            placement: {
                from: "top",
                align: "center"
            },
            delay: 5000,
            timer: 2000
        }
    ;
    $("#modal").modal('toggle');
    if (isSuccess) {
        $.notify(options, $.extend(settings, {type: 'success'}));
    } else {
        $.notify(options, $.extend(settings, {type: 'danger'}));
    }
    window.location.replace('main')
}
