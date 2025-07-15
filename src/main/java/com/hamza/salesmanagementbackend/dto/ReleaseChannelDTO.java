package com.hamza.salesmanagementbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for release channel information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseChannelDTO {

    @JsonProperty("channel")
    private ReleaseChannel channel;

    @JsonProperty("latestVersion")
    private String latestVersion;

    @JsonProperty("description")
    private String description;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("subscriberCount")
    private Long subscriberCount;

    @JsonProperty("releaseFrequency")
    private String releaseFrequency;

    @JsonProperty("stabilityLevel")
    private StabilityLevel stabilityLevel;

    @JsonProperty("lastReleaseDate")
    private LocalDateTime lastReleaseDate;

    @JsonProperty("nextExpectedRelease")
    private LocalDateTime nextExpectedRelease;

    @JsonProperty("autoUpdateEnabled")
    private Boolean autoUpdateEnabled;

    @JsonProperty("requiresApproval")
    private Boolean requiresApproval;

    @JsonProperty("minimumClientVersion")
    private String minimumClientVersion;

    /**
     * Release channel types
     */
    public enum ReleaseChannel {
        STABLE("stable", "Stable releases with thorough testing"),
        BETA("beta", "Beta releases for testing new features"),
        NIGHTLY("nightly", "Nightly builds with latest changes"),
        LTS("lts", "Long-term support releases"),
        HOTFIX("hotfix", "Critical hotfix releases");

        private final String channelName;
        private final String description;

        ReleaseChannel(String channelName, String description) {
            this.channelName = channelName;
            this.description = description;
        }

        public String getChannelName() {
            return channelName;
        }

        public String getDescription() {
            return description;
        }

        @JsonProperty("channelName")
        public String getChannelNameJson() {
            return channelName;
        }

        @JsonProperty("channelDescription")
        public String getChannelDescriptionJson() {
            return description;
        }
    }

    /**
     * Stability levels
     */
    public enum StabilityLevel {
        EXPERIMENTAL(1, "Experimental - may contain bugs"),
        ALPHA(2, "Alpha - early testing phase"),
        BETA(3, "Beta - feature complete, testing"),
        RC(4, "Release Candidate - near production ready"),
        STABLE(5, "Stable - production ready"),
        LTS(6, "Long Term Support - extended stability");

        private final int level;
        private final String description;

        StabilityLevel(int level, String description) {
            this.level = level;
            this.description = description;
        }

        public int getLevel() {
            return level;
        }

        public String getDescription() {
            return description;
        }

        @JsonProperty("stabilityLevel")
        public int getStabilityLevelJson() {
            return level;
        }

        @JsonProperty("stabilityDescription")
        public String getStabilityDescriptionJson() {
            return description;
        }
    }

    /**
     * Get channel display name
     */
    public String getChannelDisplayName() {
        return channel != null ? channel.getChannelName().toUpperCase() : "UNKNOWN";
    }

    /**
     * Get stability description
     */
    public String getStabilityDescription() {
        return stabilityLevel != null ? stabilityLevel.getDescription() : "Unknown stability";
    }

    /**
     * Check if channel is production ready
     */
    public boolean isProductionReady() {
        return stabilityLevel != null && 
               (stabilityLevel == StabilityLevel.STABLE || stabilityLevel == StabilityLevel.LTS);
    }

    /**
     * Check if channel requires manual approval
     */
    public boolean requiresManualApproval() {
        return requiresApproval != null && requiresApproval;
    }
}
