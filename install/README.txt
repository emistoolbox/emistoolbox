Steps to create emistoolbox-install.exe

- emistoolbox.exe - create new emistoolbox.exe if the Jetty launcher has changed. 
    (TODO - create Ant task)

- Copy other files into distribution directory install/deploy/emistoolbox (depending on whether they changed): 

    - emistoolbox.war
    - default/*
    
- Create installation file using WinRAR - SFX mode with a shortcut
    - Default installation directory c:\emistoolbox
    - Icon bin/emistoolbox.ico

- An existing emistoolbox-install.exe can also be updated if opened in WinRAR. 
