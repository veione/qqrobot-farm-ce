window.onload = function() {
    //自动填写用户名密码
    //如果不存在，返回undefined
    let username = $.cookie("robot_username");
    let password = $.cookie("robot_password");
    //undefined == null 为true
    //undefined === null 为false
    if(username != null && password != null) {
        $("#username").val(username);
        $("#password").val(password);
        $("#remember_password").prop("checked", true);
    }
}

function login(form) {
    if(form.robot_username.value === "") {
        alert("用户名不能为空！");
        form.robot_username.focus();
        return;
    }
    if(form.robot_password.value === "") {
        alert("密码不能为空！");
        form.robot_password.focus();
        return;
    }
    //执行登录
    let loginBtn = $("#login_btn");
    let willSavePassword = $("#remember_password").prop("checked");
    loginBtn.attr("disabled", "disabled");
    $.ajax({
        url: contextPath + "api/checkLogin",
        type: 'post',
        dataType: 'json',
        data: {
            robot_username: form.robot_username.value,
            robot_password: form.robot_password.value
        },
        success: function(data) {
            //是否保存密码
            if(willSavePassword) {
                //只有密码正确才保存密码
                if(data.check_passed) savePassword(form);
            } else {
                //删除现有的保存
                deletePassword();
            }
            //判断密码是否正确
            if(data.check_passed) {
                window.location.href = contextPath;
            } else {
                alert("密码错误");
            }
        },
        error: function(xhr) {
            alert("请求失败，状态码：" + xhr.status);
        },
        complete: function(xhr) {
            loginBtn.removeAttr("disabled");
        }
    });
}

function savePassword(form) {
    $.cookie("robot_username", form.robot_username.value, { expires: 30 });
    $.cookie("robot_password", form.robot_password.value, { expires: 30 });
}

function deletePassword() {
    $.removeCookie("robot_username");
    $.removeCookie("robot_password");
}