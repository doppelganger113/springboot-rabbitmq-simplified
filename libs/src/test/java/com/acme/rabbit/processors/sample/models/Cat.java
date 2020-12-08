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
public class Cat extends Animal {
  @Nullable
  int age;

  public Cat(String name, int age) {
    super(name);
    this.age = age;
  }
}
