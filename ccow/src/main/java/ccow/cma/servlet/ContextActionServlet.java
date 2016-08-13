package ccow.cma.servlet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.api.representation.Form;

import ccow.cma.IContextAction;
import ccow.cma.servlet.helpers.ContextState;
import ccow.cma.servlet.helpers.IContextAgentRepository;
import ccow.cma.servlet.helpers.IQualifiedContextAgent;

@Path("/ContextAction")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_FORM_URLENCODED)
@Singleton
public class ContextActionServlet implements IContextAction {

	private final IContextAgentRepository repository;
	private final ContextState contextState;

	@Inject
	public ContextActionServlet(final IContextAgentRepository repository, final ContextState contextState) {
		this.repository = repository;
		this.contextState = contextState;
	}

	@Context
	UriInfo uriInfo;

	@GET
	@Path("Perform")
	@Override
	public Form Perform(@QueryParam("cpCallBackURL") final String cpCallBackURL,
			@QueryParam("cpErrorURL") final String cpErrorURL,
			@QueryParam("participantCoupon") final long participantCoupon,
			@QueryParam("inputNames") final String inputNames, @QueryParam("inputValues") final String inputValues,
			@QueryParam("appSignature") final String appSignature) {

		final String[] names = inputNames.split("\\|");
		final String[] values = inputValues.split("\\|");
		
		final IQualifiedContextAgent assignedContextAgent = repository.findContextAgent(names, values);

		// 1.4.5 Context Actions
		final long contextCoupon = contextState.startSessionChanges(assignedContextAgent.getAgentCoupon());
		final Form caForm = assignedContextAgent.ContextChangesPending(assignedContextAgent.getAgentCoupon(), uriInfo.getBaseUri().toString() + "/ContextManager", inputNames, inputValues, contextCoupon, "");
		contextState.endSession();

		final Form f = new Form();
		f.add("actionCoupon", caForm.getFirst("actionCoupon"));
		f.add("outputNames", caForm.getFirst("itemNames"));
		f.add("outputValues", caForm.getFirst("itemValues"));
		
		// Secure not supported
		f.add("managerSignature", "");
		return f;
	}

}
