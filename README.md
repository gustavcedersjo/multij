# MultiJ #
MultiJ provides multi-methods to Java.

## Example ##
Multi-methods are defined as methods in a Java interface with the annotation `@Module`.
```java
@Module
interface Example {
    default int test(Object o) {
        return 1;
    }
    default int test(String s) {
        return 2;
    }
}
```
The multi-method `test` is invoked by first creating an instance of the module and then invoking any of the `test` methods on the instance. MultiJ will select the most specific method with respect to the runtime types of the arguments.
```java
Example example = MultiJ.instance(Example.class);
Object o = "hello";
int x = example.test(o); // x = 2
```
Doing the same thing on a regular Java object would not generate the same result.
```java
Example example = new Example() {};
Object o = "hello";
int x = example.test(o); // x = 1
```