package org.springframework.cloud.gateway.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CLIENT_RESPONSE_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.RESPONSE_COMMITTED_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.getAttribute;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientResponse;

/**
 * @author Spencer Gibb
 */
public class WriteResponseFilter implements GlobalFilter, Ordered {

	private static final Log log = LogFactory.getLog(WriteResponseFilter.class);

	public static final int WRITE_RESPONSE_FILTER_ORDER = -1;

	@Override
	public int getOrder() {
		return WRITE_RESPONSE_FILTER_ORDER;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		// NOTICE: nothing in "pre" filter stage as CLIENT_RESPONSE_ATTR is not added
		// until the WebHandler is run
		return chain.filter(exchange).then(() -> {
			HttpClientResponse clientResponse = getAttribute(exchange, CLIENT_RESPONSE_ATTR, HttpClientResponse.class);
			if (clientResponse == null) {
				return Mono.empty();
			}
			log.trace("WriteResponseFilter start");
			ServerHttpResponse response = exchange.getResponse();

			//make other filters aware that the response has been committed
			response.beforeCommit(() -> {
				exchange.getAttributes().put(RESPONSE_COMMITTED_ATTR, true);
				return Mono.empty();
			});

			NettyDataBufferFactory factory = (NettyDataBufferFactory) response.bufferFactory();
			//TODO: what if it's not netty

			final Flux<NettyDataBuffer> body = clientResponse.receive()
					.retain() //TODO: needed?
					.map(factory::wrap);

			return response.writeWith(body);
		});
	}

}
