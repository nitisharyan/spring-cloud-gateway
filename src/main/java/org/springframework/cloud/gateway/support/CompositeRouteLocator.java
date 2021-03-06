package org.springframework.cloud.gateway.support;

import org.springframework.cloud.gateway.api.Route;
import org.springframework.cloud.gateway.api.RouteLocator;
import reactor.core.publisher.Flux;

/**
 * @author Spencer Gibb
 */
public class CompositeRouteLocator implements RouteLocator {

	private final Flux<RouteLocator> delegates;

	public CompositeRouteLocator(Flux<RouteLocator> delegates) {
		this.delegates = delegates;
	}

	@Override
	public Flux<Route> getRoutes() {
		return this.delegates.flatMap(RouteLocator::getRoutes);
	}
}
