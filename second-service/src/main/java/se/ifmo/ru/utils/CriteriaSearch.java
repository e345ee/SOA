package se.ifmo.ru.utils;

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.persistence.criteria.*;
import org.hibernate.StatelessSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CriteriaSearch {
    public CriteriaSearch() {}
    public static <E> List<E> search(StatelessSession statelessSession,
                                     Class<E> entityType, Map<String, Object> filters,
                                     PageRequest pageRequest, List<Sort<E>> sorts) {
        CriteriaBuilder cb = statelessSession.getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(entityType);
        Root<E> root = cq.from(entityType);
        List<Order> orderBy = new ArrayList<>();
        if (sorts != null) {
            for (Sort<E> sort : sorts) {
                Path<?> col = path(root, sort.property());
                if (sort.isAscending()) {
                    orderBy.add(cb.asc(col));
                } else {
                    orderBy.add(cb.desc(col));
                }
            }
        }
        cq.select(root).where(whereFilters(cb,root, filters)).orderBy(orderBy);
        return statelessSession.createQuery(cq)
                .setFirstResult((int) (pageRequest.page()-1)*pageRequest.size())
                .setMaxResults(pageRequest.size())
                .getResultList();
    }
    public static <E> Long count(StatelessSession session, Class<E> type, Map<String, Object> filters) {
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<E> root = cq.from(type);
        cq.select(cb.count(root)).where(whereFilters(cb,root, filters));
        return session.createQuery(cq).getSingleResult();
    }

    private static <E> Predicate[] whereFilters(CriteriaBuilder cb, Root<E> root, Map<String, Object> filters){
        List<Predicate> where = new ArrayList<>();
        if (filters != null) {
            for (var filter : filters.entrySet()) {
                where.add(toPredicate(cb,root, filter.getKey(), filter.getValue()));
            }
        }
        return where.toArray(Predicate[]::new);
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Predicate toPredicate(CriteriaBuilder cb, Root<?> root, String key, Object value) {
        if (key.endsWith(">=")) {
            Path column = path(root, key.substring(0, key.length() - 2));
            return cb.greaterThanOrEqualTo(column, (Comparable) value);
        }
        if (key.endsWith("<=")) {
            Path column = path(root, key.substring(0, key.length() - 2));
            return cb.lessThanOrEqualTo(column, (Comparable) value);
        }
        if (key.endsWith("~")) {
            Expression<String> column = path(root, key.substring(0, key.length() - 1))
                    .as(String.class);
            String pattern = "%" + escapeLike(value.toString().toLowerCase(Locale.ROOT)) + "%";
            return cb.like(cb.lower(column), pattern, '\\');
        }
        Path<?> column = path(root, key);
        return cb.equal(column, value);
    }

    private static String escapeLike(String text) {
        return text.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
    private static Path<?> path(Path<?> root, String property) {
        Path<?> p = root;
        for (String part : property.split("\\.")) { //чтобы у location.x, можно было взять x
            p = p.get(part);
        }
        return p;
    }
}
