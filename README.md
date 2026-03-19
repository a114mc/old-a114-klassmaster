# old-a114-klassmaster

Based on [Bruhfuscator](https://github.com/erxson/Bruhfuscator).

<div align="center" id="alert">
    <h1>It's buggy, do not use for starter base!</h1>
    <h2>Legacy shit alert</h2>
    <h3>AI slop obfuscator alert</h3>
    <h4>Skid alert</h4>
</div>


# ?

I've tried my best on developing it, but it is now abandoned due to 
historical bugs that I cannot fix.

Keeping an abandoned repository private makes me feel bad, so I decided to 
open-source it.

I do not suggest that you should use it as a starter base.

# Can I use it?

Use of old-a114-klassmaster is governed by the MIT license.

# Thanks

- [iiiiiiiris for the Obfuscator base](https://github.com/iiiiiiiris/Ambien)
- [ColonialBuilders](https://github.com/ColonialBuilders/ColonialObfuscator)
- [Souvenir String Encryption](https://github.com/Body-Alhoha/Souvenir)
- [Dimples1337/goto-java-obfuscator\(404\)](https://github.com/Dimples1337/goto-java-obfuscator)
- [Araykal](https://github.com/MyJ2c/Open-MyJ2c)
- [sim0n](https://github.com/sim0n/Caesium)
- [0x150](https://github.com/0x3C50)
- [netindev](https://github.com/netindev/)
- [ChatGPT](https://chatgpt.com/)
- [bzym2](https://github.com/bzym2)

# Usage

* Clone this project
* Open IntelliJ IDEA (Community or Ultimate)
* Drag the cloned repository directory to IntelliJ IDEA's home page
* Click "Trust and open project"\(Because this is not a malware🤣\)
* Wait and click notification "Load Maven Project"
* Click the shared run configuration "Entrypoint obfuscate"
* If the program argument contains -cfg settings.json and the JSON file was not found, it will create the configuration.
* Edit config.json, comments in the JSON file was probably allowed
* Save your changes and run it again!

# **README Before using:**

* Use Java 17 with language level 17 for the GUI module,
language level 1.8 for others(already defined in pom.xml)

* Some feature(s) should not be used with any kind of obfuscator(s) (E.G. Allatori string encryption)

# Note for users

- Heavily obfuscated code will increase the obfuscated jar's size, and lags the program.

- This is ***not a miner***; abnormal CPU usage is caused by ASM doing ***wacky 
  bytecode manipulation stuffs***!

- Don't take this obfuscator seriously! This obfuscator(bruhfuscator) just made for fun.

- I cannot prevent your software from being cracked by using this obfuscator

Run the jar using the `--create-config` argument to create a base config

Then run the obfuscator jar using the `-config` or `-c` argument (including the path to your
config)

###### To see all the arguments it offers, you can use the '-help' argument

