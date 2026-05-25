package org.lodder.subtools.multisubdownloader.framework;

import java.util.Set;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.framework.service.providers.ServiceProvider;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class Bootstrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrapper.class);

    public Bootstrapper(UserInteractionHandler userInteractionHandler) {
        Reflections reflections = new Reflections("org.lodder.subtools.multisubdownloader");
        Set<Class<? extends ServiceProvider>> providerClasses = reflections.getSubTypesOf(ServiceProvider.class);

        for (Class serviceProviderClass : providerClasses) {
            try {
                // Instantiate service provider
                ServiceProvider provider = (ServiceProvider) serviceProviderClass.getConstructor().newInstance();
                // Register provider
                provider.register(userInteractionHandler);
                LOGGER.debug("ServiceProvider: '{}' registered.", provider.getClass().getName());
            } catch (Exception e) {
                LOGGER.error("ServiceProvider: '{}' failed to create instance.", serviceProviderClass.getName());
            }
        }
    }
}
