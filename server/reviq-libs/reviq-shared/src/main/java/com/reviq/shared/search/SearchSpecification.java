package com.reviq.shared.search;

import com.reviq.shared.search.enums.FieldType;
import jakarta.annotation.Nonnull;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class SearchSpecification<T> implements Specification<T> {

    private final Class<T> clazz;
    private final SearchRequest searchRequest;

    public SearchSpecification(Class<T> clazz, SearchRequest searchRequest) {
        this.clazz = clazz;
        this.searchRequest = searchRequest;
    }

    @Nullable
    @Override
    public Predicate toPredicate(@Nonnull Root<T> root, @Nonnull CriteriaQuery<?> query,
                                  @Nonnull CriteriaBuilder cb) {
        if (!searchRequest.getSorts().isEmpty()) {
            getSortOrders(root, cb).forEach(query::orderBy);
        }

        Predicate filterPredicate = getFilterPredicate(root, cb);
        Predicate deletedPredicate = getDeletedPredicate(root, cb);
        Predicate finalPredicate = combinePredicate(cb, filterPredicate, deletedPredicate);

        if (finalPredicate != null) {
            query.where(finalPredicate);
            return query.getRestriction();
        }
        return null;
    }

    private Predicate getDeletedPredicate(Root<T> root, CriteriaBuilder cb) {
        if (hasField("deleted")) {
            try {
                return cb.equal(root.get("deleted"), false);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private boolean hasField(String fieldName) {
        Class<?> current = clazz;
        while (current != null) {
            try {
                current.getDeclaredField(fieldName);
                return true;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return false;
    }

    private Predicate combinePredicate(CriteriaBuilder cb, Predicate... predicates) {
        Predicate[] nonNull = Arrays.stream(predicates)
                .filter(Objects::nonNull)
                .toArray(Predicate[]::new);
        if (nonNull.length == 0) return null;
        if (nonNull.length == 1) return nonNull[0];
        return cb.and(nonNull);
    }

    private Predicate getFilterPredicate(Root<T> root, CriteriaBuilder cb) {
        if (searchRequest.getFilters().isEmpty()) return null;

        Predicate[] predicates = searchRequest.getFilters().stream()
                .map(filter -> buildPredicate(filter, root, cb))
                .filter(Objects::nonNull)
                .toArray(Predicate[]::new);

        return predicates.length == 0 ? null : cb.and(predicates);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildPredicate(FilterRequest filter, Root<T> root, CriteriaBuilder cb) {
        Path<Object> path = getPath(root, filter.getKey());
        if (path == null) return null;

        Object value = parseValue(filter.getFieldType(), filter.getValue());
        Object valueTo = parseValue(filter.getFieldType(), filter.getValueTo());
        Object[] values = filter.getValues().stream()
                .map(v -> parseValue(filter.getFieldType(), v))
                .filter(Objects::nonNull)
                .toArray();

        return switch (filter.getOperator()) {
            case EQUAL -> {
                if (value == null) yield null;
                if (path.getJavaType().isAssignableFrom(UUID.class))
                    yield cb.equal(path.as(UUID.class), UUID.fromString(value.toString()));
                yield cb.equal(path, value);
            }
            case NOT_EQUAL -> value != null ? cb.notEqual(path, value) : null;
            case LIKE -> value != null ? cb.like(cb.lower(path.as(String.class)),
                    "%" + value.toString().toLowerCase() + "%") : null;
            case NOT_LIKE -> value != null ? cb.notLike(cb.lower(path.as(String.class)),
                    "%" + value.toString().toLowerCase() + "%") : null;
            case STARTS_WITH -> value != null ? cb.like(cb.lower(path.as(String.class)),
                    value.toString().toLowerCase() + "%") : null;
            case END_WITH -> value != null ? cb.like(cb.lower(path.as(String.class)),
                    "%" + value.toString().toLowerCase()) : null;
            case IN -> values.length > 0 ? path.in(values) : null;
            case NOT_IN -> values.length > 0 ? cb.not(path.in(values)) : null;
            case GREATER_THAN -> buildComparison(path, value, cb, true, false);
            case GREATER_OR_EQUAL_TO -> buildComparison(path, value, cb, true, true);
            case LESS_THAN -> buildComparison(path, value, cb, false, false);
            case LESS_OR_EQUAL_TO -> buildComparison(path, value, cb, false, true);
            case BETWEEN -> buildBetween(path, value, valueTo, cb);
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildComparison(Path<Object> path, Object value, CriteriaBuilder cb,
                                       boolean greaterThan, boolean orEqual) {
        if (value == null) return null;
        Class<?> type = path.getJavaType();

        if (type.isAssignableFrom(Integer.class)) {
            int v = Integer.parseInt(value.toString());
            return greaterThan
                    ? (orEqual ? cb.greaterThanOrEqualTo(path.as(Integer.class), v) : cb.greaterThan(path.as(Integer.class), v))
                    : (orEqual ? cb.lessThanOrEqualTo(path.as(Integer.class), v) : cb.lessThan(path.as(Integer.class), v));
        }
        if (type.isAssignableFrom(Long.class)) {
            long v = Long.parseLong(value.toString());
            return greaterThan
                    ? (orEqual ? cb.greaterThanOrEqualTo(path.as(Long.class), v) : cb.greaterThan(path.as(Long.class), v))
                    : (orEqual ? cb.lessThanOrEqualTo(path.as(Long.class), v) : cb.lessThan(path.as(Long.class), v));
        }
        if (type.isAssignableFrom(Double.class)) {
            double v = Double.parseDouble(value.toString());
            return greaterThan
                    ? (orEqual ? cb.greaterThanOrEqualTo(path.as(Double.class), v) : cb.greaterThan(path.as(Double.class), v))
                    : (orEqual ? cb.lessThanOrEqualTo(path.as(Double.class), v) : cb.lessThan(path.as(Double.class), v));
        }
        if (type.isAssignableFrom(LocalDate.class)) {
            LocalDate v = parseLocalDate(value.toString());
            if (v == null) return null;
            return greaterThan
                    ? (orEqual ? cb.greaterThanOrEqualTo(path.as(LocalDate.class), v) : cb.greaterThan(path.as(LocalDate.class), v))
                    : (orEqual ? cb.lessThanOrEqualTo(path.as(LocalDate.class), v) : cb.lessThan(path.as(LocalDate.class), v));
        }
        if (type.isAssignableFrom(LocalDateTime.class)) {
            LocalDateTime v = parseLocalDateTime(value.toString());
            if (v == null) return null;
            return greaterThan
                    ? (orEqual ? cb.greaterThanOrEqualTo(path.as(LocalDateTime.class), v) : cb.greaterThan(path.as(LocalDateTime.class), v))
                    : (orEqual ? cb.lessThanOrEqualTo(path.as(LocalDateTime.class), v) : cb.lessThan(path.as(LocalDateTime.class), v));
        }
        if (type.isAssignableFrom(Instant.class)) {
            Instant v = Instant.parse(value.toString());
            return greaterThan
                    ? (orEqual ? cb.greaterThanOrEqualTo(path.as(Instant.class), v) : cb.greaterThan(path.as(Instant.class), v))
                    : (orEqual ? cb.lessThanOrEqualTo(path.as(Instant.class), v) : cb.lessThan(path.as(Instant.class), v));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Predicate buildBetween(Path<Object> path, Object value, Object valueTo, CriteriaBuilder cb) {
        if (value == null || valueTo == null) return null;
        Class<?> type = path.getJavaType();

        if (type.isAssignableFrom(Integer.class))
            return cb.between(path.as(Integer.class), Integer.parseInt(value.toString()), Integer.parseInt(valueTo.toString()));
        if (type.isAssignableFrom(Long.class))
            return cb.between(path.as(Long.class), Long.parseLong(value.toString()), Long.parseLong(valueTo.toString()));
        if (type.isAssignableFrom(Double.class))
            return cb.between(path.as(Double.class), Double.parseDouble(value.toString()), Double.parseDouble(valueTo.toString()));
        if (type.isAssignableFrom(LocalDate.class)) {
            LocalDate from = parseLocalDate(value.toString());
            LocalDate to = parseLocalDate(valueTo.toString());
            return (from != null && to != null) ? cb.between(path.as(LocalDate.class), from, to) : null;
        }
        if (type.isAssignableFrom(LocalDateTime.class)) {
            LocalDateTime from = parseLocalDateTime(value.toString());
            LocalDateTime to = parseLocalDateTime(valueTo.toString());
            return (from != null && to != null) ? cb.between(path.as(LocalDateTime.class), from, to) : null;
        }
        if (type.isAssignableFrom(Instant.class))
            return cb.between(path.as(Instant.class), Instant.parse(value.toString()), Instant.parse(valueTo.toString()));
        return null;
    }

    private Path<Object> getPath(Root<T> root, String key) {
        if (key.contains(".")) {
            String[] parts = key.split("\\.");
            Join<Object, Object> join = root.join(parts[0], JoinType.LEFT);
            for (int i = 1; i < parts.length - 1; i++) {
                join = join.join(parts[i], JoinType.LEFT);
            }
            return join.get(parts[parts.length - 1]);
        }
        return root.get(key);
    }

    private List<Order> getSortOrders(Root<T> root, CriteriaBuilder cb) {
        return searchRequest.getSorts().stream()
                .map(sort -> sort.getDirection().build(root, cb, sort))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object parseValue(FieldType fieldType, Object value) {
        if (value == null || fieldType == null) return value;
        if (fieldType.equals(FieldType.ENUM_STRING)) {
            String[] parts = value.toString().split("\\.");
            if (parts.length == 2) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.getType().isEnum() && field.getType().getSimpleName().equals(parts[0])) {
                        return Enum.valueOf((Class<Enum>) field.getType(), parts[1]);
                    }
                }
            }
        }
        return fieldType.parse(value.toString());
    }

    public static Pageable getPageable(Integer page, Integer size) {
        return PageRequest.of(
                Objects.requireNonNullElse(page, 0),
                Objects.requireNonNullElse(size, 100)
        );
    }

    private LocalDate parseLocalDate(String value) {
        if (value == null) return null;
        try { return LocalDateTime.parse(value).toLocalDate(); } catch (Exception ignored) {}
        try { return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE); } catch (Exception ignored) {}
        return null;
    }

    private LocalDateTime parseLocalDateTime(String value) {
        if (value == null) return null;
        try { return LocalDateTime.parse(value); } catch (Exception ignored) {}
        try { return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(); } catch (Exception ignored) {}
        return null;
    }
}
