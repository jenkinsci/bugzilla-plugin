package hudson.plugins.bugzilla;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.FormValidation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;
import org.apache.xmlrpc.XmlRpcException;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class BugzillaProjectProperty extends JobProperty<AbstractProject<?,?>> {

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends JobPropertyDescriptor {
    	private BugzillaSession bugzillaSession;
    	private String regex;
    	private boolean useTooltips;
    	
        public DescriptorImpl() {
            super(BugzillaProjectProperty.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
        	return false;
        }

        public String getDisplayName() {
        	return "Bugzilla";
        }
        
        @Override
        public BugzillaProjectProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        	return new BugzillaProjectProperty();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) {
            try {
                regex = req.getParameter("bugzilla.regex");
            	if(req.getParameter("bugzilla.usetooltips")==null) {
            		useTooltips = false;
            		bugzillaSession = new BugzillaSession(req.getParameter("bugzilla.base"));
            	} else {
            		useTooltips = true;
					bugzillaSession = new BugzillaSession(
							req.getParameter("bugzilla.base"),
							req.getParameter("bugzilla.username"),
							req.getParameter("bugzilla.password")
					);
            	}
			} catch (MalformedURLException e) {
			} catch (XmlRpcException e) {
			}
            save();
            return true;
        }

        public String getBaseUrl() {
            if(bugzillaSession==null) return "http://bugzilla";
            return bugzillaSession.getUrl();
        }

        public String getUsername() {
        	if(bugzillaSession == null) return "";
        	return bugzillaSession.getUsername();
        }
        
        public String getPassword() {
        	if(bugzillaSession == null) return "";
        	return bugzillaSession.getPassword();
        }
        
        public boolean getUseToolTips() {
        	return useTooltips;
        }
        
        public String getRegex() {
        	if(regex == null) return "\\b[0-9.]*[0-9]\\b";
        	return regex;
        }
        
        public BugzillaSession getBugzillaSession() {
        	return bugzillaSession;
        }
        
        /**
         * Checks if the Bugzilla URL is accessible and exists.
         */
        public FormValidation doRegexCheck(@QueryParameter String value) {
            if(Util.fixEmpty(value)==null) {
                return FormValidation.error("No Bug ID regex");
            }
            try {
                Pattern.compile(value);
                return FormValidation.ok();
            } catch (PatternSyntaxException e) {
                return FormValidation.error("Pattern cannot be compiled");
            }
        }

        /**
         * Checks if the Bugzilla URL is accessible and exists.
         */
        public FormValidation doUrlCheck(@QueryParameter final String value)
                throws IOException, ServletException {
            // this can be used to check existence of any file in any URL, so admin only
            if (!Hudson.getInstance().hasPermission(Hudson.ADMINISTER)) return FormValidation.ok();
            return new FormValidation.URLCheck() {
                @Override
                protected FormValidation check() throws IOException, ServletException {
                    String url = Util.fixEmpty(value);
                    if(url==null) {
                        return FormValidation.error("No bugzilla base URL");
                    }
                    try {
                    	new BugzillaSession(url).checkVersion();
                        return FormValidation.ok();
                    } catch (MalformedURLException e) {
            			return FormValidation.error("Not a valid URL");
            		} catch (XmlRpcException e) {
            			return FormValidation.error("Error contacting bugzilla XMLRPC at this URL - tooltips may not work");
            		} 
                }
            }.check();
        }

        /**
         * Checks if the user name and password are valid.
         */
        public FormValidation doLoginCheck(@QueryParameter String url,
                @QueryParameter String user, @QueryParameter String pass) throws IOException {
            if(Util.fixEmpty(url)==null) {// URL not entered yet
                return FormValidation.ok();
            }
            BugzillaSession bsess = null;
            try {
                bsess = new BugzillaSession(url, user, pass);
                bsess.checkVersion();
            } catch (XmlRpcException e) {
                // no error report needed, since it would duplicate the error from checkUrl
                return FormValidation.ok();
            }
            if(bsess.login()) return FormValidation.ok();
            else return FormValidation.error("Invalid username/password");
        }
    }
}
