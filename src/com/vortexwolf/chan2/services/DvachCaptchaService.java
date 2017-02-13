package com.vortexwolf.chan2.services;

import com.vortexwolf.chan2.interfaces.IUrlBuilder;
import com.vortexwolf.chan2.interfaces.IWebsite;
import com.vortexwolf.chan2.models.domain.CaptchaEntity;
import com.vortexwolf.chan2.models.domain.CaptchaType;

public class DvachCaptchaService {
    public static final String IMAGE_URI = "/api/captcha/2chaptcha/image/";

    public CaptchaEntity loadCaptcha(String key, IWebsite website) {
        IUrlBuilder urlBuilder = website.getUrlBuilder();
        String imageUrl = urlBuilder.makeAbsolute(IMAGE_URI + key);

        CaptchaEntity captcha = new CaptchaEntity();
        captcha.setCaptchaType(CaptchaType.DVACH);
        captcha.setKey(key);
        captcha.setUrl(imageUrl);
        return captcha;
    }
}
