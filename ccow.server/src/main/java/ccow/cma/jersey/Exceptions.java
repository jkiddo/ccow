/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package ccow.cma.jersey;

import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Exceptions class.
 *
 * @author Santiago.Pericas-Geertsen at oracle.com
 */
public class Exceptions {

	// -- Exceptions
	public static class MyException extends Exception {

		private final Response response;

		public MyException(final Response response) {
			this.response = response;
		}

		public Response getResponse() {
			return response;
		}
	}

	public static class MySubException extends MyException {

		public MySubException(final Response response) {
			super(response);
		}
	}

	public static class MySubSubException extends MySubException {

		public MySubSubException(final Response response) {
			super(response);
		}
	}

	// -- Exception Mappers
	@Provider
	@Singleton
	public static class MyExceptionMapper implements ExceptionMapper<MyException> {

		@Override
		public Response toResponse(final MyException exception) {
			final Response r = exception.getResponse();
			return Response.status(r.getStatus()).entity("Code:" + r.getStatus() + ":" + getClass().getSimpleName())
					.build();
		}
	}
	
	@Provider
	@Singleton
	public static class CatchAllExceptionMapper implements ExceptionMapper<Throwable> {

		@Override
		public Response toResponse(final Throwable exception) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.build();
		}
	}

	@Provider
	@Singleton
	public static class MySubExceptionMapper implements ExceptionMapper<MySubException> {

		@Override
		public Response toResponse(final MySubException exception) {
			final Response r = exception.getResponse();
			return Response.status(r.getStatus()).entity("Code:" + r.getStatus() + ":" + getClass().getSimpleName())
					.build();
		}
	}

	@Provider
	@Singleton
	public static class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

		@Override
		public Response toResponse(final WebApplicationException exception) {
			final Response r = exception.getResponse();
			return Response.status(r.getStatus()).entity("Code:" + r.getStatus() + ":" + getClass().getSimpleName())
					.build();
		}
	}
}