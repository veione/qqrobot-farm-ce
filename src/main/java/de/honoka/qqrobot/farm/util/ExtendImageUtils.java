package de.honoka.qqrobot.farm.util;

import de.honoka.qqrobot.spring.boot.starter.component.util.RobotImageUtils;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.Map;

@Component
public class ExtendImageUtils {

    @SneakyThrows
    public File getImageByPost(String url, Map<String, String> params) {
        return getImageByPost(url, params, RobotImageUtils.DEFAULT_IMAGE_WIDTH);
    }

    @SneakyThrows
    public File getImageByPost(String url, Map<String, String> params, int width) {
        Connection conn = Jsoup.connect(url);
        if(params != null) {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                conn.data(entry.getKey(), entry.getValue());
            }
        }
        String html = conn.post().outerHtml();
        return robotImageUtils.htmlToImage(html, width);
    }

    @SneakyThrows
    public File getImage(String url) {
        return getImage(url, RobotImageUtils.DEFAULT_IMAGE_WIDTH);
    }

    @SneakyThrows
    public File getImage(String url, int width) {
        String html = Jsoup.connect(url).get().outerHtml();
        return robotImageUtils.htmlToImage(html, width);
    }

    @Resource
    private RobotImageUtils robotImageUtils;
}
