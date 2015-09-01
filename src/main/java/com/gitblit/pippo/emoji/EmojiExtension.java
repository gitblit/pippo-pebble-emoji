package com.gitblit.pippo.emoji;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.extension.escaper.RawFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.route.PublicResourceHandler;
import ro.pippo.core.route.ResourceHandler;
import ro.pippo.core.route.Router;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author James Moger
 */
public class EmojiExtension extends AbstractExtension {

    private final Logger log = LoggerFactory.getLogger(EmojiExtension.class);

    private final Router router;

    public EmojiExtension(Router router) {
        this.router = router;
    }

    @Override
    public Map<String, Filter> getFilters() {
        Map<String, Filter> filters = new HashMap<>();
        filters.put("emoji", new EmojiFilter(router));
        return filters;
    }

    public class EmojiFilter extends RawFilter {

        private final Pattern emojiPattern = Pattern.compile(":([\\w\\s-]+):");

        private final Router router;

        private String imgUrlPattern;

        public EmojiFilter(Router router) {
            this.router = router;
        }

        @Override
        public Object apply(Object input, Map<String, Object> args) {
            if (input == null) {
                return null;
            }

            synchronized (this) {
                if (imgUrlPattern == null) {
                    String publicPattern = router.uriPatternFor(PublicResourceHandler.class);
                    if (publicPattern == null) {
                        log.error("Failed to find a 'PublicResourceHandler'!");
                        return input;
                    }

                    String emojiUrl = router.uriFor(publicPattern, new HashMap<String, Object>() {{
                        put(ResourceHandler.PATH_PARAMETER, "emoji/{0}.png");
                    }});
                    imgUrlPattern = "<img class='emoji' src=\"" + emojiUrl + "\" title='':{1}:'' alt='':{1}:''></img>";

                    log.debug("Emoji img url pattern \"{}\"", imgUrlPattern);
                }
            }

            // Substitute emoji aliases with img urls.
            // NOTE: This does not validate emojis.
            StringBuffer sb = new StringBuffer();
            Matcher m = emojiPattern.matcher(input.toString());
            while (m.find()) {
                String code = m.group(1);
                m.appendReplacement(sb, MessageFormat.format(imgUrlPattern, code, code));
            }
            m.appendTail(sb);
            return sb.toString();
        }
    }
}
