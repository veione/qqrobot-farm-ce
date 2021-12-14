function load(requestUrl) {
	const content = $("#content");
	$.ajax({
		url: requestUrl,
		type: 'get',
		dataType: 'json',
		success: function(data) {
			content.empty();
			let info = data.data;
			let box = "<div class='box'>";
			if(data.status !== "success") {
				info = "获取信息失败！堆栈信息如下：\n" + info;
			}
			box = box + info + "</div>";
			content.append(box);
			window.scrollTo(0, document.documentElement.scrollHeight);
		},
		error: function(xhr) {
			content.empty();
			content.append("<p>加载失败，状态码：" + xhr.status + "</p>");
		}
	});
}