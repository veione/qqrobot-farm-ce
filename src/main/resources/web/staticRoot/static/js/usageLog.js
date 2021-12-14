function load(requestUrl) {
	var content = $("#content");
	$.ajax({
		url: requestUrl,
		type: 'get',
		dataType: 'json',
		success: function(data) {
			content.empty();
			const PAGE_SIZE = data.PAGE_SIZE;
			if(data.maxPage <= 0) content.append("没有结果");
			else {
				var page = data.page;
				var ele = "";
				ele += "<table id=\"usage_log_sheet\">";
				ele += "<tr><th>序号</th><th>时间</th><th>群名</th><th>QQ号</th><th>群名片或昵称</th><th>处理的信息</th><th>回复信息</th></tr>";
				var index = 1 + (page - 1) * PAGE_SIZE;
				for(var log of data.list) {
					ele += "<tr><td>" + index + "</td>";
					ele += "<td>" + log.datetime + "</td>";
					ele += "<td>" + log.groupName + "</td>";
					ele += "<td>" + log.qq + "</td>";
					ele += "<td>" + log.username + "</td>";
					ele += "<td>" + log.msg + "</td>";
					ele += "<td class='reply'>" + log.reply + "</td></tr>";
					index++;
				}
				ele += "</table>";
				ele += "<br><div id=\"page_link\">";
				if(page != 1) ele += "<a href='"+ window.location.pathname + "?page=" + (page-1) + "'>上一页</a>&emsp;";
				for(var i = 1; i <= data.maxPage; i++) {
					if(i == page)
						ele += "<span>" + i + "</span>&emsp;";
					else
						ele += "<a href='"+ window.location.pathname + "?page=" + i + "'>" + i + "</a>&emsp;";
				}
				if(page != data.maxPage) ele += "<a href='"+ window.location.pathname + "?page=" + (page+1) + "'>下一页</a>&emsp;";
				ele += "</div>";
				content.append(ele);
			}
		},
		error: function(xhr) {
			content.empty();
			content.append("<p>加载失败，状态码：" + xhr.status + "</p>");
		}
	});
}