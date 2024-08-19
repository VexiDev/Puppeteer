package gg.vexi.TicketSystem.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)  // Annotation available at runtime
public @interface AssociatedRequest {
    String value();
}
