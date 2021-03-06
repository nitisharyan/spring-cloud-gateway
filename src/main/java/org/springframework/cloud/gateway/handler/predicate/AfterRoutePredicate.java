package org.springframework.cloud.gateway.handler.predicate;

import java.time.ZonedDateTime;
import java.util.function.Predicate;

import org.springframework.web.server.ServerWebExchange;

import static org.springframework.cloud.gateway.handler.predicate.BetweenRoutePredicate.parseZonedDateTime;

/**
 * @author Spencer Gibb
 */
public class AfterRoutePredicate implements RoutePredicate {

	@Override
	public Predicate<ServerWebExchange> apply(String... args) {
		validate(1, args);
		final ZonedDateTime dateTime = parseZonedDateTime(args[0]);

		return exchange -> {
			final ZonedDateTime now = ZonedDateTime.now();
			return now.isAfter(dateTime);
		};
	}
}
