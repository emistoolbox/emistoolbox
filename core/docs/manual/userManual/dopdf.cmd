call xslt.cmd userManual.xml \dev\docbook\docbook-xsl-1.78.1\fo\docbook.xsl > userManual.fo
fop -c c:\work\emistoolbox\source\core\docs\manual\userManual\fop.xconf c:\work\emistoolbox\source\core\docs\manual\userManual\userManual.fo c:\work\emistoolbox\source\core\docs\manual\userManual\EMISToolbox_User_Manual.pdf
