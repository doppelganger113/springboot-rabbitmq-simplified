package com.acme.rabbit.processors.sample.models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Dog extends Animal {
  @Nullable
  String breed;

  public Dog(String name, String breed) {
    super(name);
    this.breed = breed;
  }
}
