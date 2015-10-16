/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin;

import it.newfammulfin.api.util.GsonReaderWriter;
import org.apache.bval.BeanValidator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.validation.ValidationFeature;

/**
 *
 * @author eric
 */
public class MyApplication extends ResourceConfig {

  public MyApplication() {
    packages(
            GsonReaderWriter.class.getPackage().getName(),
            "it.newfammulfin.api");
    //packages(true, BeanValidator.class.getPackage().getName());
    register(ValidationFeature.class);
    property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
    property(ServerProperties.BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK, true);
  }

}
