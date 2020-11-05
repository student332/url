package com.example.urlshortener;

import com.google.common.hash.Hashing;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
public class UrlShortener {

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping
    public String main(Map<String, Object> model) {
        model.put("shorted", "");
        return "main";
    }

    @GetMapping("/{id}")
    public String getUrl(@PathVariable String id) {
        String url = redisTemplate.opsForValue().get(id);
        //System.out.println(url);
        return "redirect:" + url;
    }

    @PostMapping
    public String create(@RequestParam String url, Map<String, Object> model, HttpServletRequest request) {

        UrlValidator urlValidator = new UrlValidator(
                new String[]{"http", "https"}
        );

        if (urlValidator.isValid(url)) {
            String shorted = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();

            //System.out.println("Generated shorted URL: " + shorted);
            redisTemplate.opsForValue().set(shorted, url);

            String baseURL = request.getRequestURL().toString();
            model.put("shorted", "Your shorted URL: " + baseURL + shorted);
            return "main";
        }

        throw new RuntimeException("Invalid URL: " + url);
    }
}
