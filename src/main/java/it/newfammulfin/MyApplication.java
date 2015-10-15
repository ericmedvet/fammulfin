/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.validation.ValidationConfig;

/**
 *
 * @author eric
 */
public class MyApplication extends ResourceConfig {

  public MyApplication() {
    packages(
            "it.newfammulfin.model",
            "it.newfammulfin.api",
            "it.newfammulfin.api.util");
  }
  
}
