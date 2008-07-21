package hudson.plugins.bugzilla;

import hudson.Plugin;
import hudson.model.Jobs;

/**
 * Entry point of a plugin.
 *
 * <p>
 * There must be one {@link Plugin} class in each plugin.
 * See javadoc of {@link Plugin} for more about what can be done on this class.
 *
 * @author Michael Donohue
 * @plugin
 */
public class PluginImpl extends Plugin {
    public void start() throws Exception {
        Jobs.PROPERTIES.add(BugzillaProjectProperty.DESCRIPTOR);
    	new BugzillaChangelogAnnotator().register();
    }
}
