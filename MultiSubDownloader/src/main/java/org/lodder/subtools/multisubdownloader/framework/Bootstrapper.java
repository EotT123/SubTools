package org.lodder.subtools.multisubdownloader.framework;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.serviceprovider.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class Bootstrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrapper.class);

    public Bootstrapper(UserInteractionHandler userInteractionHandler) {
        ServiceProvider.class.getPermittedSubclasses()
            .forEach(serviceProviderClass -> {
                try {
                    // Instantiate service provider
                    ServiceProvider provider = (ServiceProvider) serviceProviderClass.getConstructor().newInstance();
                    // Register provider
                    provider.register(userInteractionHandler);
                } catch (Exception ignore) {
                    LOGGER.error("ServiceProvider: '{}' failed to create instance.", serviceProviderClass.getName());
                }
            });
    }
}
