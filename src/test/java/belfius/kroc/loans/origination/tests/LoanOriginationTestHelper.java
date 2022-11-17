package belfius.kroc.loans.origination.tests;

import belfius.bpms.processes.test.model.ProcessNode;
import belfius.bpms.processes.test.model.ProcessNode.NodeType;
import belfius.gejb.cloud.processorchestrator.openapi.model.CodeValue;
import belfius.gejb.cloud.processorchestrator.openapi.model.DecisionElement;
import belfius.gejb.cloud.processorchestrator.openapi.model.DecisionResource;
import belfius.gejb.cloud.processorchestrator.openapi.model.LoanGrantingBusinessDecision;

public class LoanOriginationTestHelper {

	//////////////////////////////////////////////
	// Origination Process Steps
	//////////////////////////////////////////////
		
	/**
	 * Enumeration with all the process nodes (Start Events, intermediate events, end events, service tasks, user tasks).
	 * Sequence flows and gateways are excluded since they aren't required to be tested
	 */
	public enum OriginationProcessSteps {
		// Start events
		STARTEVENT_REQUEST_STARTED(new ProcessNode("Request started", null, NodeType.START_EVENT)),
		
		// Service Tasks
		SERVICETASK_SEND_INFORMATION_LETTER(new ProcessNode("Send Information Letter", "manageDocument", NodeType.SERVICE_TASK)),
		SERVICETASK_CALCULATE_REQUEST_ADMISSIBILITY_DECISION(new ProcessNode("Calculate Admissibility Request Decision", "calculateRequestAdmissibilityDecision", NodeType.SERVICE_TASK)),
		SERVICETASK_SEND_REPRESENTATION_CLASS_NOTIFICATION(new ProcessNode("Send representation class notification", "manageDocument", NodeType.SERVICE_TASK)),
		
		SERVICETASK_INVALIDATE_EVERYTHING(new ProcessNode("Invalidate Everything", "compensateTask", NodeType.SERVICE_TASK)),
		SERVICETASK_PROVIDE_REFUSAL_LETTER(new ProcessNode("Provide Refusal Letter", "manageDocument", NodeType.SERVICE_TASK)),		
		SERVICETASK_SET_REQUEST_STATE_TO_CANCEL(new ProcessNode("Set Requests State to Cancel", "updateActorRequestState", NodeType.SERVICE_TASK)),
		
		// User Tasks
		USERTASK_TREAT_ATTENTION_POINTS(new ProcessNode("Treat Attention Points", null, NodeType.USER_TASK)),
		
		// Intermediate events
		EVENT_REVERT(new ProcessNode("revert", null, NodeType.INTERMEDIATE_CATCHING_EVENT)),
		
		
		// End events
		ENDEVENT_REQUEST_APPROVED(new ProcessNode("Request approved", null, NodeType.END_EVENT)),
		ENDEVENT_REQUEST_REJECTED(new ProcessNode("Request rejected", null, NodeType.END_EVENT)),
		ENDEVENT_REQUEST_EXPIRED(new ProcessNode("Request expired", null, NodeType.END_EVENT)),
		ENDEVENT_REQUEST_MANUALLY_CANCELLED(new ProcessNode("Request rejected", null, NodeType.END_EVENT));

		private ProcessNode processNode;
		
		private OriginationProcessSteps(ProcessNode processNode) {
			this.processNode = processNode;
		}

		public ProcessNode getProcessNode() {
			return processNode;
		}
	}
	
	
	///////////////////////////////////
	// Business Data Enumerations
	///////////////////////////////////
	
	/**
	 * Enumeration for Admissibility Decision Result
	 */
	public static enum CalculateAdmissibilityDecisionResultCodes {
		NON_ADMISSIBLE("NON_ADMISSIBLE"),
		ATTENTION_POINTS("ATTENTION_POINTS"),
		OK("OK");
		
		private String decisionCode;

		private CalculateAdmissibilityDecisionResultCodes(String decisionCode) {
			this.setDecisionCode(decisionCode);
		}

		public String getDecisionCode() {
			return decisionCode;
		}

		public void setDecisionCode(String decisionCode) {
			this.decisionCode = decisionCode;
		}
	}
	
	/**
	 * Enumeration for Business Decision Result
	 */
	public static enum CalculateBusinessDecisionResultCodes {
		NO_AUTOMATIC("NO_AUTOMATICDECISION"),
		AUTOMATIC_APPROVAL("AUTOMATIC_APPROVAL"),
		AUTOMATIC_REFUSAL("AUTOMATIC_REFUSAL"),
		EXPERT_APPROVAL("EXPERT_APPROVAL"),
		EXPERT_REFUSAL("EXPERT_REFUSAL"),
		MISSING_DATA("MISSING_DATA");

		private String decisionCode;

		private CalculateBusinessDecisionResultCodes(String decisionCode) {
			this.setDecisionCode(decisionCode);
		}

		public String getDecisionCode() {
			return decisionCode;
		}

		public void setDecisionCode(String decisionCode) {
			this.decisionCode = decisionCode;
		}
	}
	
	/**
	 * State Codes for the origination bundle.
	 */
	public enum BundleStateCodes {
		DIGITAL_SIGNING("03"),
		MANUAL_SIGNING("10");
		
		private String codeId;
		
		private BundleStateCodes(String codeId) {
			this.setCodeId(codeId);
		}

		public String getCodeId() {
			return codeId;
		}

		public void setCodeId(String codeId) {
			this.codeId = codeId;
		}
	}
	
	/**
	 * Back State Codes for revert signal.
	 */
	public enum BackStateCode {
		SOLUTIONS_PROPOSED,
		SOLUTION_SELECTED,
		NEEDS_TO_COMPLETE,
		CONTRACT_DATA_NEEDED
	}
	
	/**
	 * Document Types.
	 */
	public enum DocumentTypeCode {
		REFUSAL_LETTER("COFI03");
		
		private String codeId;
		
		private DocumentTypeCode(String codeId) {
			this.setCodeId(codeId);
		}

		public String getCodeId() {
			return codeId;
		}

		public void setCodeId(String codeId) {
			this.codeId = codeId;
		}
	}
	
	
	///////////////////////////////////
	// Business Data generators
	///////////////////////////////////

	/**
	 * Creates an output for Offer Signed signal event.
	 * @param codeId Allowed values are: NO_AUTOMATICDECISION, AUTOMATIC_APPROVAL, AUTOMATIC_REFUSAL
	 * @return
	 */
	protected static CodeValue createBundleState(BundleStateCodes bundleStateCode) {
		CodeValue bundleState = new CodeValue();
		bundleState.setCodeId(bundleStateCode.getCodeId());
		
		return bundleState;
	}
	
	/**
	 * Creates an input for Generate Refusal Letter Service Task.
	 * @param codeId Allowed values are: REFUSAL_LETTER
	 * @return
	 */
	protected static CodeValue createDocumentType(DocumentTypeCode documentTypeCode) {
		CodeValue documentType = new CodeValue();
		documentType.setCodeId(documentTypeCode.getCodeId());
		
		return documentType;
	}
	
	/**
	 * Creates an output for Calculate Business Decision Service Task
	 * @param codeId Allowed values are: NO_AUTOMATICDECISION, AUTOMATIC_APPROVAL, AUTOMATIC_REFUSAL
	 * @return
	 */
	protected static DecisionResource createCalculateBusinessDecisionOutput(CalculateBusinessDecisionResultCodes codeId) {

		CodeValue codeValue = new CodeValue();
		codeValue.setCodeId(codeId.getDecisionCode());

		LoanGrantingBusinessDecision loanGrantingBusinessDecision = new LoanGrantingBusinessDecision();
		loanGrantingBusinessDecision.setResult(codeValue);

		DecisionElement decisionElement = new DecisionElement();
		decisionElement.setLoanGrantingBusinessDecision(loanGrantingBusinessDecision);

		DecisionResource decisionResource = new DecisionResource();
		decisionResource.setDecision(decisionElement);

		return decisionResource;
	}
}
