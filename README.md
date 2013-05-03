# AisAnalysis #

## Introduction ##

AIS analyzers

### Ais Coverage ###
AisCoverage is a tool for calculating how well AIS receivers (sources) cover a geographical area. This information is useful for determining the range of a source, if a source is malfunctioning, if a source is redundant etc. Furthermore it can be used to see how different parameters (like the weather) affect the coverage. The tool runs as a background process analyzing a stream of AIS messages either from live data sources or a file. The connection capabilities are provided by the [AisLib](https://github.com/dma-dk/AisLib), which is a middleware Java component used to retrieve AIS messages from various sources (live sources or files).
[Read more](https://github.com/dma-dk/AisAnalysis/wiki/AisCoverage)

## Prerequisites ##

* Java 1.7
* Maven

## Building ##

    mvn clean install 

## Developing in Eclipse ##

	mvn eclipse:eclipse

Import into Eclipse

## AIS coverage ##

### Rest API ###

    /coverage/rest/*

### Distribution ###

A distributable zip file is found [Read more](https://insertlink). Put this [bat file](https://insertlink) in the same folder as the zip file. Remember to modify the bat file to point to a configuration file.

Examples of configuration files can be found here:
	
	[Read from a file](https://github.com/dma-dk/AisAnalysis/blob/master/ais-analyzer-coverage/src/main/resources/coverage-fromfile-sample.xml)<br>
	[Read from a live stream using a TCP connection](https://github.com/dma-dk/AisAnalysis/blob/master/ais-analyzer-coverage/src/main/resources/coverage-fromtcp-sample.xml)<br>
	[Keep coverage results in memory only](https://github.com/dma-dk/AisAnalysis/blob/master/ais-analyzer-coverage/src/main/resources/coverage-memoryonly-sample.xml)<br>
	[Save coverage to an instance of MongoDB](https://github.com/dma-dk/AisAnalysis/blob/master/ais-analyzer-coverage/src/main/resources/coverage-mongodb-sample.xml) (Remember to install MongoDB)<br>


## AIS viewer ##

### Rest API ###

    /aisview/rest/*

### Distribution ###

A distributable zip file will be created in `target/`



