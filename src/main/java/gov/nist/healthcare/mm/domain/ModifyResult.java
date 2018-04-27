package gov.nist.healthcare.mm.domain;

public class ModifyResult {
	private String resultMessage;
	private String modifyErrors;
	private String loadedMessage;
	
	
	public String getResultMessage() {
		return resultMessage;
	}
	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}
	public String getModifyErrors() {
		return modifyErrors;
	}
	public void setModifyErrors(String modifyErrors) {
		this.modifyErrors = modifyErrors;
	}
	public String getLoadedMessage() {
		return loadedMessage;
	}
	public void setLoadedMessage(String loadedMessage) {
		this.loadedMessage = loadedMessage;
	}

	
	

}
