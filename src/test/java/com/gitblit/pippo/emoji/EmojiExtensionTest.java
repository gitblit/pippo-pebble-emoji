package com.gitblit.pippo.emoji;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ro.pippo.core.route.DefaultRouter;
import ro.pippo.core.route.PublicResourceHandler;
import ro.pippo.core.route.Route;

import java.util.HashMap;
import java.util.Map;

/**
 * @author James Moger
 */
public class EmojiExtensionTest extends Assert {

    final Map<String, Object> args = new HashMap<>();

    EmojiExtension.EmojiFilter filter;

    @Before
    public void setup() {
        PublicResourceHandler handler = new PublicResourceHandler("/public");
        DefaultRouter router = new DefaultRouter();
        router.addRoute(new Route(handler.getUriPattern(), "GET", handler));
        EmojiExtension ext = new EmojiExtension(router);
        filter = (EmojiExtension.EmojiFilter) ext.getFilters().get("emoji");
    }

    private String sub(String input) {
        return filter.apply(input, args).toString();
    }

    private void works(String alias) {
        String file = alias.replace(":", "").trim();
        assertEquals("<img class=\"emoji\" src=\"/public/emoji/" + file + ".png\" title='" + alias + "' alt='" + alias + "'></img>", sub(alias));
    }

    private void skip(String alias) {
        assertEquals(alias, sub(alias));
    }

    @Test
    public void testAliases() {
        works(":+1:");
        works(":coffee:");

        skip(":coffee2:");
        skip(":12:15:");
    }

    @Test
    public void testSub() {
        assertEquals("Woohoo! <img class=\"emoji\" src=\"/public/emoji/+1.png\" title=':+1:' alt=':+1:'></img> This is working!", sub("Woohoo! :+1: This is working!"));
    }

}