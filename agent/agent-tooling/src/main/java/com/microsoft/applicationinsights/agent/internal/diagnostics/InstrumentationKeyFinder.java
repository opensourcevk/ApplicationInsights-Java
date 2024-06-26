// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.applicationinsights.agent.internal.diagnostics;

import java.util.function.Function;

public class InstrumentationKeyFinder implements DiagnosticsValueFinder {

  private static final String PREFIX = "InstrumentationKey=";

  @Override
  public String getName() {
    return "ikey";
  }

  @Override
  public String getValue(Function<String, String> envVarsFunction) {
    String connStr = envVarsFunction.apply("APPLICATIONINSIGHTS_CONNECTION_STRING");
    if (connStr != null && !connStr.isEmpty()) {
      String[] parts = connStr.split(";");
      String instrumentationKey = null;
      for (String part : parts) {
        String trimmed = part.trim();
        if (trimmed.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) {
          instrumentationKey = trimmed.substring(PREFIX.length());
        }
      }
      // return the last instrumentation key to match ConnectionString::parseInto
      return instrumentationKey;
    }
    return envVarsFunction.apply("APPINSIGHTS_INSTRUMENTATIONKEY");
  }
}
