package gov.nist.healthcare.mm.controller;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.nist.healthcare.hl7.mm.v2.generated.ParseException;
import gov.nist.healthcare.hl7.mm.v2.generated.TokenMgrError;
import gov.nist.healthcare.hl7.mm.v2.message.util.SyntaxChecker;
import gov.nist.healthcare.hl7.mm.v2.nathancode.Issue;
import gov.nist.healthcare.hl7.mm.v2.script.execution.MessageModifierService;
import gov.nist.healthcare.hl7.mm.v2.script.execution.ModificationResult;
import gov.nist.healthcare.mm.domain.ModifyRequest;
import gov.nist.healthcare.mm.domain.ModifyResult;


//NK1|1|Wilson^Beckham^Marion^^^^L|MTH^Mother^HL70063|
// NK1-3.1="BobMarleyBaby";


//@CrossOrigin(origins = "http://localhost:4200")


@RestController
@Scope("session")

public class MMController {
 
	@Autowired
	private MessageModifierService mmService;
	@Autowired
	private ObjectMapper mapper;
//	 @Autowired
//	 private ModifyRequest ModifyReauestNathan;
	

	
	 /**
	  * This function redirects the user to the web interface. This is the function that needs to be called
	  * within the integration of this project. The url that needs to be called is /WorkBenchUI. This method
	  * expects to receive a script, an initial message and a postBackuRL to which the result will be posted to.
	 * @param ModifyReauestNathan, is the expected object to be received by the function containing a script, initial message and a postBackUrl
	 * @param response
	 * @param request
	 * @return redirection to the user interface of the MM WorkBench
	 * @throws IOException
	 * @throws ServletException
	 */
	@RequestMapping(value="/WorkBenchUI", method=RequestMethod.POST, consumes=MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	    public RedirectView redirectToUI(ModifyRequest ModifyReauestNathan, HttpServletResponse response, HttpServletRequest request) throws IOException, ServletException {
	      HttpSession session = request.getSession(true);
//	      ModifyRequest ModifyReauestNathan = null;
//	      for(Part p : request.getParts()) {
//	    	  	if(p.getName().equals("data")) {
//	    	  		String str_req = IOUtils.toString(p.getInputStream(), Charset.forName("UTF-8"));
//	    	  		ModifyReauestNathan = mapper.readValue(str_req, ModifyRequest.class);
//	    	  	}
//	      }
	      if(ModifyReauestNathan != null) {
	    	  session.setAttribute("initialMessage", ModifyReauestNathan.getInitialMessage());
		      session.setAttribute("script", ModifyReauestNathan.getScript());
		      session.setAttribute("postBackURL", ModifyReauestNathan.getPostBackURL());
	      }
	      
	      
			return new RedirectView("index.html");

	    }
	 
	

	 /**
	  * This function fetchs in the opened session to get Data to send to the front end (Initial message, script and posBackUrl)
	 * @param response
	 * @param request
	 * @return ModifyRequest, is the object sent to the front end
	 * @throws IOException
	 */
	@RequestMapping(value="/getSessionData", method=RequestMethod.GET)
	    @ResponseBody
	    public ModifyRequest getSessionData(HttpServletResponse response, HttpServletRequest request) throws IOException {
		 ModifyRequest modifyRequest = new ModifyRequest();
	      HttpSession session = request.getSession(true);
	      
	      if(session.getAttribute("initialMessage") != null) {
	 		 modifyRequest.setInitialMessage((String) session.getAttribute("initialMessage"));
	      } else {
		 		 modifyRequest.setInitialMessage(null);
	      }
	      
	      if(session.getAttribute("script") != null) {
		 		 modifyRequest.setScript((String) session.getAttribute("script"));
		      } else {
			 		 modifyRequest.setScript(null);
		      }
	      
	      if(session.getAttribute("postBackURL") != null) {
		 		 modifyRequest.setPostBackURL((String) session.getAttribute("postBackURL"));
		      } else {
			 		 modifyRequest.setPostBackURL(null);
		      }
	      


	      return modifyRequest;
	 }	 
	
    /**
     * This function is a HTTP POST request that handles the modification of the message received and send back results to the front end
     * 
	 * This functions takes as @param an initial message and a
	 * script and performs a modification to return as a result a modificationResult object
	 * containing a final message which is the initial message after transformation and errors 
	 * concerning script parsing. Calling this method will result in parsing the script given as @param, 
	 * then storing the script's commands and executing then one by one. If any error is found, the method
	 *  will return errors, else the initial message will be modified and stored in the final message.
	 *  
     * @param modifyReauest, is the expected object to be received containing the initial message to modify and the script to use
     * @return modifyResult, is almost the same object but contains a final message that is the result of the modification and errors if any were found
     */
    @RequestMapping(value="/modify", method=RequestMethod.POST)
    @ResponseBody
    public ModifyResult modify(@RequestBody ModifyRequest modifyReauest) {

		
    		ModificationResult modificationResult = new ModificationResult();
    		ModifyResult modifyResult = new ModifyResult();
    		modificationResult = mmService.modify(modifyReauest.getInitialMessage(), modifyReauest.getScript());
    		modifyResult.setResultMessage(modificationResult.getResultMessage());
    		String modifyErrors = "";
    		for(Issue i:modificationResult.getIssues()) {
    			modifyErrors+=i.toString() + "\n";
    		}
    		ParseException e;
    	
    		modifyResult.setModifyErrors(modificationResult.getModificationDetailsList(false));
    		
System.out.println(modifyErrors);



    		return modifyResult;
    }
    
	
	/**
	 * This function is used to checkSyntax. Takes a script as param and returns errors.
	 * The checkSyntax function will use the parser line by line to parse the script given as @param. 
	 * Once the script parsed, it will return errors that were found in the script. The errors will be
	 * displayed showing which column and line each error was found, what error was found and what token was expected
	 * @param script
	 * @return String, errors found in the script
	 * @throws ParseException
	 * @throws TokenMgrError
	 */
    @RequestMapping(value="/checkSyntax", method=RequestMethod.POST)
    @ResponseBody
    public ModifyResult checkSyntax(@RequestBody String script) {
    	SyntaxChecker sc = new SyntaxChecker();
		System.out.println(script);
    		ModificationResult modificationResult = new ModificationResult();
    		ModifyResult modifyResult = new ModifyResult();
    		try {
				modifyResult.setModifyErrors(sc.checkSyntax(script.replace("\r","").replace("\n","").replace("\\", "")));
				
				System.out.println(modifyResult.getModifyErrors());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TokenMgrError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		modifyResult.setResultMessage("");
    		return modifyResult;
    }
    
    /** This functions sends back a pre loaded intial message to the user interface
     * @return modifyResult, containing a loaded message to display on the user interface
     */
    @RequestMapping(value="/loadMessage", method=RequestMethod.GET)
    @ResponseBody
    public ModifyResult loadExample() {
		ModifyResult modifyResult = new ModifyResult();

    	String loadedMessage = "MSH|^~\\&|Test EHR Application|X68||NIST Test Iz Reg|20120701082240-0500||VXU^V04^VXU_V04|NIST-IZ-001.00|P|2.5.1|||ER|AL|||||Z22^CDCPHINVS\n" + 
    			"PID|1||D26376273^^^NIST MPI^MR||Snow^Madelynn^Ainsley^^^^L|Lam^Morgan^^^^^M|20070706|F||2076-8^Native Hawaiian or Other Pacific Islander^CDCREC|32 Prescott Street Ave^^Warwick^MA^02452^USA^L||^PRN^PH^^^657^5558563|||||||||2186-5^non Hispanic or Latino^CDCREC\n" + 
    			"PD1|||||||||||02^Reminder/Recall - any method^HL70215|||||A|20120701|20120701\n" + 
    			"NK1|1|Lam^Morgan^^^^^L|MTH^Mother^HL70063|32 Prescott Street Ave^^Warwick^MA^02452^USA^L|^PRN^PH^^^657^5558563\n" + 
    			"ORC|RE||IZ-783274^NDA|||||||I-23432^Burden^Donna^A^^^^^NIST-AA-1^^^^PRN||57422^RADON^NICHOLAS^^^^^^NIST-AA-1^L^^^MD\n" + 
    			"RXA|0|1|20120814||33332-0010-01^Influenza, seasonal, injectable, preservative free^NDC|0.5|mL^MilliLiter [SI Volume Units]^UCUM||00^New immunization record^NIP001|7832-1^Lemon^Mike^A^^^^^NIST-AA-1^^^^PRN|^^^X68||||Z0860BB|20121104|CSL^CSL Behring^MVX|||CP|A\n" + 
    			"RXR|C28161^Intramuscular^NCIT|LD^Left Arm^HL70163\n" + 
    			"OBX|1|CE|64994-7^Vaccine funding program eligibility category^LN|1|V05^VFC eligible - Federally Qualified Health Center Patient (under-insured)^HL70064||||||F|||20120701|||VXC40^Eligibility captured at the immunization level^CDCPHINVS\n" + 
    			"OBX|2|CE|30956-7^vaccine type^LN|2|88^Influenza, unspecified formulation^CVX||||||F\n" + 
    			"OBX|3|TS|29768-9^Date vaccine information statement published^LN|2|20120702||||||F\n" + 
    			"OBX|4|TS|29769-7^Date vaccine information statement presented^LN|2|20120814||||||F";
    	
    	modifyResult.setLoadedMessage(loadedMessage);

    	return modifyResult;
    }
    
    
    
//	@RequestMapping("/api/hi")
//	public String hi() {
//		return "Hellddo World from Restful API";
//	}
// 
}