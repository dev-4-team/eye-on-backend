package com.on.eye.api.demoInfo.service;

// import com.on.eye.api.demoInfo.entity.DemoPost;

import com.on.eye.api.demoInfo.entity.DemoPostImage;
import com.on.eye.api.demoInfo.repository.DemoPostImageRepository;
// import com.on.eye.api.demoInfo.repository.DemoPostRepository;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
@Slf4j
public class DemoImageCrawlerService {

    @Autowired
    private DemoPostImageRepository demoPostImageRepository;

    // private static final String BASE_URL = "";
    private static final String POST_URL = "";

    public void crawlPosts() {
        try {
//             Connect to the post list page
//             Document doc = Jsoup.connect(BASE_URL).get();

//             Find all post rows
//             Elements postRows = doc.select("table.data-list tbody tr");

//            for (Element row : postRows) {
//                DemoPost demoPost = new DemoPost();
//                demoPost.setPostNumber(row.select("td").first().text());
//                demoPost.setTitle(row.select("td.subject").text());
//
//                // Parse date
//                String dateStr = row.select("td:contains(2024)").text();
//                demoPost.setPostDate(LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
//
//                // Get views
//                demoPost.setViews(row.select("td").last().text());

            // Get post detail page
            String postUrl = POST_URL;
            Document postDoc = Jsoup.connect(postUrl).get();

            // Find and download images
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
