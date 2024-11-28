# PubChem ChemFinder
This tool identifies against the PubChem Database in order to check for properties of chemicals.<br>
<br>
This is a JAVA 21 project, if you are using it you MUST have Java 21 installed, if you are manually using the code from main.java you MUST be using JAVA21. Java23 may work but it is untested.<br>

## Using main.class from source
- Open IntelliJ and create a new MAVEN PROJECT.<br>
- Copy the code from main.java into your main.java of your project.<br>
- In your pom.xml add the following<br>
```    
    <dependencies>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.11.0</version>
        </dependency>
    </dependencies>
```

## Project Plans
:x: Implement a GUI<br>
:x: Sort data better<br>
:x: Increase readability<br>
:x: Improve error handling<br>
<br>
## Release b1.1 Patch Notes
- Updated Java version from JAVA 23 to JAVA 21 to increase compatibility on linux systems.
- Added debug messaging, ignore all messages with [DEBUG] unless you run into issues.
- If you run into issues, please contact me in discord.gg/arch-linux

