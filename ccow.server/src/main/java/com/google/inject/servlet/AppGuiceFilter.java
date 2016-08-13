package com.google.inject.servlet;

import java.io.IOException;
import java.lang.ref.WeakReference;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Diogo Guerra
 */
public class AppGuiceFilter extends GuiceFilter {

	// lock to ensure that all webapps using this filter will not access to the
	// static pipeline concurrently.
	// this lock will only work if all web apps use this filter. This lock is
	// not safe if other app uses GuiceFilter.
	private static final Object lock = new Object();

	// local instance of the pipeline.
	volatile FilterPipeline localPipeline;

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		// this method is a copy of the one in GuiceFilter, but using the local
		// pipeline instead of the static one.
		Context previous = localContext.get();

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		HttpServletRequest originalRequest = (previous != null) ? previous.getOriginalRequest() : request;

		try {
			localContext.set(new Context(originalRequest, request, response));

			// dispatch across the servlet pipeline, ensuring web.xml's
			// filterchain is honored
			localPipeline.dispatch(servletRequest, servletResponse, filterChain);

		} finally {
			localContext.set(previous);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		synchronized (lock) {
			// define the localPipeline with the injected pipeline.
			localPipeline = pipeline;
			// Store servlet context in a weakreference, for injection
			servletContext = new WeakReference<ServletContext>(filterConfig.getServletContext());
			localPipeline.initPipeline(filterConfig.getServletContext());
			// reset the static pipeline
			pipeline = new DefaultFilterPipeline();
		}
	}

	@Override
	public void destroy() {
		try {
			// destroy the local pipeline instead of the static one.
			localPipeline.destroyPipeline();
		} finally {
			reset();
			servletContext.clear();
		}
	}
}