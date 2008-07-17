package hudson.plugins.bugzilla;
import java.util.regex.Pattern;

import hudson.MarkupText;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet.Entry;

public class BugzillaChangelogAnnotator extends ChangeLogAnnotator {
    public static final Pattern PATTERN = Pattern.compile("\\b[1-9][0-9]*\\b");

	@Override
	public void annotate(AbstractBuild<?, ?> build, Entry change,
			MarkupText text) {
        for(SubText token : text.findTokens(PATTERN)) {
            String id = token.group(0);
            String baseUrl = BugzillaProjectProperty.DESCRIPTOR.getBaseUrl();
            token.surroundWith("<a href='"+baseUrl+"/show_bug.cgi?id="+id+"'>","</a>");
        }
	}

}
