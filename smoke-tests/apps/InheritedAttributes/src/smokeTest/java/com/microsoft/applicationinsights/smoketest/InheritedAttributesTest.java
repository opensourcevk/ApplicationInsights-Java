// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.applicationinsights.smoketest;

import static com.microsoft.applicationinsights.smoketest.EnvironmentValue.JAVA_11;
import static com.microsoft.applicationinsights.smoketest.EnvironmentValue.JAVA_11_OPENJ9;
import static com.microsoft.applicationinsights.smoketest.EnvironmentValue.JAVA_17;
import static com.microsoft.applicationinsights.smoketest.EnvironmentValue.JAVA_17_OPENJ9;
import static com.microsoft.applicationinsights.smoketest.EnvironmentValue.JAVA_21;
import static com.microsoft.applicationinsights.smoketest.EnvironmentValue.JAVA_21_OPENJ9;
import static com.microsoft.applicationinsights.smoketest.EnvironmentValue.JAVA_8;
import static com.microsoft.applicationinsights.smoketest.EnvironmentValue.JAVA_8_OPENJ9;
import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.applicationinsights.smoketest.schemav2.Data;
import com.microsoft.applicationinsights.smoketest.schemav2.Envelope;
import com.microsoft.applicationinsights.smoketest.schemav2.MessageData;
import com.microsoft.applicationinsights.smoketest.schemav2.RequestData;
import com.microsoft.applicationinsights.smoketest.schemav2.SeverityLevel;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@UseAgent
abstract class InheritedAttributesTest {

  @RegisterExtension static final SmokeTestExtension testing = SmokeTestExtension.create();

  @Test
  @TargetUri("/test")
  void test() throws Exception {
    List<Envelope> rdList = testing.mockedIngestion.waitForItems("RequestData", 1);

    Envelope rdEnvelope = rdList.get(0);
    String operationId = rdEnvelope.getTags().get("ai.operation.id");
    List<Envelope> mdList = testing.mockedIngestion.waitForMessageItemsInRequest(1, operationId);

    Envelope mdEnvelope = mdList.get(0);

    assertThat(rdEnvelope.getSampleRate()).isNull();
    assertThat(mdEnvelope.getSampleRate()).isNull();

    RequestData rd = (RequestData) ((Data<?>) rdEnvelope.getData()).getBaseData();
    MessageData md = (MessageData) ((Data<?>) mdEnvelope.getData()).getBaseData();

    assertThat(rd.getName()).isEqualTo("GET /test");
    assertThat(rd.getResponseCode()).isEqualTo("200");
    assertThat(rd.getProperties()).containsEntry("tenant", "z");
    assertThat(rd.getProperties()).hasSize(2);
    assertThat(rd.getProperties()).containsEntry("_MS.ProcessedByMetricExtractors", "True");
    assertThat(rd.getSuccess()).isTrue();

    assertThat(md.getMessage()).isEqualTo("hello");
    assertThat(md.getSeverityLevel()).isEqualTo(SeverityLevel.INFORMATION);
    assertThat(md.getProperties()).containsEntry("SourceType", "Logger");
    assertThat(md.getProperties()).containsEntry("LoggerName", "smoketestapp");
    assertThat(md.getProperties()).containsKey("ThreadName");
    assertThat(md.getProperties()).containsEntry("tenant", "z");
    assertThat(md.getProperties()).hasSize(4);
  }

  @Environment(JAVA_8)
  static class Java8Test extends InheritedAttributesTest {}

  @Environment(JAVA_8_OPENJ9)
  static class Java8OpenJ9Test extends InheritedAttributesTest {}

  @Environment(JAVA_11)
  static class Java11Test extends InheritedAttributesTest {}

  @Environment(JAVA_11_OPENJ9)
  static class Java11OpenJ9Test extends InheritedAttributesTest {}

  @Environment(JAVA_17)
  static class Java17Test extends InheritedAttributesTest {}

  @Environment(JAVA_17_OPENJ9)
  static class Java17OpenJ9Test extends InheritedAttributesTest {}

  @Environment(JAVA_21)
  static class Java21Test extends InheritedAttributesTest {}

  @Environment(JAVA_21_OPENJ9)
  static class Java21OpenJ9Test extends InheritedAttributesTest {}
}
