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
        $("#executeBtn").attr("disabled", "disabled");
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
                $("#executeBtn").removeAttr("disabled");
                showMsg(data.success, data.msg);
            },
            error: function (data) {
                $("#executeBtn").removeAttr("disabled");
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
        $("#executeBtn").attr("disabled", "disabled");
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
                    $("#executeBtn").removeAttr("disabled");
                    showMsg(data.success, data.msg);
                },
                error: function (data) {
                    $("#executeBtn").removeAttr("disabled");
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
        delay: 9000,
        placement: {
            from: "top",
            align: "center"
        },
        onClose: function () {
            location.reload();
        }
    };
    $("#modal").modal('toggle');
    if (isSuccess) {
        settings = $.extend(settings, {type: 'success'});
        $.notify(options, settings);
    } else {
        settings = $.extend(settings, {type: 'danger'});
        $.notify(options, settings);
    }
}
