package com.snow.di;

public record ComponentDefinition(Class<?> type, boolean singleton) {
    public Class<?> getType() {
        return type;
    }
    public boolean isSingleton() {
        return singleton;
    }
}
