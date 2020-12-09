package com.acme.models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Burger extends Order {
  @Nullable
  Integer calories;
}
