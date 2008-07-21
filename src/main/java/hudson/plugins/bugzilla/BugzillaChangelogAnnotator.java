package hudson.plugins.bugzilla;
import hudson.MarkupText;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet.Entry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

public class BugzillaChangelogAnnotator extends ChangeLogAnnotator {
    public static final Pattern PATTERN = Pattern.compile("\\b[0-9.]*[0-9]\\b");

	@Override
	public void annotate(AbstractBuild<?, ?> build, Entry change,
			MarkupText text) {
		HashSet<Integer> bugIds = new HashSet<Integer>();
        for(SubText token : text.findTokens(PATTERN)) {
            String id = token.group(0);
            try {
            	bugIds.add(Integer.valueOf(id));
            } catch (NumberFormatException e) {
            	continue;
            }
        }
        BugzillaSession bugzillaSession = BugzillaProjectProperty.DESCRIPTOR.getBugzillaSession();
        HashMap<Integer, String> summaryMap =
        	bugzillaSession == null ? null : bugzillaSession.getBugSummaryMap(bugIds);
        
        for(SubText token : text.findTokens(PATTERN)) {
            String id = token.group(0);
            
            Integer key = null;
            try {
            	key = Integer.valueOf(id);
            } catch (NumberFormatException e) {
            	continue;
            }
            String baseUrl = BugzillaProjectProperty.DESCRIPTOR.getBaseUrl();
            if(summaryMap == null) {
            	 token.surroundWith(String.format("<a href='%s/show_bug.cgi?id=%d'>", baseUrl, key), "</a>");
            } else if(summaryMap.containsKey(key)) {
                String summary = summaryMap.get(key);
                token.surroundWith(
                		String.format("<a href='%s/show_bug.cgi?id=%d' tooltip='%s'>", baseUrl, key, summary),
                		"</a>"
                );            	
            }
        }
	}
}
