package com.github.lwhite1.tablesaw.api;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * ParsingConfiguration is used to configure the parsing of a
 * CSV file, allowing options such as the delimiter and presence
 * or not of a header to be specified.
 *
 * It can be constructed using the Builder, accessible via the
 * static method newBuilder().
 *
 * By default the configuration does not have a header, and is
 * delimited by a comma.
 *
 * Column types can be overridden by using the column() method
 * and then invoking the isOfType() method.
 *
 * If no name has been specified, then the file name is used to
 * name the table.
 */
public final class ParsingConfiguration {
  /**
   * Columns whose types have been explicitly declared. The types
   * listed here override any type deduced during parsing.
   */
  private final ImmutableMap<String, ColumnType> overriddenColumnTypes;

  /**
   * Columns whose types are one of the Date/DateTime types and whose
   * format has been forced. The formats listed here override any format
   * deduced during parsing.
   */
  private final ImmutableMap<String, DateTimeFormatter> overriddenColumnTypesDateFormats;
  private final String tableName;
  private final String fileName;
  private final boolean header;
  private final char delimiter;
  private final ColumnType[] columnTypes;
  private final InputStream stream;

  private ParsingConfiguration(ImmutableMap<String, ColumnType> overriddenColumnTypes, String tableName, String fileName, boolean header, char delimiter, ImmutableMap<String, DateTimeFormatter> overriddenColumnTypesDateFormats, ColumnType[] columnTypes, InputStream stream) {
    this.overriddenColumnTypes = overriddenColumnTypes;
    this.overriddenColumnTypesDateFormats = overriddenColumnTypesDateFormats;
    this.tableName = tableName;
    this.fileName = fileName;
    this.header = header;
    this.delimiter = delimiter;
    this.columnTypes = columnTypes;
    this.stream = stream;
  }

  /**
   * Builder specifically targetted at modifying a column's configuration
   */
  public static class ColumnParsingConfigurationBuilder {
    private final String columnName;
    private final Builder b;

    private ColumnParsingConfigurationBuilder(String columnName, Builder b) {
      this.columnName = columnName;
      this.b = b;
    }

    /**
     * Sets this column's type
     */
    public Builder isOfType(ColumnType type) {
      b.setColumnType(columnName, type);
      return b;
    }

    /**
     * Sets this column's type and the formatter to use
     */
    public Builder isOfDateFormat(ColumnType columnType, DateTimeFormatter dateTimeFormatter) {
      switch (columnType) {
        case LOCAL_DATE:
        case LOCAL_DATE_TIME:
        case LOCAL_TIME:
          b.setColumnType(columnName, columnType);
          b.setColumnDateType(columnName, dateTimeFormatter);
          break;
        default:
          throw new IllegalArgumentException("isOfDateFormat can only be used with " +
                  "the column types LOCAL_DATE, LOCAL_DATE_TIME, LOCAL_TIME");
      }
      return b;
    }
  }

  public static class Builder {
    private final Map<String, ColumnType> overriddenColumnTypes = new HashMap();
    private final Map<String, DateTimeFormatter> overriddenColumnDateFormats = new HashMap<>();
    private String tableName;
    private String fileName;
    private boolean header = false;
    private char delimiter = ',';
    private ColumnType[] columnTypes = null;
    private InputStream stream;

    public ColumnParsingConfigurationBuilder column(String columnName) {
      return new ColumnParsingConfigurationBuilder(columnName, this);
    }

    /**
     * Sets the name of the table
     */
    public Builder named(String tableName) {
      this.tableName = tableName;
      return this;
    }

    /**
     * Sets the file to parse, in a String giving the fully-qualified
     * path to the file to read
     */
    public Builder fromFile(String fileName) {
      this.fileName = fileName;
      return this;
    }

    /**
     * Indicates that the file contains a header
     */
    public Builder withHeader() {
      this.header = true;
      return this;
    }

    /**
     * Sets the delimiter to use when parsing the file
     */
    public Builder withDelimiter(char delimiter) {
      this.delimiter = delimiter;
      return this;
    }

    /**
     * Creates an instance of the ParsingConfiguration
     * @return
     */
    public ParsingConfiguration build() {
      final ImmutableMap<String, ColumnType> overriddenColumnTypesCopy = ImmutableMap.copyOf(this.overriddenColumnTypes);
      final ImmutableMap<String, DateTimeFormatter> overriddenColumnDateFormatsCopy = ImmutableMap.copyOf(this.overriddenColumnDateFormats);
      final ColumnType[] columnTypesCopy;
      if (columnTypes != null) {
        columnTypesCopy = Arrays.copyOf(columnTypes, columnTypes.length);
      } else {
        columnTypesCopy = new ColumnType[0];
      }
      final String tableNameToUse = Strings.isNullOrEmpty(this.tableName) ? fileName : this.tableName;
      return new ParsingConfiguration(overriddenColumnTypesCopy,
                                      tableNameToUse,
                                      fileName,
                                      header,
                                      delimiter,
                                      overriddenColumnDateFormatsCopy,
                                      columnTypesCopy,
                                      stream);

    }

    public Builder setColumnTypes(ColumnType[] columnTypes) {
      this.columnTypes = columnTypes;
      return this;
    }

    public Builder setStream(InputStream stream) {
      this.stream = stream;
      return this;
    }

    /**
     * This method is called by the ColumnParsingConfigurationBuilder
     */
    private void setColumnDateType(String columnName, DateTimeFormatter dateTimeFormatter) {
      this.overriddenColumnDateFormats.put(columnName, dateTimeFormatter);
    }

    /**
     * This method is called by the ColumnParsingConfigurationBuilder
     */
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

  public boolean hasHeader() {
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

  public boolean hasColumnTypes() {
    return columnTypes != null && columnTypes.length > 0;
  }

  public ColumnType[] getColumnTypes() {
    return Arrays.copyOf(columnTypes, columnTypes.length);
  }

  public ImmutableMap<String, DateTimeFormatter> getOverriddenColumnTypesDateFormats() {
    return overriddenColumnTypesDateFormats;
  }

  public boolean hasStream() {
    return stream != null;
  }

  public InputStream getStream() {
    return stream;
  }


  /**
   * Creates a copy of the ParsingConfiguration, but for a different file.
   *
   * The name of the file is also used to name the table.
   *
   * @param name the fully-qualified path to the file to read
   */
  public ParsingConfiguration cloneWithNameFile(String name) {
    return new ParsingConfiguration(overriddenColumnTypes, name, name, header, delimiter, overriddenColumnTypesDateFormats, columnTypes, stream);
  }
}
