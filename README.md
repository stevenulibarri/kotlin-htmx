# kotlin-htmx
Seeing how well copilot does with kotlin because I was curious,
also messing with [htmx](https://htmx.org/).  
I used vscode on linux and installed the kotlin compiler manually.

### Build
```sh
kotlinc hello.kt -include-runtime -d hello.jar
```
### Run
```sh
java -jar hello.jar
```

## TODO
- learn gradle
- use some external dependency via gradle (probably an http api framework)
- how to compile more than one file?
- more interesting htmx features
- css
