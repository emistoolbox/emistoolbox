call xslt.cmd adminManual.xml \dev\docbook\docbook-xsl-1.78.1\fo\docbook.xsl > adminManual.fo
fop -c c:\work\emistoolbox\source\core\docs\manual\adminManual\fop.xconf c:\work\emistoolbox\source\core\docs\manual\adminManual\adminManual.fo c:\work\emistoolbox\source\core\docs\manual\adminManual\EMISToolbox_Administration_Manual.pdf
