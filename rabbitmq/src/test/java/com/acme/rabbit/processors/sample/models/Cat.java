package com.acme.rabbit.processors.sample.models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Cat extends Animal {
  @NotNull
  @Max(2)
  int age;

  public Cat(String name, int age) {
    super(name);
    this.age = age;
  }
}
