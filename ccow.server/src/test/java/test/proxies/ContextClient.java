package test.proxies;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.sun.jersey.api.representation.Form;

import ccow.cma.servlet.helpers.ContextParticipantProxy;

public class ContextClient extends ContextParticipantProxy{

	private final ContextManagerProxy contextManager;
	private long participantCoupon;
	private long contextCoupon;
	private final ContextDataProxy contextDataProxy;
	private final ContextActionProxy contextActionProxy;

	public ContextClient(final String applicationName, final URI contextParticipant, final ContextManagerProxy contextManager, final ContextDataProxy contextDataProxy, final ContextActionProxy contextActionProxy) {
		super(applicationName, contextParticipant);
		this.contextManager = contextManager;
		this.contextDataProxy = contextDataProxy;
		this.contextActionProxy = contextActionProxy;
	}

	public ContextClient(final String applicationName, final ContextManagerProxy contextManager, final ContextDataProxy contextDataProxy, final ContextActionProxy contextActionProxy) throws MalformedURLException, Exception {
		super(applicationName);
		this.contextManager = contextManager;
		this.contextDataProxy = contextDataProxy;
		this.contextActionProxy = contextActionProxy;
	}
	
	public long joinCommonContext(final boolean survey, final boolean wait) throws NumberFormatException, URISyntaxException {
		participantCoupon = contextManager.JoinCommonContext(this, survey, wait);
		return participantCoupon;
	}

	public long startContextChanges() {
		contextCoupon = contextManager.StartContextChanges(participantCoupon);
		return contextCoupon;
	}

	public void setItemValues(final List<String> itemNames, final List<String> itemValues) {
		contextDataProxy.SetItemValues(participantCoupon, itemNames, itemValues, contextCoupon);
	}

	public List<String> getItemValues(final List<String> itemNames, final boolean onlyChanges) {
		return contextDataProxy.GetItemValues(itemNames, onlyChanges, contextCoupon);
	}

	public Form endContextChanges() {
		return contextManager.EndContextChanges(contextCoupon);
	}

	public List<String> publishChangesDecision(final String decision) {
		return contextManager.PublishChangesDecision(contextCoupon, decision);
	}

	public void perform(final List<String> itemNames, final List<String> itemValues) {
		contextActionProxy.perform(this.getURI(), this.getURI(), itemNames, itemValues, participantCoupon);		
	}
}
