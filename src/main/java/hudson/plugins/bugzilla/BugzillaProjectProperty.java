package hudson.plugins.bugzilla;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.FormFieldValidator;

public class BugzillaProjectProperty extends JobProperty<AbstractProject<?,?>> {

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends JobPropertyDescriptor {
    	private String baseUrl;
    	
        public DescriptorImpl() {
            super(BugzillaProjectProperty.class);
            load();
        }

        public boolean isApplicable(Class<? extends Job> jobType) {
        	return false;
        }

        public String getDisplayName() {
        	return "Bugzilla";
        }
        
        public JobProperty<?> newInstance(StaplerRequest req) throws FormException {
        	return new BugzillaProjectProperty();
        }

        public boolean configure(StaplerRequest req) {
            baseUrl = req.getParameter("bugzilla.base");
            save();
            return true;
        }

        public String getBaseUrl() {
            if(baseUrl==null) return "http://bugzilla";
            return baseUrl;
        }

        /**
         * Checks if the Bugzilla URL is accessible and exists.
         */
        public void doUrlCheck(final StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            // this can be used to check existence of any file in any URL, so admin only
            new FormFieldValidator.URLCheck(req,rsp) {
                protected void check() throws IOException, ServletException {
                    String url = Util.fixEmpty(request.getParameter("value"));
                    if(url==null) {
                        error("No bugzilla base URL");
                        return;
                    }

                    try {
                        if(findText(open(new URL(url)),"bugzilla"))
                            ok();
                        else
                            error("This is not a bugzilla URL");
                    } catch (IOException e) {
                    	error("Unable to connect to URL");
                    }
                }
            }.process();
        }

        public void save() {
            super.save();
        }
    }

	@Override
	public JobPropertyDescriptor getDescriptor() {
		return DESCRIPTOR;
	}

}
