# DataFixerUpper [![Latest release](https://img.shields.io/github/release/Mojang/DataFixerUpper.svg)](https://github.com/Mojang/DataFixerUpper/releases/latest) [![License](https://img.shields.io/github/license/Mojang/DataFixerUpper.svg)](https://github.com/Mojang/DataFixerUpper/blob/master/LICENSE)
A set of utilities designed for incremental building, merging, and optimization of data transformations. Created for converting the game data for Minecraft: Java Edition between different versions of the game.

## Gradle
First include our repository:
```groovy
maven {
    url "https://libraries.minecraft.net"
}
```

And then use this library (change `(the latest version)` to the latest version!):
```groovy
compile 'com.mojang:datafixerupper:(the latest version)'
```

## Maven
First include our repository:
```xml
<repository>
  <id>minecraft-libraries</id>
  <name>Minecraft Libraries</name>
  <url>https://libraries.minecraft.net</url>
</repository>
```

And then use this library (change `(the latest version)` to the latest version!):
```xml
<dependency>
    <groupId>com.mojang</groupId>
    <artifactId>datafixerupper</artifactId>
    <version>(the latest version)</version>
</dependency>
```

# Usage
Core data types are Schema and DataFix. Schema is a set of type definitions specifying what data types the system is interested in and how they relate to each other. DataFix is a rewrite rule between types (see [references](#references) below). DataFixerBuilder takes a list of schemas and fixes converting between those schemas, and creates an optimized converter between the types describes in those schemas. DSL is a class with building blocks used to create schemas and fixes.

# Contributing
Contributions are welcome!

Most contributions will require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to,
and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

# References
## Optimizing functions
  - [Cunha, A., & Pinto, J. S. (2005). Point-free program transformation](https://scholar.google.com/scholar?q=Cunha%2C%20A.%2C%20%26%20Pinto%2C%20J.%20S.%20%282005%29.%20Point-free%20program%20transformation)
  - [Lämmel, R., Visser, E., & Visser, J. (2002). The essence of strategic programming](https://scholar.google.com/scholar?q=L%C3%A4mmel%2C%20R.%2C%20Visser%2C%20E.%2C%20%26%20Visser%2C%20J.%20%282002%29.%20The%20essence%20of%20strategic%20programming)


## How to handle recursive types
  - [Cunha, A., & Pacheco, H. (2011). Algebraic specialization of generic functions for recursive types](https://scholar.google.com/scholar?q=Cunha%2C%20A.%2C%20%26%20Pacheco%2C%20H.%20%282011%29.%20Algebraic%20specialization%20of%20generic%20functions%20for%20recursive%20types)
  - [Yakushev, A. R., Holdermans, S., Löh, A., & Jeuring, J. (2009, August). Generic programming with fixed points for mutually recursive datatypes](https://scholar.google.com/scholar?q=Yakushev%2C%20A.%20R.%2C%20Holdermans%2C%20S.%2C%20L%C3%B6h%2C%20A.%2C%20%26%20Jeuring%2C%20J.%20%282009%2C%20August%29.%20Generic%20programming%20with%20fixed%20points%20for%20mutually%20recursive%20datatypes)
  - [Magalhães, J. P., & Löh, A. (2012). A formal comparison of approaches to datatype-generic programming](https://scholar.google.com/scholar?q=Magalh%C3%A3es%2C%20J.%20P.%2C%20%26%20L%C3%B6h%2C%20A.%20%282012%29.%20A%20formal%20comparison%20of%20approaches%20to%20datatype-generic%20programming)

## Optics
  - [Pickering, M., Gibbons, J., & Wu, N. (2017). Profunctor Optics: Modular Data Accessors](https://scholar.google.com/scholar?q=Pickering%2C%20M.%2C%20Gibbons%2C%20J.%2C%20%26%20Wu%2C%20N.%20%282017%29.%20Profunctor%20Optics%3A%20Modular%20Data%20Accessors)
  - [Pacheco, H., & Cunha, A. (2010, June). Generic point-free lenses](https://scholar.google.com/scholar?q=Pacheco%2C%20H.%2C%20%26%20Cunha%2C%20A.%20%282010%2C%20June%29.%20Generic%20point-free%20lenses)

## Tying it together
  - [Cunha, A., Oliveira, J. N., & Visser, J. (2006, August). Type-safe two-level data transformation](https://scholar.google.com/scholar?q=Cunha%2C%20A.%2C%20Oliveira%2C%20J.%20N.%2C%20%26%20Visser%2C%20J.%20%282006%2C%20August%29.%20Type-safe%20two-level%20data%20transformation)
  - [Cunha, A., & Visser, J. (2011). Transformation of structure-shy programs with application to XPath queries and strategic functions](https://scholar.google.com/scholar?q=Cunha%2C%20A.%2C%20%26%20Visser%2C%20J.%20%282011%29.%20Transformation%20of%20structure-shy%20programs%20with%20application%20to%20XPath%20queries%20and%20strategic%20functions)
  - [Pacheco, H., & Cunha, A. (2011, January). Calculating with lenses: optimising bidirectional transformations](https://scholar.google.com/scholar?q=Pacheco%2C%20H.%2C%20%26%20Cunha%2C%20A.%20%282011%2C%20January%29.%20Calculating%20with%20lenses%3A%20optimising%20bidirectional%20transformations)

![GitHub forks](https://img.shields.io/github/forks/Mojang/DataFixerUpper.svg?style=social&label=Fork) ![GitHub stars](https://img.shields.io/github/stars/Mojang/DataFixerUpper.svg?style=social&label=Stars)
