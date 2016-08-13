
package test.manual;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.reflections.ReflectionUtils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.jersey.api.representation.Form;

import ccow.cma.servlet.ContextDataServlet;
import ccow.cma.servlet.ContextManagementRegistryServlet;
import ccow.cma.servlet.ContextManagerServlet;
import ccow.cma.servlet.helpers.ContextState;

public class Generator {

	@Test
	public void doGenerate() {

		Map<Class, String> classToVariable = Maps.newHashMap();
		classToVariable.put(ContextManagementRegistryServlet.class, "cmrs");
		classToVariable.put(ContextManagerServlet.class, "cms");
		classToVariable.put(ContextDataServlet.class, "cds");

		ContextState session = new ContextState();
		Map<Class<?>, Object> servletsToObjects = Maps.newHashMap();
		servletsToObjects.put(ContextManagementRegistryServlet.class, new ContextManagementRegistryServlet());
		servletsToObjects.put(ContextManagerServlet.class, new ContextManagerServlet(session));
		servletsToObjects.put(ContextDataServlet.class, new ContextDataServlet(session));

		Map<String, Class<?>> stringToClass = FluentIterable.from(servletsToObjects.keySet())
				.uniqueIndex(new Function<Class<?>, String>() {

					@Override
					public String apply(Class<?> input) {
						return input.getAnnotation(Path.class).value();
					}
				});
		Map<Class<?>, Set<Method>> classToMethods = Maps.newHashMap();

		for (Class<?> s : servletsToObjects.keySet()) {
			Set<Method> ms = ReflectionUtils.getAllMethods(s, ReflectionUtils.withAnnotation(Path.class));
			for (Method m : ms) {
				Set<Method> c = classToMethods.get(m.getDeclaringClass());
				if (c == null)
					classToMethods.put(m.getDeclaringClass(), Sets.newHashSet(m));
				else
					c.add(m);
			}
		}
		
		boolean firstStatement = true;

		for (Entry<Class<?>, Set<Method>> c : classToMethods.entrySet()) {
			Path a = c.getKey().getAnnotation(Path.class);

			for (Method v : c.getValue()) {

				String returnValue = "";
				Class<?> returns = v.getReturnType();
				if (Form.class.equals(returns))
					returnValue = "form = ";
				StringBuilder sb = new StringBuilder();
				if(!firstStatement)
				{
					sb.append("else ");
				}
				else
				{
					firstStatement = false;
				}
				sb.append("if(\"" + v.getName() + "\".equalsIgnoreCase(method)) {" + System.lineSeparator()
						+ returnValue + classToVariable.get(c.getKey()) + "." + v.getName() + "(");

				List<String> stringList = Lists.newArrayList();
				for (Parameter p : v.getParameters()) {
					QueryParam qp = p.getAnnotation(QueryParam.class);
					Context ca = p.getAnnotation(Context.class);

					if (qp != null) {
						if (p.getParameterizedType() == long.class)
							stringList.add("Long.parseLong(queryMap.get(\"" + qp.value() + "\"))");
						else if (p.getParameterizedType() == boolean.class)
							stringList.add("Boolean.parseBoolean(queryMap.get(\"" + qp.value() + "\"))");
						else if (p.getParameterizedType() == String.class)
							stringList.add("queryMap.get(\"" + qp.value() + "\")");
						else
							throw new RuntimeException("Unsupported type");
					} else if (ca != null) {

						Class<?> pT = p.getType();
						if (pT.isAssignableFrom(UriInfo.class)) {
							stringList.add("RequestHandlerUtil.toUriInfo(uri)");
						} else if (pT.isAssignableFrom(HttpServletResponse.class)) {
							stringList.add("RequestHandlerUtil.getNewResponse()");
						} else {
							throw new RuntimeException("Unsupported type");
						}
					}
				}
				sb.append(Joiner.on(", ").join(stringList));
				sb.append(");" + System.lineSeparator() + "}");
				System.out.println(sb.toString());
			}
		}
	}
}
