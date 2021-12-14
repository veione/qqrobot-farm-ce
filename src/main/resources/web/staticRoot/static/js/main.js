function check(button) {
    return confirm("确定执行" + button.value + "吗？");
}

function relogin(button) {
    if(!check(button)) return;
    window.location.href = contextPath + "action/relogin";
}

function sendTestMessage(button) {
    if(!check(button)) return;
    window.location.href = contextPath + "action/sendTestMessage";
}

function switchWillSendTestMessageOnRelogin() {
    let btns = $(".switch_btn");
    btns.attr("disabled", "disabled");
    $.ajax({
        url: contextPath + "api/switchWillSendTestMessageOnRelogin",
        type: 'get',
        success: function(data) {
            location.reload();
        },
        error: function(xhr) {
            alert("请求失败，状态码：" + xhr.status);
        },
        complete: function(xhr) {
            btns.removeAttr("disabled");
        }
    });
}

function switchWillResendOnSendFailed() {
    let btns = $(".switch_btn");
    btns.attr("disabled", "disabled");
    $.ajax({
        url: contextPath + "api/switchWillResendOnSendFailed",
        type: 'get',
        success: function(data) {
            location.reload();
        },
        error: function(xhr) {
            alert("请求失败，状态码：" + xhr.status);
        },
        complete: function(xhr) {
            btns.removeAttr("disabled");
        }
    });
}