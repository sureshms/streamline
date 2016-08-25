/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.hortonworks.iotas.streams.service;

import com.codahale.metrics.annotation.Timed;
import com.hortonworks.iotas.common.QueryParam;
import com.hortonworks.iotas.streams.catalog.TopologyProcessor;
import com.hortonworks.iotas.streams.catalog.service.StreamCatalogService;
import com.hortonworks.iotas.common.util.WSUtils;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.List;

import static com.hortonworks.iotas.common.catalog.CatalogResponse.ResponseMessage.ENTITY_NOT_FOUND;
import static com.hortonworks.iotas.common.catalog.CatalogResponse.ResponseMessage.ENTITY_NOT_FOUND_FOR_FILTER;
import static com.hortonworks.iotas.common.catalog.CatalogResponse.ResponseMessage.EXCEPTION;
import static com.hortonworks.iotas.common.catalog.CatalogResponse.ResponseMessage.SUCCESS;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * Processor component within an IotasTopology
 */
@Path("/api/v1/catalog/topologies/{topologyId}/processors")
@Produces(MediaType.APPLICATION_JSON)
public class TopologyProcessorCatalogResource {
    private StreamCatalogService catalogService;

    public TopologyProcessorCatalogResource(StreamCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /**
     * <p>
     * Lists all the processors in the topology or the ones matching specific query params. For example to
     * list all the processors in the topology,
     * </p>
     * <b>GET /api/v1/catalog/topologies/:TOPOLOGY_ID/processors</b>
     * <p>
     * <pre>
     * {
     *   "responseCode": 1000,
     *   "responseMessage": "Success",
     *   "entities": [{
     *     "id": 1,
     *     "topologyId": 1,
     *     "name": "ParserProcessor",
     *     "config": {
     *       "properties": {
     *         "parallelism": 1
     *       }
     *     },
     *     "type": "PARSER",
     *     "outputStreams": [{stream1 data..}, {stream2 data..}]
     *   }]
     * }
     * </pre>
     */
    @GET
    @Timed
    public Response listTopologyProcessors(@PathParam("topologyId") Long topologyId, @Context UriInfo uriInfo) {
        List<QueryParam> queryParams = WSUtils.buildTopologyIdAwareQueryParams(topologyId, uriInfo);

        try {
            Collection<TopologyProcessor> processors = catalogService.listTopologyProcessors(queryParams);
            if (processors != null) {
                return WSUtils.respond(OK, SUCCESS, processors);
            }
        } catch (Exception ex) {
            return WSUtils.respond(INTERNAL_SERVER_ERROR, EXCEPTION, ex.getMessage());
        }

        return WSUtils.respond(NOT_FOUND, ENTITY_NOT_FOUND_FOR_FILTER, queryParams.toString());
    }

    /**
     * <p>
     * Gets a specific topology processor by Id. For example,
     * </p>
     * <b>GET /api/v1/catalog/topologies/:TOPOLOGY_ID/processors/:PROCESSOR_ID</b>
     * <pre>
     * {
     *   "responseCode": 1000,
     *   "responseMessage": "Success",
     *   "entity": {
     *     "id": 1,
     *     "topologyId": 1,
     *     "name": "ParserProcessor",
     *     "config": {
     *       "properties": {
     *         "parallelism": 1
     *       }
     *     },
     *     "type": "PARSER",
     *     "outputStreams": [{stream1 data..}, {stream2 data..}]
     *   }
     * }
     * </pre>
     */
    @GET
    @Path("/{id}")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTopologyProcessorById(@PathParam("topologyId") Long topologyId, @PathParam("id") Long processorId) {
        try {
            TopologyProcessor source = catalogService.getTopologyProcessor(processorId);
            if (source != null && source.getTopologyId().equals(topologyId)) {
                return WSUtils.respond(OK, SUCCESS, source);
            }
        } catch (Exception ex) {
            return WSUtils.respond(INTERNAL_SERVER_ERROR, EXCEPTION, ex.getMessage());
        }
        return WSUtils.respond(NOT_FOUND, ENTITY_NOT_FOUND, buildMessageForCompositeId(topologyId, processorId));
    }

    /**
     * <p>
     * Creates a topology processor. For example,
     * </p>
     * <b>POST /api/v1/catalog/topologies/:TOPOLOGY_ID/processors</b>
     * <pre>
     * {
     *   "name": "ParserProcessor",
     *   "config": {
     *     "properties": {
     *       "parallelism": 1
     *     }
     *   },
     *   "type": "PARSER",
     *   "outputStreamIds": [1]
     *   OR
     *   "outputStreams" : [{stream1 data..}, {stream2 data..}]
     * }
     * </pre>
     * <i>Sample success response: </i>
     * <pre>
     * {
     *   "responseCode": 1000,
     *   "responseMessage": "Success",
     *   "entity": {
     *     "id": 1,
     *     "topologyId": 1,
     *     "name": "ParserProcessor",
     *     "config": {
     *       "properties": {
     *         "parallelism": 1
     *       }
     *     },
     *     "type": "PARSER",
     *     "outputStreams": [{stream1 data..}, {stream2 data..}]
     *   }
     * }
     * </pre>
     */
    @POST
    @Timed
    public Response addTopologyProcessor(@PathParam("topologyId") Long topologyId, TopologyProcessor topologyProcessor) {
        try {
            TopologyProcessor createdProcessor = catalogService.addTopologyProcessor(topologyId, topologyProcessor);
            return WSUtils.respond(CREATED, SUCCESS, createdProcessor);
        } catch (Exception ex) {
            return WSUtils.respond(INTERNAL_SERVER_ERROR, EXCEPTION, ex.getMessage());
        }
    }

    /**
     * <p>Updates a topology processor.</p>
     * <p>
     * <b>PUT /api/v1/catalog/topologies/:TOPOLOGY_ID/processors/:PROCESSOR_ID</b>
     * <pre>
     * {
     *   "name": "ParserProcessor",
     *   "config": {
     *     "properties": {
     *       "parallelism": 5
     *     }
     *   },
     *   "type": "PARSER",
     *   "outputStreamIds": [1]
     * }
     * </pre>
     * <i>Sample success response: </i>
     * <pre>
     * {
     *   "responseCode": 1000,
     *   "responseMessage": "Success",
     *   "entity": {
     *     "id": 1,
     *     "topologyId": 1,
     *     "name": "ParserProcessor",
     *     "config": {
     *       "properties": {
     *         "parallelism": 5
     *       }
     *     },
     *     "type": "PARSER",
     *     "outputStreamIds": [1]
     *   }
     * }
     * </pre>
     */
    @PUT
    @Path("/{id}")
    @Timed
    public Response addOrUpdateTopologyProcessor(@PathParam("topologyId") Long topologyId, @PathParam("id") Long processorId,
                                              TopologyProcessor topologyProcessor) {
        try {
            TopologyProcessor createdTopologyProcessor = catalogService.addOrUpdateTopologyProcessor(topologyId, processorId, topologyProcessor);
            return WSUtils.respond(CREATED, SUCCESS, createdTopologyProcessor);
        } catch (Exception ex) {
            return WSUtils.respond(INTERNAL_SERVER_ERROR, EXCEPTION, ex.getMessage());
        }
    }

    /**
     * <p>
     * Removes a topology processor.
     * </p>
     * <b>DELETE /api/v1/catalog/topologies/:TOPOLOGY_ID/processors/:PROCESSOR_ID</b>
     * <pre>
     * {
     *   "responseCode": 1000,
     *   "responseMessage": "Success",
     *   "entity": {
     *     "id": 1,
     *     "topologyId": 1,
     *     "name": "ParserProcessor",
     *     "config": {
     *       "properties": {
     *         "parallelism": 5
     *       }
     *     },
     *     "type": "PARSER",
     *     "outputStreamIds": [1]
     *   }
     * }
     * </pre>
     */
    @DELETE
    @Path("/{id}")
    @Timed
    public Response removeTopologyProcessor(@PathParam("topologyId") Long topologyId, @PathParam("id") Long processorId) {
        try {
            TopologyProcessor topologyProcessor = catalogService.removeTopologyProcessor(processorId);
            if (topologyProcessor != null) {
                return WSUtils.respond(OK, SUCCESS, topologyProcessor);
            } else {
                return WSUtils.respond(NOT_FOUND, ENTITY_NOT_FOUND, processorId.toString());
            }
        } catch (Exception ex) {
            return WSUtils.respond(INTERNAL_SERVER_ERROR, EXCEPTION, ex.getMessage());
        }
    }

    private String buildMessageForCompositeId(Long topologyId, Long processorId) {
        return String.format("topology id <%d>, processor id <%d>", topologyId, processorId);
    }
}
