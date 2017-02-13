package com.vortexwolf.chan2.test;

import org.apache.http.Header;

import android.test.InstrumentationTestCase;

import com.vortexwolf.chan2.boards.makaba.MakabaWebsite;
import com.vortexwolf.chan2.common.Factory;
import com.vortexwolf.chan2.interfaces.IHttpStringReader;
import com.vortexwolf.chan2.interfaces.IWebsite;
import com.vortexwolf.chan2.models.domain.CaptchaType;
import com.vortexwolf.chan2.services.HtmlCaptchaChecker;
import com.vortexwolf.chan2.settings.ApplicationSettings;

public class HtmlCaptchaCheckerTest extends InstrumentationTestCase {

    private final IWebsite mWebsite = new MakabaWebsite();

    public void testCanSkip() {
        String responseText = "OK";

        HtmlCaptchaChecker checker = new HtmlCaptchaChecker(new FakeHttpStringReader(responseText), Factory.resolve(ApplicationSettings.class));
        HtmlCaptchaChecker.CaptchaResult result = checker.canSkipCaptcha(mWebsite, CaptchaType.MAILRU, "", "");

        assertTrue(result.canSkip);
    }

    public void testMustEnter() {
        String responseText = "CHECK\nSomeKey";

        HtmlCaptchaChecker checker = new HtmlCaptchaChecker(new FakeHttpStringReader(responseText), Factory.resolve(ApplicationSettings.class));
        HtmlCaptchaChecker.CaptchaResult result = checker.canSkipCaptcha(mWebsite, CaptchaType.MAILRU, "", "");

        assertFalse(result.canSkip);
        assertEquals("SomeKey", result.captchaKey);
    }

    private class FakeHttpStringReader implements IHttpStringReader {

        private final String mResponse;

        public FakeHttpStringReader(String response) {
            this.mResponse = response;
        }

        @Override
        public String fromUri(String uri) {
            return this.mResponse;
        }

        @Override
        public String fromUri(String uri, Header[] customHeaders) {
            return this.mResponse;
        }
    }
}
