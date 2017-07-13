/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.services.dispatchers.ws;

import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.websocket.Session;

/**
 * This contains all the sessions which are received via a {@link org.wso2.carbon.messaging.CarbonMessage}.
 */
public class WebSocketConnectionManager {

    /* Server Related Maps */
    // Map<serviceName, Map<sessionId, session>>
    private final Map<String, Map<String, Session>> broadcastSessions = new ConcurrentHashMap<>();
    // Map<groupName, Map<sessionId, session>>
    private final Map<String, Map<String, Session>> connectionGroups = new ConcurrentHashMap<>();
    // Map<NameToStoreConnection, Session>
    private final Map<String, Session> connectionStore = new ConcurrentHashMap<>();

    /* Mediation related maps */
    // Map <clientSessionID, associatedClientServiceName>
    private final Map<String, String> clientSessionToClientServiceMap = new ConcurrentHashMap<>();
    // Map <parentServiceName, Connector>
    private final Map<String, List<BConnector>> parentServiceToClientConnectorsMap = new ConcurrentHashMap<>();
    // Map<Connector, Map<serverSessionID, ClientSession>>
    private final Map<BConnector, Map<String, Session>> clientConnectorSessionsMap = new ConcurrentHashMap<>();

    private static final WebSocketConnectionManager sessionManager = new WebSocketConnectionManager();

    private WebSocketConnectionManager() {
    }

    public static WebSocketConnectionManager getInstance() {
        return sessionManager;
    }

    /**
     * Add {@link Session} to session the broadcast group of a given service.
     *
     * @param service {@link ServiceInfo} of the service.
     * @param session {@link Session} to add to the broadcast group.
     */
    public void addConnectionToBroadcast(ServiceInfo service, Session session) {
        if (broadcastSessions.containsKey(service.getName())) {
            broadcastSessions.get(service.getName()).put(session.getId(), session);
        } else {
            Map<String, Session> sessionMap = new ConcurrentHashMap<>();
            sessionMap.put(session.getId(), session);
            broadcastSessions.put(service.getName(), sessionMap);
        }
    }

    /**
     * Remove {@link Session} from a broadcast group. This should be mostly called when a WebSocket connection is
     * Closed.
     *
     * @param service {@link ServiceInfo} of the service.
     * @param session {@link Session} to remove from the broadcast group.
     */
    public void removeConnectionFromBroadcast(ServiceInfo service, Session session) {
        if (broadcastSessions.containsKey(service.getName())) {
            broadcastSessions.get(service.getName()).remove(session.getId());
        }
    }

    /**
     * Get a list of Sessions for broadcasting for a given service.
     *
     * @param service name of the service.
     * @return the list of sessions which are connected to a given service.
     */
    public List<Session> getBroadcastConnectionList(ServiceInfo service) {
        if (broadcastSessions.containsKey(service.getName())) {
            return broadcastSessions.get(service.getName()).entrySet().stream()
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    /**
     * Add {@link Session} to session the session group.
     *
     * @param groupName name of the group.
     * @param session {@link Session} to remove from the broadcast group.
     */
    public void addConnectionToGroup(String groupName, Session session) {
        if (connectionGroups.containsKey(groupName)) {
            connectionGroups.get(groupName).put(session.getId(), session);
        } else {
            Map<String, Session> sessionMap = new ConcurrentHashMap<>();
            sessionMap.put(session.getId(), session);
            connectionGroups.put(groupName, sessionMap);
        }
    }

    /**
     * Remove {@link Session} from a session group. This should be mostly called when a WebSocket connection is
     * Closed.
     *
     * @param groupName name of the broadcast.
     * @param session {@link Session} to remove from the group.
     * @return true if the connection name was found and connection is removed.
     */
    public boolean removeConnectionFromGroup(String groupName, Session session) {
        if (connectionGroups.containsKey(groupName)) {
            connectionGroups.get(groupName).remove(session.getId());
            return true;
        }
        return false;
    }

    /**
     * Remove the whole group from the groups map.
     *
     * @param groupName name of the group.
     * @return true if connection group name exists and connection group is removed successfully.
     */
    public boolean removeConnectionGroup(String groupName) {
        if (connectionGroups.containsKey(groupName)) {
            connectionGroups.remove(groupName);
            return true;
        }
        return false;
    }

    /**
     * Get the list of the connections which belongs to a specific group.
     *
     * @param groupName name of the connection group.
     * @return a list of connections belongs to the mentioned group name.
     */
    public List<Session> getConnectionGroup(String groupName) {
        if (connectionGroups.containsKey(groupName)) {
            return connectionGroups.get(groupName).entrySet().stream()
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    /**
     * Store connection with the name given by the user for future usages.
     *
     * @param connectionName name of the connection given by the user.
     * @param session {@link Session} to store.
     */
    public void storeConnection(String connectionName, Session session) {
        connectionStore.put(connectionName, session);
    }

    /**
     * Remove connection from the connection store.
     *
     * @param connectionName connection name which should be removed from the store.
     * @return true if connection name was found in connection store and removed successfully.
     */
    public boolean removeConnectionFromStore(String connectionName) {
        if (connectionStore.containsKey(connectionName)) {
            connectionStore.remove(connectionName);
            return true;
        }
        return false;
    }

    /**
     * Get the stored connection from the connection store.
     *
     * @param connectionName name of the connection which should be removed.
     * @return Session of the stored connection if connections is found else return null.
     */
    public Session getStoredConnection(String connectionName) {
        return connectionStore.get(connectionName);
    }

    /**
     * Adding new connection to the connection manager.
     * This adds the server session to the related maps and create necessary client connections to the client
     * connectors.
     *
     * @param service {@link ServiceInfo} of the service which session is related.
     * @param session Session of the connection.
     * @param carbonMessage carbon message received.
     */
    public void addServerSession(ServiceInfo service, Session session, CarbonMessage carbonMessage) {
        /*
            If service is in the parentServiceToClientConnectorsMap means it might have client connectors relates to
            the service. So for each client connector new connections should be created for remote server.
         */
        if (parentServiceToClientConnectorsMap.containsKey(service.getName())) {
            for (BConnector bConnector : parentServiceToClientConnectorsMap.get(service.getName())) {
                Session clientSession = initializeClientConnection(bConnector, carbonMessage);
                Session serverSession = (Session) carbonMessage.getProperty(Constants.WEBSOCKET_SERVER_SESSION);
                String clientServiceName = bConnector.getStringField(1);
                addClientServiceNameToClientSession(clientSession, clientServiceName);
                clientConnectorSessionsMap.get(bConnector).put(serverSession.getId(), clientSession);
            }
        }

        // Adding connection to broadcast group.
        addConnectionToBroadcast(service, session);
    }

    /**
     * Map session against the related service name.
     *
     * @param clientSession Session of the client.
     * @param clientServiceName Name of the client service.
     */
    public void addClientServiceNameToClientSession(Session clientSession, String clientServiceName) {
        clientSessionToClientServiceMap.put(clientSession.getId(), clientServiceName);
    }

    /**
     * Retrieve the related client service name for a given client session.
     *
     * @param clientSession Session of the client.
     * @return the name of the client service related to the given client session.
     */
    public String getClientServiceNameOfClientSession(Session clientSession) {
        if (clientSessionToClientServiceMap.containsKey(clientSession.getId())) {
            return clientSessionToClientServiceMap.get(clientSession.getId());
        }
        throw new BallerinaException("Cannot find the client service to dispatch the message");
    }

    /**
     * Add ballerina level client connector to the connection manager and initialize related maps.
     * <b>This method should be only used if the WebSocket parent service exists.</b>
     *
     * @param parentService {@link ServiceInfo} where the connector is created.
     * @param connector {@link BConnector} which should be added to the connection manager.
     * @Param clientSession newly created client session for the client connector.
     */
    public void addClientConnector(ServiceInfo parentService, BConnector connector, Session clientSession) {
        if (parentServiceToClientConnectorsMap.containsKey(parentService.getName())) {
            parentServiceToClientConnectorsMap.get(parentService.getName()).add(connector);
        } else {
            // Adding connector against parent service.
            List<BConnector> connectors = new LinkedList<>();
            connectors.add(connector);
            parentServiceToClientConnectorsMap.put(parentService.getName(), connectors);
        }

        // Initiating clientConnectorSessionsMap list for the given connector.
        Map<String, Session> sessions = new HashMap<>();
        sessions.put(Constants.DEFAULT, clientSession);
        clientConnectorSessionsMap.put(connector, sessions);
    }

    /**
     * <p>This method should be carefully used. This is only used when creating the initial connection to the
     * remote server by the client connector which is created by the the services other than WebSocket.</p>
     * This also add the initial connection to the the same mapping but using "Default" keyword as the
     * parent service name.
     *
     * @param bConnector
     * @param session
     */
    public void addClientConnectorWithoutParentService(BConnector bConnector, Session session) {
        Map<String, Session> sessions = new HashMap<>();
        sessions.put(Constants.DEFAULT, session);
        clientConnectorSessionsMap.put(bConnector, sessions);
    }

    /**
     * Get all the client sessions created against the given connector.
     *
     * @param bConnector {@link BConnector} to retrieve the client sessions.
     * @return a list of client sessions related to given client connector.
     */
    public List<Session> getSessionsForConnector(BConnector bConnector) {
        if (clientConnectorSessionsMap.containsKey(bConnector)) {
            return clientConnectorSessionsMap.get(bConnector).entrySet().stream().
                    map(Map.Entry::getValue).collect(Collectors.toList());
        }

        return new LinkedList<>();
    }

    /**
     * Retrieve the client session related to the connector and the server session.
     *
     * @param bConnector {@link BConnector} related to the session.
     * @param serverSession server session of the {@link BConnector} related to the client session.
     * @return the client session of relate to the {@link BConnector} and the relevant server session.
     */
    public Session getClientSessionForConnector(BConnector bConnector, Session serverSession) {
        return clientConnectorSessionsMap.get(bConnector).get(serverSession.getId());
    }

    /**
     * Connections can be saved in multiple places according to there need of use.
     * Once the connection is closed or because of the reason if it has to be removed from all the places
     * that it is referred to use this method.
     *
     * @param session {@link Session} which should be removed from all the places.
     */
    public void removeConnectionFromAll(Session session) {
        // Removing session from broadcast sessions map
        broadcastSessions.entrySet().forEach(
                entry -> entry.getValue().entrySet().removeIf(
                        innerEntry -> innerEntry.getKey().equals(session.getId())
                )
        );
        //Removing session from groups map
        connectionGroups.entrySet().forEach(
                entry -> entry.getValue().entrySet().removeIf(
                        innerEntry -> innerEntry.getKey().equals(session.getId())
                )
        );
        // Removing session from connection store
        connectionStore.entrySet().removeIf(
                entry -> entry.getValue().equals(session)
        );
    }

    private Session initializeClientConnection(BConnector bConnector, CarbonMessage carbonMessage) {
        String remoteUrl = bConnector.getStringField(0);

        // Initializing a client connection.
        ClientConnector clientConnector =
                BallerinaConnectorManager.getInstance().getClientConnector(Constants.PROTOCOL_WEBSOCKET);
        carbonMessage.setProperty(Constants.TO, remoteUrl);
        Session clientSession;
        try {
            clientSession = (Session) clientConnector.init(carbonMessage, null, null);
        } catch (ClientConnectorException e) {
            throw new BallerinaException("Error occurred during initializing the connection to " + remoteUrl);
        }

        return clientSession;
    }
}
