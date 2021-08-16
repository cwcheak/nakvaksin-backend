package org.nakvaksin;

import com.google.cloud.firestore.Firestore;
import io.quarkus.runtime.ShutdownEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class ApplicationLifecycleBean {
    private static final Logger log = LoggerFactory.getLogger("ApplicationLifecycleBean");

    @Inject
    Firestore firestore;

    void onStop(@Observes ShutdownEvent ev) {
        log.info("The application is stopping...");
        try {
            firestore.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
