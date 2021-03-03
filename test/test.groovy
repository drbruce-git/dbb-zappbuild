@groovy.transform.BaseScript com.ibm.dbb.groovy.ScriptLoader baseScript
import groovy.transform.*
import com.ibm.dbb.build.*


@Field BuildProperties properties = BuildProperties.getInstance()

// use CliBuilder to parse test.groovy input options
def cli = new CliBuilder(
   usage: '$DBB_HOME/bin/groovyz test.groovy <options>',
   header: '\nAvailable options (use -h for help):\n',
   footer: '\nInformation provided via above options is used to execute build against ZAppBuild.\n')

cli.with
{
   h(longOpt: 'help', 'Show usage information')
   
   // required options for test framework
   r(longOpt: 'repoPath', 'Path to local repository to test. (Optional) Defaults to zAppBuild', args: 1)
   b(longOpt: 'branchName', 'Feature Branch to test', args: 1, required: true)
   
   // required options to run zAppBuild build.groovy
   a(longOpt: 'application', 'Application that is being tested (example: MortgageApplication)', args: 1, required: true)
   q(longOpt: 'hlq', 'HLQ for dataset reation / deletion (example: IBMDBB.ZAPP.BUILD)', args: 1, required: true)   
   u(longOpt: 'url', 'DBB Web Application server URL', args: 1, required: true)
   i(longOpt: 'id', 'DBB Web Application user id', args: 1, required: true)
   p(longOpt: 'password', 'DBB Web Application user password', args: 1)
   P(longOpt: 'passwordFile', 'DBB Web Application user password file', args: 1)
}

def options = cli.parse(args)
if (!options) {
	cli.usage()
    return
}
   
// Show usage text when -h or --help option is used.
if (options.h || options.help) {
    cli.usage() 
    return
}

// store the command line arguments in a map to pass to test scripts
def argMap = [:]
if (options.r) argMap.repoPath = options.r
if (options.b) argMap.branchName = options.b
if (options.a) argMap.application = options.a
if (options.q) argMap.hlq = options.q
if (options.u) argMap.url = options.u
if (options.i) argMap.id = options.i
if (options.p) argMap.password = options.p
if (options.P) argMap.passwordFile = options.P

// set repoPath to default value if null/empty
if (!argMap.repoPath)
	argMap.repoPath = "${getScriptDir()}/applications/${argMap.application}"

// Load application test.properties file
properties.load(new File("${getScriptDir()}/applications/${argMap.application}/test.properties"))

// run test.init
init(argMap)

// run the test scripts
if (properties.test_testOrder) {
	println("** Invoking test scripts according to test list order: ${properties.test_testOrder}")
	
	String[] testList = properties.test_testOrder.split(',')
	testList.each { script ->
	   // load the Groovy test script class	
	   def testScript = loadScript(new File("testScripts/$script"))
	   
	   // run test script's init method
//	   if (testScript.metaclass.respondsTo(testScript, "init", Map))
		   testScript.init(argMap)
		  
	   // run the test script	   
	   testScript._run(argMap)
	   
	   // run test script's cleanUp method
//	   if (testScript.metaclass.respondsTo(testScript, "cleanUp", Map))
	   testScript.cleanUp(argMap)
    }
}
else {
	println("*! No test scripts to run in application ${argMap.application}.  Nothing to do.")
}

// run test.cleanUp
cleanUp(argMap)

// end script


def init(argMap) {
	println "*** Executing test.init()"
	println "*** Passed argMap values"
	argMap.each { key, value ->
		println "**** $key = $value"
	}
}

def cleanUp(argMap) {
	println "*** Executing test.cleanUp()"
	println "*** Passed argMap values"
	argMap.each { key, value ->
		println "**** $key = $value"
	}
	
}
// /u/dbbAutomation/workspace/Automation_Jobs/DBB_All_BuildS/DBBZtoolkitTar/bin/groovyz /u/dbbAutomation/workspace/Automation_Jobs/ZAppBuildTest/ZAppBuild/dbb-zappbuild/test/test.groovy -r /u/dbbAutomation/workspace/Automation_Jobs/ZAppBuildTest/ZAppBuild/dbb-zappbuild -b AutomationTest -a MortgageApplication -q IBMDBB.ZAPPB.BUILD -s https://dbbdev.rtp.raleigh.ibm.com:19443/dbb/ -u ADMIN -p ADMIN -n 9 -f epsmort.bms,epsmlis.bms,epsnbrvl.cbl,epscsmrt.cbl,epsmlist.cbl,epsmpmt.cbl,epscmort.cbl,epscsmrd.cbl,epsmlist.lnk -i epsmort.bms,epscmort.cbl -m 2 -c /bms/epsmort.bms
// /u/dbbAutomation/workspace/Automation_Jobs/DBB_All_BuildS/DBBZtoolkitTar/bin/groovyz /u/dbbAutomation/workspace/Automation_Jobs/ZAppBuildTest/ZAppBuild/dbb-zappbuild/test/test.groovy -r /u/dbbAutomation/workspace/Automation_Jobs/ZAppBuildTest/ZAppBuild/dbb-zappbuild -b AutomationTest -a MortgageApplication -q IBMDBB.ZAPPB.BUILD -s https://dbbdev.rtp.raleigh.ibm.com:19443/dbb/ -u ADMIN -p ADMIN -f epsmort.bms,epsmlis.bms,epsnbrvl.cbl,epscsmrt.cbl,epsmlist.cbl,epsmpmt.cbl,epscmort.cbl,epscsmrd.cbl,epsmlist.lnk -i epsmort.bms,epscmort.cbl -c /bms/epsmort.bms
