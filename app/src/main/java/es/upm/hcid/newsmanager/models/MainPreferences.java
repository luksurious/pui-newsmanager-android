package es.upm.hcid.newsmanager.models;

import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.Properties;

/**
 * Wrapper for the shared preferences item for main preferences.
 * <p>
 * Provides methods to easily save & read the currently needed objects.
 */
public class MainPreferences {
    public static String NAME = "Main";

    public static String USER_KEY = "User";
    public static String LOGGED_IN_KEY = "LoggedIn";
    public static String CONFIG_KEY = "Config";

    private SharedPreferences preferences;

    /**
     * @param preferences The shared preferences object for this ::NAME
     */
    public MainPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public boolean isUserLoggedIn() {
        return preferences.getBoolean(LOGGED_IN_KEY, false);
    }

    public User getLoggedInUser() {
        String userJson = preferences.getString(MainPreferences.USER_KEY, "");
        if (userJson.equals("")) {
            return null;
        }

        return new Gson().fromJson(userJson, User.class);
    }

    public void saveUser(User loggedInUser) {
        preferences.edit()
                .putString(MainPreferences.USER_KEY, new Gson().toJson(loggedInUser))
                .putBoolean(MainPreferences.LOGGED_IN_KEY, true)
                .apply();
    }

    public void logout() {
        preferences.edit()
                .remove(MainPreferences.USER_KEY)
                .putBoolean(MainPreferences.LOGGED_IN_KEY, false)
                .apply();
    }

    public Properties getConfig() {
        String preferencesString = preferences.getString(MainPreferences.CONFIG_KEY, "");
        if (preferencesString.equals("")) {
            return null;
        }

        return new Gson().fromJson(preferencesString, Properties.class);
    }

    public void saveConfig(Properties config) {
        preferences.edit()
                .putString(MainPreferences.CONFIG_KEY, new Gson().toJson(config))
                .apply();
    }

    public boolean contains(String key) {
        return preferences.contains(key);
    }

    public SharedPreferences.Editor edit() {
        return preferences.edit();
    }
}
