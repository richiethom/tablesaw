package com.github.lwhite1.tablesaw.api;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Richard on 22/07/2016.
 */
public final class ParsingConfiguration {

    private final ImmutableMap<String, ColumnType> overriddenColumnTypes;
    private final String tableName;
    private final String fileName;
    private final boolean header;
    private final char delimiter;

    private ParsingConfiguration(ImmutableMap<String, ColumnType> overriddenColumnTypes, String tableName, String fileName, boolean header, char delimiter) {
        this.overriddenColumnTypes = overriddenColumnTypes;
        this.tableName = tableName;
        this.fileName = fileName;
        this.header = header;
        this.delimiter = delimiter;
    }

    public static class ColumnParsingConfigurationBuilder {
        private final String columnName;
        private final Builder b;

        private ColumnParsingConfigurationBuilder(String columnName, Builder b) {
            this.columnName = columnName;
            this.b = b;
        }

        public Builder isOfType(ColumnType type) {
            b.setColumnType(columnName, type);
            return b;
        }
    }

    public static class Builder {
        private final Map<String, ColumnType> overriddenColumnTypes = new HashMap();
        private String tableName;
        private String fileName;
        private boolean header = true;
        private char delimiter = ',';

        public ColumnParsingConfigurationBuilder column(String columnName) {
            return new ColumnParsingConfigurationBuilder(columnName, this);
        }

        public Builder named(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder fromFile(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder withHeader() {
            this.header = true;
            return this;
        }

        public Builder withoutHeader() {
            this.header = false;
            return this;
        }

        public Builder withDelimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public ParsingConfiguration build() {
            return new ParsingConfiguration(ImmutableMap.copyOf(overriddenColumnTypes), Strings.isNullOrEmpty(tableName) ? fileName : tableName, fileName, header, delimiter);
        }

        private void setColumnType(String columnName, ColumnType type) {
            this.overriddenColumnTypes.put(columnName, type);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isHeader() {
        return header;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public String getTableName() {
        return tableName;
    }

    public ImmutableMap<String, ColumnType> getOverriddenColumnTypes() {
        return overriddenColumnTypes;
    }
}
