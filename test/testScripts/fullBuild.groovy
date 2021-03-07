@groovy.transform.BaseScript com.ibm.dbb.groovy.ScriptLoader baseScript
import groovy.transform.*
import com.ibm.dbb.*
import com.ibm.dbb.build.*

@Field BuildProperties properties = BuildProperties.getInstance()
println "\n** Executing test script fullBuild.groovy"

// Get the DBB_HOME location
def dbbHome = EnvVars.getHome()
if (argMap.verbose) println "** DBB_HOME = ${dbbHome}"

// create full build command
def fullBuildCommand = [] 
fullBuildCommand << "${dbbHome}/bin/groovyz"
fullBuildCommand << "${properties.zAppBuildDir}/build.groovy"
fullBuildCommand << "--workspace ${properties.workspace}"
fullBuildCommand << "--application ${argMap.app}"
fullBuildCommand << "--outDir ${properties.zAppBuildDir}/out"
fullBuildCommand << "--hlq ${argMap.hlq}"
fullBuildCommand << "--logEncoding UTF-8"
fullBuildCommand << "--url ${argMap.url}"
fullBuildCommand << "--id ${argMap.id}"
fullBuildCommand << (argMap.pw ? "--pw ${argMap.pw}" : "--pwFile ${argMap.pwFile}")
fullBuildCommand << (argMap.verbose ? "--verbose" : "")
fullBuildCommand << "--fullBuild

// run full build 
println "** Executing ${fullBuildCommand.join(" ")}"
def process = ['bash', '-c', fullBuildCommand.join(" ")].execute()
def outputStream = new StringBuffer();
process.waitForProcessOutput(outputStream, System.err)

//validate build results
println "** Validating full build results"
def expectedFilesBuiltList = properties.fullBuild_expectedFilesBuilt.split(',')

try {
	// Validate clean build
	assert outputStream.contains("Build State : CLEAN") : "*! FULL BUILD FAILED"

	// Validate expected number of files built
	def numFullFiles = expectedFilesBuiltList.size()
	assert outputStream.contains("Total files processed : ${numFullFiles}") : "*! TOTAL FILES PROCESSED ARE NOT EQUAL TO ${numFullFiles}"

	// Validate expected built files in output stream
	assert fileList.count{ i-> outputStream.contains(i) } == fileList.size() : "*! FILES PROCESSED IN THE FULLBUILD DOES NOT CONTAIN THE LIST OF FILES PASSED ${fileList}"
	
	println "**Full Build Test : SUCCESS"
}
finally {
	if (properties.verbose) {
		println "** Full Build Console: "
		println outputStream
		println ""
	}
	
	cleanUpDatasets(argMap)
}

// script end

//*************************************************************
// Method Definitions
//*************************************************************

def cleanUpDatasets(argMap) {
	def segments = properties.fullBuild_datasetsToCleanUp.split(',')
	
	println "Deleting test PDSEs ${segments}"
	segments.each { segment ->
	    def pds = "'${argMap.hlq}.${segment}'"
	    if (ZFile.dsExists(pds)) {
	       if (argMap.verbose) println "** Deleting ${pds}"
	       ZFile.remove("//$pds")
	    }
	}
}