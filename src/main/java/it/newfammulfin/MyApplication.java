/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin;

import it.newfammulfin.api.util.GsonReaderWriter;
import org.apache.bval.jsr303.ApacheValidationProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

/**
 *
 * @author eric
 */
public class MyApplication extends ResourceConfig {

  public MyApplication() {
    packages(
            GsonReaderWriter.class.getPackage().getName(),
            "it.newfammulfin.api");
    register(ApacheValidationProvider.class);
    property(ServerProperties.BV_FEATURE_DISABLE, false);
  }

}
