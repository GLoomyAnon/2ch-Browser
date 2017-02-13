package com.vortexwolf.chan2.common;

import android.net.Uri;

import com.vortexwolf.chan2.boards.fourchan.FourchanWebsite;
import com.vortexwolf.chan2.boards.makaba.MakabaWebsite;
import com.vortexwolf.chan2.interfaces.IWebsite;

public class Websites {
    public static IWebsite getDefault() {
        return Factory.resolve(MakabaWebsite.class);
    }

    // Handles external URLs which are declared in AndroidManifest
    public static IWebsite fromUri(Uri uri) {
        String host = uri.getHost();
        if (MakabaWebsite.URI_PATTERN.matcher(host).find()) {
            return Factory.resolve(MakabaWebsite.class);
        } else if (FourchanWebsite.URI_PATTERN.matcher(host).find()) {
            return Factory.resolve(FourchanWebsite.class);
        }

        return null;
    }

    public static IWebsite fromName(String name) {
        if (MakabaWebsite.NAME.equals(name)) {
            return Factory.resolve(MakabaWebsite.class);
        } else if (FourchanWebsite.NAME.equals(name)) {
            return Factory.resolve(FourchanWebsite.class);
        }

        return null;
    }
}
