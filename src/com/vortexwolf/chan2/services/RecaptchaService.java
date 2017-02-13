package com.vortexwolf.chan2.services;

import android.net.Uri;

import java.io.InputStream;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicHeader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.vortexwolf.chan2.common.Constants;
import com.vortexwolf.chan2.common.Factory;
import com.vortexwolf.chan2.common.library.ExtendedHttpClient;
import com.vortexwolf.chan2.common.utils.IoUtils;
import com.vortexwolf.chan2.common.utils.RegexUtils;
import com.vortexwolf.chan2.exceptions.HttpRequestException;
import com.vortexwolf.chan2.models.domain.CaptchaEntity;
import com.vortexwolf.chan2.models.domain.CaptchaType;
import com.vortexwolf.chan2.services.http.HttpStreamModel;
import com.vortexwolf.chan2.services.http.HttpStreamReader;
import com.vortexwolf.chan2.services.http.HttpStringReader;
import com.vortexwolf.chan2.settings.ApplicationSettings;

public class RecaptchaService {
    private static final String CLOUDFLARE_CHECK_KEY = "6LeT6gcAAAAAAAZ_yDmTMqPH57dJQZdQcu6VFqog";
    private static final String SEND_POST_KEY = "6LcM2P4SAAAAAD97nF449oigatS5hPCIgt8AQanz";
    private static final String RECAPTCHA_CHALLENGE_URI = "http://www.google.com/recaptcha/api/challenge?k=";
    private static final String FALLBACK_URI = "http://www.google.com/recaptcha/api/fallback?k=";
    private static final String IMAGE_URI = "http://www.google.com/recaptcha/api/image?c=";
    private static final String RELOAD_URI = "http://www.google.com/recaptcha/api/reload?type=image&c=%s&k=%s";

    private static final Pattern jsChallengePattern = Pattern.compile("challenge.?:.?'([\\w-]+)'");
    private static final Pattern jsReloadChallengePattern = Pattern.compile("Recaptcha.finish_reload.'(.*?)'.*");


    public static boolean isCloudflareCaptchaPage(String html) {
        return html.contains(CLOUDFLARE_CHECK_KEY);
    }

    public static CaptchaEntity loadCloudflareCaptcha() {
        Uri refererUri = Factory.resolve(ApplicationSettings.class).getDomainUri();
        return loadRecaptcha(CLOUDFLARE_CHECK_KEY, refererUri.toString());
    }

    public static CaptchaEntity loadPostingRecaptcha(String key, String referer) {
        return loadRecaptcha(key, referer);
    }

    public static CaptchaEntity loadRecaptcha(String key, String referer) {
        HttpStringReader httpReader = Factory.resolve(HttpStringReader.class);
        Header[] extraHeaders = referer != null ? new Header[] { new BasicHeader("Referer", referer) } : null;

        try {
            // Download the first challenge id
            String originalResponse = httpReader.fromUri(RECAPTCHA_CHALLENGE_URI + key, extraHeaders);
            String originalChallenge = RegexUtils.getGroupValue(originalResponse, jsChallengePattern, 1);
            if (originalChallenge == null) {
                return null;
            }

            // Download the final challenge id based on the first one
            String reloadUri = String.format(RELOAD_URI, originalChallenge, key);
            String reloadResponse = httpReader.fromUri(reloadUri, extraHeaders);
            String reloadChallenge = RegexUtils.getGroupValue(reloadResponse, jsReloadChallengePattern, 1);
            if (reloadChallenge == null) {
                return null;
            }

            CaptchaEntity captcha = new CaptchaEntity();
            captcha.setKey(reloadChallenge);
            captcha.setUrl(IMAGE_URI + reloadChallenge);
            captcha.setCaptchaType(CaptchaType.RECAPTCHA_V1);
            return captcha;
        } catch (HttpRequestException e) {
            return null;
        }
    }

    public static String getHash(String challenge, String answer) throws Exception {
        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, Constants.MULTIPART_BOUNDARY, Constants.UTF8_CHARSET);
        entity.addPart("c", new StringBody(challenge, Constants.UTF8_CHARSET));
        entity.addPart("response", new StringBody(answer, Constants.UTF8_CHARSET));

        HttpStreamModel httpStreamModel = null;
        try {
            httpStreamModel = Factory.resolve(HttpStreamReader.class).fromUri(FALLBACK_URI + SEND_POST_KEY, null, entity, null, null);
            InputStream stream = httpStreamModel.stream;
            String response = IoUtils.convertStreamToString(stream);
            Document document = Jsoup.parseBodyFragment(response);
            Elements verificationToken = document.select("div.fbc-verification-token textarea");
            String hash = verificationToken.text();
            return hash;
        } finally {
            ExtendedHttpClient.releaseRequestResponse(httpStreamModel.request, httpStreamModel.response);
        }
    }
}
