import com.amazonaws.regions.*
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClient;
import com.amazonaws.services.simplesystemsmanagement.model.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.logging.Logger

/**
  * Simple wrap class to get parameter from parameter store
*/

class SSMQuery {
  def Logger logger = Logger.getLogger("jenkins.SSMQuery")
  def REGION       = (Regions.getCurrentRegion() != null )? Regions.getCurrentRegion().getName() : "us-east-1"
  def AWSSimpleSystemsManagementClient awsSimpleSystemsManagement

  public SSMQuery(){
    awsSimpleSystemsManagement = AWSSimpleSystemsManagementClient.builder().withRegion(REGION).build();
  }

  /**
   * Get parameter from SSM, with or without encryption (use IAM role for decryption)
   * Throws {@Link com.amazonaws.services.simplesystemsmanagement.model.ParameterNotFoundException} if not found
   * @param key
   * @param encryption
   * @return value
   */

  public String getParameterByName(name){
    try{
      GetParameterRequest getparameterRequest = new GetParameterRequest().withName(name).withWithDecryption(true);
      GetParameterResult result = awsSimpleSystemsManagement.getParameter(getparameterRequest);
      return result.getParameter().getValue();
    }
    catch(Exception ex){
      logger.error("Parameter " + name + " was not found")
      return ""
    }
  }

  /**
   * Get parameter from SSM by path, with or without encryption (use IAM role for decryption)
   * Returns Map of all values, with all path parameters removed, since we assume that the path is for environment
   * @param path
   * @param encryption
   * @return Map of all values in path
   */

  public Map<String, String> getParametersByPath(String path, boolean encryption) {
    try{
      GetParametersByPathRequest getParametersByPathRequest = new GetParametersByPathRequest().withPath(path)
              .withWithDecryption(encryption)
              .withRecursive(true);
      String token = null;
      Map<String, String> params = new HashMap<>();

      getParametersByPathRequest.setNextToken(token);
      GetParametersByPathResult parameterResult = awsSimpleSystemsManagement.getParametersByPath(getParametersByPathRequest);
      token = parameterResult.getNextToken();
      params.putAll(addParamsToMap(parameterResult.getParameters()));

      while (token != null) {
          getParametersByPathRequest.setNextToken(token);
          parameterResult = awsSimpleSystemsManagement.getParametersByPath(getParametersByPathRequest);
          token = parameterResult.getNextToken();
          params.putAll(addParamsToMap(parameterResult.getParameters()));
      }
      return params;
    }
    catch(Exception ex){
      logger.error("Parameters on " + path + " had an exception")
      return ""
    }
  }

}
