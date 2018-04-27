package gov.nist.healthcare.mm.domain;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("session")
public class ModifyRequest {
	
	private String initialMessage;
	private String script;
	private String postBackURL;


	public String getInitialMessage() {
		return initialMessage;
	}
	public void setInitialMessage(String initialMessage) {
		this.initialMessage = initialMessage;
	}
	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
	}
	public String getPostBackURL() {
		return postBackURL;
	}
	public void setPostBackURL(String postBackURL) {
		this.postBackURL = postBackURL;
	}

}
