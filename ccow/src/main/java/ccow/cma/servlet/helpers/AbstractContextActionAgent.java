package ccow.cma.servlet.helpers;

import com.sun.jersey.api.representation.Form;

import ccow.cma.IContextAgent;
import ccow.cma.IImplementationInformation;

public abstract class AbstractContextActionAgent implements IContextAgent, IImplementationInformation {

	@Override
	public Form ContextChangesPending(final long agentCoupon, final String contextManager, final String itemNames,
			final String itemValues, final long contextCoupon, final String managerSignature) {

		final String[] names = itemNames.split("\\|");
		final String[] values = itemValues.split("\\|");
		
		systemCall(names, values);
		
		final Form form = new Form();
		// 17.3.3.2.2 Outputs
		// final String[]itemNames
		// final String[] itemValues
		form.add("itemNames", "");
		form.add("itemValues", "");

		form.add("agentCoupon", agentCoupon);
		form.add("contextCoupon", contextCoupon);
		form.add("agentSignature", "");

		// The interface MappingAgent, deprecated in CMA 1.4, has been removed
		// altogether.
		// 17.3.3.2.3 Outputs for Mapping Agents
		form.add("decision", "");
		form.add("reason", "");

		return form;
	}

	protected abstract void systemCall(final String[] itemNames, final String [] itemValues);

	@Override
	public void Ping() {

	}
}
