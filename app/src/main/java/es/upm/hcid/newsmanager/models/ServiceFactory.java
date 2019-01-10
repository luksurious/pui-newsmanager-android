package es.upm.hcid.newsmanager.models;

import android.content.Context;

import java.io.IOException;
import java.util.Properties;

import es.upm.hcid.newsmanager.R;
import es.upm.hcid.newsmanager.assignment.ModelManager;

/**
 * This factory creates common objects needed across multiple activities.
 * It needs the specific activity/context and the main preferences.
 */
public class ServiceFactory {
    private Context parentContext;
    private MainPreferences mainPreferences;

    /**
     * @param parentContext   The activity in which the factory is created
     * @param mainPreferences An instance of the main preferences
     */
    public ServiceFactory(Context parentContext, MainPreferences mainPreferences) {
        this.parentContext = parentContext;
        this.mainPreferences = mainPreferences;
    }

    /**
     * Creates a connection/model manager, using the global config, and logged in user into account
     *
     * @return A new model manager for the current state
     */
    public ModelManager createModelManager() {
        Properties config = getConfig();
        Properties connectionProps = new Properties();
        connectionProps.setProperty(ModelManager.ATTR_SERVICE_URL, config.getProperty("API_URL"));
        connectionProps.setProperty(ModelManager.ATTR_ANON_API_KEY, config.getProperty("API_KEY"));
        connectionProps.setProperty(ModelManager.ATTR_REQUIRE_SELF_CERT, "TRUE");

        ModelManager mm = new ModelManager(connectionProps);

        if (mainPreferences.isUserLoggedIn()) {
            User loggedInUser = mainPreferences.getLoggedInUser();

            mm.setLoggedInUser(loggedInUser);
        }

        return mm;
    }

    /**
     * Return the global config
     *
     * @return The global config as Properties
     */
    public Properties getConfig() {
        Properties config;
        if (!mainPreferences.contains(MainPreferences.CONFIG_KEY)) {
            config = readConfig();
            mainPreferences.saveConfig(config);
        } else {
            config = mainPreferences.getConfig();
        }

        return config;
    }

    /**
     * Read the config from the config file
     *
     * @return The config properties
     */
    private Properties readConfig() {
        Properties config = new Properties();
        try {
            config.load(parentContext.getResources().openRawResource(R.raw.config));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }
}
