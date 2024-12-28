package com.on.eye.api.demoInfo.service;

import com.on.eye.api.demoInfo.entity.DemoPostImage;
import com.on.eye.api.demoInfo.repository.DemoPostImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Service
@Slf4j
public class DemoImageCrawlerService {

    @Autowired
    private DemoPostImageRepository demoPostImageRepository;

    private static final String POST_URL = "";

    public void crawlPosts() {
        try {
            String postUrl = POST_URL;
            Document postDoc = Jsoup.connect(postUrl).get();

            // save imagesUrl
            Elements images = postDoc.select("div.reply-content img");
            if (!images.isEmpty()) {
                Element image = images.first();
                String imgUrl = image.attr("abs:src");
                DemoPostImage demoPostImage = new DemoPostImage();
                demoPostImage.setImageUrl(imgUrl);

                demoPostImageRepository.save(demoPostImage);
            }
        } catch (IOException e) {
            log.error("Error crawling posts: ", e);
        }
    }

    private byte[] downloadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        try (InputStream in = url.openStream()) {
            return IOUtils.toByteArray(in);
        }
    }
}
