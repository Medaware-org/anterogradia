## Medaware&trade; Anterogradia

A markup system for the Medaware platform

## Example Program

```js
@library
"org.medaware.anterogradia.libs.Memory"

progn
{
    mem:set(key = "guest",
        value = random
    {
        "Braun",
            "Merkel"
    }
),

    sequence
    {
        "Hello, ",

        if (cond = equal(a = mem:
        get(key = "guest"),
            b = "Braun"
    ),
        then = "Mr. ",
    else
        = "Mrs. "
    ),

        mem:get(key = "guest")
    }
}
```

## Kotlin API

The minimalistic Kotlin API is based around a single function that returns an `AnterogradiaResult` object.
This object can be directly processed via the `use` function, which exposes all of its components:

| input          | output          | except       |
|----------------|-----------------|--------------|
| The input code | Compiled output | `Exception?` |

```kotlin
Anterogradia.invokeCompiler(input).use { input, output, except ->
    /* Process the result */
}
```

## Working with Anterogradia - Basic Concepts

1) All data types are strings. Integer literals are syntactically allowed,
   but are treated indifferently from a regular string literal.

2) There are just two types of expressions: **String literals** and **Function calls**.

3) Function calls reflectively invoke functions implemented in a Kotlin "library". Every one of these
   functions returns a String, yet they accept Nodes as parameters. Therefore, functions have flow control
   capabilities. (Take a look at `if`, for example).

4) The "program" is always based off of a single expression, such that in the end, from the compiler's point of view,
   there is a single root node to evaluate. Functions such as `sequence` and `progn` are useful in such cases.

5) At the top of the program, before the aforementioned expression, is the right place for `@library` directives. These
   are used by the runtime
   to load additional Kotlin libraries. The standard library (`org.medaware.anterogradia.libs.Standard`) is always
   imported by default.

6) A function that is not a part of the standard library must be prefixed with the prefix of the library that it belongs
   to. In the above example,
   the functions `set` and `get` belong to the `Memory` library with the `mem` prefix and must therefore be prefixed
   accordingly. Also, the prefix-free
   namespace is reserved exclusively for the standard library.

7) There are two types of functions: **Variadic** and **Discrete**. The former take an arbitrary number of unnamed
   parameters, discrete functions
   have a fixed number of arguments that must explicitly be assigned by name. Therefore, however, the ordering of the
   arguments does not matter.
   Discrete functions use parentheses `( .. )` and variadic functions use curly brackets `{ .. }` for their bodies (see
   difference between `sequence` and `if`
   in the example above).

## Standard Library

Below is a brief documentation of all the standard lib functions

### about

| about                                        |
|----------------------------------------------|
| Returns basic information about the library. |

---

### sequence

| sequence                                                             | (variadic arguments) |
|----------------------------------------------------------------------|----------------------|
| Evaluate varargs and join the resulting strings of each one of them. | Expressions          |

---

### progn

| progn                                                                           | (variadic arguments) |
|---------------------------------------------------------------------------------|----------------------|
| Evaluate varargs and return the result of the last one. Same as in Common Lisp. | Expressions          |

---

### nothing

| nothing                                                       |
|---------------------------------------------------------------|
| Returns an empty string. Use as a placeholder. No parameters. |

---

### repeat

| repeat                                 | count                     | str              |
|----------------------------------------|---------------------------|------------------|
| Repeats 'str' 'count' amount of times. | Number of times to repeat | String to repeat |

| repeat                                                                            | count                     | str              | separator        |
|-----------------------------------------------------------------------------------|---------------------------|------------------|------------------|
| Repeats 'str' 'count' amount of times. Separates each iteration with 'separator'. | Number of times to repeat | String to repeat | String separator |

---

### random

| random                                                                   | (variadic arguments) |
|--------------------------------------------------------------------------|----------------------|
| randomly evaluates one expression from the variadic list and returns it. | Expressions          |

---

### if

| if                                                                | cond                                                  | then       | else       |
|-------------------------------------------------------------------|-------------------------------------------------------|------------|------------|
| Evaluates 'then' if 'cond' is "true", otherwise evaluates 'else'. | "true" or "yes" for true. Otherwise considered false. | Expression | Expression |

---

### equal

| equal                                                 | a          | b          |
|-------------------------------------------------------|------------|------------|
| Evaluates to "true" if 'a' == 'b'. Otherwise "false". | Expression | Expression |