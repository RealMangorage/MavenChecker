package org.mangorage.mavenchecker.core;

/**
 * When do you want to trigger the {@link org.mangorage.mavenchecker.core.webhook.Webhook}
 *
 * Currently just NEW_ARTIFACTS triggers when ever a new artifact is published
 * E.G triggers if org.mangorage:example:1.0.1 is published
 */
public enum ActionType {
    NEW_ARTIFACTS,
    NONE
}
