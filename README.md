# pippo-pebble-emoji
an emoji filter for Pebble and Pippo

> This requires Pippo >= 0.7.0-SNAPSHOT.

**Add the dependency.**

```xml
<dependency>
    <groupId>com.gitblit.pippo</groupId>
    <artifactId>pippo-pebble-emoji</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Make sure you added a PublicResourceHandler**. 

```java
Pippo pippo = new Pippo();
pippo.getApplication().addPublicResourceRoute();
```

**Register the filter in your custom template engine.**

```java
public class MyEngine extends PebbleTemplateEngine {

    @Override
    protected void init(Application application, PebbleEngine engine) {
        engine.addExtension(new EmojiExtension(application.getRouter()));
    }
}
```

**Register your custom template engine.**

```java
Pippo pippo = new Pippo();
pippo.getApplication().setTemplateEngine(new MyTemplateEngine());
```

**Use the filter.**

    {{ ":coffee: Java is my favorite language!" | emoji | raw }}
    {{ ":coffee: Java is my favorite language!" | emoji('cssclass') | raw }}    
    {{ ":coffee: Java is my favorite language!" | emoji(fixed=true) | raw }}
    {{ ":coffee: Java is my favorite language!" | emoji(inline=true) | raw }}
