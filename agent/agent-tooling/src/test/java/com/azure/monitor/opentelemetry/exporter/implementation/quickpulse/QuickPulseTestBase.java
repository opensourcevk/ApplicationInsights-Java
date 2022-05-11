/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.TelemetryUtil.getExceptions;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.FluxUtil;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.ExceptionTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.RemoteDependencyTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.RequestTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.FormattedDuration;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.FormattedTime;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;
import reactor.core.publisher.Mono;

public class QuickPulseTestBase extends TestBase {
  private static final String APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE =
      "https://monitor.azure.com//.default";

  HttpPipeline getHttpPipelineWithAuthentication() {
    if (getTestMode() == TestMode.RECORD || getTestMode() == TestMode.LIVE) {
      TokenCredential credential =
          new ClientSecretCredentialBuilder()
              .tenantId(System.getenv("AZURE_TENANT_ID"))
              .clientSecret(System.getenv("AZURE_CLIENT_SECRET"))
              .clientId(System.getenv("AZURE_CLIENT_ID"))
              .build();
      return getHttpPipeline(
          new BearerTokenAuthenticationPolicy(
              credential, APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE));
    } else {
      return getHttpPipeline();
    }
  }

  HttpPipeline getHttpPipeline(HttpPipelinePolicy... policies) {
    HttpClient httpClient;
    if (getTestMode() == TestMode.RECORD || getTestMode() == TestMode.LIVE) {
      httpClient = HttpClient.createDefault();
    } else {
      httpClient = interceptorManager.getPlaybackClient();
    }
    List<HttpPipelinePolicy> allPolicies = new ArrayList<>();
    allPolicies.add(interceptorManager.getRecordPolicy());
    allPolicies.addAll(Arrays.asList(policies));
    return new HttpPipelineBuilder()
        .httpClient(httpClient)
        .policies(allPolicies.toArray(new HttpPipelinePolicy[0]))
        .build();
  }

  public static TelemetryItem createRequestTelemetry(
      String name, Date timestamp, long durationMillis, String responseCode, boolean success) {
    RequestTelemetryBuilder telemetryBuilder = RequestTelemetryBuilder.create();
    telemetryBuilder.addProperty("customProperty", "customValue");
    telemetryBuilder.setName(name);
    telemetryBuilder.setDuration(FormattedDuration.fromNanos(MILLISECONDS.toNanos(durationMillis)));
    telemetryBuilder.setResponseCode(responseCode);
    telemetryBuilder.setSuccess(success);
    telemetryBuilder.setUrl("foo");
    telemetryBuilder.setTime(FormattedTime.offSetDateTimeFromEpochMillis(timestamp.getTime()));
    return telemetryBuilder.build();
  }

  public static TelemetryItem createRemoteDependencyTelemetry(
      String name, String command, long durationMillis, boolean success) {
    RemoteDependencyTelemetryBuilder telemetryBuilder = RemoteDependencyTelemetryBuilder.create();
    telemetryBuilder.addProperty("customProperty", "customValue");
    telemetryBuilder.setName(name);
    telemetryBuilder.setData(command);
    telemetryBuilder.setDuration(FormattedDuration.fromNanos(MILLISECONDS.toNanos(durationMillis)));
    telemetryBuilder.setSuccess(success);
    return telemetryBuilder.build();
  }

  public static TelemetryItem createExceptionTelemetry(Exception exception) {
    ExceptionTelemetryBuilder telemetryBuilder = ExceptionTelemetryBuilder.create();
    telemetryBuilder.setExceptions(getExceptions(exception));
    return telemetryBuilder.build();
  }

  static class ValidationPolicy implements HttpPipelinePolicy {

    private final CountDownLatch countDown;
    private final String expectedRequestBody;

    ValidationPolicy(CountDownLatch countDown, String expectedRequestBody) {
      this.countDown = countDown;
      this.expectedRequestBody = expectedRequestBody;
    }

    @Override
    public Mono<HttpResponse> process(
        HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
      Mono<String> asyncString =
          FluxUtil.collectBytesInByteBufferStream(context.getHttpRequest().getBody())
              .map(bytes -> new String(bytes, StandardCharsets.UTF_8));
      asyncString.subscribe(
          value -> {
            if (Pattern.matches(expectedRequestBody, value)) {
              countDown.countDown();
            }
          });
      return next.process();
    }
  }
}