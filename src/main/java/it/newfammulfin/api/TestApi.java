/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.newfammulfin.api;

import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 * @author eric
 */
@Path("/hi")
public class TestApi {
  
  @GET @Produces("text/plain")
  public String getMessage() {
    return "Ciao, ora è: "+(new Date()).toString();
  }
  
}
