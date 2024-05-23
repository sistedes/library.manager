/*******************************************************************************
* Copyright (c) 2023 Sistedes
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v2.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
* Abel Gómez - initial API and implementation
*******************************************************************************/

package es.sistedes.library.manager;

import java.net.URI;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.sistedes.library.manager.dspace.model.DSRoot;
import reactor.core.publisher.Mono;

public class DSpaceConnectionManager {

	public static class DSpaceConnection {
		
		private static final Logger logger = LoggerFactory.getLogger(DSpaceConnection.class);
		
		private static final int TIMEOUT_MINUTES = 25;
		private DSRoot dsRoot;
		private Date lastIssued;
		private Thread refreshThread;
		private volatile boolean exit = false;
		
		private DSpaceConnection(URI uri, String email, String password) {
			dsRoot = DSRoot.create(uri);
			dsRoot.getAuthnEndpoint().doLogin(email, password);
			lastIssued = Calendar.getInstance().getTime();
			refreshThread = new Thread() {
				@Override
				public void run() {
					while (!exit) {
						if (Calendar.getInstance().getTime().toInstant().getEpochSecond() - lastIssued.toInstant().getEpochSecond() > TIMEOUT_MINUTES * 60) {
							ResponseEntity<Void> result = dsRoot.getAuthnEndpoint().refreshAuth();
							if (result.getStatusCode().isError()) {
								logger.error("Unable to refresh JWT token!");
							}
							lastIssued = Calendar.getInstance().getTime();
						}
						try {
							sleep(1000);
						} catch (InterruptedException e) {
						}
					}
				}
			};
			refreshThread.setDaemon(true);
			refreshThread.start();
		}
		
		public DSRoot getDsRoot() {
			return dsRoot;
		}
		
		public void close() {
			dsRoot.getAuthnEndpoint().doLogout();
			exit = true;
		}
	}
	
	public static DSpaceConnection createConnection(URI uri, String email, String password) {
		return new DSpaceConnection(uri, email, password);
	}
	
	private static class CsrfClientExchangeFilterFunction implements ExchangeFilterFunction {

		public static final CsrfClientExchangeFilterFunction INSTANCE = new CsrfClientExchangeFilterFunction();

		private static final String AUTHORIZATION_HEADER = "Authorization";
		private static final String DSPACE_XSRF_TOKEN = "DSPACE-XSRF-TOKEN";
		private static final String X_XSRF_TOKEN = "X-XSRF-TOKEN";

		private String xsrfToken = "";
		private String authToken = "";

		private MultiValueMap<String, String> prevCookies = new LinkedMultiValueMap<>();

		@Override
		public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
			ClientRequest newRequest = ClientRequest.from(request).headers((headers) -> {
				headers.set(X_XSRF_TOKEN, xsrfToken);
				headers.set(AUTHORIZATION_HEADER, "Bearer " + authToken);
			}).cookies((cookies) -> {
				cookies.addAll(prevCookies);
			}).build();
			return next.exchange(newRequest).flatMap(response -> {
				xsrfToken = response.headers().header(DSPACE_XSRF_TOKEN).stream().findAny().orElse(xsrfToken);
				authToken = response.headers().header(AUTHORIZATION_HEADER).stream().findAny().orElse(authToken);
				response.cookies().forEach((name, cookie) -> {
					prevCookies.put(name, Arrays.asList(cookie.get(0).getValue()));
				});
				return Mono.just(response);
			});
		}
	}

	public static WebClient buildClient() {
		return buildClient(null);
	}

	public static WebClient buildClient(URI baseUri) {
		return WebClient.builder().baseUrl(baseUri != null ? baseUri.toString() : null).filter(CsrfClientExchangeFilterFunction.INSTANCE)
				.codecs(clientDefaultCodecsConfigurer -> {
					clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(createDefaultObjectMapper(), MediaType.APPLICATION_JSON));
				})
				.codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(1024 * 1024))
				.build();
	}
	
	public static ObjectMapper createDefaultObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		return mapper;
	}

}