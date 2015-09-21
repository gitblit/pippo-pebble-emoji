package com.gitblit.pippo.emoji;

import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.route.PublicResourceHandler;
import ro.pippo.core.route.ResourceHandler;
import ro.pippo.core.route.Router;
import ro.pippo.core.util.ClasspathUtils;
import ro.pippo.core.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public class EmojiFilter implements Filter {

        private final Pattern emojiPattern = Pattern.compile(":([\\w_+-]+):");

        private final Router router;

        private final Set<String> aliases;

        private String imgUrlPattern;

        public EmojiFilter(Router router) {
            this.router = router;
            this.aliases = loadAliases();
        }

        public List<String> getArgumentNames() {
            return Collections.singletonList("class");
        }

        @Override
        public Object apply(Object input, Map<String, Object> args) {
            if (input == null) {
                return null;
            }

            String imgClass = (String) args.get("class");
            if (StringUtils.isNullOrEmpty(imgClass)) {
                imgClass = "emoji";
            }

            synchronized (this) {
                if (imgUrlPattern == null) {
                    String publicPattern = router.uriPatternFor(PublicResourceHandler.class);
                    if (publicPattern == null) {
                        log.error("Failed to find a 'PublicResourceHandler'!");
                        return input;
                    }

                    String emojiUrl = router.uriFor(publicPattern, new HashMap<String, Object>() {{
                        put(ResourceHandler.PATH_PARAMETER, "emoji/{1}.png");
                    }});
                    imgUrlPattern = "<img class=\"{0}\" src=\"" + emojiUrl + "\" title='':{2}:'' alt='':{2}:''></img>";

                    log.debug("Emoji img url pattern \"{}\"", imgUrlPattern);
                }
            }

            // Substitute emoji aliases with img urls.
            // NOTE: This does not validate emojis.
            StringBuffer sb = new StringBuffer();
            Matcher m = emojiPattern.matcher(input.toString());
            while (m.find()) {
                String alias = m.group(1).trim();
                if (aliases.contains(alias)) {
                    m.appendReplacement(sb, MessageFormat.format(imgUrlPattern, imgClass, alias, alias));
                }
            }
            m.appendTail(sb);
            return sb.toString();
        }

        private Set<String> loadAliases() {
            Set<String> aliases = new HashSet<>();
            try {
                URL url = ClasspathUtils.locateOnClasspath("public/emoji/emojis.txt");
                Pattern aliasPattern = Pattern.compile("(.+)\\.png");
                try (InputStream is = url.openStream()) {
                    for (String line : CharStreams.readLines(new InputStreamReader(is))) {
                        Matcher m = aliasPattern.matcher(line);
                        if (m.find()) {
                            String alias = m.group(1);
                            aliases.add(alias);
                        }
                    }
                }
            } catch (IOException e) {
                log.error("Failed to read emojis list", e);
            }
            return aliases;
        }
    }
}
