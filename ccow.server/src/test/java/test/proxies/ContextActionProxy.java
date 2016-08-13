package test.proxies;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import com.google.common.base.Joiner;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

import ccow.cma.IContextAction;

public class ContextActionProxy {

	private final InnerProxy ip;

	public ContextActionProxy(final URL address) throws URISyntaxException {
		ip = new InnerProxy(address);
	}

	class InnerProxy implements IContextAction {
		private final WebResource client;

		public InnerProxy(final URL address) throws URISyntaxException {
			client = new Client().resource(address.toURI()).queryParam("interface", "ContextAction");
		}

		@Override
		public Form Perform(final String cpCallBackURL, final String cpErrorURL, final long participantCoupon,
				final String inputNames, final String inputValues, final String appSignature) {
			final Form response = client.queryParam("interface", "ContextAction").queryParam("method", "Perform")
					.queryParam("cpCallBackURL", cpCallBackURL).queryParam("cpErrorURL", cpErrorURL)
					.queryParam("participantCoupon", participantCoupon + "").queryParam("inputNames", inputNames)
					.queryParam("inputValues", inputValues).queryParam("appSignature", appSignature).get(Form.class);
			return response;
		}
	}

	public Form perform(final URI cpCallBackURL, final URI cpErrorURL, final List<String> itemNames, final List<String> itemValues, final long participantCoupon) {
		return ip.Perform(cpCallBackURL.toString(), cpErrorURL.toString(), participantCoupon,
				Joiner.on("|").join(itemNames), Joiner.on("|").join(itemValues), "");
	}
}
