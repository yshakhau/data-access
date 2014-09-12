/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.api.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceImpl;
import org.pentaho.platform.web.http.api.resources.JaxbList;

/**
 * This service provides methods for listing, creating, downloading, uploading, and removal of JDBC data sources.
 */
public class JDBCDatasourceResource {

  protected ConnectionServiceImpl service;
  private static final Log logger = LogFactory.getLog( JDBCDatasourceResource.class );

  public JDBCDatasourceResource() {
    service = new ConnectionServiceImpl();
  }

  /**
   * Remove the JDBC data source for a given JDBC ID.
   *
   * <p><b>Example Request:</b><br />
   *    GET /pentaho/plugin/data-access/api/datasource/jdbc/{name}/remove
   * </p>
   *
   * @param name The name of the JDBC datasource to remove
   *
   * @return A 200 response code representing the successful removal of the JDBC datasource
   */
  @GET
  @Path( "/{name : .+}/remove" )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "JDBC datasource removed successfully." ),
    @ResponseCode( code = 304,
      condition = "User is not authorized to remove the JDBC datasource or the connection does not exist." ),
    @ResponseCode( code = 500, condition = "An unexected error occurred while deleting the JDBC datasource." )
  } )
  public Response deleteConnection( @PathParam( "name" ) String name ) {
    try {
      boolean success = service.deleteConnection( name );
      if ( success ) {
        return buildOkResponse();
      } else {
        return buildNotModifiedResponse();
      }
    } catch ( Throwable t ) {
      return buildServerErrorResponse();
    }

  }

  /**
   * Get a list of JDBC datasource IDs.
   *
   * <p><b>Example Request:</b><br />
   *    GET /data-access/api/datasource/jdbc/ids
   * </p>
   *
   * @return A list of JDBC datasource IDs.
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.xml">
   *      {
   *        "Item": [
   *          {
   *            "@type": "xs:string",
   *            "$": "AgileBI"
   *          },
   *          {
   *            "@type": "xs:string",
   *            "$": "Audit"
   *          },
   *          {
   *            "@type": "xs:string",
   *            "$": "SampleData"
   *          },
   *          {
   *            "@type": "xs:string",
   *            "$": "TestDataSourceResource"
   *          },
   *          {
   *            "@type": "xs:string",
   *            "$": "baseball connection"
   *          },
   *          {
   *            "@type": "xs:string",
   *            "$": "baseball connection"
   *          },
   *          {
   *            "@type": "xs:string",
   *            "$": "live_logging_info"
   *          },
   *          {
   *            "@type": "xs:string",
   *            "$": "pentaho_operations_mart"
   *          }
   *        ]
   *      }
   *    </pre>
   */
  @GET
  @Path( "/ids" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully retrieved the list of JDBC datasource IDs" ),
  } )
  public JaxbList<String> getConnectionIDs() {
    List<String> connStrList = new ArrayList<String>();
    try {
      List<IDatabaseConnection> conns = service.getConnections();
      for ( IDatabaseConnection conn : conns ) {
        conn.setPassword( null );
        connStrList.add( conn.getName() );
      }
    } catch ( ConnectionServiceException e ) {
      logger.error( "Error " + e.getMessage() );
    }
    JaxbList<String> connections = new JaxbList<String>( connStrList );
    return connections;
  }

  /**
   * Export a JDBC datasource connection.
   *
   * <p><b>Example Request:</b><br />
   *    GET /pentaho/plugin/data-access/api/datasource/jdbc/{name}/download
   * </p>
   *
   * @param name The name of the JDBC datasource to retrieve
   *
   * @return A Response object containing the JDBC connection in XML or JSON form.
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.xml">
   *      {
   *        "SQLServerInstance": null,
   *        "accessType": "NATIVE",
   *        "accessTypeValue": "NATIVE",
   *        "attributes": {
   *          "PORT_NUMBER": "9001"
   *        },
   *        "changed": false,
   *        "connectSql": "",
   *        "connectionPoolingProperties": {},
   *        "dataTablespace": "",
   *        "databaseName": "SampleData",
   *        "databasePort": "9001",
   *        "databaseType": {
   *          "defaultDatabasePort": 9001,
   *          "extraOptionsHelpUrl": "http://hsqldb.sourceforge.net/doc/guide/ch04.html#N109DA",
   *          "name": "Hypersonic",
   *          "shortName": "HYPERSONIC"
   *        },
   *        "extraOptions": {},
   *        "forcingIdentifiersToLowerCase": false,
   *        "forcingIdentifiersToUpperCase": false,
   *        "hostname": "localhost",
   *        "id": "00ac4db3-7567-4019-8917-1b6f512ee162",
   *        "indexTablespace": "",
   *        "informixServername": "",
   *        "initialPoolSize": 0,
   *        "maximumPoolSize": 0,
   *        "name": "TestDataSourceResource",
   *        "partitioned": false,
   *        "password": "password",
   *        "quoteAllFields": false,
   *        "streamingResults": false,
   *        "username": "pentaho_user",
   *        "usingConnectionPool": true,
   *        "usingDoubleDecimalAsSchemaTableSeparator": false
   *        }
   *    </pre>
   */
  @GET
  @Path( "/{name : .+}/download" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully retrieved the JDBC datasource" ),
    @ResponseCode( code = 500, condition = "An error occurred retrieving the JDBC datasource" )
  } )
  public Response getConnection( @PathParam( "name" ) String name ) {
    try {
      return buildOkResponse( service.getConnectionByName( name ) );
    } catch ( ConnectionServiceException e ) {
      logger.error( "Error " + e.getMessage() );
      return buildServerErrorResponse();
    }
  }

  /**
   * Add a JDBC datasource connection.
   *
   * <p><b>Example Request:</b><br />
   *    POST /pentaho/plugin/data-access/api/datasource/jdbc/import
   * </p>
   *
   * @param connection A DatabaseConnection in JSON representation
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   */
  @POST
  @Path( "/import" )
  @Consumes( { APPLICATION_JSON } )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "JDBC datasource added successfully." ),
    @ResponseCode( code = 304, condition = "User is not authorized to add JDBC datasources." ),
    @ResponseCode( code = 500, condition = "An unexected error occurred while adding the JDBC datasource." )
  } )
  public Response add( DatabaseConnection connection ) {
    try {
      validateAccess();
      boolean success = service.addConnection( connection );
      if ( success ) {
        return buildOkResponse();
      } else {
        return buildNotModifiedResponse();
      }
    } catch ( Throwable t ) {
      logger.error( "Error " + t.getMessage() );
      return buildServerErrorResponse();
    }
  }

  /**
   * Update an existing JDBC datasource connection.
   *
   * <p><b>Example Request:</b><br />
   *    POST /pentaho/plugin/data-access/api/datasource/jdbc/update
   * </p>
   *
   * @param connection A DatabaseConnection in JSON representation.
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   */
  @POST
  @Path( "/update" )
  @Consumes( { APPLICATION_JSON } )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "JDBC datasource updated successfully." ),
    @ResponseCode( code = 304, condition = "User is not authorized to update the JDBC datasource or the connection does not exist." ),
    @ResponseCode( code = 500, condition = "An unexected error occurred while updating the JDBC datasource." )
  } )
  public Response update( DatabaseConnection connection ) {
    try {
      if ( StringUtils.isBlank( connection.getPassword() ) ) {
        IDatabaseConnection savedConn = service.getConnectionById( connection.getId() );
        connection.setPassword( savedConn.getPassword() );
      }
      boolean success = service.updateConnection( connection );
      if ( success ) {
        return buildOkResponse();
      } else {
        return buildNotModifiedResponse();
      }
    } catch ( Throwable t ) {
      logger.error( "Error " + t.getMessage() );
      return buildServerErrorResponse();
    }
  }

  protected Response buildOkResponse() {
    return Response.ok().build();
  }

  protected Response buildOkResponse( IDatabaseConnection connection ) {
    return Response.ok( connection ).build();
  }

  protected Response buildNotModifiedResponse() {
    return Response.notModified().build();
  }

  protected Response buildServerErrorResponse() {
    return Response.serverError().build();
  }

  protected void validateAccess() throws PentahoAccessControlException {
    DatasourceService.validateAccess();
  }

}
