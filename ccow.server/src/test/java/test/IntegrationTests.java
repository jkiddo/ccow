package test;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.servlet.AppGuiceFilter;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.representation.Form;

import ccow.cma.jersey.CCOWContextListener;
import ccow.cma.jersey.InlinedContextAgentRepositoryModule;
import ccow.cma.servlet.helpers.ContextState;
import ccow.cma.servlet.helpers.IContextParticipantProxy;
import test.proxies.ContextActionProxy;
import test.proxies.ContextClient;
import test.proxies.ContextDataProxy;
import test.proxies.ContextManagerProxy;
import test.proxies.ContextManagerRegistryProxy;
import test.proxies.util.ParticipantHoster;

public class IntegrationTests {

	private static Server ccowServer;
	private static int serverPort = 2116;
	private static String host = "localhost";
	private static ContextState commonContext = new ContextState();
	private final URI serverAddress = URI.create("http://" + host + ":" + serverPort);
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

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@BeforeClass
	public static void beforeClass() throws Exception {
		ccowServer = new Server(serverPort);
		final ServletContextHandler sch = new ServletContextHandler(ccowServer, "/");
		sch.addEventListener(new CCOWContextListener(commonContext, new InlinedContextAgentRepositoryModule()));
		sch.addFilter(AppGuiceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
		sch.addServlet(DefaultServlet.class, "/");
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

		system1 = new ContextClient("Bookplan", cm, cdp, cx);
		system2 = new ContextClient("Columna", cm, cdp, cx);
		system3 = new ContextClient("Anywhere", cm, cdp, cx);

		participantServer = new ParticipantHoster(Lists.newArrayList(system2, system1, system3));

		system2.joinCommonContext(false, false);
		system1.joinCommonContext(false, false);
		system3.joinCommonContext(false, false);
	}

	@After
	public void after() throws Exception {
		final Collection<IContextParticipantProxy> participants = commonContext.getParticipants();
		for (final IContextParticipantProxy p : participants) {
			commonContext.removeParticipant(p.getParticipantCoupon());
		}
		participantServer.stopServer();
	}

	@Test(expected = RuntimeException.class)
	public void nonOwnerTriesToChangeContext() throws Exception {

		system1.startContextChanges();
		system2.startContextChanges();
	}

	@Test
	public void participantSetsContextAndFetchesLatestState() throws Exception {

		final long contextTicket = system1.startContextChanges();
		assertEquals(contextTicket, cm.GetMostRecentContextCoupon());
	}

	@Test
	public void participantSetsDefaultContextValues() throws Exception {

		system2.startContextChanges();
		system2.setItemValues(itemNames, itemValues);
		assertEquals(system2.getItemValues(itemNames, false), itemValues);
	}

	@Test
	public void participantSetsContextValues() throws Exception {

		system2.startContextChanges();
		itemNames.add("Patient.Id.IdList.1");
		itemValues.add("070683ABXD^^^CPR^PI^&1.2.208.176.1.2&ISO");
		system2.setItemValues(itemNames, itemValues);
		final List<String> result = system2.getItemValues(itemNames, false);
		assertEquals(result.size(), itemValues.size());
		assertEquals(itemValues.containsAll(result), true);
	}

	@Test
	public void participantSetsContextValuesAndEndsContext() throws Exception {

		system2.startContextChanges();
		system2.setItemValues(itemNames, itemValues);
		final Form form = system2.endContextChanges();
		final boolean _continue = !Boolean.getBoolean(form.getFirst("noContinue"));
		final String stringResponses = form.getFirst("responses");
		final List<String> reasons;
		if (Strings.isNullOrEmpty(stringResponses))
			reasons = Lists.newArrayList();
		else
			reasons = Lists.newArrayList(Splitter.on("|").split(stringResponses));
		assertEquals(true, _continue);
		assertEquals(true, reasons.isEmpty());
	}

	@Test
	public void participantSetsContextValuesAndEndsContextAndPublishes() throws Exception {

		participantSetsContextValuesAndEndsContext();
		system2.publishChangesDecision("accept");
	}


	@Test
	public void doContextActionInAlreadyActiveContextSession() {
		
		system2.startContextChanges();
		thrown.expect(UniformInterfaceException.class);
//		thrown.expectMessage("Expected exception to be thrown");

		system3.perform(itemNames, itemValues);
	}
}