package com.acme.rabbit.processors;

public interface EventProcessor<T> extends EventProcessing<T>, FailureProcessing<T> {
}