package test;

import static org.junit.Assert.assertEquals;

import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.servlet.AppGuiceFilter;
import com.sun.jersey.api.representation.Form;

import ccow.cma.jersey.CCOWContextListener;
import ccow.cma.jersey.InlinedContextAgentRepositoryModule;
import ccow.cma.servlet.helpers.ContextState;
import test.proxies.ContextActionProxy;
import test.proxies.ContextClient;
import test.proxies.ContextDataProxy;
import test.proxies.ContextManagerProxy;
import test.proxies.ContextManagerRegistryProxy;
import test.proxies.util.ParticipantHoster;
import test.proxies.ws.WSContextParticipantImpl;

public class WebSocketsTestSuite {

	private static Server ccowServer;
	private static int serverPort = 2116;
	private static String host = "localhost";
	private static ContextState commonContext = new ContextState();
	private final URI serverAddress = URI.create("http://" + host + ":" + serverPort );
	private List<String> itemNames;
	private List<String> itemValues;
	private ParticipantHoster participantServer;

	private URL cmr;
	private ContextManagerProxy cm;
	private ContextDataProxy cdp;
	private ContextActionProxy cx;
	private ContextClient system1;
	private ContextClient system2;
	private ContextClient system3;
	private long participantCouponSystem1;
	private long participantCouponSystem2;
	private long participantCouponSystem3;

	@BeforeClass
	public static void beforeClass() throws Exception {
		ccowServer = new Server(serverPort);

		final CCOWContextListener c = new CCOWContextListener(commonContext, new InlinedContextAgentRepositoryModule());

		final MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
		ccowServer.addBean(mbContainer);

		final ServletContextHandler webSocketServletContextHandler = new ServletContextHandler(ccowServer, "/ws",
				ServletContextHandler.SESSIONS);
		webSocketServletContextHandler.addEventListener(c);
		WebSocketServerContainerInitializer.configureContext(webSocketServletContextHandler);

		final ServletContextHandler restServletContextHandler = new ServletContextHandler(ccowServer, "/");
		restServletContextHandler.addEventListener(c);
		restServletContextHandler.addFilter(AppGuiceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

		 final ContextHandlerCollection contexts = new ContextHandlerCollection();
	        contexts.setHandlers(new Handler[] { webSocketServletContextHandler, restServletContextHandler});
	        
		ccowServer.setHandler(contexts);
		ccowServer.start();

	}

	@AfterClass
	public static void afterClass() throws Exception {
		ccowServer.stop();
	}

	@Before()
	public void before() throws Exception {
		itemNames = Lists.newArrayList("User.Id.Cpr.Number", "User.Co.IdCardId");
		itemValues = Lists.newArrayList("0102030405", "0987654321");

		cmr = new ContextManagerRegistryProxy(serverAddress).Locate("CCOW.ContextManager", "1.5", "", "");
		cm = new ContextManagerProxy(cmr);
		cdp = new ContextDataProxy(cmr);
		cx = new ContextActionProxy(cmr);

		system2 = new ContextClient("Bookplan", cm, cdp, cx);
		system1 = new ContextClient("Columna", cm, cdp, cx);
		system3 = new ContextClient("Anywhere", cm, cdp, cx);

		participantServer = new ParticipantHoster(Lists.newArrayList(system1, system2, system3));

		participantCouponSystem1 = system1.joinCommonContext(false, false);
		participantCouponSystem2 = system2.joinCommonContext(false, false);
		participantCouponSystem3 = system3.joinCommonContext(false, false);
	}

	@Test
	public void goThroughContextChangeAndVerifyWithWSClient() throws Exception {
		
		final WSContextParticipantImpl wsClient = new WSContextParticipantImpl(host, serverPort);
		wsClient.joinCommonContext();
		system1.startContextChanges();
		system1.setItemValues(itemNames, itemValues);
		final Form form = system1.endContextChanges();
		final boolean _continue = !Boolean.getBoolean(form.getFirst("noContinue"));
		final String stringResponses = form.getFirst("responses");
		final List<String> reasons;
		if (Strings.isNullOrEmpty(stringResponses))
			reasons = Lists.newArrayList();
		else
			reasons = Lists.newArrayList(Splitter.on("|").split(stringResponses));
		assertEquals(true, _continue);
		assertEquals(true, reasons.isEmpty());
		assertEquals(true, wsClient.getConnection().getLatestMessage() != null);
		wsClient.close();
	}
}
