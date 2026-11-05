package se.ifmo.ru.repository;

import io.github.timur0o31.soa.log.RequestTrace;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class MeasuredConnectionProvider extends DatasourceConnectionProviderImpl {
    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        return proxy(Connection.class, (wrapper, method, args) -> {
            Object result = invoke(connection, method, args);
            String sql = args != null && args.length > 0 && args[0] instanceof String text ? text : "";
            if (result instanceof CallableStatement call) return measured(CallableStatement.class, call, sql);
            if (result instanceof PreparedStatement prepared) return measured(PreparedStatement.class, prepared, sql);
            if (result instanceof Statement statement) return measured(Statement.class, statement, sql);
            return result;
        });
    }

    private static <T extends Statement> T measured(Class<T> type, T statement, String sql) {
        return proxy(type, (wrapper, method, args) -> {
            if (!method.getName().startsWith("execute")) return invoke(statement, method, args);
            long start = System.nanoTime();
            boolean success = false;
            try {
                Object result = invoke(statement, method, args);
                success = true;
                return result;
            } finally {
                String query = args != null && args.length > 0 && args[0] instanceof String text ? text : sql;
                RequestTrace.sql((System.nanoTime() - start) / 1_000, success, fingerprint(query));
            }
        });
    }

    private static String fingerprint(String sql) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(sql.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException failure) {
            throw new IllegalStateException(failure);
        }
    }

    private static Object invoke(Object target, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
