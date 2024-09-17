## Medaware&trade; Anterogradia

A markup system for the Medaware platform

## Example Program
```js
@library "org.medaware.anterogradia.libs.Memory"

progn {
    mem:set(key = "guest",
            value = random {
                "Braun",
                "Merkel"
            }),

    sequence {
        "Hello, ",
        
        if (cond    =   equal(  a = mem:get(key = "guest"),
                                b = "Braun"),
            then    =   "Mr. ",
            else    =   "Mrs. "),
            
        mem:get(key = "guest")
    }
}
```

## Basic Concepts

1) All data types are strings. Integer literals are syntactically allowed,
   but are treated indifferently from a regular string literal.

2) There are just two types of expressions: **String literals** and **Function calls**.

3) Function calls reflectively invoke functions implemented in a Kotlin "library". Every one of these
   functions returns a String, yet they accept Nodes as parameters. Therefore, functions have flow control
   capabilities. (Take a look at `if`, for example)

4) The "program" is always based off of a single expression, such that in the end, from the compiler's point of view,
   there is a single root node to evaluate. Functions such as `sequence` and `progn` are useful in such cases.

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