package ch.admin.bit.jeap.jme.cdct.consumer.infrastructure.task;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Note: The consumer does not require all fields from the provider's tasks. Only the id, title, and content
 * are included in the consumer's tasks, while additional fields such as tag and createdAt from the provider
 * are ignored. Therefore, when defining pacts for the provider, this consumer will disregard those extra fields.
 */
@Data
@NoArgsConstructor
public class Task {

    @NonNull
    private String id;

    @NonNull
    private String title;

    @NonNull
    private String content;

}
