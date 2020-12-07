package com.acme.rabbit.processors.sample;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class FirstTypeMessage extends SampleTestMessage {
  @Nullable
  Integer value;
}
