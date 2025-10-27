package com.snow.di;

import com.snow.util.Lifetime;

public record ComponentDefinition(Class<?> type, Lifetime lifetime) {}
