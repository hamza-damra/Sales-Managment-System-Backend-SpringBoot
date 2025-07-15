package com.hamza.salesmanagementbackend.entity;

/**
 * Enum representing different release channels for application versions
 */
public enum ReleaseChannel {
    STABLE("stable", "Stable releases with thorough testing", 5),
    BETA("beta", "Beta releases for testing new features", 3),
    NIGHTLY("nightly", "Nightly builds with latest changes", 1),
    LTS("lts", "Long-term support releases", 6),
    HOTFIX("hotfix", "Critical hotfix releases", 4);

    private final String channelName;
    private final String description;
    private final int stabilityLevel;

    ReleaseChannel(String channelName, String description, int stabilityLevel) {
        this.channelName = channelName;
        this.description = description;
        this.stabilityLevel = stabilityLevel;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getDescription() {
        return description;
    }

    public int getStabilityLevel() {
        return stabilityLevel;
    }

    /**
     * Get channel by name (case-insensitive)
     */
    public static ReleaseChannel fromString(String channelName) {
        if (channelName == null) {
            return STABLE; // Default to stable
        }
        
        for (ReleaseChannel channel : values()) {
            if (channel.channelName.equalsIgnoreCase(channelName)) {
                return channel;
            }
        }
        
        return STABLE; // Default to stable if not found
    }

    /**
     * Check if this channel is production ready
     */
    public boolean isProductionReady() {
        return this == STABLE || this == LTS || this == HOTFIX;
    }

    /**
     * Check if this channel requires approval for auto-updates
     */
    public boolean requiresApproval() {
        return this == NIGHTLY || this == BETA;
    }
}
