package belfius.kroc.loans.origination.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import belfius.bpms.processes.test.BelfiusBusinessProcessBaseTestCase;
import belfius.bpms.processes.test.model.ProcessNode;
import belfius.bpms.processes.test.model.ProcessNode.NodeType;
import belfius.gejb.cloud.processorchestrator.openapi.model.CodeValue;
import belfius.kroc.loans.origination.tests.LoanOriginationTestHelper.CalculateAdmissibilityDecisionResultCodes;
import belfius.kroc.loans.origination.tests.LoanOriginationTestHelper.OriginationProcessSteps;

public class LoanOriginationTest extends BelfiusBusinessProcessBaseTestCase {

	private static final Logger logger = LoggerFactory.getLogger(LoanOriginationTest.class);

	public LoanOriginationTest () {
		super(true, true, "./belfius/kroc/process/LoanOrigination.bpmn");
	}

	@Override
	public void buildRuntimeEnvironment() {
		super.buildRuntimeEnvironment();
		this.registerWorkItemHandlers();
		System.setProperty("jboss.server.config.dir", "src/test/resources");
	}

	/**
	 * Request rejected - Admissibility Decision: not admissible
	 */
	@Test
	public void testRequestRejected_AdmissibilityDecision_NotAdmissible() {
		String correlationId = "1234";

		Long processInstanceId = startProcess(correlationId);
		/*
		 * Send Information Letter Service Task
		 */
		sendInformationLetterServiceTask(processInstanceId, correlationId);

		/*
		 * Calculate Admissibility Request Decision Service Task
		 */
		Map<String, Object> calculateAdmissibilityRequestDecisionHandlerInputs = new HashMap<String, Object>();
		calculateAdmissibilityRequestDecisionHandlerInputs.put("correlationId", correlationId);
		
		Map<String, Object> calculateAdmissibilityRequestDecisionHandlerOutputs = new HashMap<String, Object>();
		CodeValue admissibilityDecisionResult = new CodeValue();
		admissibilityDecisionResult.setCodeId(CalculateAdmissibilityDecisionResultCodes.NON_ADMISSIBLE.getDecisionCode());
		calculateAdmissibilityRequestDecisionHandlerOutputs.put("result", admissibilityDecisionResult);

		assertServiceTaskCompleted(
				processInstanceId,
				OriginationProcessSteps.SERVICETASK_CALCULATE_REQUEST_ADMISSIBILITY_DECISION.getProcessNode(),
				calculateAdmissibilityRequestDecisionHandlerInputs,
				calculateAdmissibilityRequestDecisionHandlerOutputs);
		
		/*
		 * Cancel Request Event Subprocess
		 */
		assertRequestCancellationEventSubprocess(processInstanceId);
		

		/*
		 * Process instance should be in aborted state.
		 */
		assertProcessInstanceAborted(processInstanceId);
	}
	
	/**
	 * Request rejected - Attention Points: stop request
	 */
	@Test
	public void testRequestRejected_AdmissibilityDecision_TreatAttentionPoints_StopRequest() {
		String correlationId = "1234";
		String bankerUserId = "jbpmAdmin";

		Long processInstanceId = startProcess(correlationId);
		/*
		 * Send Information Letter Service Task
		 */
		sendInformationLetterServiceTask(processInstanceId, correlationId);

		/*
		 * Calculate Admissibility Request Decision Service Task
		 */
		Map<String, Object> calculateAdmissibilityRequestDecisionHandlerInputs = new HashMap<String, Object>();
		calculateAdmissibilityRequestDecisionHandlerInputs.put("correlationId", correlationId);
		
		Map<String, Object> calculateAdmissibilityRequestDecisionHandlerOutputs = new HashMap<String, Object>();
		CodeValue admissibilityDecisionResult = new CodeValue();
		admissibilityDecisionResult.setCodeId(CalculateAdmissibilityDecisionResultCodes.ATTENTION_POINTS.getDecisionCode());
		calculateAdmissibilityRequestDecisionHandlerOutputs.put("result", admissibilityDecisionResult);

		assertServiceTaskCompleted(
				processInstanceId,
				OriginationProcessSteps.SERVICETASK_CALCULATE_REQUEST_ADMISSIBILITY_DECISION.getProcessNode(),
				calculateAdmissibilityRequestDecisionHandlerInputs,
				calculateAdmissibilityRequestDecisionHandlerOutputs);

		/*
		 * Treat Attention Points User Task
		 */
		Map<String, Object> treatAttentionPointsUserTaskOutputs = new HashMap<String, Object>();
		treatAttentionPointsUserTaskOutputs.put("bankerChoice", false);
		
		assertUserTaskCompleted(processInstanceId,OriginationProcessSteps.USERTASK_TREAT_ATTENTION_POINTS.getProcessNode().getNodeName(), bankerUserId, null, treatAttentionPointsUserTaskOutputs);
		
		/*
		 * Cancel Request Event Subprocess
		 */
		assertRequestCancellationEventSubprocess(processInstanceId);

		/*
		 * Process instance should be in aborted state.
		 */
		assertProcessInstanceAborted(processInstanceId);
	}

	/**
	 * Request accepted - Admissibility Decision: admissible
	 */
	@Test
	public void testRequestAccepted_AdmissibilityDecision_Admissible() {
		String correlationId = "1234";

		Long processInstanceId = startProcess(correlationId);
		/*
		 * Send Information Letter Service Task
		 */
		sendInformationLetterServiceTask(processInstanceId, correlationId);

		/*
		 * Calculate Admissibility Request Decision Service Task
		 */
		Map<String, Object> calculateAdmissibilityRequestDecisionHandlerInputs = new HashMap<String, Object>();
		calculateAdmissibilityRequestDecisionHandlerInputs.put("correlationId", correlationId);

		Map<String, Object> calculateAdmissibilityRequestDecisionHandlerOutputs = new HashMap<String, Object>();
		CodeValue admissibilityDecisionResult = new CodeValue();
		admissibilityDecisionResult.setCodeId(CalculateAdmissibilityDecisionResultCodes.ADMISSIBLE.getDecisionCode());
		calculateAdmissibilityRequestDecisionHandlerOutputs.put("result", admissibilityDecisionResult);

		assertServiceTaskCompleted(
				processInstanceId,
				OriginationProcessSteps.SERVICETASK_CALCULATE_REQUEST_ADMISSIBILITY_DECISION.getProcessNode(),
				calculateAdmissibilityRequestDecisionHandlerInputs,
				calculateAdmissibilityRequestDecisionHandlerOutputs);

		/*
		 * Send representation class notification Service Task
		 */
		Map<String, Object> sendRepresentationClassNotificationHandlerInputs = new HashMap<String, Object>();
		sendRepresentationClassNotificationHandlerInputs.put("correlationId", correlationId);
		Boolean errorResponse = true;
		sendRepresentationClassNotificationHandlerInputs.put("gejbHandleErrorResponse", errorResponse);

		assertServiceTaskCompleted(
				processInstanceId,
				OriginationProcessSteps.SERVICETASK_SEND_REPRESENTATION_CLASS_NOTIFICATION.getProcessNode(),
				null,
				null);

		/*
		 * Update Actor Request Status Service Task
		 */
		Map<String, Object> updateActorRequestStatusHandlerInputs = new HashMap<String, Object>();
		updateActorRequestStatusHandlerInputs.put("actor-request-number", 1234);
		CodeValue state = new CodeValue();
		state.setCodeId(LoanOriginationTestHelper.ActorRequestStatusCodes.NEEDS_TO_COMPLETE.getCodeId());
		updateActorRequestStatusHandlerInputs.put("state", state);

		assertServiceTaskCompleted(
				processInstanceId,
				OriginationProcessSteps.SERVICETASK_SET_REQUEST_STATE_TO_NEEDS_TO_COMPLETE.getProcessNode(),
				null,
				null);

		assertNodeTriggered(processInstanceId, "Borrower Correctly Identified");

		/*
		 * Process instance should be in aborted state.
		 */
		assertProcessInstanceActive(processInstanceId);
	}

	private Long startProcess(String correlationId) {
		System.setProperty("belfius.kroc.origination.process.expiration", "PT60S");

		Map<String, Object> processInputs = new HashMap<String, Object>();
		processInputs.put("mainProcessId", correlationId);
		processInputs.put("correlationId", correlationId);

		return startProcessInstance("loanOrigination.LoanOrigination", processInputs);
	}

	private void sendInformationLetterServiceTask(Long processInstanceId, String mainProcessId) {
		Map<String, Object> sendInformationLetterHandlerInputs = new HashMap<String, Object>();
		sendInformationLetterHandlerInputs.put("correlationId", mainProcessId);

		assertServiceTaskCompleted(
				processInstanceId,
				OriginationProcessSteps.SERVICETASK_SEND_INFORMATION_LETTER.getProcessNode(),
				sendInformationLetterHandlerInputs,
				null);

	}

	private void borrowersIdentificationHappyPath() {

	}
	///////////////////////////
	// Helper methods
	///////////////////////////
	
	private void assertRequestCancellationEventSubprocess(Long processInstanceId) {
		/*
		 * Invalidate Everything Service Task
		 */
		assertServiceTaskCompleted(
				processInstanceId,
				OriginationProcessSteps.SERVICETASK_INVALIDATE_EVERYTHING.getProcessNode(),
				null,
				null);
		
		/*
		 * Provide Refusal Letter Service Task
		 */
		assertServiceTaskCompleted(
				processInstanceId,
				OriginationProcessSteps.SERVICETASK_PROVIDE_REFUSAL_LETTER.getProcessNode(),
				null,
				null);
		
		/*
		 * Invalidate Everything Service Task
		 */
		assertServiceTaskCompleted(
				processInstanceId,
				OriginationProcessSteps.SERVICETASK_SET_REQUEST_STATE_TO_CANCEL.getProcessNode(),
				null,
				null);
	}

	private void registerWorkItemHandlers () {
		List<ProcessNode> originationServiceTasksList = new ArrayList<ProcessNode>();


		for(OriginationProcessSteps originationProcessStep : OriginationProcessSteps.values()) {
			ProcessNode originationProcessNode = originationProcessStep.getProcessNode();

			if(originationProcessNode.getNodeType().equals(NodeType.SERVICE_TASK))
				originationServiceTasksList.add(originationProcessNode);
		}

		ProcessNode[] originationServiceTasksArray = new ProcessNode[originationServiceTasksList.size()];

		registerWorkItemHandlers(originationServiceTasksList.toArray(originationServiceTasksArray));
	}

}
