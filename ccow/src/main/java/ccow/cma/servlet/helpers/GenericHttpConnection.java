package ccow.cma.servlet.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.util.ReaderWriter;

public class GenericHttpConnection {

	private final HttpTransport httpTransport = new NetHttpTransport();
	private GenericUrl genericUrl;

	public GenericHttpConnection resource(final URI uri) {
		genericUrl = new GenericUrl(uri);
		return this;
	}

	public GenericHttpConnection queryParam(final String key, final String value) {
		genericUrl.put(key, value);
		return this;
	}

	public HttpResponse get() {
		try {
			final HttpRequest request = httpTransport.createRequestFactory().buildGetRequest(genericUrl);
			request.getHeaders().setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			return request.execute();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Form get(final Class<Form> clazz) {
		try {
			
//			MultivaluedMap<String, String> result = new FormMultivaluedMapProvider().readFrom(new MultivaluedMapImpl(),
//					MediaType.APPLICATION_FORM_URLENCODED_TYPE, response.getContent());
//			Form form = new Form();
//			form.putAll(result);
//			return form;
			return toForm(get().getContent());
		} catch (final IOException e) {
			throw new RuntimeException(e);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Form toForm(final InputStream inputStream) throws Exception {

		final String encoded = ReaderWriter.readFromAsString(new InputStreamReader(inputStream));
		// com.sun.jersey.core.impl.provider.entity.BaseFormProvider
		final Form map = new Form();
		final String charsetName = "UTF-8";
		final StringTokenizer tokenizer = new StringTokenizer(encoded, "&");
		String token;
		try {
			while (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken();
				final int idx = token.indexOf('=');
				if (idx < 0) {
					map.add(URLDecoder.decode(token, charsetName), null);
				} else if (idx > 0) {
					map.add(URLDecoder.decode(token.substring(0, idx), charsetName),
							URLDecoder.decode(token.substring(idx + 1), charsetName));
				}
			}
			return map;
		} catch (final IllegalArgumentException ex) {
			throw new WebApplicationException(ex, Status.BAD_REQUEST);
		}
	}

}
