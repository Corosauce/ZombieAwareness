package net.minecraft.src;
//Uncomment that line if you use MCP.

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MLProp2 {
String name() default "";

String info() default "";

double min() default Double.NEGATIVE_INFINITY;

double max() default Double.POSITIVE_INFINITY;

    int test = 0;
}