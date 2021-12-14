function load(requestUrl) {
    let content = $('#content');
    $.ajax({
        url: requestUrl,
        type: 'get',
        dataType: 'json',
        success: function(data) {
            content.empty();
            let box = "<div class='box'>";
            if(data.status === "success") {
                for(let exception of data.list) {
                    box += "<p class='line'>ID：" + exception.id + "</p>";
                    box += "<p class='line'>" + exception.datetime + "</p>";
                    let exceptionText = exception.exceptionText;
                    exceptionText = exceptionText.replace(/\</g, "&lt;").replace(/\>/g, "&gt;").replace(/\n/g, "<br>");
                    box += "<p class='line'>" + exceptionText + "</p>";
                    box += "<hr>";
                }
            } else {
                box += "获取信息失败！堆栈信息如下：\n" + data.info;
            }
            box += "</div>";
            content.append(box);
        },
        error: function(xhr) {
            content.empty();
            content.append("<p>加载失败，状态码：" + xhr.status + "</p>");
        }
    });
}