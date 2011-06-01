
/**
 * ServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */

    package es.emergya.webservices;

    /**
     *  ServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class ServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public ServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public ServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getUltimasPosiciones method
            * override this method for handling normal response from getUltimasPosiciones operation
            */
           public void receiveResultgetUltimasPosiciones(
                    ServiceStub.GetUltimasPosicionesResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUltimasPosiciones operation
           */
            public void receiveErrorgetUltimasPosiciones(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getRecursosEnPeriodo method
            * override this method for handling normal response from getRecursosEnPeriodo operation
            */
           public void receiveResultgetRecursosEnPeriodo(
                    ServiceStub.GetRecursosEnPeriodoResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRecursosEnPeriodo operation
           */
            public void receiveErrorgetRecursosEnPeriodo(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for loginEF method
            * override this method for handling normal response from loginEF operation
            */
           public void receiveResultloginEF(
                    ServiceStub.LoginEFResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from loginEF operation
           */
            public void receiveErrorloginEF(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for actualizaLoginEF method
            * override this method for handling normal response from actualizaLoginEF operation
            */
           public void receiveResultactualizaLoginEF(
                    ServiceStub.ActualizaLoginEFResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from actualizaLoginEF operation
           */
            public void receiveErroractualizaLoginEF(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getRutasRecursos method
            * override this method for handling normal response from getRutasRecursos operation
            */
           public void receiveResultgetRutasRecursos(
                    ServiceStub.GetRutasRecursosResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRutasRecursos operation
           */
            public void receiveErrorgetRutasRecursos(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getRutasRecursosFromBBDD method
            * override this method for handling normal response from getRutasRecursosFromBBDD operation
            */
           public void receiveResultgetRutasRecursosFromBBDD(
                    ServiceStub.GetRutasRecursosFromBBDDResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRutasRecursosFromBBDD operation
           */
            public void receiveErrorgetRutasRecursosFromBBDD(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getIncidenciasAbiertasEnPeriodo method
            * override this method for handling normal response from getIncidenciasAbiertasEnPeriodo operation
            */
           public void receiveResultgetIncidenciasAbiertasEnPeriodo(
                    ServiceStub.GetIncidenciasAbiertasEnPeriodoResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getIncidenciasAbiertasEnPeriodo operation
           */
            public void receiveErrorgetIncidenciasAbiertasEnPeriodo(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getPosicionesIncidencias method
            * override this method for handling normal response from getPosicionesIncidencias operation
            */
           public void receiveResultgetPosicionesIncidencias(
                    ServiceStub.GetPosicionesIncidenciasResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPosicionesIncidencias operation
           */
            public void receiveErrorgetPosicionesIncidencias(java.lang.Exception e) {
            }
                


    }
    