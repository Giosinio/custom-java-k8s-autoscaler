package com.csubb.dissertation.customautoscaler.util;

import jakarta.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Function;

@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Util {

    public static String formatNullableDouble(@Nullable Double value, Function<Double, Double> convertFunction) {
        return Objects.isNull(value) ? "null" : String.format("%.2f", convertFunction.apply(value));
    }
}
