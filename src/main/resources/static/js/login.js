function login() {
    var username = $("#username").val();
    var password = $("#password").val();
    $.ajax({
        type: "POST",
        url: "/login",
        data: {
            name: username,
            password: password
        },
        cache: false,
        success: function(data) {
            console.log("success ", data.response);
        }
    });
}