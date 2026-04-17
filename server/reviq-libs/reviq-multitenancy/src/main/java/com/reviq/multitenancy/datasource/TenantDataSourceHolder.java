package com.reviq.multitenancy.datasource;

import javax.sql.DataSource;

public class TenantDataSourceHolder {

    private static final ThreadLocal<DataSource> CURRENT = new ThreadLocal<>();

    public static void set(DataSource dataSource) {
        CURRENT.set(dataSource);
    }

    public static DataSource get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
