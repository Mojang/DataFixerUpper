# DataFixerUpper [![Latest release](https://img.shields.io/github/release/Mojang/DataFixerUpper.svg)](https://github.com/Mojang/DataFixerUpper/releases/latest) [![License](https://img.shields.io/github/license/Mojang/DataFixerUpper.svg)](https://github.com/Mojang/DataFixerUpper/blob/master/LICENSE)
A set of utilities designed for incremental building, merging and optimization of data transformations. Created for converting the game data for Minecraft: Java Edition between different versions of the game.

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
Core data types are Schema and DataFix: Schema is a set of type definitions specifying what data types the system is interested in and how they relate to each other, DataFix is is a rewrite rule between types (see [references](#references) below). DataFixerBuilder takes a list of schemas and fixes converting between those schemas, and creates an optimized converter between the types describes in those schemas. DSL is a class with building blocks used to create schemas and fixes.

# Contributing
Contributions are welcome!

Most contributions will require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to,
and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

# References
## Optimizing functions
  - Cunha, A., & Pinto, J. S. (2005). [Point-free program transformation](https://haslab.uminho.pt/alcino/files/pfpt.pdf)
  - Lämmel, R., Visser, E., & Visser, J. (2002). [The essence of strategic programming](https://www.researchgate.net/publication/277289331_The_Essence_of_Strategic_Programming)

## How to handle recursive types
  - Cunha, A., & Pacheco, H. (2011). [Algebraic specialization of generic functions for recursive types](http://alfa.di.uminho.pt/~hpacheco/publications/msfp08.pdf)
  - Yakushev, A. R., Holdermans, S., Löh, A., & Jeuring, J. (2009, August). [Generic programming with fixed points for mutually recursive datatypes](https://www.andres-loeh.de/Rec/MutualRec.pdf)
  - Magalhães, J. P., & Löh, A. (2012). [A formal comparison of approaches to datatype-generic programming](https://arxiv.org/abs/1202.2920)

## Optics
  - Pickering, M., Gibbons, J., & Wu, N. (2017). [Profunctor Optics: Modular Data Accessors](https://arxiv.org/abs/1703.10857)
  - Pacheco, H., & Cunha, A. (2010, June). [Generic point-free lenses](http://alfa.di.uminho.pt/~hpacheco/publications/mpc10.pdf)

## Tying it together
  - Cunha, A., Oliveira, J. N., & Visser, J. (2006, August). [Type-safe two-level data transformation](https://pdfs.semanticscholar.org/7274/d3b311e8bbbe9011f6f1ef0232901886a6c5.pdf)
  - Cunha, A., & Visser, J. (2011). [Transformation of structure-shy programs with application to XPath queries and strategic functions](https://www.researchgate.net/publication/220131555_Transformation_of_structure-shy_programs_with_application_to_XPath_queries_and_strategic_functions)
  - Pacheco, H., & Cunha, A. (2011, January). [Calculating with lenses: optimising bidirectional transformations](https://dl.acm.org/citation.cfm?id=1929501.1929520)

![GitHub forks](https://img.shields.io/github/forks/Mojang/DataFixerUpper.svg?style=social&label=Fork) ![GitHub stars](https://img.shields.io/github/stars/Mojang/DataFixerUpper.svg?style=social&label=Stars)
