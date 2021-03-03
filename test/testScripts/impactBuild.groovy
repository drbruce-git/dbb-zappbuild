groovy.transform.BaseScript com.ibm.dbb.groovy.ScriptLoader baseScript
import groovy.transform.*
import com.ibm.dbb.*
import com.ibm.dbb.build.*

/****************************************************************************************
1. Creates an automation branch from ${branchName} 
2. Sets the values up for datasets in the datasets.properties
3. Cleans up test PDSEs
4. Runs a full build using mortgage application
@param repoPath              Path to ZAppBuild Repo
@param branchName            Feature branch to create a test(automation) branch against
@param app                   Application that is being tested (example: MortgageApplication)
@param hlq                   hlq to delete segments from (example: IBMDBB.ZAPP.BUILD)
@param serverURL             Server URL example(https://dbbdev.rtp.raleigh.ibm.com:19443/dbb/)
@param userName              User for server
@param password              Password for server
@param fullFiles             Build files for verification
******************************************************************************************/

@Field BuildProperties properties = BuildProperties.getInstance()
println "*** Executing impact build using these build properties\n${properties.list()}\n"
def dbbHome = EnvVars.getHome()

def zAppBuildDir = getScriptDir()
println "*** This is zAppBuildDir home****:${zAppBuildDir}"
println "*** This is dbb home****:${dbbHome}"

println "*** Passed argMap values"
argMap.each { key, value ->
	println "**** $key = $value"
}

/*def runFullBuild = """
    cd ${properties.repoPath}
    git checkout ${properties.branchName}
    git checkout -b automation ${properties.branchName}
    mv ${properties.repoPath}/test/samples/${properties.app}/datasets.properties ${properties.repoPath}/build-conf/datasets.properties
    ${dbbHome}/bin/groovyz ${properties.repoPath}/build.groovy --workspace ${properties.repoPath}/samples --application ${properties.app} --outDir ${properties.repoPath}/out --hlq ${properties.hlq} --logEncoding UTF-8 --url ${properties.serverURL} --id ${properties.userName} --pw ${properties.password} --fullBuild
"""
def process = ['bash', '-c', runFullBuild].execute()
def outputStream = new StringBuffer();
process.waitForProcessOutput(outputStream, System.err)

def list = properties.fullFiles
def listNew = list.split(',')
def numFullFiles = listNew.size()
assert outputStream.contains("Build State : CLEAN") && outputStream.contains("Total files processed : ${numFullFiles}") : "///***EITHER THE FULLBUILD FAILED OR TOTAL FILES PROCESSED ARE NOT EQUAL TO ${numFullFiles}.\n HERE IS THE OUTPUT FROM FULLBUILD \n$outputStream\n"

def files = properties.fullFiles
List<String> fileList = []
if (files) {
  fileList.addAll(files.trim().split(',')) 
  assert fileList.count{ i-> outputStream.contains(i) } == fileList.size() : "///***FILES PROCESSED IN THE FULLBUILD DOES NOT CONTAIN THE LIST OF FILES PASSED ${fileList}.\n HERE IS THE OUTPUT FROM FULLBUILD \n$outputStream\n"
}*/

def init (argMap) {
	println "*** Executing impactBuild.init()"
	println "*** Passed argMap values"
	argMap.each { key, value ->
		println "**** $key = $value"
	}
}

def cleanUp (argMap) {
	println "*** Executing impactBuild.cleanUp()"
	println "*** Passed argMap values"
	argMap.each { key, value ->
		println "**** $key = $value"
	}
}