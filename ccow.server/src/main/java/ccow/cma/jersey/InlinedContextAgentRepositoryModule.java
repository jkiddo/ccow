package ccow.cma.jersey;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;

import ccow.cma.servlet.helpers.IContextAgentRepository;
import ccow.cma.servlet.helpers.IQualifiedContextAgent;

public class InlinedContextAgentRepositoryModule extends AbstractModule {

	private final Map<String, IQualifiedContextAgent> mapOfAgents;

	public InlinedContextAgentRepositoryModule(final Map<String, IQualifiedContextAgent> mapOfAgents) {
		this.mapOfAgents = mapOfAgents;
	}

	public InlinedContextAgentRepositoryModule() {
		this(ImmutableMap.<String, IQualifiedContextAgent> builder().build());
	}

	@Override
	protected void configure() {
		bind(IContextAgentRepository.class).toInstance(new IContextAgentRepository() {
			
			@Override
			public IQualifiedContextAgent findContextAgent(final String[] names, final String[] values) {
				IQualifiedContextAgent agent;
				for(final String name : names)
				{
					agent = mapOfAgents.get(name);
					if(agent != null)
						return agent;
				}
				return null;
			}
		});
	}

}
