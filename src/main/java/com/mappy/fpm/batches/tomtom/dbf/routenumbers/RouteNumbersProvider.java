package com.mappy.fpm.batches.tomtom.dbf.routenumbers;

import com.google.common.collect.ImmutableList;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.TomtomDbfReader;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;

@Slf4j
@Singleton
public class RouteNumbersProvider extends TomtomDbfReader {

    private final Map<Long, List<RouteNumbers>> routenumbers = newHashMap();
    private final Integer INTERNATIONAL_ROUTE = 5;

    @Inject
    public RouteNumbersProvider(TomtomFolder folder) {
        super(folder);
        readFile("rn.dbf", this::getRouteNumbers);
    }

    public Optional<String> getInternationalRouteNumbers(Long id) {
        return getRoute(id, routeNumbers -> INTERNATIONAL_ROUTE.equals(routeNumbers.getRouteNumberType()));
    }

    public Optional<String> getNationalRouteNumbers(Long id) {
        return getRoute(id, routeNumbers -> !INTERNATIONAL_ROUTE.equals(routeNumbers.getRouteNumberType()));
    }

    public Optional<String> getRouteTypeOrderByPriority(Long id) {
        return ofNullable(routenumbers.get(id))
                .orElse(ImmutableList.of())
                .stream()
                .sorted(comparing(RouteNumbers::getRouteNumberPriority))
                .map(type -> String.valueOf(type.getRouteNumberType()))
                .findFirst();
    }

    private Optional<String> getRoute(Long id, Predicate<RouteNumbers> routeNumbersPredicate) {
        return ofNullable(routenumbers.get(id))
                .orElse(ImmutableList.of())
                .stream()
                .filter(routeNumbersPredicate)
                .sorted(comparing(RouteNumbers::getRouteNumberPriority))
                .map(RouteNumbers::getFullRouteNumber)
                .findFirst();
    }

    private void getRouteNumbers(DbfRow row) {
        RouteNumbers routeNumbers = RouteNumbers.fromDbf(row);
        List<RouteNumbers> geocodingAttributes = routenumbers.containsKey(routeNumbers.getId()) ? routenumbers.get(routeNumbers.getId()) : newArrayList();
        geocodingAttributes.add(routeNumbers);
        routenumbers.put(routeNumbers.getId(), geocodingAttributes);
    }
}
