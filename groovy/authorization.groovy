import hudson.*
import hudson.security.*
import jenkins.model.*
import java.util.*
import com.michelin.cio.hudson.plugins.rolestrategy.*
import com.synopsys.arc.jenkins.plugins.rolestrategy.*
import java.lang.reflect.*
import java.util.logging.*
import groovy.json.*

def env = System.getenv()
def AUTHZ_JSON_FILE = System.getenv('AUTHZ_JSON_FILE') ?: "/usr/share/jenkins/ref/config/auth.json"
/**
 * ===================================
 *
 *                Roles
 *
 * ===================================
 */
def globalRoleRead = "read"
def globalBuildRole = "build"
def globalRoleAdmin = "admin"

/**
 * ===================================
 *
 *           Users and Groups
 *
 * ===================================
 */
def access = [
  admins: ["anonymous"],
  builders: [],
  readers: []
]

if (AUTHZ_JSON_FILE != "")  {
  println "Get role authorizations from file ${AUTHZ_JSON_FILE}"
  File f = new File(AUTHZ_JSON_FILE)
  def jsonSlurper = new JsonSlurper()
  def jsonText = f.getText()
  access = jsonSlurper.parseText( jsonText )
}
else {
  println "Warning! env.AUTHZ_JSON_FILE not specified!"
  println "Granting anonymous admin access"
}

/**
 * ===================================
 *
 *           Permissions
 *
 * ===================================
 */

// TODO: drive these from a config file
def adminPermissions = [
"hudson.model.Hudson.Administer",
"hudson.model.Hudson.Read"
]

def readPermissions = [
"hudson.model.Hudson.Read",
"hudson.model.Item.Discover",
"hudson.model.Item.Read"
]

def buildPermissions = [
"hudson.model.Hudson.Read",
"hudson.model.Item.Build",
"hudson.model.Item.Cancel",
"hudson.model.Item.Read",
"hudson.model.Run.Replay"
]

def roleBasedAuthenticationStrategy = new RoleBasedAuthorizationStrategy()
Jenkins.instance.setAuthorizationStrategy(roleBasedAuthenticationStrategy)


/**
 * ===================================
 *
 *               HACK
 * Inspired by https://issues.jenkins-ci.org/browse/JENKINS-23709
 * Deprecated by on https://github.com/jenkinsci/role-strategy-plugin/pull/12
 *
 * ===================================
 */

Constructor[] constrs = Role.class.getConstructors();
for (Constructor<?> c : constrs) {
  c.setAccessible(true);
}

// Make the method assignRole accessible
Method assignRoleMethod = RoleBasedAuthorizationStrategy.class.getDeclaredMethod("assignRole", RoleType.class, Role.class, String.class);
assignRoleMethod.setAccessible(true);
println("HACK! changing visibility of RoleBasedAuthorizationStrategy.assignRole")

/**
 * ===================================
 *
 *           Permissions
 *
 * ===================================
 */

Set<Permission> adminPermissionSet = new HashSet<Permission>();
adminPermissions.each { p ->
  def permission = Permission.fromId(p);
  if (permission != null) {
    adminPermissionSet.add(permission);
  } else {
    println("${p} is not a valid permission ID (ignoring)")
  }
}

Set<Permission> buildPermissionSet = new HashSet<Permission>();
buildPermissions.each { p ->
  def permission = Permission.fromId(p);
  if (permission != null) {
    buildPermissionSet.add(permission);
  } else {
    println("${p} is not a valid permission ID (ignoring)")
  }
}

Set<Permission> readPermissionSet = new HashSet<Permission>();
readPermissions.each { p ->
  def permission = Permission.fromId(p);
  if (permission != null) {
    readPermissionSet.add(permission);
  } else {
    println("${p} is not a valid permission ID (ignoring)")
  }
}

/**
 * ===================================
 *
 *      Permissions -> Roles
 *
 * ===================================
 */

// admins
Role adminRole = new Role(globalRoleAdmin, adminPermissionSet);
roleBasedAuthenticationStrategy.addRole(RoleType.Global, adminRole);

// builders
Role buildersRole = new Role(globalBuildRole, buildPermissionSet);
roleBasedAuthenticationStrategy.addRole(RoleType.Global, buildersRole);

// anonymous read
Role readRole = new Role(globalRoleRead, readPermissionSet);
roleBasedAuthenticationStrategy.addRole(RoleType.Global, readRole);

/**
 * ===================================
 *
 *      Roles -> Groups/Users
 *
 * ===================================
 */

access.admins.each { l ->
  println("Granting admin to ${l}")
  roleBasedAuthenticationStrategy.assignRole(RoleType.Global, adminRole, l);
}

access.builders.each { l ->
  println("Granting builder to ${l}")
  roleBasedAuthenticationStrategy.assignRole(RoleType.Global, buildersRole, l);
}

access.readers.each { l ->
  println("Granting read to ${l}")
  roleBasedAuthenticationStrategy.assignRole(RoleType.Global, readRole, l);
}

Jenkins.instance.save()
