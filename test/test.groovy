@groovy.transform.BaseScript com.ibm.dbb.groovy.ScriptLoader baseScript
import groovy.transform.*
import com.ibm.dbb.build.*

println "** Executing zAppBuild test framework test/test.groovy"

BuildProperties properties = BuildProperties.getInstance()
def zAppBuildDir = getScriptDir()

// Parse test script arguments
def argMap = createArgMap(args)

// Load application test.properties file
properties.load(new File("${getScriptDir()}/applications/${argMap.app}/test.properties"))

// add some additional properties
properties.testBranch = 'zAppBuildTesting'
properties.zAppBuildDir = getScriptDir().getParent()
if (argMap.appRepo) {
	properties.appLocation = argMap.appRepo
	properties.workspace = new File(properties.appLocation).getParent()
}
else { // default to zAppBuild repo locations
	properties.appLocation = "${properties.zAppBuildDir}/samples/${argMap.app}"
	properties.workspace = "${properties.zAppBuildDir}/samples"
}

// dump arguments and properties
if (argMap.verbose) dumpArgsProps(argMap)

// create a test branch to run under
createTestBranch(argMap)

// run the test scripts
try {
	if (properties.test_testOrder) {
		println("** Invoking test scripts according to test list order: ${properties.test_testOrder}")
		
		String[] testOrder = properties.test_testOrder.split(',')
		
		testOrder.each { script ->
		   // run the test script	
		   println("** Invoking $script")   
		   runScript(new File("testScripts/$script"), argMap)
	    }
	}
	else {
		println("*! No test scripts to run in application ${argMap.application}.  Nothing to do.")
	}
}
finally {
	// delete test branch
	deleteTestBranch(argMap)
}
// end script


//************************************************************
// Method definitions
//************************************************************

/*
 * Parse command line arguments and store in argMap
 */
def createArgMap(String [] args) {
	// use CliBuilder to parse test.groovy input options
	def cli = new CliBuilder(
	   usage: '$DBB_HOME/bin/groovyz test.groovy <options>',
	   header: '\nAvailable options (use -h for help):\n',
	   footer: '\nInformation provided via above options is used to execute build against ZAppBuild.\n')
	
	cli.with
	{
	   h(longOpt: 'help', 'Show usage information')
	   
	   // test framework options
	   b(longOpt: 'branch', 'zAppBuild branch to test', args: 1, required: true)
	   l(longOpt: 'appRepo', '[Optional] location of external app repo to use in test. Defaults to zAppBuild', args: 1)

	   
	   // zAppBuild options
	   a(longOpt: 'app', 'Application that is being tested (example: MortgageApplication)', args: 1, required: true)
	   q(longOpt: 'hlq', 'HLQ for dataset reation / deletion (example: USER.BUILD)', args: 1, required: true)
	   u(longOpt: 'url', 'DBB Web Application server URL', args: 1, required: true)
	   i(longOpt: 'id', 'DBB Web Application user id', args: 1, required: true)
	   p(longOpt: 'pw', 'DBB Web Application user password', args: 1)
	   P(longOpt: 'pwFile', 'DBB Web Application user password file', args: 1)
	   v(longOpt: 'verbose', 'Flag indicating to print trace statements')
	}
	
	def options = cli.parse(args)
	
	// Show usage text when -h or --help option is used.
	if (options.h) {
		cli.usage()
		System.exit(0)
	}
	
	// store the command line arguments in a map to pass to test scripts
	def argMap = [:]
	if (options.b) argMap.branch = options.b
	if (options.r) argMap.appRepo = options.r
	if (options.a) argMap.app = options.a
	if (options.q) argMap.hlq = options.q
	if (options.u) argMap.url = options.u
	if (options.i) argMap.id = options.i
	if (options.p) argMap.pw = options.p
	if (options.P) argMap.pwFile = options.P
	if (options.v) argMap.verbose = 'true'
	
	return argMap
}

/*
 * Create and checkout a local test branch for testing
 */
def createTestBranch(argMap) {
	println "** Creating and checking out branch ${properties.testBranch}"
	def createTestBranch = """
    cd ${properties.zAppBuildDir}
    git checkout ${argMap.branch}
    git checkout -b ${properties.testBranch} ${argMap.branch}
"""
	def job = ['bash', '-c', createTestBranch].execute()
	job.waitFor()
	def createBranch = job.in.text
	println "** Git Exit code: " + job.exitValue()
	if (argMap.verbose) println "** Output:  $createBranch"
}

/*
 * Deletes test branch
 */
def deleteTestBranch(argMap) {
	println "\n** Deleting test branch ${properties.testBranch}"
	def deleteTestBranch = """
    cd ${properties.zAppBuildDir}
    git rest --hard ${properties.testBranch}
    git checkout ${argMap.branch}
    git branch -D ${properties.testBranch}
"""
	def job = ['bash', '-c', deleteTestBranch].execute()
	job.waitFor()
	def deleteBranch = job.in.text
	println "** Git Exit code: " + job.exitValue()
	if (argMap.verbose) println "** Output:  $deleteBranch"
}

def dumpArgsProps(argMap) {
	println "** Passed arguments a startup"
    argMap.each { key, value ->
		println "$key = $value"
	}	
	
	println "** Properties loaded from applications/${argMap.app}/test.properties"
	println properties.list()
}

