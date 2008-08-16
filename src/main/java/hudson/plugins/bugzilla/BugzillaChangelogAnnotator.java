package hudson.plugins.bugzilla;
import hudson.MarkupText;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet.Entry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class BugzillaChangelogAnnotator extends ChangeLogAnnotator {
	private static int getId(SubText token) {
		String id = null;
		for(int i = 0;;i++) {
			id = token.group(i);

			try {
				return Integer.valueOf(id);
			} catch (NumberFormatException e) {
				LOGGER.log(Level.FINE, "{0} is not a number in group {1}, trying next group", new Object[]{id, i});
				continue;
			}
		}
	}
	@Override
	public void annotate(AbstractBuild<?, ?> build, Entry change,
			MarkupText text) {
		Pattern pattern = null;
		String regex = BugzillaProjectProperty.DESCRIPTOR.getRegex();
		try {
			pattern = Pattern.compile(regex);
		} catch (PatternSyntaxException e) {
			LOGGER.log(Level.WARNING, "Cannot compile pattern: {0}", regex);
			return;
		}
		HashSet<Integer> bugIds = new HashSet<Integer>();
		if(BugzillaProjectProperty.DESCRIPTOR.getUseToolTips()) {
			for(SubText token : text.findTokens(pattern)) {
				try {
					bugIds.add(getId(token));
				} catch (NumberFormatException e) {
					continue;
				}
			}
		}
		BugzillaSession bugzillaSession = BugzillaProjectProperty.DESCRIPTOR.getBugzillaSession();
		HashMap<Integer, String> summaryMap = null;
		if( (!bugIds.isEmpty()) && bugzillaSession != null) {
			summaryMap = bugzillaSession.getBugSummaryMap(bugIds);
		}

		for(SubText token : text.findTokens(pattern)) {
			Integer key = null;
			try {
				key = getId(token);
			} catch (Exception e) {
				// this means we've exhausted all groups, and didn't find an integer
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

	private static final Logger LOGGER = Logger.getLogger(BugzillaChangelogAnnotator.class.getName());

}
