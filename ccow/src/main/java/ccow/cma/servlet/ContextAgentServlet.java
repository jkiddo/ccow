package ccow.cma.servlet;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.representation.Form;

import ccow.cma.IContextAgent;

@Path("/ContextAgent")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_FORM_URLENCODED)
@Singleton
public class ContextAgentServlet implements IContextAgent{

	@Override
	@GET
	@Path("ContextChangesPending")
	public Form ContextChangesPending(final long agentCoupon, final String contextManager, final String itemNames, final String itemValues,
			final long contextCoupon, final String managerSignature) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@GET
	@Path("Ping")
	public void Ping() {
		// TODO Auto-generated method stub
		
	}

}
