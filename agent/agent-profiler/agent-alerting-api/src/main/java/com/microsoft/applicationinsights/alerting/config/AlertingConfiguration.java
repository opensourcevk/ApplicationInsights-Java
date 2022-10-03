// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.applicationinsights.alerting.config;

import com.microsoft.applicationinsights.alerting.aiconfig.AlertingConfig;
import java.util.Objects;
import javax.annotation.Nullable;

/** Contains the overall configuration of the entire alerting subsystem. */
public class AlertingConfiguration {

  // Alert configuration for CPU telemetry
  private final AlertConfiguration cpuAlert;

  // Alert configuration for MEMORY telemetry
  private final AlertConfiguration memoryAlert;

  // Alert configuration for the periodic profiling
  private final DefaultConfiguration defaultConfiguration;

  // Alert configuration for manual profiling
  private final CollectionPlanConfiguration collectionPlanConfiguration;

  public AlertingConfiguration(
      AlertConfiguration cpuAlert,
      AlertConfiguration memoryAlert,
      DefaultConfiguration defaultConfiguration,
      CollectionPlanConfiguration collectionPlanConfiguration) {
    this.cpuAlert = cpuAlert;
    this.memoryAlert = memoryAlert;
    this.defaultConfiguration = defaultConfiguration;
    this.collectionPlanConfiguration = collectionPlanConfiguration;
  }

  public DefaultConfiguration getDefaultConfiguration() {
    return defaultConfiguration;
  }

  public AlertConfiguration getCpuAlert() {
    return cpuAlert;
  }

  public AlertConfiguration getMemoryAlert() {
    return memoryAlert;
  }

  public CollectionPlanConfiguration getCollectionPlanConfiguration() {
    return collectionPlanConfiguration;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof AlertingConfiguration)) {
      return false;
    }
    AlertingConfiguration that = (AlertingConfiguration) obj;
    return Objects.equals(cpuAlert, that.cpuAlert)
        && Objects.equals(memoryAlert, that.memoryAlert)
        && Objects.equals(defaultConfiguration, that.defaultConfiguration)
        && Objects.equals(collectionPlanConfiguration, that.collectionPlanConfiguration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cpuAlert, memoryAlert, defaultConfiguration, collectionPlanConfiguration);
  }

  /** Alert configuration for a given telemetry type. */
  public static class AlertConfiguration {

    private final AlertMetricType type;
    private final boolean enabled;
    private final float threshold;
    private final long profileDuration;
    private final long cooldown;

    @Nullable private final AlertingConfig.RequestTrigger requestTrigger;

    public AlertConfiguration(
        AlertMetricType type,
        boolean enabled,
        float threshold,
        long profileDuration,
        long cooldown) {
      this.type = type;
      this.enabled = enabled;
      this.threshold = threshold;
      this.profileDuration = profileDuration;
      this.cooldown = cooldown;
      this.requestTrigger = null;
    }

    public AlertConfiguration(
        AlertMetricType type,
        boolean enabled,
        float threshold,
        long profileDuration,
        long cooldown,
        @Nullable AlertingConfig.RequestTrigger requestTrigger) {
      this.type = type;
      this.enabled = enabled;
      this.threshold = threshold;
      this.profileDuration = profileDuration;
      this.cooldown = cooldown;
      this.requestTrigger = requestTrigger;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public float getThreshold() {
      return threshold;
    }

    public long getProfileDuration() {
      return profileDuration;
    }

    public long getCooldown() {
      return cooldown;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof AlertConfiguration)) {
        return false;
      }
      AlertConfiguration that = (AlertConfiguration) obj;
      return enabled == that.enabled
          && Float.compare(that.threshold, threshold) == 0
          && profileDuration == that.profileDuration
          && cooldown == that.cooldown;
    }

    @Override
    public int hashCode() {
      return Objects.hash(enabled, threshold, profileDuration, cooldown);
    }

    public AlertMetricType getType() {
      return type;
    }

    @Override
    public String toString() {
      return "AlertConfiguration{"
          + "type="
          + type
          + ", enabled="
          + enabled
          + ", threshold="
          + threshold
          + ", profileDuration="
          + profileDuration
          + ", cooldown="
          + cooldown
          + '}';
    }

    public AlertingConfig.RequestTrigger getRequestTrigger() {
      return requestTrigger;
    }
  }

  public static class AlertConfigurationBuilder {
    private boolean enabled;
    private float threshold;
    private long profileDuration;
    private long cooldown;
    private AlertMetricType type;

    public AlertConfigurationBuilder setEnabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public AlertConfigurationBuilder setThreshold(float threshold) {
      this.threshold = threshold;
      return this;
    }

    public AlertConfigurationBuilder setProfileDuration(long profileDuration) {
      this.profileDuration = profileDuration;
      return this;
    }

    public AlertConfigurationBuilder setCooldown(long cooldown) {
      this.cooldown = cooldown;
      return this;
    }

    public AlertConfigurationBuilder setType(AlertMetricType type) {
      this.type = type;
      return this;
    }

    public AlertConfiguration createAlertConfiguration() {
      return new AlertConfiguration(type, enabled, threshold, profileDuration, cooldown);
    }
  }
}
