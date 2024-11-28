# PubChem ChemFinder
WARNING. THIS IS THE BETA BRANCH. THIS MAY BE UNSTABLE. PLEASE RETURN TO THE MAIN BRANCH IF YOU WISH TO USE THIS SOFTWARE FOR ANYTHING OTHER THAN TESTING.<br>
<br>
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

## Long Term Goals
:x: Implement a GUI<br>
:x: Sort data better<br>
✔️ Increase readability<br>
✔️ Improve error handling<br>
<br>
## Release b1.1 Patch Notes
- Added rate limit handling.
- Increased debug message prevalence.
- If you run into issues, please contact me in discord.gg/arch-linux


