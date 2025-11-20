/*
 * Copyright (C) 2020 see AJAN-service/AUTHORS.txt (German Research Center for Artificial Intelligence, DFKI).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package de.dfki.asr.ajan.startup;

import static de.dfki.asr.ajan.AJANDataBase.Store.*;

import de.dfki.asr.ajan.AJANDataBase;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ActionPluginBinding;
import de.dfki.asr.ajan.common.TripleDataBase;
import de.dfki.asr.ajan.logic.agent.AgentManager;
import de.dfki.asr.ajan.pluginsystem.AJANPluginLoader;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.EndpointExtension;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeDefinitionsExtension;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.commons.io.FilenameUtils;
import org.cyberborean.rdfbeans.exceptions.RDFBeanValidationException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveImports"})
public class InitialDataProvider {

  private static final Logger LOG = LoggerFactory.getLogger(InitialDataProvider.class);
  @Autowired private AJANPluginLoader pluginLoader;

  @Autowired
  @AJANDataBase(BEHAVIOR)
  private TripleDataBase behaviors;

  @Autowired private AgentManager agentManager;

  @Autowired
  @AJANDataBase(AGENT_TEMPLATE)
  private TripleDataBase agents;

  @Autowired
  @AJANDataBase(DOMAIN)
  private TripleDataBase domain;

  @Autowired
  @AJANDataBase(ACTION_SERVICE)
  private TripleDataBase services;

  @Autowired
  @AJANDataBase(NODE_DEFINITIONS)
  private TripleDataBase nodeDefinitions;

  @Autowired
  @AJANDataBase(EDITOR_DATA)
  private TripleDataBase editorData;

  @Value("${publicHostName:localhost}")
  private String publicHostName;

  @Value("${usePort:true}")
  private boolean usePort;

  @Value("${loadTTLFiles:true}")
  private boolean loadFiles;

  @Value("${triplestore.initialData.behaviorsFolderPath:use-case/behaviors}")
  private String behaviorsFolderPath;

  @Value("${triplestore.initialData.agentFolderPath:use-case/agents}")
  private String agentFolderPath;

  @Value("${triplestore.initialData.serviceFolderPath:use-case/services}")
  private String serviceFolderPath;

  @Value("${triplestore.initialData.domainFolderPath:use-case/domains}")
  private String domainFolderPath;

  @Value("${triplestore.initialData.nodeDefinitionsFolderPath:use-case/editor/nodeDefinitions}")
  private String nodeDefinitionsFolderPath;

  @Value("${triplestore.initialData.editorDataFolderPath:use-case/editor/editorData}")
  private String editorDataFolderPath;

  private List<EndpointExtension> endpoints;

  @PostConstruct
  public void init() throws RDFBeanValidationException, URISyntaxException, UnknownHostException {
    LOG.info(" init()");
    pluginLoader.init();
    if (loadFiles) {
      loadTTLFromFolders();
    }
    pushPluginsToStore(pluginLoader, services);
    loadEndpoints(pluginLoader);
    agentManager.setBaseURI(publicHostName, usePort);
  }

  private void loadTTLFromFolders() {
    Map<String, TripleDataBase> tripleStoreMap = new ConcurrentHashMap<>();
    copyNodeDefinitionsExtensions();
    // loads ttl file(s) for each folder to its corresponding repository
    tripleStoreMap.put(agentFolderPath, agents);
    tripleStoreMap.put(behaviorsFolderPath, behaviors);
    tripleStoreMap.put(domainFolderPath, domain);
    tripleStoreMap.put(serviceFolderPath, services);
    tripleStoreMap.put(nodeDefinitionsFolderPath, nodeDefinitions);
    tripleStoreMap.put(editorDataFolderPath, editorData);
    pushRDFGraphs(tripleStoreMap);
  }

  private void copyNodeDefinitionsExtensions() {
    List<NodeDefinitionsExtension> files =
        pluginLoader.getPLUGIN_MANAGER().getExtensions(NodeDefinitionsExtension.class);
    LOG.info("Number of NodeDefinitionsExtension files found: {}", files.size());
    files.forEach(
        (pathExtension) -> {
          try {
            Path origin = pathExtension.getDefinitionPath();
            if (origin == null) {
              LOG.warn("NodeDefinitionsExtension file path is null, skipping.");
              return;
            }
            LOG.info("NodeDefinitionsExtension file found: {}", origin);
            Path target = Paths.get(nodeDefinitionsFolderPath).resolve(origin.getFileName());
            LOG.info("Copying file from {} to {}", origin, target);
            Files.createDirectories(target.getParent());
            Files.copy(origin, target, StandardCopyOption.REPLACE_EXISTING);
          } catch (IOException | IllegalArgumentException e) {
            LOG.error("Error while getting NodeDefinitionsExtension file: {}", e.getMessage());
            LOG.error(Arrays.toString(e.getStackTrace()));
          }
        });
  }

  private void pushRDFGraphs(final Map<String, TripleDataBase> tripleStoreMap) {
    tripleStoreMap
        .entrySet()
        .forEach(
            (entry) -> {
              String folderPath = entry.getKey();
              TripleDataBase repo = entry.getValue();
              repo.clear();
              ArrayList<String> result = (ArrayList<String>) getAllFilesFromFolder(folderPath);
              result.forEach(
                  (filePath) -> {
                    pushFileToStore(filePath, repo);
                  });
            });
  }

  @SuppressWarnings("CallToPrintStackTrace")
  private List<String> getAllFilesFromFolder(final String folderPath) {
    // returns list of files in the `folderPath`.
    List<String> result = new ArrayList<>();
    try (Stream<Path> walk = Files.walk(Paths.get(folderPath))) {
      result =
          walk.filter(Files::isRegularFile).map(x -> x.toString()).collect(Collectors.toList());
    } catch (IOException e) {
      LOG.info("Error " + e);
    }
    return result;
  }

  private void pushFileToStore(final String inputFile, final TripleDataBase tripleDataBase) {
    LOG.info("read repository file: " + inputFile);
    String extension = FilenameUtils.getExtension(inputFile).toLowerCase();
    try (InputStream input = new FileInputStream(inputFile)) {
      switch (extension) {
        case "ttl":
          tripleDataBase.add(input, RDFFormat.TURTLE);
          break;
        case "trig":
          tripleDataBase.add(input, RDFFormat.TRIG);
          break;
        case "json":
          tripleDataBase.add(input, RDFFormat.JSONLD);
          break;
        default:
          LOG.error("No known RDFFormat used for: " + inputFile);
          break;
      }
    } catch (IOException ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  private void loadEndpoints(final AJANPluginLoader loader) {
    endpoints = loader.getPLUGIN_MANAGER().getExtensions(EndpointExtension.class);
    endpoints.stream()
        .forEach(
            (endpointExtension) -> {
              endpointExtension.initServer(agentManager);
            });
  }

  private void pushPluginsToStore(
      final AJANPluginLoader loader, final TripleDataBase tripleDataBase) {
    List<ActionPluginBinding> plugins =
        loader.getPLUGIN_MANAGER().getExtensions(ActionPluginBinding.class);
    plugins.stream()
        .map((plugin) -> InitUtil.getActionDescription(plugin))
        .forEach(
            (desc) -> {
              tripleDataBase.add(desc);
            });
  }

  @PreDestroy
  public void destroy() {
    endpoints.stream()
        .forEach(
            (endpointExtension) -> {
              endpointExtension.destroyServer();
            });
  }
}
