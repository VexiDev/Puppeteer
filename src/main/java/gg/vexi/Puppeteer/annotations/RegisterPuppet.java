package gg.vexi.Puppeteer.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterPuppet {
    String value() default "";
}
