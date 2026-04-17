package com.reviq.shared.search.enums;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public enum FieldType {
    BOOLEAN {
        public Object parse(String value) {
            return Boolean.valueOf(value);
        }
    },
    CHAR {
        public Object parse(String value) {
            return value.charAt(0);
        }
    },
    DATE {
        public Object parse(String value) {
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception ignored) {}
            try {
                return java.time.LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            } catch (Exception e) {
                log.info("Failed to parse DATE: {}", e.getMessage());
            }
            return null;
        }
    },
    INSTANT {
        public Object parse(String value) {
            try {
                return Instant.parse(value);
            } catch (Exception e) {
                log.info("Failed to parse INSTANT: {}", e.getMessage());
            }
            return null;
        }
    },
    DOUBLE {
        public Object parse(String value) {
            return Double.valueOf(value);
        }
    },
    INTEGER {
        public Object parse(String value) {
            return Integer.valueOf(value);
        }
    },
    LONG {
        public Object parse(String value) {
            return Long.valueOf(value);
        }
    },
    STRING {
        public Object parse(String value) {
            return value;
        }
    },
    UUID {
        public Object parse(String value) {
            return value;
        }
    },
    ENUM_STRING {
        public Object parse(String value) {
            return value;
        }
    };

    public abstract Object parse(String value);
}
