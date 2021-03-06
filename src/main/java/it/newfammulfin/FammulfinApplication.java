/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin;

import it.newfammulfin.api.RegisteredUserResource;
import it.newfammulfin.api.util.GroupRetrieverRequestFilter;
import it.newfammulfin.api.util.GsonReaderWriter;
import javax.validation.Configuration;
import javax.validation.Validation;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

/**
 *
 * @author eric
 */
public class FammulfinApplication extends ResourceConfig {

  public FammulfinApplication() {
    packages(
            RegisteredUserResource.class.getPackage().getName(),
            GsonReaderWriter.class.getPackage().getName(),
            GroupRetrieverRequestFilter.class.getPackage().getName());
    property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true); //could be moved in web.xml
  }

}
