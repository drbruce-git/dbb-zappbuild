@groovy.transform.BaseScript com.ibm.dbb.groovy.ScriptLoader baseScript
import groovy.transform.*
import com.ibm.dbb.*
import com.ibm.dbb.build.*
import com.ibm.jzos.ZFile

@Field BuildProperties props = BuildProperties.getInstance()
println "\n** Executing test script fullBuild.groovy"

// Get the DBB_HOME location
def dbbHome = EnvVars.getHome()
if (props.verbose) println "** DBB_HOME = ${dbbHome}"

// create full build command
def fullBuildCommand = [] 
fullBuildCommand << "${dbbHome}/bin/groovyz"
fullBuildCommand << "${props.zAppBuildDir}/build.groovy"
fullBuildCommand << "--workspace ${props.workspace}"
fullBuildCommand << "--application ${props.app}"
fullBuildCommand << "--outDir ${props.zAppBuildDir}/out"
fullBuildCommand << "--hlq ${props.hlq}"
fullBuildCommand << "--logEncoding UTF-8"
fullBuildCommand << "--url ${props.url}"
fullBuildCommand << "--id ${props.id}"
fullBuildCommand << (props.pw ? "--pw ${props.pw}" : "--pwFile ${props.pwFile}")
fullBuildCommand << (props.verbose ? "--verbose" : "")
fullBuildCommand << "--fullBuild"

// run full build 
println "** Executing ${fullBuildCommand.join(" ")}"
def process = ['bash', '-c', fullBuildCommand.join(" ")].execute()
def outputStream = new StringBuffer();
process.waitForProcessOutput(outputStream, System.err)

//validate build results
println "** Validating full build results"
def expectedFilesBuiltList = props.fullBuild_expectedFilesBuilt.split(',')

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
	if (props.verbose) {
		println "** Full Build Console: "
		println outputStream
		println ""
	}
	
	cleanUpDatasets(props)
}

// script end

//*************************************************************
// Method Definitions
//*************************************************************

def cleanUpDatasets(BuildProperties props) {
	def segments = props.fullBuild_datasetsToCleanUp.split(',')
	
	println "Deleting test PDSEs ${segments}"
	segments.each { segment ->
	    def pds = "'${props.hlq}.${segment}'"
	    if (ZFile.dsExists(pds)) {
	       if (props.verbose) println "** Deleting ${pds}"
	       ZFile.remove("//$pds")
	    }
	}
}