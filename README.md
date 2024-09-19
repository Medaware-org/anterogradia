## Medaware&trade; Anterogradia

A custom markup system for the Medaware platform

## Example Program

```js
progn {
   "guest" := random {"Braun", "Merkel"},
   sequence {
      "Hello, ",
      if (&"guest" = "Braun") {
         "Mr "
      } else {
         "Mrs. "
      },
      &"guest", "!"
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

The `invokeCompiler` method takes an optional `parameters` argument, which is a `HashMap<String, String>` of
startup parameters that will be accessible to the script at runtime.

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

## Importing Libraries
Additional libs can be imported from within the script using the following syntax:
```
@library "path.to.library.Clazz" as identifier 
```
Where `identifier` is the prefix to be used when referring to the library's functions.
Note that the import directives must be placed at the very top of the file and must occur
consecutively after each-other, i.e.
```js
@library "org.medaware.anterogradia.libs.HTML" as h
@library "org.medaware.anterogradia.libs.MedawareDesignToolkit" as mdk
@library "org.medaware.anterogradia.libs.FormattingLib" as fmt

sequence {
    ...
```

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

### _if

> :warning: *Superseded by the if construct*

| _if                                                             | cond                                                  | then       | else       |
|-----------------------------------------------------------------|-------------------------------------------------------|------------|------------|
| Evaluates 'then' if 'cond' is true, otherwise evaluates 'else'. | "true" or "yes" for true. Otherwise considered false. | Expression | Expression |

---

### equal

> :warning: *Syntax binding available*

| equal                                                 | a          | b          |
|-------------------------------------------------------|------------|------------|
| Evaluates to "true" if 'a' == 'b'. Otherwise "false". | Expression | Expression |

---

### param

| param                                | key           |
|--------------------------------------|---------------|
| Retrieve a runtime startup parameter | Parameter key |

---

### set

> :warning: *Syntax binding available*

| set                            | key           | value            |
|--------------------------------|---------------|------------------|
| Sets or creates a new variable | variable name | value expression |

---

### get

> :warning: *Syntax binding available*

| get                                         | key           |
|---------------------------------------------|---------------|
| Retrieves the value of an existing variable | variable name |

---

### astd

| astd                                                  | expr       |
|-------------------------------------------------------|------------|
| Generate valid ANTG source string from the expression | Expression |

## Syntax bindings

To improve developer experience, some functions or expressions can be written in the form of dedicated syntactical entities.
Despite not looking like it, syntactical bindings still utilize the function infrastructure of ANTG and thus evaluate to function
calls. Think of them as glorified preprocessors. Below is a list of all such bindings available as of writing this entry.

### Conditional Statement
Old function form
```
if (condition, then-block, else-block)
```

can ba rewritten as a construct
```
if ( condition ) { then-block } else { else-block }
```

The original `if` function has been renamed to `_if`.

### Comparison
Function form
```
equal ( left, right )
```
has an alternate form of
```
left = right
```

### Setting a variable
Function form
```
set (key, value)
```
Syntax binding
```
key := value
```

### Retrieving a variable
Function form
```
get (key)
```
Syntax binding
```
& key
```
