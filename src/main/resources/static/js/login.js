function login(){
    var username = $("#username").val();
    var password = $("#password").val();
    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/login",
        data: JSON.stringify({
            name: username,
            password:password
        }),
        dataType: 'json',
        cache: false
    });
}