# AisAnalysis #

## Introduction ##

AIS analyzers

## Prerequisites ##

* Java 1.7
* Maven

## Building ##

    mvn clean install 

## Developing in Eclipse ##

	mvn eclipse:eclipse

Import into Eclipse

## AIS coverage ##
AisCoverage is a tool for calculating how well AIS receivers (sources) cover a geographical area. This information is useful for determining the range of a source, if a source is malfunctioning, if a source is redundant etc. Furthermore it can be used to see how different parameters (like the weather) affect the coverage. The tool runs as a background process analyzing a stream of AIS messages either from live data sources or a file. The connection capabilities are provided by the [AisLib](https://github.com/dma-dk/AisLib), which is a middleware Java component used to retrieve AIS messages from various sources (live sources or files).
[Read more](https://github.com/dma-dk/AisAnalysis/wiki/AisCoverage)

### Rest API ###

    /coverage/rest/*

### Distribution ###

A distributable zip file is found [here](http://fuka.dk/ais-coverage-analysis.zip). <br>
Be aware: As it contains executable files, your browser may post a warning when you download the file. <br><br>
When you have downloaded the zip file, extract it to your desired location, and open the folder. <br>
Modify the configuration file (configuration.xml)to suit your needs (For guidance, Look at the 4 configuration samples (also included)). If you will be using several standart configurations you can edit the -file part in the bat file to point at the configuration file you wish you use for the given test<br>

Run the coverage.bat file (windows) or the coverage.sh file (linux) to start the coverage-analysis. While the service is running, you can view the progress/result by opening your browser at the address given in the configuration file (sample-default-address = localhost:8090/coverage/)
<br><br>
When running tests from files, please use the memory only option. <br>
When running tests over longer periods using mongodb you might experience some issues. A solution to this, is being worked on.

Examples of configuration files can be found here:<br>
[Read from a file](https://github.com/dma-dk/AisAnalysis/blob/master/ais-analyzer-coverage/src/main/resources/coverage-fromfile-sample.xml)<br>
[Read from a live stream using a TCP connection](https://github.com/dma-dk/AisAnalysis/blob/master/ais-analyzer-coverage/src/main/resources/coverage-fromtcp-sample.xml)<br>
[Keep coverage results in memory only](https://github.com/dma-dk/AisAnalysis/blob/master/ais-analyzer-coverage/src/main/resources/coverage-memoryonly-sample.xml)<br>
[Store coverage results using an instance of MongoDB](https://github.com/dma-dk/AisAnalysis/blob/master/ais-analyzer-coverage/src/main/resources/coverage-mongodb-sample.xml) (Remember to install MongoDB)<br>


## AIS viewer ##

### Rest API ###

    /aisview/rest/*

### Distribution ###

A distributable zip file will be created in `target/`


