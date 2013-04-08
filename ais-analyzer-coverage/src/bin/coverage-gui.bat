@echo OFF
java -Dlog4j.configuration="file:log4j.xml" -cp ".;lib/*;./*" -Xmn256M -Xms512M -Xmx1024M dk.dma.ais.analysis.coverage.AisCoverageGUI %*
