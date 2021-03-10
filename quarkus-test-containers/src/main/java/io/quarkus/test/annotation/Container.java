package io.quarkus.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.quarkus.test.ManagedResourceBuilder;
import io.quarkus.test.containers.ContainerManagedResourceBuilder;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Container {
	public String image();

	public int port();

	public String expectedLog();

	public String command() default "";

	Class<? extends ManagedResourceBuilder> builder() default ContainerManagedResourceBuilder.class;
}