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

import org.springframework.http.MediaType;
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

import reactor.core.publisher.Mono;

public class DSpaceConnectionManager {

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
				}).build();
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