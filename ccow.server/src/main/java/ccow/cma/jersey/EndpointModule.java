package ccow.cma.jersey;

import com.google.inject.AbstractModule;

import ccow.cma.jersey.Exceptions.CatchAllExceptionMapper;
import ccow.cma.jersey.Exceptions.MyExceptionMapper;
import ccow.cma.jersey.Exceptions.WebApplicationExceptionMapper;
import ccow.cma.servlet.ContextActionServlet;
import ccow.cma.servlet.ContextDataServlet;
import ccow.cma.servlet.ContextManagementRegistryServlet;
import ccow.cma.servlet.ContextManagerServlet;
import ccow.cma.servlet.helpers.ContextState;

public class EndpointModule extends AbstractModule{

	private final ContextState commonContext;
	
	public EndpointModule(final ContextState commonContext) {
		this.commonContext = commonContext;
	}

	@Override
	protected void configure() {
		bind(ContextState.class).toInstance(commonContext);
		bind(ContextManagementRegistryServlet.class).asEagerSingleton();
		bind(ContextManagerServlet.class).asEagerSingleton();
		bind(ContextDataServlet.class).asEagerSingleton();
		bind(ContextActionServlet.class).asEagerSingleton();
		bind(WebApplicationExceptionMapper.class);
		bind(MyExceptionMapper.class);
		bind(CatchAllExceptionMapper.class);
	}
}
