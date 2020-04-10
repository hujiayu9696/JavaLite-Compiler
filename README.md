# JavaLite-Compiler
A compiler that compiles Mini-Java to MIPS Assembly

## Build and run
This project is built based on Gradle.

Run `gradle release` to build the entire project.

After that, a `minijava.jar` file will be created under the `build` directory.

Then, run `java -jar minijava.jar source.java output.asm` to compile the Mini-Java source code `source.java`, and it will generate the corresponding MIPS Assembly code `output.asm`.

## Example Mini-Java Code
The example Mini-Java source code that can be compiled by this project are shown in the `examples` directory.